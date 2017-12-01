package nl.hetbaarnschlyceum.pws.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import static nl.hetbaarnschlyceum.pws.PWS.print;

public class ConnectionReadThread extends Thread
{
    private DataInputStream dataInputStream;
    private Socket socket;
    private ConnectionThread connectionThread;

    public ConnectionReadThread(ConnectionThread connectionThread,
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
            dataInputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e)
        {
            print("[FOUT] Er kon geen InputStream van de server worden opgehaald");
            connectionThread.closeConnection();
            e.printStackTrace();
        }
    }

    protected void closeConnection()
    {
        if (dataInputStream != null)
        {
            try
            {
                dataInputStream.close();
            } catch (IOException e)
            {
                print("[FOUT] De InputStream kon niet worden gesloten");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        while (true)
        {
            try
            {
                connectionThread.processDataReceived(dataInputStream.readUTF());
            } catch (IOException e)
            {
                print("[FOUT] De InputStream kon niet worden afgelezen");
                e.printStackTrace();
            }
        }
    }
}
