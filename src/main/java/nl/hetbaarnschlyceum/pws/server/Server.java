package nl.hetbaarnschlyceum.pws.server;

import nl.hetbaarnschlyceum.pws.server.tc.TCServer;
import nl.hetbaarnschlyceum.pws.server.tc.client.Client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.Executors;

public class Server implements Runnable
{
    private final int port;
    private final String prefix;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    public Server(int port, String prefix)
    {
        this.port = port;
        this.prefix = prefix;
        init();
    }

    private void init() {
        try {
            this.print("Server selectors worden gemaakt..");
            this.selector = Selector.open();
            this.serverSocketChannel = ServerSocketChannel.open();
            this.serverSocketChannel.bind(new InetSocketAddress("localhost", this.port));
            this.serverSocketChannel.configureBlocking(false);
            this.serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e)
        {
            this.print("Er is een fout op getreden tijdens het opstarten van de server (%s): ", e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }

        this.print("Server selectors zijn gemaakt.");
    }

    private void print(String msg, String... args)
    {
        System.out.printf("[%s] %s\n", this.prefix, String.format(msg, args));
    }

    private void handleAccept(SelectionKey key)
            throws IOException {
        SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
        String address = socketChannel.socket().getInetAddress() + ":" + socketChannel.socket().getPort();
        socketChannel.configureBlocking(false);
        socketChannel.register(this.selector, SelectionKey.OP_READ, address);
        TCServer.getClientManager().clientConnect(socketChannel);

        this.print("Verbinding van %s", address);
    }

    private void handleRead(SelectionKey key)
            throws IOException {
        // msg syntax:
        // UUID><DATA

        SocketChannel socketChannel = (SocketChannel) key.channel();
        StringBuilder stringBuilder = new StringBuilder();
        Client client = TCServer.getClientManager().getClient(socketChannel);
        ByteBuffer tempBuffer = ByteBuffer.allocate(1024);

        int read = 0;
        while ((read = socketChannel.read(tempBuffer)) > 0) {
            tempBuffer.flip();

            byte[] bytes = new byte[tempBuffer.limit()];
            tempBuffer.get(bytes);

            /*DEBUG:
            print("Ontvangen: " + new String(bytes));*/
        }

        tempBuffer.flip();
        client.byteBuffer.flip();
        client.byteBuffer = ByteBuffer.allocate(1024).put(client.byteBuffer).put(tempBuffer);

        System.out.println(client);

        client.byteBuffer.flip();
        byte[] bytes = new byte[client.byteBuffer.limit()];
        client.byteBuffer.get(bytes);
        stringBuilder.append(new String(bytes));

        String data = stringBuilder.toString();
        System.out.println("Raw data: " + data);
        if (data.length() > 4 && data.substring(data.length() - 4).equals("_&2d"))
        {
            this.processData(client, data);
            client.byteBuffer.clear();
        }
    }

    private void processData(Client client, String data)
    {
        data = data.substring(0, data.length() - 4);

        try
        {
            System.out.println("Data: " + data);

            //MySQL register test:
            if (data.contains("name=") && data.contains("hash=") && data.contains("number="))
            {
                String[] dataArr = data.split("&");

                if (dataArr.length > 2)
                {
                    String name = null;
                    String hash = null;
                    int number = 0;

                    for (String d : dataArr)
                    {
                        String[] dt = d.split("=");
                        switch (dt[0])
                        {
                            case "name":
                                name = dt[1];
                                break;
                            case "hash":
                                hash = dt[1];
                                break;
                            case "number":
                                number = Integer.valueOf(dt[1]);
                                break;
                            default:
                                break;
                        }
                    }

                    int result = TCServer.getClientManager().registerClient(client, name, number, hash);
                    System.out.println("Resultaat: " + result);
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            // Hier alle exceptions opvangen die mogelijk voorkomen
        }
    }

    public static void sendMessage(UUID uuid, String data)
    {
        sendMessage(TCServer.getClientManager().getClient(uuid).getSocketChannel(), data);
    }

    public static void sendMessage(Client client, String data)
    {
        sendMessage(client.getSocketChannel(), data);
    }

    private static void sendMessage(SocketChannel socketChannel, String data)
    {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data.getBytes());
        try {
            socketChannel.write(byteBuffer);
            byteBuffer.rewind();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        this.print("Wachten op inkomende verbindingen..");

        try {
            Iterator<SelectionKey> iterator;
            SelectionKey key;

            while (this.serverSocketChannel.isOpen())
            {
                this.selector.select();
                iterator = this.selector.selectedKeys().iterator();

                while(iterator.hasNext()) {
                    key = iterator.next();
                    iterator.remove();

                    if(key.isAcceptable())
                    {
                        // Binnenkomende verbinding
                        this.handleAccept(key);
                    }

                    if(key.isReadable())
                    {
                        // Binnenkomende data
                        this.handleRead(key);
                    }
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
