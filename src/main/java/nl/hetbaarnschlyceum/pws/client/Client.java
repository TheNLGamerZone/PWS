package nl.hetbaarnschlyceum.pws.client;

public class Client
{
    public Client()
    {
        print("Client wordt gestart..");
    }

    private void print(String string)
    {
        System.out.printf("[Client] %s\n", string);
    }
}
