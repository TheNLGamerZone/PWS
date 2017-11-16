package nl.hetbaarnschlyceum.pws.server.tc.client;

import nl.hetbaarnschlyceum.pws.server.tc.TCServer;

import java.nio.channels.SocketChannel;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public int registerClient(Client client, String name, int number, String hash) throws SQLException {
        System.out.println("Gebruiker wordt gemaakt");
        // Return codes:
        //   1: Success
        //   0: Failed -> Name taken
        //  -1: Failed -> Number taken
        //  -2: Failed -> Invalid name (Zou niet mogelijk moeten zijn, maarja)

        //TODO: Hier naamcheck maken
        if (1 == 2)
        {
            return -2;
        }

        System.out.println("a");
        int availabilityCheck = this.checkAvailable(name, number);
        System.out.println("b");

        if (availabilityCheck != 1)
        {
            return availabilityCheck;
        }
        System.out.println("c");

        String sqlQuery = "INSERT INTO CLIENTS (name, " +
                "public_cl, " +
                "number, " +
                "public_key, " +
                "hash, " +
                "hidden, " +
                "whitelist, " +
                "uuid, " +
                "status) " +
                "VALUES(" +
                "'" + name + "', " +
                "1, " +
                number + " , " +
                "'WIP', " +
                "'" + hash + "', " +
                "0, " +
                "NULL, " +
                "'" +  client.getUuid() + "' , " +
                "1)";
        System.out.println("d");

        TCServer.getSQL().updateQuery(sqlQuery);
        System.out.println("e");

        client.setName(name);
        client.setNumber(number);
        client.setPublic_cl(true);
        client.setHidden(false);
        client.setStatus(Status.ONLINE);
        System.out.println("f");

        return 1;
    }

    // TODO: Hier gaat het fout
    private int checkAvailable(String name, int number) throws SQLException {
        String sqlQueryName = "SELECT * FROM CLIENTS WHERE (name = ?)";
        String sqlQueryNumber = "SELECT * FROM CLIENTS WHERE (number = ?)";

        ResultSet queryResultName = TCServer.getSQL().runQuery(sqlQueryName, name);
        ResultSet queryResultNumber = TCServer.getSQL().runQuery(sqlQueryNumber, String.valueOf(number));

        if (queryResultName.next())
        {
            // Naam is bezet -> 0
            return 0;
        } else if (queryResultNumber.next())
        {
            // Nummer is bezet -> -1
            return -1;
        } else
        {
            // Zowel naam als nummer zijn nog vrij -> 1
            return 1;
        }
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
