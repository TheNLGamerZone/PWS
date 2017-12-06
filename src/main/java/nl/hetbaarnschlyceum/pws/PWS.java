package nl.hetbaarnschlyceum.pws;

import nl.hetbaarnschlyceum.pws.crypto.ECDSA;
import nl.hetbaarnschlyceum.pws.crypto.KeyManagement;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.KeyPair;
import java.security.Security;

public class PWS {
    // Globale instellingen
    public static final int corePoolThreads = 5;
    public static Modes currentMode;
    public static KeyPair keyPair;

    public static void print(String string, String... args)
    {
        System.out.printf("[%s]%s\n", currentMode.getPrefix(), String.format(string, args));
    }

    public enum MessageIdentifier
    {
        REQUEST("REQ", 1), // client -> server                 REQ<<->>request_id<<&>>hmac<<&>>message_time<<&>>request
        REQUEST_RESULT("REQ_RSL", 1), // server -> client      REQ_RSL<<->>request_id<<&>>hmac<<&>>message_time<<&>>request_result
        CONNECTED("CONN_ACK", 0), // client -> server          CONN_ACK<<->>message_id<<&>>hmac<<&>>message_time
        DH_START("DH_ST", 1), // server -> client              DH_ST<<->>message_id<<&>>hmac<<&>>message_time<<&>>public_server
        DH_ACK("DH_ACK", 2), // client -> server               DH_ACK<<->>message_id<<&>>hmac<<&>>message_time<<&>>public_client<<&>>IV
        LOGIN("LGN", 0), // server -> client                   LGN<<->>message_id<<&>>hmac<<&>>message_time
        LOGIN_INFORMATION("LGN_INF", 4), // client -> server   LGN_INF<<->>message_id<<&>>hmac<<&>>message_time<<&>>username<<&>>password_hash<<&>>new_user<<&>>number_if_new
        LOGIN_RESULT("LGN_RSL", 1), // server -> client        LGN_RSL<<->>message_id<<&>>hmac<<&>>message_time<<&>>login_result
        DISCONNECT("DISC", 0) // server <-> client             DISC<<->>message_id<<&>>hmac<<&>>message_time
        ;

        private String dataID;
        private int arguments;

        MessageIdentifier(String dataID,
                          int arguments)
        {
            this.dataID = dataID;
            this.arguments = arguments;
        }

        public String getDataID()
        {
            return this.dataID;
        }

        public int getArguments()
        {
            return this.arguments;
        }
    }

    public enum Modes
    {
        TC_SERVER("tcs",
                "nl.hetbaarnschlyceum.pws.server.tc.TCServer",
                "TC Server"),
        CM_SERVER("cms",
                "nl.hetbaarnschlyceum.pws.server.cm.CMServer",
                "CM Server"),
        CLIENT("cl",
                "nl.hetbaarnschlyceum.pws.client.Client",
                "Client"),
        KEYGEN("keygen",
                "nl.hetbaarnschlyceum.pws.crypto.ECDSA",
                "KeyGen"),
        CRYPTOTEST("null",
                "null",
                "CryptoTest");

        private String argName;
        private String fqName;
        private String prefix;

        Modes(String argName, String fqName, String prefix)
        {
            this.argName = argName;
            this.fqName = fqName;
            this.prefix = prefix;
        }

        public String getArgName()
        {
            return this.argName;
        }

        public String getFqName()
        {
            return this.fqName;
        }

        public String getPrefix()
        {
            return this.prefix;
        }
    }

    public static void main(String[] args)
    {
        URL url = PWS.class.getProtectionDomain().getCodeSource().getLocation();
        String jarPath = null;
        try {
            jarPath = URLDecoder.decode(url.getFile(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String path = new File(jarPath).getParentFile().getPath();

        Options options = new Options();

        Option programMode = new Option("m", "mode", true, "Mode van het programma");
        programMode.setRequired(true);
        programMode.setArgName("tcs/cms/cl");
        programMode.setArgs(1);
        options.addOption(programMode);

        Option sqlHost = new Option("sqlH",
                "sqlHost",
                true,
                "Hostname van de SQL server (Standaard: localhost)");
        sqlHost.setArgs(1);
        options.addOption(sqlHost);

        Option sqlPort = new Option("sqlP",
                "sqlPort",
                true,
                "Port van de SQL server (Standaard: 3306)");
        sqlPort.setArgs(1);
        options.addOption(sqlPort);

        Option sqlUser = new Option("sqlU",
                "sqlUser",
                true,
                "Gebruiker van de SQL server (Standaard: root)");
        sqlUser.setArgs(1);
        options.addOption(sqlUser);

        Option sqlPass = new Option("sqlPS",
                "sqlPass",
                true,
                "Wachtwoord van de SQL server");
        sqlPass.setArgs(1);
        options.addOption(sqlPass);

        Option serverPort = new Option("sP",
                "serverPort",
                true,
                "Port van de TC server (Standaard: 9348)");
        serverPort.setArgs(1);
        options.addOption(serverPort);

        Option serverIP = new Option("sIP",
                "serverIP",
                true,
                "IP van de TC server (Standaard: 95.85.53.98)");
        serverIP.setArgs(1);
        options.addOption(serverIP);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try
        {
            cmd = parser.parse(options, args);
        } catch (ParseException e)
        {
            System.out.println(e.getMessage());
            formatter.printHelp("pws-core", options);

            System.exit(1);
            return;
        }

        String programModeString = cmd.getOptionValue("mode");

        // Kijken of het een cryptotest is
        if (programModeString.equals("cryptotest"))
        {
            currentMode = Modes.CRYPTOTEST;

            preStartInit(currentMode.getPrefix(), path);
            KeyManagement.runTests();
            return;
        }

        for (Modes mode : Modes.values())
        {
            if (mode.getArgName().equalsIgnoreCase(programModeString))
            {
                String host = "localhost",
                        port = "3306",
                        user = "root",
                        pass = null,
                        tcport = "9348",
                        serverAdress = "95.85.53.98";
                System.out.printf("Programma gestart in modus: %s\n", mode.name());

                if (mode == Modes.TC_SERVER)
                {
                    if (cmd.hasOption("sqlPass"))
                    {
                        host = cmd.hasOption("sqlHost") ? cmd.getOptionValue("sqlHost") : "localhost";
                        port = cmd.hasOption("sqlPort") ? cmd.getOptionValue("sqlPort") : "3306";
                        user = cmd.hasOption("sqlUser") ? cmd.getOptionValue("sqlUser") : "root";
                        pass = cmd.getOptionValue("sqlPass");

                        if (cmd.hasOption("serverPort"))
                        {
                            tcport = cmd.getOptionValue("serverPort");
                        }

                        if (!(StringUtils.isNumeric(tcport) && StringUtils.isNumeric(port)))
                        {
                            System.out.println("'serverPort' en 'sqlPort' moeten numerieke waarden zijn!");
                            System.exit(-1);
                        }
                    }
                    else
                    {
                        System.out.printf("Programma is gestart in modus TC_SERVER, " +
                                "maar er zijn geen SQL gegevens ingevoerd. \n" +
                                "Zie de helppagina hieronder voor meer informatie:\n");
                        formatter.printHelp("pws-tcs", options);
                        System.exit(-1);
                    }
                }

                if (mode == Modes.CLIENT)
                {
                    if (cmd.hasOption("serverIP"))
                    {
                        serverAdress = cmd.getOptionValue("serverIP");
                    }

                    if (cmd.hasOption("serverPort"))
                    {
                        String temPort = cmd.getOptionValue("serverPort");

                        if (StringUtils.isNumeric(temPort))
                        {
                            tcport = temPort;
                        } else
                        {
                            System.out.printf("'serverPort' heeft geen numerieke waarde gekregen, " +
                                    "standaard waarde wordt gebruikt: 9348");
                        }
                    }
                }

                currentMode = mode;

                // Hier kunnen nog dingen gebeuren voor startup
                preStartInit(mode.getPrefix(), path);

                try {
                    Class<?> mClass = Class.forName(mode.getFqName());
                    Constructor<?> mConstructor;

                    if (mode == Modes.TC_SERVER)
                    {
                        mConstructor = mClass.getConstructor(String.class,
                                String.class,
                                String.class,
                                String.class,
                                String.class);
                        mConstructor.newInstance(host, port ,user, pass, tcport);
                    }
                    else if (mode == Modes.KEYGEN)
                    {
                        try {
                            ECDSA.generateKeyPair(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else if (mode == Modes.CLIENT)
                    {
                        mConstructor = mClass.getConstructor(String.class,
                                String.class);
                        mConstructor.newInstance(serverAdress, tcport);
                    }
                    else
                    {
                        mConstructor = mClass.getConstructor();
                        mConstructor.newInstance();
                    }
                } catch (ClassNotFoundException
                        | NoSuchMethodException
                        | InstantiationException
                        | IllegalAccessException
                        | InvocationTargetException e) {
                    e.printStackTrace();
                }
                return;
            }
        }

        // Op het moment dat JVM tot hier komt is er dus geen matchende mode gevonden
        // Gebruiker heeft dus niet tcs, cms of cl gekozen als optie
        System.out.printf("%s is geen geldige modus. Kiez uit tcs, cms of cl.\n", programModeString);
        formatter.printHelp("pws-core", options);
        System.exit(1);
    }

    private static void preStartInit(String prefix, String path)
    {
        Security.addProvider(new BouncyCastleProvider());
        KeyManagement.init(path);

        // Controleren of BouncyCastle geladen is
        if (Security.getProvider("BC") == null)
        {
            System.out.printf("[%s][FOUT] De BouncyCastle security provider kon niet worden geladen!\n",
                    prefix);
            System.exit(-1);
        } else
        {
            System.out.printf("[%s][INFO] De BouncyCastle security provider is geladen\n",
                    prefix);
        }

        // EC sleutel laden voor ECDSA
    }
}
