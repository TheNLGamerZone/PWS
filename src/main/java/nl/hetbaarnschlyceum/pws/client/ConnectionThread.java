package nl.hetbaarnschlyceum.pws.client;

import nl.hetbaarnschlyceum.pws.PWS;
import nl.hetbaarnschlyceum.pws.crypto.AES;
import nl.hetbaarnschlyceum.pws.crypto.ECDH;
import nl.hetbaarnschlyceum.pws.crypto.Hash;
import nl.hetbaarnschlyceum.pws.crypto.KeyManagement;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static nl.hetbaarnschlyceum.pws.PWS.print;

public class ConnectionThread implements Runnable
{
    private Socket socket;
    private BufferedWriter bufferedWriter;
    private Thread thread;
    private BlockingQueue<String> blockingQueue;
    private HashMap<String, String> resultList;
    private ConnectionReadThread readThread;
    private StringBuilder stringBuffer;

    private long messageCount;
    private SecretKey sessionKey;
    private IvParameterSpec initializationVector;
    private String hmacKey;

    public ConnectionThread(String serverIP, int port)
    {
        print("[INFO] Verbinden met de server (%s:%s)..", serverIP, String.valueOf(port));

        try
        {
            socket = new Socket(serverIP, port);
            blockingQueue = new LinkedBlockingDeque<>();
            resultList = new HashMap<>();
            stringBuffer = new StringBuilder();

            print("[INFO] Verbonden met de server (%s:%s)", serverIP, String.valueOf(port));
            init();
        } catch (IOException e) {
            print("[FOUT] De client kon niet met de server (%s:%s) verbinden: %s",
                    serverIP,
                    String.valueOf(port),
                    e.getMessage());
        }
    }

    boolean isConnected()
    {
        return socket != null && socket.isConnected();
    }

    private void init()
    {
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            if (thread == null)
            {
                readThread = new ConnectionReadThread(this,
                        socket);
                thread = new Thread(this);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void closeConnection()
    {
        if (thread != null)
        {
            thread.interrupt();
            thread = null;
        }

        if (bufferedWriter != null)
        {
            try
            {
                bufferedWriter.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        if (socket != null)
        {
            try
            {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (readThread != null)
        {
            readThread.closeConnection();
            readThread.interrupt();
            readThread.connectionThread = null;
        }
    }

    public String requestFromServer(String request)
    {
        return this.requestFromServer(request, false, true);
    }

    public void processedRequestFromServer(String processedRequest)
    {
        this.requestFromServer(processedRequest, true, false);
    }

    private String requestFromServer(String request,
                                     boolean processed,
                                     boolean blocking)
    {
        String formattedRequest;

        if (processed)
        {
            formattedRequest = request;
        } else
        {
            formattedRequest = prepareMessage(PWS.MessageIdentifier.REQUEST, request);
        }

        String messageID = request.split("<<->>")[0]
                .split("<<&>>")[0];

        if (blockingQueue.offer(formattedRequest))
        {
            if (blocking) {
                while (true) {
                    String requestResult = resultList.get(messageID);

                    if (requestResult != null) {
                        resultList.remove(messageID);
                        return requestResult;
                    }
                }
            }
        }
        return null;
    }

    void processDataReceived(String data)
    {
        if (data == null ||
                data.equals(""))
        {
            return;
        }

        stringBuffer.append(data);

        if (stringBuffer.toString().contains("_&2d"))
        {
            data = stringBuffer.toString();
            data = data.substring(0, data.length() - 4);
            stringBuffer = new StringBuilder();

            print("[INFO] Data verwerken: %s", data);

            Object[] messageData = checkValidMessage(data);

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

                if (messageIdentifier == PWS.MessageIdentifier.DH_START)
                {
                    // Verwachte reactie: DH_ACK
                    KeyPair keyPair = ECDH.generateKeyPair();
                    String publicKeyClient = ECDH.getPublicData(keyPair);
                    String publicKeyServer = (String) messageData[1];
                    this.initializationVector = AES.generateIV();
                    String response = prepareMessage(PWS.MessageIdentifier.DH_ACK,
                            publicKeyClient,
                            KeyManagement.bytesToHex(this.initializationVector.getIV()));

                    byte[] sharedSecret = KeyManagement.hexToBytes(Hash.generateHash(ECDH.getSecret(keyPair, publicKeyServer)));
                    this.sessionKey = new SecretKeySpec(sharedSecret, 0,sharedSecret.length, "AES");

                    this.processedRequestFromServer(response);
                } else if (messageIdentifier == PWS.MessageIdentifier.LOGIN)
                {
                    // Verwachte reactie: LOGIN_INFORMATION
                } else if (messageIdentifier == PWS.MessageIdentifier.LOGIN_RESULT)
                {
                    // Verwachte reactie: niks -> ingelogd
                }
            }
        }
    }

    //TODO: AES enzo toevoegen
    String prepareMessage(PWS.MessageIdentifier messageIdentifier,
                                String... arguments)
    {
        if (arguments.length == messageIdentifier.getArguments())
        {
            StringBuilder stringBuilder = new StringBuilder();

            for (String argument : arguments)
            {
                stringBuilder.append("<<&>>").append(argument);
            }

            String formattedMessage = String.format("%s<<->>%s<<&>>%s%s_&2d",
                    messageIdentifier.getDataID(),
                    UUID.randomUUID().toString(),
                    "HMAC_X8723784X", //HMAC
                    stringBuilder.toString());

            String hMAC = "null";

            if (sessionKey != null)
            {
                hMAC = Hash.generateHMAC(formattedMessage, hmacKey);
            }

            return formattedMessage.replace("HMAC_X8723784X", hMAC);
        }

        return null;
    }

    private Object[] checkValidMessage(String data)
    {
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

        if (messageIdentifier.getArguments() + 2 != args.length)
        {
            return null;
        }

        ArrayList<String> arguments = new ArrayList<>();
        int i = 0;

        for (String arg : args)
        {
            i++;

            if (i < 3)
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
        }

        return messageData;
    }

    @Override
    public void run() {
        while (thread != null)
        {
            try
            {
                if (bufferedWriter != null) {
                    String request = blockingQueue.take();

                    print("[INFO] Request verstuurd: %s", request);

                    if (sessionKey != null &&
                            hmacKey != null)
                    {
                        String hMAC = Hash.generateHMAC(request, hmacKey);
                        request = String.format("%s<<*3456*34636*>>%s",
                                hMAC,
                                request);
                    }

                    bufferedWriter.write(request);
                    bufferedWriter.flush();
                }
            } catch (IOException |
                    InterruptedException e) {
                closeConnection();
                e.printStackTrace();
                break;
            }
        }
    }
}
