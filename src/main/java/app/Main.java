package app;

import crypto.EntropyCollector;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.InputStream;

public class Main {

    public static void main(String[] args) throws Exception {
        BaoPass baoPass = new BaoPass(new EntropyCollector());
        baoPass.run();
    }

}
