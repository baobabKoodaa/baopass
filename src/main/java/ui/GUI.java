package ui;

import app.Main;
import crypto.AES;
import crypto.EncryptedMessage;
import crypto.EntropyCollector;
import app.BaoPass;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.InputStream;

public class GUI {
    private JFrame frame;
    private JPanel canvas;
    private BaoPass baoPass;
    private EntropyCollector entropyCollector;
    private JLabel sitePass;

    private JLabel lockIcon;
    private boolean locked;
    private ImageIcon closedLockIcon;
    private ImageIcon openLockIcon;

    private JSearchTextField keywordField;

    public GUI(BaoPass baoPass, EntropyCollector entropyCollector) throws Exception {
        this.baoPass = baoPass;
        this.entropyCollector = entropyCollector;
        frame = new JFrame();
        //canvas = new Canvas();
        canvas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        canvas.setPreferredSize(new Dimension(260, 70));
        //canvas.setBackground(Color.white);

        keywordField = new JSearchTextField(20);
        canvas.add(keywordField);

        //JTextField bah = new JTextField(20);
        //canvas.add(bah);

        String path = "lock-closed-25.png";
        closedLockIcon = new ImageIcon(ImageIO.read(Main.class.getClassLoader().getResourceAsStream(path)));
        path = "lock-open-25.png";
        openLockIcon = new ImageIcon(ImageIO.read(Main.class.getClassLoader().getResourceAsStream(path)));

        lockIcon = new JLabel(closedLockIcon);
        canvas.add(lockIcon);
        LockIconClickListener lockIconClickListener = new LockIconClickListener(this);
        lockIcon.addMouseListener(lockIconClickListener);
        lockClicked();

        String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for ( int i = 0; i < fonts.length; i++ ) {
            //System.out.println(fonts[i]);
        }

        sitePass = new JLabel("");
        sitePass.setFont(new Font("Monospaced", Font.PLAIN, 20));
        canvas.add(sitePass);

        canvas.setFocusable(true);
        canvas.requestFocus();
        EntropyListener inputEntropyListener = new EntropyListener(this, entropyCollector);
        canvas.addMouseListener(inputEntropyListener);
        canvas.addMouseMotionListener(inputEntropyListener);
        canvas.addKeyListener(inputEntropyListener);
        keywordField.addKeyListener(inputEntropyListener);
        keywordField.getDocument().addDocumentListener(inputEntropyListener);

        repaint();
        frame.getContentPane().add(canvas);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        repaint();
    }

    public void lockClicked() {
        locked = !locked;
        lockIcon.setIcon(locked ? closedLockIcon : openLockIcon);
    }

    public void generateSitePass() throws Exception {
        String kw = keywordField.getText();
        if (kw.isEmpty()) {
            sitePass.setText("");
        } else {
            char[] pass = baoPass.generateSitePass(baoPass.getMasterKey(), keywordField.getText().toCharArray());
            sitePass.setText(new String(pass));
        }
    }

    public void repaint() {
        canvas.repaint();
    }

    private class Canvas extends JPanel {
        //@Override
        public void paintComponentx(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;

            g2d.setColor(Color.BLACK);
            //g2d.drawString("Testijuttu", 100, 100);
        }

    }

    public void userClickedOn(Point point) {
        try {
            char[] mkey = baoPass.generateMasterKey();
            baoPass.setMasterKey(mkey);
            for (int i=1; i<=100; i++) {
                //char[] sitekey = baoPass.generateSitePass(mkey, ("facebook" + i).toCharArray());
                //System.out.println("Site key length " + sitekey.length + " contents " + new String(sitekey));
            }


            //String temp1 = new String(mkey);
            //EncryptedMessage enc = AES.encrypt(new String(mkey).getBytes(), "passu".toCharArray());
            //System.out.println("cipher length " + enc.getCipherText().length + " had input length " + new String(mkey).getBytes().length);
            //enc.saveToFile("test.txt");
            //enc = new EncryptedMessage("test.txt");
            //String temp2 = new String(AES.decrypt(enc, "passu".toCharArray()));
            //System.out.println("********* Match ? " + temp1.equals(temp2));
            //System.out.println("          first " + temp1);
            //System.out.println("          secon " + temp2);
            //System.out.println("          ciphe " + new String(enc.getCipherText()));


        } catch (Exception ex) {
            System.out.println("Error " + ex.toString());
        }
        repaint();
    }


    public static Point getPoint(MouseEvent e) {
        return new Point(e.getY(), e.getX());
    }
}
