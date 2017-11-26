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
    public static KeyPair generateKeyPair()
    {
        try
        {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDH", "BC");

            keyPairGenerator.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());

            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    protected static byte[] savePublicKey(PublicKey publicKey)
    {
        return ((ECPublicKey) publicKey).getQ().getEncoded();
    }

    protected static byte[] savePrivateKey(PrivateKey privateKey)
    {
        return ((ECPrivateKey) privateKey).getD().toByteArray();
    }

    private static PublicKey loadPublicKey(byte[] publicData)
    {
        try {
            ECParameterSpec ecParameterSpec = ECNamedCurveTable.getParameterSpec("secp256r1");
            ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(ecParameterSpec.getCurve().decodePoint(publicData),
                    ecParameterSpec);
            KeyFactory keyFactory = KeyFactory.getInstance("ECDH", "BC");

            return keyFactory.generatePublic(publicKeySpec);
        } catch (InvalidKeySpecException |
                NoSuchProviderException |
                NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    private static PrivateKey loadPrivateKey (byte [] privateData)
    {
        try {
            ECParameterSpec ecParameterSpec = ECNamedCurveTable.getParameterSpec("secp256r1");
            ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(new BigInteger(privateData),
                    ecParameterSpec);
            KeyFactory keyFactory = KeyFactory.getInstance("ECDH", "BC");

            return keyFactory.generatePrivate(privateKeySpec);
        } catch (InvalidKeySpecException |
                NoSuchProviderException |
                NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    private static String calculateSecret (byte[] privateData, byte[] publicData)
    {
        try {
            KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH", "BC");

            keyAgreement.init(loadPrivateKey(privateData));
            keyAgreement.doPhase(loadPublicKey(publicData), true);

            return bytesToHex(keyAgreement.generateSecret());
        } catch (NoSuchProviderException |
                NoSuchAlgorithmException |
                InvalidKeyException e)
        {
            e.printStackTrace();
        }

        return null;
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

        return calculateSecret(privateBytes, publicBytes);
    }
}
