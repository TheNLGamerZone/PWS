package nl.hetbaarnschlyceum.pws.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

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

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel socketChannel = ((ServerSocketChannel) key.channel()).accept();
        String address =  (new StringBuilder(socketChannel.socket().getInetAddress().toString()))
                .append(":")
                .append(socketChannel.socket().getPort()).toString();
        socketChannel.configureBlocking(false);
        socketChannel.register(this.selector, SelectionKey.OP_READ);

        this.print("Verbinding van %s", address);
    }

    private void handleRead(SelectionKey key)
    {

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
