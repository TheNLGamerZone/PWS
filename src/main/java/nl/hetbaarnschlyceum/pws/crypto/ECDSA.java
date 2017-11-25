package nl.hetbaarnschlyceum.pws.crypto;

import nl.hetbaarnschlyceum.pws.PWS;

import java.io.*;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import static nl.hetbaarnschlyceum.pws.PWS.print;
import static nl.hetbaarnschlyceum.pws.crypto.KeyManagement.bytesToHex;
import static nl.hetbaarnschlyceum.pws.crypto.KeyManagement.hexToBytes;

public class ECDSA {
    /*
    Deze functie maakt de sleutels aan voor de TC server
     */
    public static void generateKeyPair(String path)
            throws IOException
    {
        String fileSeparator = System.getProperty("file.separator");
        String dirPath = path + fileSeparator + "keys" + fileSeparator;

        print("[INFO] Sleutels worden aangemaakt in %s", dirPath);

        File file = new File(dirPath);
        file.mkdir();

        try
        {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");

            keyPairGenerator.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());

            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PrivateKey privateKey = keyPair.getPrivate();
            PublicKey publicKey = keyPair.getPublic();

            print("[INFO] Sleutels zijn gemaakt:");
            print("[INFO]   Prive sleutel: %s", bytesToHex(privateKey.getEncoded()));
            print("[INFO]   Publieke sleutel: %s", bytesToHex(publicKey.getEncoded()));
            print("[INFO] Sleutels worden opgeslagen..");

            // PrivateKey opslaan
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKey.getEncoded());
            FileOutputStream fileOutputStream = new FileOutputStream(dirPath + "private.key");

            fileOutputStream.write(pkcs8EncodedKeySpec.getEncoded());
            fileOutputStream.close();

            // PublicKey opslaan
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey.getEncoded());
            fileOutputStream = new FileOutputStream(dirPath + "public.key");

            fileOutputStream.write(x509EncodedKeySpec.getEncoded());
            fileOutputStream.close();

            print("[INFO] Sleutels zijn opgeslagen in %s", dirPath);
        } catch (NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException e)
        {
            print("[FOUT] Sleutels konden niet gemaakt worden: %s",
                    e.getMessage());
            e.printStackTrace();
        }
    }

    public static String createSignature(String message)
            throws NoSuchProviderException,
            NoSuchAlgorithmException,
            InvalidKeyException,
            UnsupportedEncodingException,
            SignatureException,
            NoKeyPairLoadedException
    {
        if (PWS.currentMode == PWS.Modes.TC_SERVER)
        {
            // Gebruik TC sleutel
            KeyPair keyPair = PWS.keyPair;

            if (keyPair != null)
            {
                Signature signature = Signature.getInstance("SHA256withECDSA", "BC");

                signature.initSign(keyPair.getPrivate());
                signature.update(message.getBytes("UTF-8"));

                return bytesToHex(signature.sign());
            } else
            {
                throw new NoKeyPairLoadedException("TC_SIGNATURE_SIGN");
            }
        } else
        {
            print("[FOUT] Programma probeert een bericht te ondertekenen, maar heeft geen sleutel");
        }

        return null;
    }

    public static boolean verifySignature(String message, String signatureData)
            throws NoSuchProviderException,
            NoSuchAlgorithmException,
            NoKeyPairLoadedException,
            InvalidKeyException,
            UnsupportedEncodingException,
            SignatureException
    {
        KeyPair keyPair = PWS.keyPair;

        if (keyPair != null)
        {
            Signature signature = Signature.getInstance("SHA256withECDSA", "BC");

            signature.initVerify(keyPair.getPublic());
            signature.update(message.getBytes("UTF-8"));

            return signature.verify(hexToBytes(signatureData));
        } else
        {
            throw new NoKeyPairLoadedException("TC_SIGNATURE_VERIFY");
        }
    }
}
