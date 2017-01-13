package ui;

import crypto.AES;
import crypto.EncryptedMessage;
import crypto.EntropyCollector;
import app.BaoPass;

import javax.swing.*;
import java.awt.*;

public class GUI {
    private JFrame frame;
    private JPanel canvas;
    private BaoPass baoPass;
    private EntropyCollector entropyCollector;

    public GUI(BaoPass baoPass, EntropyCollector entropyCollector) {
        this.baoPass = baoPass;
        this.entropyCollector = entropyCollector;
        frame = new JFrame();
        canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(800, 800));
        canvas.setBackground(Color.white);
        JLabel label = new JLabel("testi");
        canvas.setFocusable(true);
        canvas.requestFocus();
        canvas.add(label);
        Listener inputListener = new Listener(this, entropyCollector);
        canvas.addMouseListener(inputListener);
        canvas.addMouseMotionListener(inputListener);
        canvas.addKeyListener(inputListener);

        repaint();
        frame.getContentPane().add(canvas);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }

    public void repaint() {
        canvas.repaint();
    }

    private class Canvas extends JPanel {
        @Override
        public void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;

            g2d.setColor(Color.BLACK);
            g2d.drawString("Testijuttu", 100, 100);
        }

    }

    public void userClickedOn(Point point) {
        try {
            char[] mkey = baoPass.generateMasterKey();
            System.out.println("Master key length " + mkey.length + " contents " + new String(mkey));
            for (int i=1; i<=100; i++) {
                char[] sitekey = baoPass.generateSitePass(mkey, ("facebook" + i).toCharArray());
                System.out.println("Site key length " + sitekey.length + " contents " + new String(sitekey));
            }


            String temp1 = new String(mkey);
            EncryptedMessage enc = AES.encrypt(new String(mkey).getBytes(), "passu".toCharArray());
            System.out.println("cipher length " + enc.getCipherText().length + " had input length " + new String(mkey).getBytes().length);
            enc.saveToFile("test.txt");
            enc = new EncryptedMessage("test.txt");
            String temp2 = new String(AES.decrypt(enc, "passu".toCharArray()));
            System.out.println("********* Match ? " + temp1.equals(temp2));
            System.out.println("          first " + temp1);
            System.out.println("          secon " + temp2);
            System.out.println("          ciphe " + new String(enc.getCipherText()));


        } catch (Exception ex) {
            System.out.println("Error " + ex.toString());
        }
        repaint();
    }


}
