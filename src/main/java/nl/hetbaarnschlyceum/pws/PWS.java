package nl.hetbaarnschlyceum.pws;

import org.apache.commons.cli.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class PWS {
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
                System.out.printf("Programma gestart in modus: %s\n", mode.name());

                try {
                    Class<?> mClass = Class.forName(mode.getFqName());
                    Constructor<?> mConstructor = mClass.getConstructor();
                    mConstructor.newInstance();
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
