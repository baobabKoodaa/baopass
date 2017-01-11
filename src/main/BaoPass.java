package main;

import crypto.EntropyCollector;
import crypto.PBKDF2;
import ui.GUI;
import crypto.Utils;

import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

public class BaoPass {

    EntropyCollector entropyCollector;

    public BaoPass() throws NoSuchAlgorithmException {
        this.entropyCollector = new EntropyCollector();
    }

    public void run() throws NoSuchAlgorithmException {
        GUI gui = new GUI(this, entropyCollector);
    }

    public char[] generateMasterKey() throws NoSuchAlgorithmException {
        byte[] keyPartFromMainEntropySource = Utils.requestRandomBytesFromOS(32);
        byte[] keyPartFromAdditionalEntropy = entropyCollector.consume(16);
        byte[] masterKey = combine(keyPartFromMainEntropySource, keyPartFromAdditionalEntropy);
        return Utils.getUrlSafeCharsFromBytes(masterKey);
    }

    public char[] generateSitePass(char[] mkey, String keyword) {
        char[] combined = new char[mkey.length + keyword.length()];
        for (int i=0; i<mkey.length; i++) {
            combined[i] = mkey[i];
        }
        for (int i=0; i<keyword.length(); i++) {
            combined[i+mkey.length] = keyword.charAt(i);
        }
        byte[] salt = new byte[16]; // TODO
        SecretKey siteKey = PBKDF2.PBKDF2(combined, salt, 1000, 32);
        return Utils.getUrlSafeCharsFromBytes(siteKey.getEncoded());
    }

    /** Combines byte arrays into one, overwrites original arrays. */
    byte[] combine(byte[] a, byte[] b) {
        byte[] out = new byte[a.length + b.length];
        moveSafely(a, out, 0);
        moveSafely(b, out, a.length);
        return out;
    }

    /** Moves bytes from one array to another, overwrites original contents with '0'. */
    void moveSafely(byte[] src, byte[] dest, int start) {
        for (int i=0; i<src.length; i++) {
            dest[start+i] = src[i];
            src[i] = '0';
        }
    }

}
