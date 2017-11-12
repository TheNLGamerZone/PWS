package nl.hetbaarnschlyceum.pws.server.tc;

public class TCServer {
    private void print(String string)
    {
        System.out.printf("[TC Server] %s\n", string);
    }

    public TCServer()
    {
        print("TC server wordt gestart..");
    }
}
