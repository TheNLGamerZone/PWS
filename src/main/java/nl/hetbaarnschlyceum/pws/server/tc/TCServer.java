package nl.hetbaarnschlyceum.pws.server.tc;

import nl.hetbaarnschlyceum.pws.server.Server;

public class TCServer
{
    private Server server;

    public TCServer()
    {
        print("TC server wordt gestart..");
    }

    private void print(String string)
    {
        System.out.printf("[TC Server] %s\n", string);
        this.server = new Server(0);
    }
}
