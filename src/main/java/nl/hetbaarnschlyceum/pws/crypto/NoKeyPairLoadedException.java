package nl.hetbaarnschlyceum.pws.crypto;

public class NoKeyPairLoadedException extends Exception {
    public NoKeyPairLoadedException(String key)
    {
        super("De volgende sleutel is nog niet geladen, maar is wel nodig: " + key);
    }
}
