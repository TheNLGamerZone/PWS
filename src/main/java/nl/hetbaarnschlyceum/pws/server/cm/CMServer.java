package nl.hetbaarnschlyceum.pws.server.cm;

public class CMServer {
    private void print(String string)
    {
        System.out.printf("[CM Server] %s\n", string);
    }

    public CMServer()
    {
        print("CM server wordt gestart..");
    }
}
