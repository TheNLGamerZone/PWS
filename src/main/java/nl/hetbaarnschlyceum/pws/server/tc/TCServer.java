package nl.hetbaarnschlyceum.pws.server.tc;

import nl.hetbaarnschlyceum.pws.PWS;
import nl.hetbaarnschlyceum.pws.server.Server;
import nl.hetbaarnschlyceum.pws.server.tc.client.ClientManager;
import nl.hetbaarnschlyceum.pws.server.tc.sql.SQL;

import java.util.Scanner;
import java.util.concurrent.*;

public class TCServer
{
    private Thread server;

    private static SQL sql;
    private static ScheduledExecutorService executor;
    private static ClientManager clientManager;

    public TCServer(String sqlHost,
                    String sqlPort,
                    String sqlUser,
                    String sqlPass,
                    String serverPort)
    {
        PWS.print("[INFO] TC server wordt gestart op port %s..", sqlPort);

        sql = new SQL(String.format("%1$s:%2$s", sqlHost, sqlPort),
                sqlUser,
                sqlPass);
        executor = Executors.newScheduledThreadPool(PWS.corePoolThreads);
        clientManager = new ClientManager();
        this.server = new Thread(new Server(Integer.valueOf(serverPort)));
        this.server.start();

        Scanner scanner = new Scanner(System.in);

        while (true)
        {
            String cmd = scanner.nextLine();

            switch (cmd){
                case "stop":
                    PWS.print("[INFO] Server wordt gestopt..");
                    System.exit(2);
                    break;
                default:
                    break;
            }
        }
    }

    public static SQL getSQL()
    {
        return sql;
    }

    public static ClientManager getClientManager()
    {
        return clientManager;
    }

    public static Future<?> executeTask(Callable callable)
    {
        return executor.submit(callable);
    }

    public static ScheduledFuture<?> executeDelayedTask(Callable callable,
                                                        long delay,
                                                        TimeUnit timeUnit)
    {
        return executor.schedule(callable, delay, timeUnit);
    }
}
