package crypto;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class AES {

    static final String cipherInstance = "AES/GCM/NoPadding";

    /** Iterations for PBKDF2 when generating encryption key from master password.
     *  Needs to be high, because user chosen passwords may be of low quality. */
    static final int ITERATIONS = 10000;
    static final int KEY_LENGTH_BYTES = 16;

    public static EncryptedMessage encrypt(final byte[] dataToEncrypt, final char[] password) throws Exception {
        /* Generate random IV and random salt. */
        SecureRandom rng = new SecureRandom();
        byte[] salt = new byte[32]; /* Can be any size. */
        byte[] ivBytes = new byte[16]; /* Must be AES block size, 128 bits. */
        rng.nextBytes(salt);
        rng.nextBytes(ivBytes);

        /* In order to encrypt we need to turn password into 256 random-looking bits. */
        SecretKeySpec AESkey = generateAESkey(password, salt);

        /* Encryption. */
        Cipher aes = Cipher.getInstance(cipherInstance);
        GCMParameterSpec iv = new GCMParameterSpec(16 * Byte.SIZE, ivBytes);
        aes.init(Cipher.ENCRYPT_MODE, AESkey, iv);
        byte[] encryptedData = aes.doFinal(dataToEncrypt);

        /* In order to decrypt later, we need to save iv, salt, iterations and key length. */
        return new EncryptedMessage(encryptedData, salt, ivBytes, ITERATIONS, KEY_LENGTH_BYTES);
    }

    public static byte[] decrypt(final EncryptedMessage encryptedMessage, final char[] password) throws Exception {
        byte[] cipherText = encryptedMessage.getCipherText();
        byte[] salt = encryptedMessage.getSalt();
        byte[] ivBytes = encryptedMessage.getIv();
        int iterations = encryptedMessage.getIterationsPBKDF2();
        int keyLength = encryptedMessage.getKeyLengthPBKDF2();
        SecretKeySpec AESkey = generateAESkey(password, salt);
        Cipher aes = Cipher.getInstance(cipherInstance);
        GCMParameterSpec iv = new GCMParameterSpec(16 * Byte.SIZE, ivBytes);
        aes.init(Cipher.DECRYPT_MODE, AESkey, iv);
        byte[] decryptedData = aes.doFinal(cipherText);
        return decryptedData;
    }

    private static SecretKeySpec generateAESkey(final char[] password, final byte[] salt) throws Exception {
        SecretKey secretKey = PBKDF2.PBKDF2(password, salt, ITERATIONS, KEY_LENGTH_BYTES);
        /* AES expects the key in a "AES" KeySpec object. */
        return new SecretKeySpec(secretKey.getEncoded(), "AES");
    }



}
