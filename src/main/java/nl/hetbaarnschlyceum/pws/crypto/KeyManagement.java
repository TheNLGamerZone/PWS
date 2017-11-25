package nl.hetbaarnschlyceum.pws.crypto;

import nl.hetbaarnschlyceum.pws.PWS;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import static nl.hetbaarnschlyceum.pws.PWS.print;

public class KeyManagement {
    final private static char[] hexArray = "0123456789abcdef".toCharArray();

    private static String dirPath;

    public static void init(String currentPath)
    {
        if (PWS.currentMode != PWS.Modes.KEYGEN) {
            String fileSeparator = System.getProperty("file.separator");
            dirPath = currentPath + fileSeparator + "keys" + fileSeparator;

            print("[INFO] KeyManager geladen in folder %s", dirPath);
            try {
                PWS.keyPair = loadKeys();
            } catch (IOException
                    | NoSuchProviderException
                    | NoSuchAlgorithmException
                    | InvalidKeySpecException e)
            {
                e.printStackTrace();
            }
        }
    }

    private static KeyPair loadKeys()
            throws IOException,
            NoSuchProviderException,
            NoSuchAlgorithmException,
            InvalidKeySpecException
    {
        // Als de modus TC server is wordt hier de PrivateKey gelezen
        byte[] privateKeyData = PWS.currentMode == PWS.Modes.TC_SERVER ? readPrivateKey() : null;

        // PublicKey lezen
        File publicKeyFile = new File(dirPath + "public.key");
        FileInputStream fileInputStream = new FileInputStream(dirPath + "public.key");
        byte[] publicKeyData = new byte[(int) publicKeyFile.length()];

        fileInputStream.read(publicKeyData);
        fileInputStream.close();

        // KeyPair maken van de data
        KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKeyData);
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);

        PrivateKey privateKey = null;
        if (privateKeyData != null)
        {
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKeyData);
            privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        }

        print("[INFO] Sleutels zijn geladen:");
        if (privateKey != null) print("[INFO]   Prive sleutel: %s", bytesToHex(privateKey.getEncoded()));
        print("[INFO]   Publieke sleutel: %s", bytesToHex(publicKey.getEncoded()));

        return new KeyPair(publicKey, privateKey);
    }

    private static byte[] readPrivateKey()
            throws IOException
    {
        File privateKeyFile = new File(dirPath + "private.key");
        FileInputStream fileInputStream = new FileInputStream(dirPath + "private.key");
        byte[] privateKeyData = new byte[(int) privateKeyFile.length()];

        fileInputStream.read(privateKeyData);
        fileInputStream.close();

        return privateKeyData;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexToBytes(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
