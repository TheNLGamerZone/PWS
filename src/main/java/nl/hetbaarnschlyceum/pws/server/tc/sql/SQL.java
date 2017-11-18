package nl.hetbaarnschlyceum.pws.server.tc.sql;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.sql.*;
import java.util.UUID;

import static nl.hetbaarnschlyceum.pws.server.tc.TCServer.print;

public class SQL {
    private final String jdbcDriver = "com.mysql.jdbc.Driver";
    private String dbAddress;
    private final String user;
    private final String pass;

    private DataSource dataSource;

    public SQL(String dbAddress,
               String user,
               String pass)
    {
        this.dbAddress = "jdbc:mysql://" + dbAddress + "/";
        this.user = user;
        this.pass = pass;
        this.checkDB();
    }

    private void checkDB()
    {
        boolean dbReady = false;

        try
        {
            print("De MySQL driver wordt geladen..");
            Class.forName(this.jdbcDriver);
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }

        print("Verbinding maken met de MySQL server (%s)..", this.dbAddress);

        try
                (
                        Connection connection = DriverManager.getConnection(this.dbAddress, this.user, this.pass);
                        ResultSet resultSet = connection.getMetaData().getCatalogs()

                )
        {
            print("Databases controleren..");
            while (resultSet.next())
            {
                if (resultSet.getString(1).equalsIgnoreCase("PWSTCS"))
                {
                    dbReady = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (dbReady)
        {
            // Database is aangemaakt, connection pool kan worden gemaakt
            this.dbAddress = this.dbAddress + "PWSTCS";
            this.createConnectionPool();
        } else
        {
            // Database is nog niet aangemaakt, dus die moeten eerst worden gemaakt
            print("Database aanmaken..");
            this.createDatabase();
        }
    }

    private void createDatabase()
    {
        try (
                Connection connection = DriverManager.getConnection(this.dbAddress, this.user, this.pass);
                Statement statement = connection.createStatement()
                )
        {
            statement.executeUpdate("CREATE DATABASE PWSTCS");
            print("Database aangemaakt");

            this.createConnectionPool();
            this.createTables();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void createTables()
            throws SQLException
    {
        print("Tables aan het maken..");
        this.updateQuery("USE PWSTCS");
        this.updateQuery("CREATE TABLE CLIENTS (" +
                "name VARCHAR(255) UNIQUE NOT NULL, " +
                "public_cl BOOL NOT NULL, " +
                "number INTEGER UNIQUE NOT NULL, " +
                "public_key LONGBLOB NOT NULL, " +
                "hash VARCHAR(64) NOT NULL, " +
                "hidden BOOL NOT NULL, " +
                "whitelist MEDIUMTEXT, " +
                "uuid VARCHAR(36) UNIQUE NOT NULL, " +
                "status INTEGER NOT NULL, " +
                "failed_attempts TEXT, " +
                "PRIMARY KEY (uuid))");
        print("Tables gemaakt");
    }

    private void createConnectionPool()
    {
        print("Connection pool maken..");
        GenericObjectPool objectPool = new GenericObjectPool();
        objectPool.setMaxActive(4);

        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(this.dbAddress,
                this.user,
                this.pass);
        new PoolableConnectionFactory(connectionFactory,
                objectPool,
                null,
                null,
                false,
                true);
        this.dataSource = new PoolingDataSource(objectPool);
        print("Connection pool gemaakt");
    }

    private PreparedStatement createPreparedStatement(Connection connection,
                                                      String query,
                                                      PreparedStatementSetter preparedStatementSetter)
            throws SQLException
    {
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatementSetter.setValues(preparedStatement);

        return preparedStatement;
    }

    public boolean entryExists(String query,
                               PreparedStatementSetter preparedStatementSetter)
            throws SQLException
    {
        try (
                Connection connection = this.dataSource.getConnection();
                PreparedStatement preparedStatement = this.createPreparedStatement(connection,
                        query,
                        preparedStatementSetter);
                ResultSet resultSet = preparedStatement.executeQuery()
                )
        {
            if (resultSet.next())
            {
                return true;
            }
        }

        return false;
    }

    public Object[] loadClient(String name)
            throws SQLException
    {
        Object[] clientData = new Object[7];
        PreparedStatementSetter preparedStatementSetter = preparedStatement -> preparedStatement.setString(1, name);

        try (
                Connection connection = this.dataSource.getConnection();
                PreparedStatement preparedStatement = this.createPreparedStatement(connection,
                        "SELECT public_cl, " +
                                "number, " +
                                "public_key, " +
                                "hidden, " +
                                "whitelist, " +
                                "uuid, " +
                                "status " +
                                "FROM CLIENTS WHERE name = ?",
                        preparedStatementSetter);
                ResultSet resultSet = preparedStatement.executeQuery()
                )
        {
            if (resultSet.next())
            {
                clientData[0] = resultSet.getBoolean("public_cl"); // Boolean die aangeeft of een gebruiker te vinden is
                clientData[1] = resultSet.getInt("number"); // Nummer van de gebruiker
                clientData[2] = resultSet.getBlob("public_key"); // Publieke RSA sleutel van de gebruiker
                clientData[3] = resultSet.getBoolean("hidden"); // Boolean die aangeeft of de gebruiker een whitelist heeft
                clientData[4] = resultSet.getString("whitelist"); // Array met nummers die op de whitelist staan
                clientData[5] = UUID.fromString(resultSet.getString("uuid")); // Universally unique identifier van de gebruiker
                clientData[6] = resultSet.getInt("status"); // Status van de gebruiker die aangeeft of de gebruiker online is

                return clientData;
            } else
            {
                return null;
            }
        }
    }

    public String getClientHash(String name)
            throws SQLException
    {
        PreparedStatementSetter preparedStatementSetter = preparedStatement -> preparedStatement.setString(1, name);

        try (
                Connection connection = this.dataSource.getConnection();
                PreparedStatement preparedStatement = this.createPreparedStatement(connection,
                        "SELECT hash FROM CLIENTS WHERE name = ?",
                        preparedStatementSetter);
                ResultSet resultSet = preparedStatement.executeQuery()
                )
        {
            if (resultSet.next())
            {
                return resultSet.getString("hash");
            } else
            {
                return "NOTFOUND";
            }
        }
    }

    @Deprecated
    public ResultSet runQuery(String query,
                              String... args)
    {
        ResultSet resultSet = null;
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try
        {
            connection = this.dataSource.getConnection();
            preparedStatement = connection.prepareStatement(query);
            int index = 1;

            for (String arg : args)
            {
                preparedStatement.setString(index, arg);
                index++;
            }

            resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try
            {
                if (preparedStatement != null)
                {
                    preparedStatement.close();
                }

                if (connection != null)
                {
                    connection.close();
                }
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }

        return resultSet;
    }

    public void updateQuery(String query)
            throws SQLException
    {
        try (
                Connection connection = this.dataSource.getConnection();
                Statement statement = connection.createStatement()
                )
        {
            statement.executeUpdate(query);
        }
    }
}
