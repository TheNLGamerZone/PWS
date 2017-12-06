package nl.hetbaarnschlyceum.pws.client;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import nl.hetbaarnschlyceum.pws.PWS;
import nl.hetbaarnschlyceum.pws.client.gui.GUIMainClass;
import nl.hetbaarnschlyceum.pws.client.tasks.CallTask;
import nl.hetbaarnschlyceum.pws.server.tc.OperationResult;
import nl.hetbaarnschlyceum.pws.server.tc.Request;

import static nl.hetbaarnschlyceum.pws.PWS.print;

public class Client
{
    public static final String[] forbiddenStrings = new String[]{"&",
            "=",
            "_&2d",
            "<<->>",
            "<<&>>",
            "<<*3456*34636*>>",
            "<<&6236&>>",
            "HMAC_X8723784X",
            "MSGCOUNT_X987231X",
            "DEVNM_X76786X"
    };
    private static ConnectionThread connectionThread;
    private static String serverIP;
    private static int serverPort;

    public static boolean connectionEstablished = false;
    public static boolean registerUser;
    public static int registerNumber;
    public static String username;
    public static String hashedPassword;

    public Client(String serverIP, String serverPort)
    {
        print("[INFO] Client wordt gestart..");
        Client.serverIP = serverIP;
        Client.serverPort = Integer.valueOf(serverPort);

        GUIMainClass.start();
    }

    public static boolean initConnection(String username,
                                         String password,
                                         boolean registerAttempt)
    {
        registerUser = registerAttempt;

        if (connectionEstablished)
        {
            String response = ConnectionThread.prepareMessage(PWS.MessageIdentifier.LOGIN_INFORMATION,
                    Client.username,
                    Client.hashedPassword,
                    String.valueOf(registerUser),
                    (registerUser) ? String.valueOf(registerNumber) : "0"
            );

            ConnectionThread.processedRequestFromServer(response);
            return true;
        } else {
            connectionThread = new ConnectionThread(serverIP, serverPort);

            if (connectionThread.isConnected()) {
                connectionThread.processedRequestFromServer(
                        connectionThread.prepareMessage(PWS.MessageIdentifier.CONNECTED)
                );

                return true;
            }
        }
        return false;
    }

    public static void startCall(int targetNumber)
    {
        Alert callAlert = new Alert(Alert.AlertType.CONFIRMATION);

        Platform.runLater(() -> {
            callAlert.setContentText(targetNumber + " bellen..");
            callAlert.setHeaderText("Bellen..");
            callAlert.getButtonTypes().clear();
            callAlert.show();
        });

        ConnectionThread.requestFromServer(
                Request.CALL_REQUEST
                        .replace("NUMBER",
                                String.valueOf(targetNumber)
                        )
                , new CallTask(callAlert)
        );
    }
}
