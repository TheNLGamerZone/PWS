package nl.hetbaarnschlyceum.pws.server.tc.client;

import nl.hetbaarnschlyceum.pws.PWS;
import nl.hetbaarnschlyceum.pws.server.Server;
import nl.hetbaarnschlyceum.pws.server.tc.OperationResult;
import nl.hetbaarnschlyceum.pws.server.tc.Request;
import nl.hetbaarnschlyceum.pws.server.tc.TCServer;

import javax.crypto.SecretKey;
import java.nio.channels.SocketChannel;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import static nl.hetbaarnschlyceum.pws.PWS.print;

public class ClientManager {
    private ArrayList<Client> loadedClients;

    public ClientManager()
    {
        this.loadedClients = new ArrayList<>();
    }

    public void clientConnect(SocketChannel socketChannel, String IP)
    {
        this.loadedClients.add(new Client(socketChannel, IP));
    }

    private void failedLoginAttempt(Client client)
    {
        //TODO: Hier nog iets maken dat een tempban oid maakt
        client.addFailedAttempt();
    }

    public void processRequestResult(Client client, String requestResult)
    {
        //TODO: Dit maken
    }

    public void call(Client client,
                     int targetNumber,
                     UUID requestID)
    {
        print("[INFO] Gebruikers %s probeert %s te bellen..",
                client.getName(),
                String.valueOf(targetNumber)
        );
        String response = Server.prepareMessage(client,
                PWS.MessageIdentifier.REQUEST_RESULT,
                requestID,
                Request.CALL_REQUEST_RESULT.replace("OR",
                        String.valueOf(OperationResult.FAILED_SYS_ERROR))
        );

        try
        {
            if (targetNumber == client.getNumber())
            {
                response = Server.prepareMessage(client,
                        PWS.MessageIdentifier.REQUEST_RESULT,
                        requestID,
                        Request.CALL_REQUEST_RESULT.replace("OR",
                                String.valueOf(OperationResult.FAILED_SAME_NUMBER))
                );

                Server.sendMessage(client, response);
                return;
            }

            int existingNumber = checkAvailable("DEVNM_X76786X", targetNumber);

            if (existingNumber != OperationResult.FAILED_DUPLICATE_NUMBER)
            {
                response = Server.prepareMessage(client,
                        PWS.MessageIdentifier.REQUEST_RESULT,
                        requestID,
                        Request.CALL_REQUEST_RESULT.replace("OR",
                                String.valueOf(OperationResult.FAILED_UNKNOWN_NUMBER))
                );

                Server.sendMessage(client, response);
                return;
            }

            Client targetClient = getClient(targetNumber);

            if (targetClient == null
                    || targetClient.getStatus() == Status.OFFLINE
                    || targetClient.getStatus() == Status.INVISIBLE
                    || targetClient.getStatus() == Status.DO_NOT_DISTURB)
            {
                response = Server.prepareMessage(client,
                        PWS.MessageIdentifier.REQUEST_RESULT,
                        requestID,
                        Request.CALL_REQUEST_RESULT.replace("OR",
                                String.valueOf(OperationResult.FAILED_USER_OFFLINE))
                );

                Server.sendMessage(client, response);
                return;
            }

            response = Server.prepareMessage(client,
                    PWS.MessageIdentifier.REQUEST_RESULT,
                    requestID,
                    Request.CALL_REQUEST_RESULT.replace("OR",
                            String.valueOf(OperationResult.SUCCESS_CALLING))
            );

            Server.sendRequest(targetClient,
                    Request.CALL_REQUEST.replace("NUMBER", String.valueOf(client.getNumber()))
            );

        } catch (SQLException e) {
            print("[FOUT] Er is iets mis gegaan bij het bellen van %s door %s: %s",
                    String.valueOf(targetNumber),
                    client.getName(),
                    e.getMessage()
            );
        }

        print("[INFO] Resultaat verzoek %s: %s", client.getName(), response);
        Server.sendMessage(client, response);
    }

    public void clientConnectionDropped(Client client)
    {
        clientLogout(client, true);
    }

    private SecretKey clientLogout(Client client,
                                   boolean silent)
    {
        if (!silent) {
            print("[INFO] Gebruiker %s probeert uit te loggen..", client.getName());
        }

        if (this.loadedClients.contains(client))
        {
            SecretKey sessionKey = client.getSessionKey();

            this.loadedClients.remove(client);

            print("[INFO] Gebruiker %s is uitgelogd", client.getName());
            return sessionKey;
        }

        return null;
    }

    /**
     * Hiermee kan de client uitloggen
     * @param client De client
     * @return Een string met de AES-sleutels, zodat er nog een laatste bericht kan worden gestuurd
     */
    public SecretKey clientLogout(Client client)
    {
        return clientLogout(client, false);
    }

    public void loginTask(Client client, String name, String hash)
    {
        String response = Server.prepareMessage(client,
                PWS.MessageIdentifier.LOGIN_RESULT,
                String.valueOf(OperationResult.FAILED_SYS_ERROR)
        );

        try
        {
            int loginResult = clientLogin(client, name, hash);
            response = Server.prepareMessage(client,
                    PWS.MessageIdentifier.LOGIN_RESULT,
                    String.valueOf(loginResult)
            );
        } catch (SQLException e) {
            print("[FOUT] Er is iets mis gegaan bij het inloggen van gebruiker %s: %s",
                    name,
                    e.getMessage()
            );
        }

        Server.sendMessage(client, response);
    }

    public void registerTask(Client client,
                             String name,
                             int number,
                             String hash)
    {
        String response = Server.prepareMessage(client,
                PWS.MessageIdentifier.LOGIN_RESULT,
                String.valueOf(OperationResult.FAILED_SYS_ERROR)
        );

        try {
            int registerResult = registerClient(client, name, number, hash);
            response = Server.prepareMessage(client,
                    PWS.MessageIdentifier.LOGIN_RESULT,
                    String.valueOf(registerResult)
            );
        } catch (SQLException e) {
            print("[FOUT] Er is iets mis gegaan bij het registreren van gebruiker %s: %s",
                    name,
                    e.getMessage()
            );
        }

        Server.sendMessage(client, response);
    }

    public int clientLogin(Client client, String name, String hash)
            throws SQLException
    {
        print("[INFO] Gebruiker %s probeert in te loggen..", name);

        if (checkAvailable(name, -1) == OperationResult.UNIQUE_USER)
        {
            print("[INFO] Gebruiker %s bestaat niet", name);
            return OperationResult.FAILED_UNKNOWN_USER;
        }

        String sqlHash = TCServer.getSQL().getClientHash(name);

        if (!sqlHash.equals(hash))
        {
            print("[INFO] Gebruiker %s heeft een verkeerd wachtwoord opgegeven", name);
            this.failedLoginAttempt(client);
            return OperationResult.FAILED_INCORRECT_HASH;
        }

        print("[INFO] Gebruiker %s heeft een correct wachtwoord opgegeven", name);
        return loadClient(client, name);
    }

    public int loadClient(Client client, String name)
            throws SQLException
    {
        print("[INFO] Gebruiker %s wordt geladen..", name);

        if (checkAvailable(name, -1) == OperationResult.UNIQUE_USER)
        {
            print("[INFO] Gebruiker %s bestaat niet", name);
            return OperationResult.FAILED_UNKNOWN_USER;
        }

        Object[] clientData = TCServer.getSQL().loadClient(name);

        try {
            client.setName(name);
            client.setPublic((boolean) clientData[0]);
            client.setNumber((int) clientData[1]);
            client.setRSAKey((Blob) clientData[2]); // WIP
            client.setHidden((boolean) clientData[3]); // WIP
            client.setWhitelist(new int[]{0, 1}); // WIP
            client.setUUID((UUID) clientData[5]);
            client.setStatus((int) clientData[6]);
        } catch (NullPointerException e)
        {
            print("[FOUT] Gebruiker %s kon niet worden aangemaakt, " +
                    "omdat de SQL-server niet volledige informatie kon geven!", name);
            return OperationResult.FAILED_SYS_ERROR;
        }

        return OperationResult.SUCCESS_LOGGED_IN;
    }

    public int registerClient(Client client,
                              String name,
                              int number,
                              String hash)
            throws SQLException
    {
        print("[INFO] Gebruiker %1$s met nummer %2$s wordt aangemaakt..", name, String.valueOf(number));

        //TODO: Hier naamcheck maken
        if (1 == 2)
        {
            return OperationResult.FAILED_NAMECHECK;
        }

        int availabilityCheck = this.checkAvailable(name, number);

        if (availabilityCheck != OperationResult.UNIQUE_USER)
        {
            print("[INFO] Gebruiker %1$s met nummer %2$s kon niet worden aangemaakt (%s)", name, String.valueOf(number), String.valueOf(availabilityCheck));
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
                "'" +  client.getUUID() + "' , " +
                "1)";

        TCServer.getSQL().updateQuery(sqlQuery);
        client.setName(name);
        client.setNumber(number);
        client.setPublic(true);
        client.setHidden(false);
        client.setStatus(Status.ONLINE);

        print("[INFO] Gebruiker %1$s met nummer %2$s aangemaakt", name, String.valueOf(number));
        return OperationResult.SUCCESS_LOGGED_IN;
    }

    private int checkAvailable(String name,
                               int number)
            throws SQLException {
        boolean nameOccupied = TCServer.getSQL().entryExists("SELECT * FROM CLIENTS WHERE (name = ?)",
                preparedStatement -> preparedStatement.setString(1, name));
        boolean resultOccupied = TCServer.getSQL().entryExists("SELECT * FROM CLIENTS WHERE (number = ?)",
                preparedStatement -> preparedStatement.setInt(1, number));

        if (nameOccupied)
        {
            return OperationResult.FAILED_DUPLICATE_NAME;
        } else if (resultOccupied && number != -1)
        {
            return OperationResult.FAILED_DUPLICATE_NUMBER;
        } else
        {
            return OperationResult.UNIQUE_USER;
        }
    }

    public ArrayList<Client> getClients() {
        return loadedClients;
    }

    public Client getClient(UUID uuid)
    {
        for (Client client : loadedClients)
        {
            if (client.getUUID().equals(uuid))
            {
                return client;
            }
        }

        return null;
    }

    public Client getClient(int number)
    {
        for (Client client : loadedClients)
        {
            if (client.getNumber() == number)
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
