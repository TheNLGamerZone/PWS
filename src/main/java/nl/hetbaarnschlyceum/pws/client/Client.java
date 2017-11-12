package nl.hetbaarnschlyceum.pws.client;

public class Client {
    private void print(String string)
    {
        System.out.printf("[Client] %s\n", string);
    }

    public Client()
    {
        print("Client wordt gestart..");
    }
}
