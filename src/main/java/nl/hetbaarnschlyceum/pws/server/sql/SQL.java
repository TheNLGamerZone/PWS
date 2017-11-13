package nl.hetbaarnschlyceum.pws.server.sql;

public class SQL {
    private final String jdbcDriver = "com.mysql.jdbc.Driver";
    private final String dbAddress;
    private final String user;
    private final String pass;

    public SQL(String dbAddress, String user, String pass)
    {
        this.dbAddress = "jdbc:mysql://" + dbAddress + "/";
        this.user = user;
        this.pass = pass;

        this.checkDB();
    }

    private void checkDB()
    {

    }
}
