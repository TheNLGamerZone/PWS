package nl.hetbaarnschlyceum.pws.crypto;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.util.Base64;

public class AES {
    public static String encrypt(String message,
                                 SecretKey secretKey,
                                 IvParameterSpec ivParameterSpec)
    {
        try {
            byte[] messageBytes = message.getBytes("UTF-8");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING", "BC");

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

            return KeyManagement.bytesToString(Base64.getEncoder().encode(cipher.doFinal(messageBytes)));
        } catch (GeneralSecurityException |
                UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static String decrypt(String ciphertext,
                                 SecretKey secretKey,
                                 IvParameterSpec ivParameterSpec)
    {
        try {
            byte[] cipherBytes = ciphertext.getBytes("UTF-8");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING", "BC");

            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(cipherBytes)), "UTF-8");
        } catch (GeneralSecurityException
                | UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static IvParameterSpec generateIV()
    {
        SecureRandom secureRandom = new SecureRandom();
        byte[] bytesIV = new byte[16];

        secureRandom.nextBytes(bytesIV);
        return new IvParameterSpec(bytesIV);
    }

    public static SecretKey generateKey()
    {
        try
        {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", "BC");

            keyGenerator.init(256);

            return keyGenerator.generateKey();
        } catch (GeneralSecurityException e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
