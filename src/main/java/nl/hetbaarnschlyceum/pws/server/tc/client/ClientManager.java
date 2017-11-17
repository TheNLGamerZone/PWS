package nl.hetbaarnschlyceum.pws.server.tc.client;

import nl.hetbaarnschlyceum.pws.server.tc.TCServer;
import nl.hetbaarnschlyceum.pws.server.tc.sql.SQL;

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

    public int registerClient(Client client, String name, int number, String hash)
            throws SQLException
    {
        System.out.println("Gebruiker wordt gemaakt");
        // Return codes:
        //   1: Success
        //   0: Failed -> Name taken
        //  -1: Failed -> Number taken
        //  -2: Failed -> Invalid name (Zou niet mogelijk moeten zijn, maarja)
        //  -3: Failed -> Syserr

        //TODO: Hier naamcheck maken
        if (1 == 2)
        {
            return -2;
        }

        int availabilityCheck = this.checkAvailable(name, number);

        if (availabilityCheck != 1)
        {
            return availabilityCheck;
        }

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

        TCServer.getSQL().updateQuery(sqlQuery);
        client.setName(name);
        client.setNumber(number);
        client.setPublic_cl(true);
        client.setHidden(false);
        client.setStatus(Status.ONLINE);

        return 1;
    }

    // TODO: Hier gaat het fout
    private int checkAvailable(String name, int number)
            throws SQLException {
        boolean nameOccupied = TCServer.getSQL().entryExists("SELECT * FROM CLIENTS WHERE (name = ?)",
                preparedStatement -> preparedStatement.setString(1, name));
        boolean resultOccupied = TCServer.getSQL().entryExists("SELECT * FROM CLIENTS WHERE (number = ?)",
                preparedStatement -> preparedStatement.setInt(1, number));

        if (nameOccupied)
        {
            // Naam is bezet -> 0
            return 0;
        } else if (resultOccupied)
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
