package nl.hetbaarnschlyceum.pws.crypto;

import static nl.hetbaarnschlyceum.pws.server.tc.TCServer.print;

public class KeyManagement {
    private static String path;

    public static void init(String currentPath)
    {
        //TODO: Path nog even aanpassen
        path = currentPath;

        print("[INFO] KeyManager geladen in folder %s", path);
    }
}
