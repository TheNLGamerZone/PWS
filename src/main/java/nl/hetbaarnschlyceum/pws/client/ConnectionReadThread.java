package nl.hetbaarnschlyceum.pws.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

import static nl.hetbaarnschlyceum.pws.PWS.print;

public class ConnectionReadThread extends Thread
{
    private BufferedReader bufferedReader;
    private Socket socket;
    ConnectionThread connectionThread;

    ConnectionReadThread(ConnectionThread connectionThread,
                                Socket socket)
    {
        this.socket = socket;
        this.connectionThread = connectionThread;

        this.openConnection();
        this.start();
    }

    private void openConnection()
    {
        try
        {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e)
        {
            print("[FOUT] Er kon geen InputStream van de server worden opgehaald");
            connectionThread.closeConnection();
            e.printStackTrace();
        }
    }

    void closeConnection()
    {
        if (bufferedReader != null)
        {
            try
            {
                bufferedReader.close();
            } catch (IOException e)
            {
                print("[FOUT] De InputStream kon niet worden gesloten");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        while (connectionThread != null)
        {
            try
            {
                StringBuffer sb = new StringBuffer();
                while (bufferedReader.ready()) {
                    char[] c = new char[] { 1024 };
                    bufferedReader.read(c);
                    sb.append(c);
                }

                connectionThread.processDataReceived(sb.toString());
            } catch (SocketException e)
            {
                print("[FOUT] De verbinding met de server werd onverwacht verbroken");
                connectionThread.closeConnection();
            } catch (IOException e)
            {
                print("[FOUT] De InputStream kon niet worden afgelezen");
                connectionThread.closeConnection();
            }
        }
    }
}
