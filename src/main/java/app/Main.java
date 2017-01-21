package app;

import crypto.EntropyCollector;
import ui.GUI;
import ui.Views.FirstLaunchView;
import ui.Views.MainView;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.InputStream;

public class Main {

    public static void main(String[] args) throws Exception {
        EntropyCollector entropyCollector = new EntropyCollector();
        BaoPass baoPass = new BaoPass(entropyCollector);
        String initialView = (baoPass.loadEncryptedMasterKey() ? MainView.id : FirstLaunchView.id);

        /* Swing GUIs are recommended to run on the Event Dispatch Thread. */
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    GUI gui = new GUI(baoPass, entropyCollector, initialView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

}
