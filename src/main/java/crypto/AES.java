package crypto;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class AES {

    /** Defines cipher instance. Message-padding not needed in GCM mode. */
    static final String cipherInstance = "AES/GCM/NoPadding";

    /** Iterations for generateKey when generating encryption key from master password.
     *  Needs to be high, because user chosen passwords may be of low quality. */
    static final int ITERATIONS = 10000;

    /** GCM mode requires key size to be either 128, 196 or 256 bits. */
    static final int KEY_LENGTH_BYTES = 32;

    /** In our use case, GCM Security tag size is related to the probability
     *  that we detect . 128 is the max size. */
    static final int GCM_TAG_SIZE = 128;

    public static EncryptedMessage encrypt(final byte[] dataToEncrypt, final char[] password)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        /* Generate random IV and random salt. */
        SecureRandom rng = new SecureRandom();
        byte[] salt = new byte[32]; /* Can be any size. */
        byte[] ivBytes = new byte[12]; /* Recommended size for GCM. */
        rng.nextBytes(salt);
        rng.nextBytes(ivBytes);

        /* In order to encrypt we need to turn password into 256 random-looking bits. */
        SecretKeySpec AESkey = PBKDF2.generateAESkey(password, salt, ITERATIONS, KEY_LENGTH_BYTES);

        /* Encrypt. */
        Cipher aes = Cipher.getInstance(cipherInstance);
        GCMParameterSpec iv = new GCMParameterSpec(GCM_TAG_SIZE, ivBytes);
        aes.init(Cipher.ENCRYPT_MODE, AESkey, iv);
        byte[] encryptedData = aes.doFinal(dataToEncrypt);

        /* In order to decrypt later, we need to save iv, salt, iterations and key length. */
        return new EncryptedMessage(encryptedData, salt, ivBytes, ITERATIONS, KEY_LENGTH_BYTES);
    }

    public static byte[] decrypt(final EncryptedMessage encryptedMessage, final char[] password) throws Exception {
        /* Set convenience variables. */
        byte[] cipherText = encryptedMessage.getCipherText();
        byte[] salt = encryptedMessage.getSalt();
        byte[] ivBytes = encryptedMessage.getIv();
        int iterations = encryptedMessage.getIterationsPBKDF2();
        int keyLength = encryptedMessage.getKeyLengthPBKDF2();

        /* Decrypt. */
        SecretKeySpec AESkey = PBKDF2.generateAESkey(password, salt, iterations, keyLength);
        Cipher aes = Cipher.getInstance(cipherInstance);
        GCMParameterSpec iv = new GCMParameterSpec(GCM_TAG_SIZE, ivBytes);
        aes.init(Cipher.DECRYPT_MODE, AESkey, iv);
        byte[] decryptedData = aes.doFinal(cipherText);
        return decryptedData;
    }

}
