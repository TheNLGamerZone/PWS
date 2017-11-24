package nl.hetbaarnschlyceum.pws.crypto;

import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Hash {
    public static String generateHMAC(String message, String key)
    {
        try {
            final Charset asciiCs = Charset.forName("US-ASCII");
            final Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            final SecretKeySpec secret_key = new javax.crypto.spec.SecretKeySpec(asciiCs.encode(key).array(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            final byte[] mac_data = sha256_HMAC.doFinal(asciiCs.encode(message).array());
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
}
