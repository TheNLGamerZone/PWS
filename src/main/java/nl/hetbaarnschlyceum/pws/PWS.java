package nl.hetbaarnschlyceum.pws;

import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class PWS {
    // Globale instellingen
    public static final int corePoolThreads = 5;

    public enum Modes
    {
        TC_SERVER("tcs", "nl.hetbaarnschlyceum.pws.server.tc.TCServer"),
        CM_SERVER("cms", "nl.hetbaarnschlyceum.pws.server.cm.CMServer"),
        CLIENT("cl", "nl.hetbaarnschlyceum.pws.client.Client");

        private String argName;
        private String fqName;

        Modes(String argName, String fqName)
        {
            this.argName = argName;
            this.fqName = fqName;
        }

        public String getArgName()
        {
            return this.argName;
        }

        public String getFqName()
        {
            return this.fqName;
        }
    }

    public static void main(String[] args)
    {
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

        for (Modes mode : Modes.values())
        {
            if (mode.getArgName().equalsIgnoreCase(programModeString))
            {
                String host = "localhost", port = "3306", user = "root", pass = null, tcport = "9348";
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
}
