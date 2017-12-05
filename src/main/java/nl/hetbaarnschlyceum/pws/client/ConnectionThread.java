package nl.hetbaarnschlyceum.pws.client;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import nl.hetbaarnschlyceum.pws.PWS;
import nl.hetbaarnschlyceum.pws.client.gui.GUIMainClass;
import nl.hetbaarnschlyceum.pws.client.gui.LoginScreen;
import nl.hetbaarnschlyceum.pws.client.gui.MainScreen;
import nl.hetbaarnschlyceum.pws.crypto.AES;
import nl.hetbaarnschlyceum.pws.crypto.ECDH;
import nl.hetbaarnschlyceum.pws.crypto.Hash;
import nl.hetbaarnschlyceum.pws.crypto.KeyManagement;
import nl.hetbaarnschlyceum.pws.server.tc.OperationResult;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.KeyPair;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static nl.hetbaarnschlyceum.pws.PWS.print;

public class ConnectionThread implements Runnable
{
    private static Socket socket;
    private static BufferedWriter bufferedWriter;
    private static Thread thread;
    private static BlockingQueue<String> blockingQueue;
    private static HashMap<String, String> resultList;
    private static ConnectionReadThread readThread;
    private static StringBuilder stringBuffer;

    private static long lastMessageReceived;
    private static SecretKey sessionKey;
    private static IvParameterSpec initializationVector;
    private static String hmacKey;
    private static boolean dhParamsReady = false;

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
        return requestFromServer(request, false, true);
    }

    public static void processedRequestFromServer(String processedRequest)
    {
        requestFromServer(processedRequest, true, false);
    }

    private static String requestFromServer(String request,
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
        data = stringBuffer.toString();
        stringBuffer = new StringBuilder();

        print("[INFO] Data ontvangen: %s", data);

        Object[] messageData = checkValidMessage(data);

        if (messageData == null)
        {
            return;
        }

        if (messageData[0] == PWS.MessageIdentifier.REQUEST_RESULT)
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
                initializationVector = AES.generateIV();
                String response = prepareMessage(PWS.MessageIdentifier.DH_ACK,
                        publicKeyClient,
                        KeyManagement.bytesToHex(initializationVector.getIV()));

                byte[] sharedSecret = KeyManagement.hexToBytes(Hash.generateHash(ECDH.getSecret(keyPair, publicKeyServer)));
                sessionKey = new SecretKeySpec(sharedSecret, 0,sharedSecret.length, "AES");
                dhParamsReady = true;

                processedRequestFromServer(response);
            } else if (messageIdentifier == PWS.MessageIdentifier.LOGIN)
            {
                // Verwachte reactie: LOGIN_INFORMATION
                String response = prepareMessage(PWS.MessageIdentifier.LOGIN_INFORMATION,
                        Client.username,
                        Client.hashedPassword,
                        String.valueOf(Client.registerUser), // Niet registreren maar inloggen
                        (Client.registerUser) ? String.valueOf(Client.registerNumber) : "0"
                );
                Client.connectionEstablished = true;

                processedRequestFromServer(response);
            } else if (messageIdentifier == PWS.MessageIdentifier.LOGIN_RESULT)
            {
                // Verwachte reactie: niks -> ingelogd
                int loginResult = Integer.valueOf((String) messageData[1]);

                if (loginResult == OperationResult.SUCCESS_LOGGED_IN)
                {
                    // Gebruiker is ingelogd -> naar MainScreen

                    Platform.runLater(() -> MainScreen.showMainscreen(GUIMainClass.window));
                } else
                {
                    String connectionType = (Client.registerUser) ? "registreren" : "inloggen";

                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);

                        alert.setContentText("Bij het " + connectionType + " is de volgende fout opgetreden: " +
                                OperationResult.errorMessages[loginResult]
                        );
                        alert.show();
                        LoginScreen.toggleControls();
                    });
                }
            }
        }
    }

    //TODO: AES enzo toevoegen
    static String prepareMessage(PWS.MessageIdentifier messageIdentifier,
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
            if (sessionKey != null &&
                    hmacKey != null)
            {
                hMAC = Hash.generateHMAC(formattedMessage, hmacKey);
            }

            return formattedMessage.replace("HMAC_X8723784X", hMAC);
        }

        return null;
    }

    private Object[] checkValidMessage(String data)
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

            if (!Hash.generateHMAC(encryptedData, hmacKey).equals(hmacTotal))
            {
                System.out.println("HMAC #1 was niet goed");
                return null;
            }

            String decryptedData = AES.decrypt(encryptedData,
                    sessionKey,
                    initializationVector
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

            if (!Hash.generateHMAC(rawData, hmacKey).equals(hmacData))
            {
                System.out.println("HMAC #2 was niet goed");
                return null;
            }

            // Eerste waarde zetten als dit eerste versleutelde bericht is
            if (data.split("<<->>")[0].equals(PWS.MessageIdentifier.LOGIN.getDataID()))
            {
                lastMessageReceived = System.currentTimeMillis() - 20 * 1000;
            }

            if (timeSend < lastMessageReceived ||
                    System.currentTimeMillis() - timeSend > 10 * 1000 ||
                    System.currentTimeMillis() - timeSend < 1)
            {
                System.out.println("Verkeerde lastMessageReceived. Vereist: " + lastMessageReceived
                        + ", gevonden: " + timeSend);

                if (System.currentTimeMillis() - timeSend > 10 * 1000 ||
                        System.currentTimeMillis() - timeSend < 1)
                {
                    System.out.println("Verschil te groot");
                }

                return null;
            }

            lastMessageReceived = timeSend;
        }

        print("[INFO] Data verwerken: %s", data);
        if (data.split("<<->>").length != 2)
        {
            System.out.println("Geen versleuteling (meer), maar niet genoeg argumenten");
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
            System.out.println("Geen MID");
            return null;
        }

        String[] args = data.split("<<->>")[1].split("<<&>>");

        if (messageIdentifier.getArguments() + 3 != args.length)
        {
            System.out.println("Te veel argumenten");
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

    @Override
    public void run() {
        while (thread != null)
        {
            try
            {
                if (bufferedWriter != null) {
                    String request = blockingQueue.take()
                            .replace("MSGCOUNT_X987231X", String.valueOf(System.currentTimeMillis()));

                    if (sessionKey != null &&
                            hmacKey != null)
                    {
                        request = AES.encrypt(request, sessionKey, initializationVector);
                        String hMAC = Hash.generateHMAC(request, hmacKey);
                        request = String.format("%s<<*3456*34636*>>%s",
                                hMAC,
                                request
                        );
                    }

                    if (dhParamsReady)
                    {
                        hmacKey = Hash.generateHash(
                                        Base64.getEncoder().encodeToString(sessionKey.getEncoded())
                        );
                        dhParamsReady = false;
                    }

                    print("[INFO] Request v" +
                            "erstuurd: %s", request);

                    bufferedWriter.write(request + "_&2d");
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
