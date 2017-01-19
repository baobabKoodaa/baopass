package crypto;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/** A wrapper for encrypted master key with methods to save/load from file. */
public class EncryptedMessage {

    private byte[] cipherText;
    private byte[] salt;
    private byte[] iv;
    private int iterationsPBKDF2;
    private int keyLengthPBKDF2;

    /** Normal constructor for creating new master keys. */
    public EncryptedMessage(
            final byte[] cipherText,
            final byte[] salt,
            final byte[] iv,
            final int iterationsPBKDF2,
            final int keyLengthPBKDF2
    ) {
        this.cipherText = cipherText;
        this.salt = salt;
        this.iv = iv;
        /* Iterations and keyLength are saved to allow changing those
         * parameters for new master keys without breaking any existing ones. */
        this.iterationsPBKDF2 = iterationsPBKDF2;
        this.keyLengthPBKDF2 = keyLengthPBKDF2;
    }

    /** Constructor for loading an old master key from file. */
    public EncryptedMessage(File sourceFile) throws FileNotFoundException {
        HashMap<String, String> map = getMapFromFile(sourceFile);
        this.cipherText = Utils.getBytesFromUrlSafeChars(map.get("cipherText").toCharArray());
        this.salt = Utils.getBytesFromUrlSafeChars(map.get("salt").toCharArray());
        this.iv = Utils.getBytesFromUrlSafeChars(map.get("iv").toCharArray());
        this.iterationsPBKDF2 = Integer.parseInt(map.get("PBKDF2iterationsForAESkey"));
        this.keyLengthPBKDF2 = Integer.parseInt(map.get("AESkeyLen")) / 8;
    }

    private HashMap<String, String> getMapFromFile(File file) throws FileNotFoundException {
        Scanner scanner = new Scanner(file, "UTF-8");
        HashMap<String, String> map = new HashMap<>();
        scanner.nextLine(); // first line is general info.
        while (scanner.hasNext()){
            String[] line = scanner.nextLine().split(":", 2);
            String key = line[0];
            String val = line[1];
            map.put(key, val);
        }
        scanner.close();
        return map;
    }

    /** Save this encrypted master key to file. */
    public void saveToFile(String filepath) throws IOException {
        if (new File(filepath).exists()) {
            //throw new FileAlreadyExistsException("File already exists! " + file.toString());
        }
        List<String> output = new ArrayList<>();
        output.add("Master key encrypted with AES/GCM/NoPadding, saved in Base64URLSafe encoding.");
        output.add("bitsOfEntropy:384"); /* Just for convenience. */
        output.add("cipherText:" + new String(Utils.getUrlSafeCharsFromBytes(cipherText)));
        output.add("salt:" + new String(Utils.getUrlSafeCharsFromBytes(salt)));
        output.add("iv:" + new String(Utils.getUrlSafeCharsFromBytes(iv)));
        output.add("PBKDF2iterationsForAESkey:" + iterationsPBKDF2);
        output.add("AESkeyLen:" + keyLengthPBKDF2 * 8);
        Path file = Paths.get(filepath);
        Files.write(file, output, Charset.forName("UTF-8"));
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        EncryptedMessage that = (EncryptedMessage) o;

        if (iterationsPBKDF2 != that.iterationsPBKDF2)
            return false;
        if (keyLengthPBKDF2 != that.keyLengthPBKDF2)
            return false;
        if (!Arrays.equals(cipherText, that.cipherText))
            return false;
        if (!Arrays.equals(salt, that.salt))
            return false;
        return Arrays.equals(iv, that.iv);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(cipherText);
        result = 31 * result + Arrays.hashCode(salt);
        result = 31 * result + Arrays.hashCode(iv);
        result = 31 * result + iterationsPBKDF2;
        result = 31 * result + keyLengthPBKDF2;
        return result;
    }
}
