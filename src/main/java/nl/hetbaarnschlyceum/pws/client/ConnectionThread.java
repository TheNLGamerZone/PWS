package nl.hetbaarnschlyceum.pws.client;

import nl.hetbaarnschlyceum.pws.PWS;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import static nl.hetbaarnschlyceum.pws.PWS.print;

public class ConnectionThread implements Runnable
{
    private Socket socket;
    private DataOutputStream dataOutputStream;
    private Thread thread;
    private BlockingQueue<String> blockingQueue;
    private ArrayList<String> resultList;
    private ConnectionReadThread readThread;

    public ConnectionThread(String serverIP, int port)
    {
        print("[INFO] Verbinden met de server (%s:%s)..", serverIP, String.valueOf(port));

        try
        {
            socket = new Socket(serverIP, port);
            blockingQueue = new LinkedBlockingDeque<>();
            resultList = new ArrayList<>();

            print("[INFO] Verbonden met de server (%s:%s)", serverIP, String.valueOf(port));
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init()
    {
        try {
            dataOutputStream = new DataOutputStream(socket.getOutputStream());

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
        thread.interrupt();

        if (dataOutputStream != null)
        {
            try
            {
                dataOutputStream.close();
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

        readThread.closeConnection();
        readThread.interrupt();
    }

    public String requestFromServer(String request)
    {
        String requestID = UUID.randomUUID().toString();
        String formattedRequest = String.format("%s@#&$@%s", requestID, request);

        if (blockingQueue.offer(formattedRequest))
        {
            while (true)
            {
                for (String result : resultList)
                {
                    if (result.substring(0, 35).equals(requestID))
                    {
                        String requestResult = result.substring(41);

                        resultList.remove(result);
                        return requestResult;
                    }
                }
            }
        }
        return null;
    }

    void processDataReceived(String data)
    {

    }

    //TODO: AES enzo toevoegen
    public String prepareMessage(PWS.MessageIdentifier messageIdentifier,
                                String... arguments)
    {
        if (arguments.length == messageIdentifier.getArguments())
        {
            StringBuilder stringBuilder = new StringBuilder();

            for (String argument : arguments)
            {
                stringBuilder.append("<<&>>" + argument);
            }

            String formattedArguments = stringBuilder.toString().substring(5);
            String formattedRequest = String.format("%s<<->>%s",
                    messageIdentifier.getDataID(),
                    formattedArguments);

            return formattedRequest;
        }

        return null;
    }

    @Override
    public void run() {
        while (thread != null)
        {
            try
            {
                String request = blockingQueue.poll();

                dataOutputStream.writeUTF(request);
                dataOutputStream.flush();
            } catch (IOException e) {
                closeConnection();
                e.printStackTrace();
            }
        }
    }
}
