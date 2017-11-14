package nl.hetbaarnschlyceum.pws.server.tc.client;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.UUID;

public class ClientManager {
    private ArrayList<Client> loadedClients;

    public ClientManager()
    {
        this.loadedClients = new ArrayList<>();
    }

    public void clientConnect(SocketChannel socketChannel)
    {
        this.loadedClients.add(new Client(socketChannel));
    }

    public int registerClient(String name, int number, String hash)
    {
        return 0;
    }

    private boolean checkAvailable(int number)
    {
        return false;
    }

    public Client getClient(UUID uuid)
    {
        for (Client client : loadedClients)
        {
            if (client.getUuid().equals(uuid))
            {
                return client;
            }
        }

        return null;
    }

    public Client getClient(SocketChannel socketChannel)
    {
        for (Client client : loadedClients)
        {
            if (client.getSocketChannel() == socketChannel)
            {
                return client;
            }
        }

        return null;
    }
}
