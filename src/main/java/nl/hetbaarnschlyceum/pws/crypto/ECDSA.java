package nl.hetbaarnschlyceum.pws.crypto;

import nl.hetbaarnschlyceum.pws.PWS;

import static nl.hetbaarnschlyceum.pws.server.tc.TCServer.print;

public class ECDSA {
    final private static char[] hexArray = "0123456789abcdef".toCharArray();

    public static byte[] createSignature(String message)
    {
        if (PWS.currentMode == PWS.Modes.TC_SERVER)
        {
            // Gebruik TC sleutel

        } else
        {
            print("[FOUT] Programma probeert een bericht te ondertekenen, maar heeft geen sleutel");
        }

        return null;
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
