package crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static app.Configuration.*;

public class AES {

    public static EncryptedMessage encrypt(final byte[] dataToEncrypt, final char[] password)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        /* Generate random IV and random salt. */
        SecureRandom rng = new SecureRandom();
        byte[] salt = new byte[32]; /* Can be any size. */
        byte[] ivBytes = new byte[12]; /* Recommended size for GCM. */
        rng.nextBytes(salt);
        rng.nextBytes(ivBytes);

        /* Before we can encrypt we need to generate an AES key from the master password. */
        SecretKeySpec AESkey = PBKDF2.generateAESkey(password, salt, ENCRYPTION_ITERATIONS, ENCRYPTION_KEY_LENGTH_BYTES);

        /* Encrypt. */
        Cipher aes = Cipher.getInstance(ENCRYPTION_CIPHER);
        GCMParameterSpec iv = new GCMParameterSpec(ENCRYPTION_GCM_TAG_SIZE, ivBytes);
        aes.init(Cipher.ENCRYPT_MODE, AESkey, iv);
        byte[] encryptedData = aes.doFinal(dataToEncrypt);

        /* In order to decrypt later, we need to save iv, salt, iterations and key length. */
        return new EncryptedMessage(encryptedData, salt, ivBytes, ENCRYPTION_ITERATIONS, ENCRYPTION_KEY_LENGTH_BYTES);
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
        long b = System.nanoTime();
        Cipher aes = Cipher.getInstance(ENCRYPTION_CIPHER);
        GCMParameterSpec iv = new GCMParameterSpec(ENCRYPTION_GCM_TAG_SIZE, ivBytes);
        aes.init(Cipher.DECRYPT_MODE, AESkey, iv);
        byte[] decryptedData = aes.doFinal(cipherText);
        return decryptedData;
    }

}
