package ui;

import app.Main;
import crypto.EntropyCollector;
import app.BaoPass;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class GUI {

    private static final int LOCK_ID = 1;
    private static final int SITE_PASS_ID = 2;
    private static final int BUTTON_LOAD_MASTER_KEY_ID = 3;
    private static final int BUTTON_NEW_MASTER_KEY_ID = 4;
    private static final int CHECKBOX_REMEMBER_KEY_ID = 5;

    public static final String MAIN_VIEW_ID = "MAIN_VIEW_ID";
    public static final String FIRST_LAUNCH_VIEW_ID = "FIRST_LAUNCH_VIEW_ID";

    private JFrame frame;
    private JPanel viewHolder;
    private JPanel mainView;
    private JPanel firstLaunchView;
    private CardLayout cardLayout;
    private Dimension dimension;
    private Font regularFont;

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

        dimension = new Dimension(260, 170);
        regularFont = new Font("Tahoma", Font.PLAIN, 12);
        frame = new JFrame("BaoPass");
        loadAppIcon();
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
        firstLaunchView.setLayout(new GridBagLayout());
        //firstLaunchView.setBackground(new Color(255, 255, 255));
        viewHolder.add(firstLaunchView, FIRST_LAUNCH_VIEW_ID);

        JPanel buttons = new JPanel(new GridLayout(4, 1));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weighty = 1;

        JLabel welcomeText = new JLabel("Welcome to BaoPass!");
        welcomeText.setHorizontalAlignment(JLabel.CENTER);
        welcomeText.setFont(regularFont);
        buttons.add(welcomeText, gbc);
        JButton buttonCreate = createButton("Create new master key");
        buttons.add(buttonCreate, gbc);
        buttonCreate.addMouseListener(new ClickListener(this, BUTTON_NEW_MASTER_KEY_ID));
        JButton buttonLoad = createButton("Load old master key");
        buttonLoad.addMouseListener(new ClickListener(this, BUTTON_LOAD_MASTER_KEY_ID));
        buttons.add(buttonLoad, gbc);
        JCheckBox checkBoxRemember = new JCheckBox("Remember this key");
        checkBoxRemember.setFont(regularFont);
        checkBoxRemember.setHorizontalAlignment(JLabel.CENTER);
        if (baoPass.getPreferenceRememberKey()) {
            checkBoxRemember.doClick();
        }
        checkBoxRemember.addActionListener(new ClickListener(this, CHECKBOX_REMEMBER_KEY_ID));
        buttons.add(checkBoxRemember, BorderLayout.CENTER);

        firstLaunchView.addMouseListener(inputEntropyListener);
        firstLaunchView.addMouseMotionListener(inputEntropyListener);
        firstLaunchView.addKeyListener(inputEntropyListener);
        firstLaunchView.add(buttons);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(142, 68, 173));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Tahoma", Font.BOLD, 12));
        button.addMouseMotionListener(inputEntropyListener);
        button.addMouseListener(inputEntropyListener);
        return button;
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

        sitePass = new JLabel("Site password");
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
            case BUTTON_NEW_MASTER_KEY_ID:
                if (baoPass.createNewMasterKey("passu".toCharArray())) {
                    changeView(MAIN_VIEW_ID);
                }
                break;
            case BUTTON_LOAD_MASTER_KEY_ID:
                if (baoPass.loadEncryptedMasterKey(askUserForFile())) {
                    changeView(MAIN_VIEW_ID);
                }
                break;
            case CHECKBOX_REMEMBER_KEY_ID:
                baoPass.setPreferenceRememberKey(!baoPass.getPreferenceRememberKey());
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
        sitePass.setText("Copied to clipboard");
    }

    public void generateSitePass() throws Exception {
        String kw = keywordField.getText();
        if (kw.isEmpty()) {
            sitePass.setText("Site password");
        } else {
            char[] pass = baoPass.generateSitePass(kw.toCharArray());
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

    private void changeView(String view) {
        cardLayout.show(viewHolder, view);
    }

    private File askUserForFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Choose encrypted keyfile");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    /** Try to load icon for the app, use default icon if something goes wrong. */
    private void loadAppIcon() {
        try {
            //Image large = ImageIO.read(Main.class.getClassLoader().getResourceAsStream("icon2.png"));
            Image small = ImageIO.read(Main.class.getClassLoader().getResourceAsStream("B-25.png"));
            java.util.List<Image> list = new ArrayList<>();
            //list.add(large);
            list.add(small);
            frame.setIconImages(list);
        } catch (Exception e) {
            /* Use default icon. */
        }
    }
}
