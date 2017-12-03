package nl.hetbaarnschlyceum.pws.client;

import nl.hetbaarnschlyceum.pws.PWS;

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
    private ConnectionThread connectionThread;

    public Client(String serverIP, String serverPort)
    {
        print("[INFO] Client wordt gestart..");

        this.connectionThread = new ConnectionThread(serverIP, Integer.valueOf(serverPort));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (this.connectionThread.isConnected())
        {
            this.connectionThread.processedRequestFromServer(
                    this.connectionThread.prepareMessage(PWS.MessageIdentifier.CONNECTED)
            );
        }
    }
}
