package main;

import main.BaoPass;

import java.security.NoSuchAlgorithmException;

public class Main {

    public static void main(String[] args) throws NoSuchAlgorithmException {
        //String salt = "bah";
        //int iterations = 137;
        //int keyLength = 12*8;
        //String out = PBKDF2(password.toCharArray(), salt.getBytes(), iterations, keyLength);

        BaoPass baoPass = new BaoPass();
        baoPass.run();
    }


}
