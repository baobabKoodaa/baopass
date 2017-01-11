package crypto;

import java.io.Serializable;

public class EncryptedMessage implements Serializable {

    private byte[] cipherText;
    private byte[] salt;
    private byte[] iv;
    private int iterationsPBKDF2;
    private int keyLengthPBKDF2;

    public EncryptedMessage(byte[] cipherText, byte[] salt, byte[] iv, int iterationsPBKDF2, int keyLengthPBKDF2) {
        this.cipherText = cipherText;
        this.salt = salt;
        this.iv = iv;
        this.iterationsPBKDF2 = iterationsPBKDF2;
        this.keyLengthPBKDF2 = keyLengthPBKDF2;
    }

    public byte[] getCipherText() {
        return cipherText;
    }

    public void setCipherText(byte[] cipherText) {
        this.cipherText = cipherText;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    public int getIterationsPBKDF2() {
        return iterationsPBKDF2;
    }

    public void setIterationsPBKDF2(int iterationsPBKDF2) {
        this.iterationsPBKDF2 = iterationsPBKDF2;
    }

    public int getKeyLengthPBKDF2() {
        return keyLengthPBKDF2;
    }

    public void setKeyLengthPBKDF2(int keyLengthPBKDF2) {
        this.keyLengthPBKDF2 = keyLengthPBKDF2;
    }
}
