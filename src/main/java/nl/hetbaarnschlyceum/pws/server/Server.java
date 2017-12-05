package nl.hetbaarnschlyceum.pws.server;

import nl.hetbaarnschlyceum.pws.PWS;
import nl.hetbaarnschlyceum.pws.crypto.AES;
import nl.hetbaarnschlyceum.pws.crypto.ECDH;
import nl.hetbaarnschlyceum.pws.crypto.Hash;
import nl.hetbaarnschlyceum.pws.crypto.KeyManagement;
import nl.hetbaarnschlyceum.pws.server.tc.TCServer;
import nl.hetbaarnschlyceum.pws.server.tc.client.Client;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.security.KeyPair;
import java.util.*;

import static nl.hetbaarnschlyceum.pws.PWS.print;

public class Server implements Runnable
{
    private final int port;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private static CharsetEncoder charsetEncoder;

    public Server(int port)
    {
        this.port = port;
        charsetEncoder = Charset.forName("US-ASCII").newEncoder();

        init();
    }

    private void init() {
        try {
            print("[INFO] Server selectors worden gemaakt..");
            this.selector = Selector.open();
            this.serverSocketChannel = ServerSocketChannel.open();
            this.serverSocketChannel.bind(new InetSocketAddress("0.0.0.0",
                    this.port));
            this.serverSocketChannel.configureBlocking(false);
            this.serverSocketChannel.register(this.selector,
                    SelectionKey.OP_ACCEPT);
        } catch (IOException e)
        {
            print("[FOUT] Er is een fout op getreden tijdens het opstarten van de server (%s): ", e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }

        print("[INFO] Server selectors zijn gemaakt.");
    }

    private void handleAccept(SelectionKey key)
            throws IOException {
        SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
        String address = socketChannel.socket().getInetAddress() + ":" + socketChannel.socket().getPort();
        socketChannel.configureBlocking(false);
        socketChannel.register(this.selector, SelectionKey.OP_READ, address);
        TCServer.getClientManager().clientConnect(socketChannel, socketChannel.socket().getInetAddress() + "");

        print("[INFO] Verbinding van %s", address);
    }

    private void handleRead(SelectionKey key)
    {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        StringBuilder stringBuilder = new StringBuilder();
        Client client = TCServer.getClientManager().getClient(socketChannel);
        ByteBuffer tempBuffer = ByteBuffer.allocate(8192);

        int read = 0;
        try {
            while ((read = socketChannel.read(tempBuffer)) > 0) {
                tempBuffer.flip();

                byte[] bytes = new byte[tempBuffer.limit()];
                tempBuffer.get(bytes);
            }
        } catch (IOException e)
        {
            print("[WAARSCHUWING] Client (%s) heeft onverwacht de verbinding verbroken: %s",
                    client.getIP(),
                    e.getMessage());
            TCServer.getClientManager().clientConnectionDropped(client);

            try {
                socketChannel.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }

        if (read < 0)
        {
            print("[WAARSCHUWING] Client (%s) heeft onverwacht de verbinding verbroken",
                    client.getIP());
            TCServer.getClientManager().clientConnectionDropped(client);

            try {
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        tempBuffer.flip();
        client.byteBuffer.flip();
        client.byteBuffer = ByteBuffer.allocate(8192).put(client.byteBuffer).put(tempBuffer);

        System.out.println(client);

        client.byteBuffer.flip();
        byte[] bytes = new byte[client.byteBuffer.limit()];
        client.byteBuffer.get(bytes);
        stringBuilder.append(new String(bytes));

        String data = stringBuilder.toString();
        if (data.length() > 4 && data.substring(data.length() - 4).equals("_&2d"))
        {
            this.processData(client, data);
            client.byteBuffer.clear();
        }
    }

    private void processData(Client client, String data)
    {
        print("[INFO] Data ontvangen: %s", data);

        try
        {
            Object[] messageData = checkValidMessage(client, data);

            if (messageData == null)
            {
                return;
            }

            if (messageData[0] == PWS.MessageIdentifier.REQUEST)
            {
                // Normaal verzoek
            } else
            {
                // Verbinding maken met de server
                PWS.MessageIdentifier messageIdentifier = (PWS.MessageIdentifier) messageData[0];

                if (messageIdentifier == PWS.MessageIdentifier.CONNECTED)
                {
                    // Verwachte reactie: DH_START
                    KeyPair keyPair = ECDH.generateKeyPair();
                    String publicKey = ECDH.getPublicData(keyPair);
                    String response = prepareMessage(client,
                            PWS.MessageIdentifier.DH_START,
                            publicKey
                    );
                    client.setDHKeys(keyPair);
                    sendMessage(client, response);
                } else if (messageIdentifier == PWS.MessageIdentifier.DH_ACK)
                {
                    // Verwachte reactie: LOGIN
                    KeyPair keyPair = client.getDHKeys();
                    String publicKeyClient = (String) messageData[1];
                    byte[] sharedSecret = KeyManagement.hexToBytes(
                            Hash.generateHash(
                                    ECDH.getSecret(keyPair, publicKeyClient)
                            )
                    );
                    SecretKey secretKey = new SecretKeySpec(sharedSecret, 0,sharedSecret.length, "AES");
                    IvParameterSpec ivParameterSpec = new IvParameterSpec(
                        KeyManagement.hexToBytes(
                                (String) messageData[2]
                        )
                    );

                    client.setInitializationVector(ivParameterSpec);
                    client.setSessionKey(secretKey);
                    client.setHMACKey(
                            Hash.generateHash(
                                    Base64.getEncoder().encodeToString(client.getSessionKey().getEncoded())
                            )
                    );

                    String response = prepareMessage(client,
                            PWS.MessageIdentifier.LOGIN);

                    sendMessage(client, response);
                } else if (messageIdentifier == PWS.MessageIdentifier.LOGIN_INFORMATION)
                {
                    // Verwachte reactie: LOGIN_RESULT
                    String username = (String) messageData[1];
                    String hashedPassword = (String) messageData[2];
                    boolean registerUser = Boolean.getBoolean((String) messageData[3]);
                    int registerNumber = Integer.valueOf((String) messageData[4]);

                    TCServer.executeTask(() -> {
                        if (registerUser)
                        {
                            TCServer.getClientManager().registerTask(client,
                                    username,
                                    registerNumber,
                                    hashedPassword);
                        } else {
                            TCServer.getClientManager().loginTask(client,
                                    username,
                                    hashedPassword);
                        }
                        return null;
                    });
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            // Hier alle exceptions opvangen die mogelijk voorkomen
        }
    }

    //TODO: AES enzo toevoegen
    public static String prepareMessage(Client client,
                                 PWS.MessageIdentifier messageIdentifier,
                                 String... arguments)
    {
        if (arguments.length == messageIdentifier.getArguments())
        {
            StringBuilder stringBuilder = new StringBuilder();

            for (String argument : arguments)
            {
                stringBuilder.append("<<&>>").append(argument);
            }

            String formattedMessage = String.format("%s<<->>%s<<&>>%s<<&>>%s%s",
                    messageIdentifier.getDataID(),
                    UUID.randomUUID().toString(),
                    "HMAC_X8723784X", // HMAC
                    "MSGCOUNT_X987231X", // Message count
                    stringBuilder.toString());

            String hMAC = "null";
            if (client.getSessionKey() != null &&
                    client.getHMACKey() != null)
            {
                hMAC = Hash.generateHMAC(formattedMessage, client.getHMACKey());
            }

            return formattedMessage.replace("HMAC_X8723784X", hMAC);
        }

        return null;
    }

    //TODO: Debugberichten bij fouten nog weghalen
    private Object[] checkValidMessage(Client client, String data)
    {
        if (!data.contains("_&2d"))
        {
            return null;
        }

        data = data.substring(0, data.length() - 4);

        // Controleren voor versleuteling ed
        if (data.contains("<<*3456*34636*>>"))
        {
            if (data.split("<<\\*3456\\*34636\\*>>").length != 2)
            {
                System.out.println("Wel versleuteling, maar niet genoeg argumenten");
                return null;
            }

            String hmacTotal = data.split("<<\\*3456\\*34636\\*>>")[0];
            String encryptedData = data.split("<<\\*3456\\*34636\\*>>")[1];

            if (!Hash.generateHMAC(encryptedData, client.getHMACKey()).equals(hmacTotal))
            {
                System.out.println("HMAC #1 was niet goed");
                return null;
            }

            String decryptedData = AES.decrypt(encryptedData,
                    client.getSessionKey(),
                    client.getInitializationVector()
            );

            if (!decryptedData.contains("<<&>>")
                    || !decryptedData.contains("<<->>"))
            {
                System.out.println("Decryptie ging niet goed: " + decryptedData);
                return null;
            }

            System.out.println("DATA: " + decryptedData);
            if (decryptedData.split("<<&>>").length < 3)
            {
                System.out.println("Te weinig argumenten: " + decryptedData);
                return null;
            }

            long timeSend = Long.valueOf(decryptedData.split("<<&>>")[2]);
            String hmacData = decryptedData.split("<<&>>")[1];
            String rawData = decryptedData
                    .replace(hmacData, "HMAC_X8723784X")
                    .replace(String.valueOf(timeSend), "MSGCOUNT_X987231X"); //TODO: VOOR DISCUSSIE -> dit is eigenlijk niet veilig omdat tijd niet onder hmac valt

            data = rawData;

            if (!Hash.generateHMAC(rawData, client.getHMACKey()).equals(hmacData))
            {
                System.out.println("HMAC #2 was niet goed");
                return null;
            }

            // Eerste waarde zetten als dit eerste versleutelde bericht is
            if (data.split("<<->>")[0].equals(PWS.MessageIdentifier.LOGIN_INFORMATION.getDataID()))
            {
                client.setLastMessageReceivedTime(System.currentTimeMillis() - 20 * 1000);
            }

            if (timeSend < client.getLastMessageReceivedTime() ||
                    System.currentTimeMillis() - timeSend > 10 * 1000 ||
                    System.currentTimeMillis() - timeSend < 1)
            {
                System.out.println("Verkeerde lastMessageReceived. Vereist: " + client.getLastMessageReceivedTime()
                        + ", gevonden: " + timeSend);

                if (System.currentTimeMillis() - timeSend > 10 * 1000 ||
                        System.currentTimeMillis() - timeSend < 1)
                {
                    System.out.println("Verschil te groot");
                }
                return null;
            }

            client.setLastMessageReceivedTime(timeSend);
        }

        print("[INFO] Data verwerken: %s", data);
        if (data.split("<<->>").length != 2)
        {
            return null;
        }

        String mID = data.split("<<->>")[0];
        PWS.MessageIdentifier messageIdentifier = null;

        for (PWS.MessageIdentifier messageID : PWS.MessageIdentifier.values())
        {
            if (mID.equals(messageID.getDataID()))
            {
                messageIdentifier = messageID;
            }
        }

        if (messageIdentifier == null)
        {
            return null;
        }

        String[] args = data.split("<<->>")[1].split("<<&>>");

        if (messageIdentifier.getArguments() + 3 != args.length)
        {
            return null;
        }

        ArrayList<String> arguments = new ArrayList<>();
        int i = 0;

        for (String arg : args)
        {
            i++;

            if (i < 4)
            {
                continue;
            }

            arguments.add(arg);
        }

        Object[] messageData = new Object[arguments.size() + 1];
        messageData[0] = messageIdentifier;
        i = 1;

        for (String arg : arguments)
        {
            messageData[i] = arg;
            i++;
        }

        return messageData;
    }

    public static void sendMessage(UUID uuid, String data)
    {
        Client client = TCServer.getClientManager().getClient(uuid);
        sendMessage(client, client.getSocketChannel(), data);
    }

    public static void sendMessage(Client client, String data)
    {
        sendMessage(client, client.getSocketChannel(), data);
    }

    private static void sendMessage(Client client, SocketChannel socketChannel, String data)
    {
        try {
            data = data.replace("MSGCOUNT_X987231X", String.valueOf(System.currentTimeMillis()));

            if (client.getSessionKey() != null
                    && client.getHMACKey() != null)
            {
                data = AES.encrypt(data, client.getSessionKey(), client.getInitializationVector());
                String hMAC = Hash.generateHMAC(data, client.getHMACKey());
                data = String.format("%s<<*3456*34636*>>%s",
                        hMAC,
                        data
                );
            }

            print("[INFO] Bericht naar %s sturen: %s", socketChannel.getRemoteAddress().toString(), data);
            socketChannel.write(charsetEncoder.encode(CharBuffer.wrap(data + "_&2d")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        print("[INFO] Wachten op inkomende verbindingen..");

        try {
            Iterator<SelectionKey> iterator;
            SelectionKey key;

            while (this.serverSocketChannel.isOpen())
            {
                this.selector.select();
                iterator = this.selector.selectedKeys().iterator();

                while(iterator.hasNext()) {
                    key = iterator.next();
                    iterator.remove();

                    if(key.isAcceptable())
                    {
                        // Binnenkomende verbinding
                        this.handleAccept(key);
                    }

                    if(key.isReadable())
                    {
                        // Binnenkomende data
                        if (key.isValid() && key.channel() instanceof SocketChannel) {
                            this.handleRead(key);
                        }
                    }
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
