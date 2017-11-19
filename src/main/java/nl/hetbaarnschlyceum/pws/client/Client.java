package nl.hetbaarnschlyceum.pws.client;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client
{
    public static final String[] forbiddenStrings = new String[]{"&", "=", "_&2d"};

    public Client()
    {
        print("Client wordt gestart..");
        this.startConnection();
    }

    private void startConnection()
    {
        try {
            Socket socket = new Socket("localhost", 9348);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            while(socket.isConnected()) {
                //synchronized (socket) {
                writeMessage(socket,writer);
                //readServerMessage(socket);
                //}
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeMessage(Socket socket, BufferedWriter writer) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter message: ");
        String output = scanner.nextLine();
        writer.write(output);
        writer.flush();
        //writer.close();
    }

    private void print(String string)
    {
        System.out.printf("[Client] %s\n", string);
    }
}
