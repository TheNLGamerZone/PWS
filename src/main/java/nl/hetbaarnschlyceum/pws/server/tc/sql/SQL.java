package nl.hetbaarnschlyceum.pws.server.tc.sql;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.sql.*;

public class SQL {
    private final String jdbcDriver = "com.mysql.jdbc.Driver";
    private String dbAddress;
    private final String user;
    private final String pass;
    private final String prefix;

    private DataSource dataSource;

    public SQL(String dbAddress, String user, String pass, String prefix)
    {
        this.dbAddress = "jdbc:mysql://" + dbAddress + "/";
        this.user = user;
        this.pass = pass;
        this.prefix = prefix;
        this.checkDB();
    }

    private void checkDB()
    {
        Connection connection = null;
        boolean dbReady = false;

        try
        {
            print("De MySQL driver wordt geladen..");
            Class.forName(this.jdbcDriver);

            print("Verbinding maken met de MySQL server (%s)..", this.dbAddress);
            connection = DriverManager.getConnection(this.dbAddress, this.user, this.pass);

            print("Databases controleren..");
            ResultSet resultSet = connection.getMetaData().getCatalogs();
            while (resultSet.next())
            {
                if (resultSet.getString(1).equalsIgnoreCase("PWSTCS"))
                {
                    dbReady = true;
                }
            }
            resultSet.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            print("Er kon geen verbinding worden gemaakt met de MySQL server (%s)", e.getMessage());
            System.exit(-1);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
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
        Connection connection = null;
        Statement statement = null;

        try {
            connection = DriverManager.getConnection(this.dbAddress, this.user, this.pass);
            statement = connection.createStatement();

            statement.executeUpdate("CREATE DATABASE PWSTCS");
            print("Database aangemaakt");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (connection != null)
                {
                    connection.close();
                }

                if (statement != null)
                {
                    statement.close();
                }
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }

        this.createConnectionPool();
        this.createTables();
    }

    private void createTables()
    {
        print("Tables aan het maken..");
        this.updateQuery("USE PWSTCS");
        this.updateQuery("CREATE TABLE CLIENTS (" +
                "name VARCHAR(255) UNIQUE NOT NULL," +
                "public_cl BOOL NOT NULL," +
                "number INTEGER UNIQUE NOT NULL," +
                "public_key LONGTEXT NOT NULL," +
                "hash VARCHAR(64) NOT NULL, " +
                "hidden BOOL NOT NULL," +
                "whitelist BLOB," +
                "uuid VARCHAR(36) UNIQUE NOT NULL," +
                "status INTEGER NOT NULL," +
                "PRIMARY KEY (uuid))");
        print("Tables gemaakt");
    }

    private void createConnectionPool()
    {
        print("Connection pool maken..");
        GenericObjectPool objectPool = new GenericObjectPool();
        objectPool.setMaxActive(4);

        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(this.dbAddress, this.user, this.pass);
        new PoolableConnectionFactory(connectionFactory, objectPool, null, null, false, true);
        this.dataSource = new PoolingDataSource(objectPool);
        print("Connection pool gemaakt");
    }

    //TODO: Hier nog luren voor een fout
    public ResultSet runQuery(String query, String... args)
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
                if (resultSet != null)
                {
                    resultSet.close();
                }

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
    {
        Connection connection = null;
        Statement statement = null;

        try
        {
            System.out.println("1");
            connection = this.dataSource.getConnection();
            System.out.println("2");

            statement = connection.createStatement();
            System.out.println("3");

            statement.executeUpdate(query);
            System.out.println("4");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try
            {
                if (statement != null)
                {
                    statement.close();
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
    }

    private void print(String string, String... args)
    {
        System.out.printf("[%s] %s\n", this.prefix, String.format(string, args));
    }
}
