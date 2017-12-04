package nl.hetbaarnschlyceum.pws.client;

import nl.hetbaarnschlyceum.pws.PWS;
import nl.hetbaarnschlyceum.pws.client.gui.GUIMainClass;

import static nl.hetbaarnschlyceum.pws.PWS.print;

public class Client
{
    public static final String[] forbiddenStrings = new String[]{"&",
            "=",
            "_&2d",
            "<<->>",
            "<<&>>",
            "<<*3456*34636*>>",
            "HMAC_X8723784X"
    };
    private static ConnectionThread connectionThread;
    private static String serverIP;
    private static int serverPort;

    public Client(String serverIP, String serverPort)
    {
        print("[INFO] Client wordt gestart..");
        Client.serverIP = serverIP;
        Client.serverPort = Integer.valueOf(serverPort);

        GUIMainClass.start();
    }

    public static boolean initConnection(String username, String password)
    {
        connectionThread = new ConnectionThread(serverIP, serverPort);

        if (connectionThread.isConnected())
        {
            connectionThread.processedRequestFromServer(
                    connectionThread.prepareMessage(PWS.MessageIdentifier.CONNECTED)
            );

            return true;
        }

        return false;
    }
}
