package app;

import crypto.EntropyCollector;
import ui.GUI;
import ui.Views.FirstLaunchView;
import ui.Views.MainView;

import java.awt.*;

public class Main {

    public static void main(String[] args) throws Exception {
        EntropyCollector entropyCollector = new EntropyCollector();
        BaoPassCore baoPassCore = new BaoPassCore(entropyCollector);
        String initialView = (baoPassCore.hasActiveKeyFile() ? MainView.id : FirstLaunchView.id);

        /* Swing GUIs are recommended to run on the Event Dispatch Thread. */
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    GUI gui = new GUI(baoPassCore, entropyCollector, initialView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

}
