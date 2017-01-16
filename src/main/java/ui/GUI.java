package ui;

import app.Main;
import crypto.EntropyCollector;
import app.BaoPass;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class GUI {

    private static final int LOCK_ID = 1;
    private static final int SITE_PASS_ID = 2;

    public static final String MAIN_VIEW_ID = "MAIN_VIEW_ID";
    public static final String FIRST_LAUNCH_VIEW_ID = "FIRST_LAUNCH_VIEW_ID";

    private JFrame frame;
    private JPanel viewHolder;
    private JPanel mainView;
    private JPanel firstLaunchView;
    private CardLayout cardLayout;
    private Dimension dimension;

    private BaoPass baoPass;
    private EntropyCollector entropyCollector;
    private EntropyListener inputEntropyListener;
    private JLabel sitePass;

    private JLabel lockIcon;
    private boolean locked;
    private ImageIcon closedLockIcon;
    private ImageIcon openLockIcon;

    private JSearchTextField keywordField;

    public GUI(BaoPass baoPass, EntropyCollector entropyCollector, String initialView) throws Exception {
        this.baoPass = baoPass;
        this.entropyCollector = entropyCollector;
        this.inputEntropyListener = new EntropyListener(entropyCollector);

        dimension = new Dimension(260, 400);
        frame = new JFrame();
        viewHolder = new JPanel();
        cardLayout = new CardLayout();
        viewHolder.setLayout(cardLayout);

        createMainView();
        createFirstLaunchView();

        String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for ( int i = 0; i < fonts.length; i++ ) {
            //System.out.println(fonts[i]);
        }

        cardLayout.show(viewHolder, initialView);
        frame.add(viewHolder);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }

    private void createFirstLaunchView() throws IOException {
        firstLaunchView = new JPanel();
        firstLaunchView.setPreferredSize(dimension);
        //firstLaunchView.setBackground(new Color(255, 255, 255));
        viewHolder.add(firstLaunchView, FIRST_LAUNCH_VIEW_ID);

        JPanel buttons = new JPanel(new GridLayout(3, 1));

        GridBagConstraints gbc = new GridBagConstraints();
        //gbc.anchor = GridBagConstraints.CENTER;
        //gbc.weighty = 1;

        JLabel welcomeText = new JLabel("       Welcome to BaoPass!");
        welcomeText.setFont(new Font("Tahoma", Font.PLAIN, 12));
        buttons.add(welcomeText, gbc);
        Button buttonCreate = new Button("Create new master key");
        buttons.add(buttonCreate, gbc);
        Button buttonLoad = new Button("Load old master key");
        buttons.add(buttonLoad, gbc);




        firstLaunchView.add(buttons, BorderLayout.CENTER);
    }

    private void createMainView() throws IOException {
        mainView = new JPanel(new FlowLayout(FlowLayout.LEFT));
        mainView.setPreferredSize(dimension);
        keywordField = new JSearchTextField(20);
        mainView.add(keywordField);

        String path = "lock-closed-25.png";
        closedLockIcon = new ImageIcon(ImageIO.read(Main.class.getClassLoader().getResourceAsStream(path)));
        path = "lock-open-25.png";
        openLockIcon = new ImageIcon(ImageIO.read(Main.class.getClassLoader().getResourceAsStream(path)));

        lockIcon = new JLabel(closedLockIcon);
        locked = true;
        mainView.add(lockIcon);
        lockIcon.addMouseListener(new ClickListener(this, LOCK_ID));

        sitePass = new JLabel("");
        sitePass.setFont(new Font("Monospaced", Font.PLAIN, 20));
        sitePass.addMouseListener(new ClickListener(this, SITE_PASS_ID));
        mainView.add(sitePass);

        mainView.setFocusable(true);
        mainView.requestFocus();

        mainView.addMouseListener(inputEntropyListener);
        mainView.addMouseMotionListener(inputEntropyListener);
        mainView.addKeyListener(inputEntropyListener);

        KeywordListener keywordListener = new KeywordListener(this);
        keywordField.getDocument().addDocumentListener(keywordListener);
        keywordField.addKeyListener(inputEntropyListener);

        viewHolder.add(mainView, MAIN_VIEW_ID);
    }

    public void userClicked(int id) {
        switch (id) {
            case LOCK_ID:
                lockClicked();
                break;
            case SITE_PASS_ID:
                sitePassClicked();
                break;
            default:
                break;
        }

    }

    private void lockClicked() {
        if (!locked) {
            baoPass.forgetMasterKeyPlainText();
            changeLockState();
        } else if (baoPass.decryptMasterKey()) {
            changeLockState();
        }
    }

    private void changeLockState() {
        locked = !locked;
        lockIcon.setIcon(locked ? closedLockIcon : openLockIcon);
    }

    private void sitePassClicked() {
        System.out.println("prev:  " + sitePass.getText());
        sitePass.setText("Copied to clipboard");
        System.out.println("after: " + sitePass.getText());
    }

    public void generateSitePass() throws Exception {
        String kw = keywordField.getText();
        if (kw.isEmpty()) {
            sitePass.setText("");
        } else {
            char[] pass = baoPass.generateSitePass(baoPass.getMasterKeyPlainText(), kw.toCharArray());
            sitePass.setText(new String(pass));
        }
    }

    public void repaint() {
        mainView.repaint();
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
            baoPass.setMasterKeyPlainText(mkey);
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
