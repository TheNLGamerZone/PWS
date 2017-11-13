package nl.hetbaarnschlyceum.pws.server.tc;

import nl.hetbaarnschlyceum.pws.server.Server;
import nl.hetbaarnschlyceum.pws.server.sql.SQL;

public class TCServer
{
    private final String prefix = "TC Server";
    private final int port = 2;
    private Thread server;
    public SQL sql;

    public TCServer()
    {
        print("TC server wordt gestart op port %s..", Integer.toString(this.port));

        this.sql = new SQL("localhost:3306", "root", "pws-test", this.prefix);
        this.server = new Thread(new Server(this.port, this.prefix));
        this.server.start();
    }

    private void print(String string, String... args)
    {
        System.out.printf("[%s] %s\n", this.prefix, String.format(string, args));
    }
}
