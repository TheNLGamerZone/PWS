package nl.hetbaarnschlyceum.pws.crypto;

import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.ECGenParameterSpec;
import javax.crypto.KeyAgreement;

import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;

import static nl.hetbaarnschlyceum.pws.crypto.KeyManagement.bytesToHex;
import static nl.hetbaarnschlyceum.pws.crypto.KeyManagement.hexToBytes;

public class ECDH {
    final private static char[] hexArray = "0123456789abcdef".toCharArray();

    public static KeyPair generateKeyPair()
    {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDH", "BC");

            keyPairGenerator.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());

            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static byte[] savePublicKey(PublicKey publicKey)
    {
        return ((ECPublicKey) publicKey).getQ().getEncoded();
    }

    private static byte[] savePrivateKey(PrivateKey privateKey)
    {
        return ((ECPrivateKey) privateKey).getD().toByteArray();
    }

    private static PublicKey loadPublicKey(byte[] publicData)
            throws InvalidKeySpecException,
            NoSuchProviderException,
            NoSuchAlgorithmException
    {
        ECParameterSpec ecParameterSpec = ECNamedCurveTable.getParameterSpec("secp256r1");
        ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(ecParameterSpec.getCurve().decodePoint(publicData),
                ecParameterSpec);
        KeyFactory keyFactory = KeyFactory.getInstance("ECDH", "BC");

        return keyFactory.generatePublic(publicKeySpec);
    }

    private static PrivateKey loadPrivateKey (byte [] privateData)
            throws InvalidKeySpecException,
            NoSuchProviderException,
            NoSuchAlgorithmException
    {
        ECParameterSpec ecParameterSpec = ECNamedCurveTable.getParameterSpec("secp256r1");
        ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(new BigInteger(privateData),
                ecParameterSpec);
        KeyFactory keyFactory = KeyFactory.getInstance("ECDH", "BC");

        return keyFactory.generatePrivate(privateKeySpec);
    }

    private static String calculateSecret (byte[] privateData, byte[] publicData)
            throws NoSuchProviderException,
            NoSuchAlgorithmException,
            InvalidKeySpecException,
            InvalidKeyException
    {
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH", "BC");

        keyAgreement.init(loadPrivateKey(privateData));
        keyAgreement.doPhase(loadPublicKey(publicData), true);

        return bytesToHex(keyAgreement.generateSecret());
    }

    /**
     * Bij de server is keypair: client.getDHKeys();
     */
    public static String getPublicData(KeyPair keyPair)
    {
        return bytesToHex(savePublicKey(keyPair.getPublic()));
    }

    /**
     * Bij de server is keypair: client.getDHKeys();
     */
    public static String getSecret(KeyPair keyPair, String publicData)
    {
        byte[] publicBytes = hexToBytes(publicData);
        byte[] privateBytes = savePrivateKey(keyPair.getPrivate());
        String result = "-1";

        try
        {
            result = calculateSecret(privateBytes, publicBytes);
        } catch (NoSuchAlgorithmException |
                NoSuchProviderException |
                InvalidKeySpecException |
                InvalidKeyException e)
        {
            e.printStackTrace();
        }

        return result;
    }
}
