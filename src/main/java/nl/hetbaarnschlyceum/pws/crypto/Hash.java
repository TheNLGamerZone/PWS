package nl.hetbaarnschlyceum.pws.crypto;

import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.DigestInputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
    public static String generateHMAC(String message, String key)
    {
        try {
            final Charset charSet = Charset.forName("US-ASCII");
            final Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            final SecretKeySpec secret_key = new javax.crypto.spec.SecretKeySpec(charSet.encode(key).array(),
                    "HmacSHA256");

            hmacSHA256.init(secret_key);

            final byte[] mac_data = hmacSHA256.doFinal(charSet.encode(message).array());
            StringBuilder result = new StringBuilder();

            for (final byte element : mac_data)
            {
                result.append(Integer.toString((element & 0xff) + 0x100, 16).substring(1));
            }

            return result.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static String generateHash(String message)
    {
        return DigestUtils.sha256Hex(message);
    }

    public static String getChecksum(InputStream inputStream)
    {
        try
        {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            DigestInputStream digestInputStream = new DigestInputStream(inputStream, messageDigest);
            byte[] buffer = new byte[2048];

            while (digestInputStream.read(buffer) != -1) {}

            digestInputStream.close();

            byte[] digest = messageDigest.digest();
            StringBuilder stringBuilder = new StringBuilder();

            for (byte aDigest : digest)
            {
                stringBuilder.append(String.format("%x", aDigest));
            }

            return stringBuilder.toString();
        } catch (NoSuchAlgorithmException
                | IOException e)
        {
            e.printStackTrace();
        } finally {
            try
            {
                inputStream.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }
}
