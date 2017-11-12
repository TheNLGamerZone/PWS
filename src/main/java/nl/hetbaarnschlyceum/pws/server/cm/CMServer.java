package nl.hetbaarnschlyceum.pws.server.cm;

public class CMServer
{
    public CMServer()
    {
        print("CM server wordt gestart..");
    }

    private void print(String string)
    {
        System.out.printf("[CM Server] %s\n", string);
    }
}
