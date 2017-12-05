package nl.hetbaarnschlyceum.pws.server.tc;

public class OperationResult {
    public static final int SUCCESS = 8;
    public static final int SUCCESS_LOGGED_IN = 7;
    public static final int UNIQUE_USER = 6;
    public static final int FAILED_DUPLICATE_NAME = 5;
    public static final int FAILED_DUPLICATE_NUMBER = 4;
    public static final int FAILED_NAMECHECK = 3;
    public static final int FAILED_INCORRECT_HASH = 2;
    public static final int FAILED_UNKNOWN_USER = 1;
    public static final int FAILED_SYS_ERROR = 0;

    public static final String[] errorMessages = new String[] {
            "Er is een systeemfout opgetreden", // FAILED_SYS_ERROR
            "Onbekende gebruikersnaam/Foutief wachtwoord", // FAILED_UNKNOWN_USER -> Voor veiligheid zelfde als fout wachtwoord
            "Onbekende gebruikersnaam/Foutief wachtwoord", // FAILED_INCORRECT_HASH -> Voor veiligheid zelfde als foute naam
            "Gebruikersnaam voldoet niet aan de eisen", // FAILED_NAMECHECK
            "Nummer bestaat al", // FAILED_DUPLICATE_NUMBER
            "Gebruikersnaam bestaat al", // FAILED_DUPLICATE_NAME
    };
}
