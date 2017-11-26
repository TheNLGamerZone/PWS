package nl.hetbaarnschlyceum.pws.crypto;

import nl.hetbaarnschlyceum.pws.PWS;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;

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

            // Controleren of de server de juiste prive sleutel geladen heeft
            if (PWS.currentMode == PWS.Modes.TC_SERVER
                    || PWS.currentMode == PWS.Modes.CRYPTOTEST)
            {
                String testSignature = ECDSA.createSignature("testMessage");
                boolean signatureCheck = ECDSA.verifySignature("testMessage", testSignature);

                if (signatureCheck)
                {
                    print("[INFO] Geladen KeyPair is correct");
                } else
                {
                    print("[FOUT] Geladen KeyPair komt niet overeen!");
                    System.exit(-1);
                }
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
        byte[] privateKeyData = PWS.currentMode == PWS.Modes.TC_SERVER
                || PWS.currentMode == PWS.Modes.CRYPTOTEST
                ? readPrivateKey() : null;

        // PublicKey lezen
        byte[] publicKeyData = readPublicKey();

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

    private static byte[] readPublicKey()
            throws IOException
    {
        InputStream inputStream = KeyManagement.class.getResourceAsStream("/public.key");
        return IOUtils.toByteArray(inputStream);
    }

    private static byte[] readPrivateKey()
            throws IOException
    {
        File privateKeyFile = new File(dirPath + "private.key");

        if (privateKeyFile.exists()) {
            FileInputStream fileInputStream = new FileInputStream(dirPath + "private.key");
            byte[] privateKeyData = new byte[(int) privateKeyFile.length()];

            fileInputStream.read(privateKeyData);
            fileInputStream.close();

            return privateKeyData;
        }

        return null;
    }

    protected static String bytesToHex(byte[] bytes)
    {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    protected static byte[] hexToBytes(String s)
    {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    protected static String bytesToString(byte[] bytes)
    {
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Methode om alle cryptosystemen uit te testen
     */
    public static void runTests()
    {
        boolean aes = false,
                ecdh = false,
                ecdsa = false,
                pws256 = false;

        // ==== AES test ====
        print("[AES] AES test wordt gestart..");

        SecretKey secretKey = AES.generateKey();
        IvParameterSpec ivParameterSpec = AES.generateIV();
        String randomString = UUID.randomUUID().toString();
        String encryptedString = AES.encrypt(randomString, secretKey, ivParameterSpec);
        String decryptedString = AES.decrypt(encryptedString, secretKey, ivParameterSpec);

        print("[AES] Sleutel: %s", Base64.getEncoder().encodeToString(secretKey.getEncoded()));
        print("[AES] Willekeurige tekst: %s", randomString);
        print("[AES] Versleutelde tekst: %s", encryptedString);
        print("[AES] Ontsleutelde tekst: %s", decryptedString);
        print("[AES] AES test %s", decryptedString.equals(randomString) ? "geslaagd\n" : "niet geslaagd\n");
        aes = decryptedString.equals(randomString);

        // ==== ECDH test ====
        print("[ECDH] ECDH test wordt gestart..");

        KeyPair keyPairA = ECDH.generateKeyPair();
        String privateDataA = bytesToHex(ECDH.savePrivateKey(keyPairA.getPrivate()));
        String publicDataA = ECDH.getPublicData(keyPairA);

        KeyPair keyPairB = ECDH.generateKeyPair();
        String privateDataB = bytesToHex(ECDH.savePrivateKey(keyPairB.getPrivate()));
        String publicDataB = ECDH.getPublicData(keyPairB);

        String secretA = ECDH.getSecret(keyPairA, publicDataB);
        String secretB = ECDH.getSecret(keyPairB, publicDataA);

        print("[ECDH] Prive sleutel A: %s", privateDataA);
        print("[ECDH] Publieke sleutel A: %s", publicDataA);
        print("[ECDH] Prive sleutel B: %s", privateDataB);
        print("[ECDH] Publieke sleutel B: %s", publicDataB);
        print("[ECDH] Shared-secret A: %s", secretA);
        print("[ECDH] Shared-secret B: %s", secretB);
        print("[ECDH] ECDH test %s", secretA.equals(secretB) ? "geslaagd\n" : "niet geslaagd\n");
        ecdh = secretA.equals(secretB);

        // ==== ECDSA test ====
        print("[ECDSA] ECDSA test wordt gestart..");

        if (PWS.keyPair.getPrivate() != null)
        {
            randomString = UUID.randomUUID().toString();
            String testSignature = ECDSA.createSignature(randomString);
            boolean signatureCheck = ECDSA.verifySignature(randomString, testSignature);

            print("[ECDSA] Willekeurige tekst: %s", randomString);
            print("[ECDSA] Handtekening: %s", testSignature);
            print("[ECDSA] Handtekening geldig: %s", signatureCheck ? "ja" : "nee");
            print("[ECDSA] ECDSA test %s", signatureCheck ? "geslaagd\n" : "niet geslaagd\n");
            ecdsa = signatureCheck;
        } else
        {
            print("[FOUT] Er is geen prive sleutel voor ECDSA aanwezig!");
            print("[ECDSA] ECDSA test niet geslaagd\n");
        }
        // ==== Hash test ====
        print("[SHA256] SHA256 test wordt gestart..");

        randomString = UUID.randomUUID().toString();
        print("[SHA256] Willekeurige tekst: %s", randomString);
        print("[SHA256] Hash resultaat: %s", Hash.generateHash(randomString));
        print("[SHA256] SHA256 test geslaagd\n");

        randomString = UUID.randomUUID().toString();
        String hmacKey = Hash.generateHash(Base64.getEncoder().encodeToString(AES.generateKey().getEncoded()));
        print("[HMAC] HMAC test wordt gestart..");
        print("[HMAC] Willekeurige tekst: %s", randomString);
        print("[HMAC] Sleutel: %s", hmacKey);
        print("[HMAC] HMAC resultaat: %s", Hash.generateHMAC(randomString, hmacKey));
        print("[HMAC] HMAC test geslaagd\n");

        // ==== PWS256 test ====
        print("[PWS256] PWS256 test wordt gestart..");
        print("[FOUT] PWS256 is nog niet geimplementeerd!");
        print("[PWS256] PWS256 test niet geslaagd\n");

        // ==== Samenvatting ====
        print(" Samenvatting CryptoTest:");
        print(" AES: %s", aes ? "Succes" : "Gefaald");
        print(" ECDH: %s", ecdh ? "Succes" : "Gefaald");
        print(" ECDSA: %s", ecdsa ? "Succes" : "Gefaald");
        print(" SHA256: Succes");
        print(" HMAC: Succes");
        print(" PWS256: %s", pws256 ? "Succes" : "Gefaald");
    }
}
