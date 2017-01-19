package ui;

import app.Main;
import crypto.EntropyCollector;
import app.BaoPass;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GUI {

    /* IDs for different GUI elements. */
    private static final int LOCK_ID = 1;
    private static final int SITE_PASS_ID = 2;
    private static final int BUTTON_LOAD_MASTER_KEY_ID = 3;
    private static final int BUTTON_NEW_MASTER_KEY_ID = 4;
    private static final int CHECKBOX_REMEMBER_KEY_ID = 5;
    private static final int TEXT_FIELD_MASTER_PASS_ID = 6;
    private static final int TEXT_FIELD_KEYWORD_ID = 7;
    private static final int MENU_ABOUT_ID = 8;
    private static final int MENU_SWITCH_KEY_ID = 9;

    /* IDs for different GUI views. */
    public static final String MAIN_VIEW_ID = "MAIN_VIEW_ID";
    public static final String FIRST_LAUNCH_VIEW_ID = "FIRST_LAUNCH_VIEW_ID";

    /* Static texts displayed to user. */
    public static final String TEXT_WHEN_NO_SITE_PASS = " "; // layout breaks if space is removed
    public static final String TOOLTIP_OPEN_LOCK = "Click here to lock down.";
    public static final String TOOLTIP_CLOSED_LOCK = "Please insert master password to decrypt keyfile.";


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

    private JMenu aboutMenu;

    private JLabel lockIcon;
    private boolean locked;
    private ImageIcon closedLockIcon;
    private ImageIcon openLockIcon;

    private JTextField keywordField;
    private JTextField masterPassField;

    public GUI(BaoPass baoPass, EntropyCollector entropyCollector, String initialView) throws Exception {
        this.baoPass = baoPass;
        this.entropyCollector = entropyCollector;
        this.inputEntropyListener = new EntropyListener(entropyCollector);

        dimension = new Dimension(300, 145);
        regularFont = new Font("Tahoma", Font.PLAIN, 12);
        frame = new JFrame("BaoPass");
        loadAppIcon();
        viewHolder = new JPanel();
        cardLayout = new CardLayout();
        viewHolder.setLayout(cardLayout);

        createMenu();
        createMainView();
        createFirstLaunchView();

        String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for ( int i = 0; i < fonts.length; i++ ) {
            //System.out.println(fonts[i]);
        }

        changeView(FIRST_LAUNCH_VIEW_ID);

        frame.add(viewHolder);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu optionsMenu = new JMenu("Options");
        optionsMenu.setFont(regularFont);
        JMenuItem switchKey = new JMenuItem("Switch active key");
        switchKey.setFont(regularFont);
        switchKey.addActionListener(new ClickListener(this, MENU_SWITCH_KEY_ID));
        optionsMenu.add(switchKey);
        menuBar.add(optionsMenu);

        aboutMenu = new JMenu("About");
        aboutMenu.setFont(regularFont);
        aboutMenu.addMouseListener(new ClickListener(this, MENU_ABOUT_ID));

        menuBar.add(aboutMenu);

        frame.setJMenuBar(menuBar);
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
        mainView = new JPanel();
        mainView.setPreferredSize(dimension);
        mainView.setLayout(new GridBagLayout());
        viewHolder.add(mainView, MAIN_VIEW_ID);

        JPanel contents = new JPanel(new GridBagLayout());
        //JPanel contents = new JPanel(new GridLayout(3, 3, 5, 5));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,3,5,3);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.ipady = 5;
        gbc.anchor = GridBagConstraints.EAST;

        /* Text labels. */
        JLabel labelMasterPassword = new JLabel("Master password: ");
        labelMasterPassword.setFont(regularFont);
        contents.add(labelMasterPassword, gbc);
        gbc.gridy++;
        JLabel labelKeyword = new JLabel("Keyword: ");
        labelKeyword.setFont(regularFont);
        contents.add(labelKeyword, gbc);
        gbc.gridy++;
        contents.add(new JSeparator(SwingConstants.HORIZONTAL));
        gbc.gridy++;
        JLabel labelSitePass = new JLabel("Site password: ");
        labelSitePass.setVerticalAlignment(JLabel.BOTTOM);
        labelSitePass.setFont(regularFont);
        contents.add(labelSitePass, gbc);
        gbc.gridx++;
        gbc.gridy = 0;
        //gbc.weightx = 1;
        //gbc.fill = GridBagConstraints.HORIZONTAL;

        /* Master pass field. */
        masterPassField = new JPasswordField(11);
        masterPassField.getDocument().addDocumentListener(new TextListener(this, TEXT_FIELD_MASTER_PASS_ID));
        contents.add(masterPassField, gbc);
        gbc.gridx++;

        /* Set up clickable lock icon. */
        String path = "lock-closed-25.png";
        closedLockIcon = new ImageIcon(ImageIO.read(Main.class.getClassLoader().getResourceAsStream(path)));
        path = "lock-open-25.png";
        openLockIcon = new ImageIcon(ImageIO.read(Main.class.getClassLoader().getResourceAsStream(path)));
        lockIcon = new JLabel(closedLockIcon);
        locked = true;
        lockIcon.addMouseListener(new ClickListener(this, LOCK_ID));
        lockIcon.setVerticalAlignment(JLabel.TOP);
        contents.add(lockIcon, gbc);
        gbc.gridy++;
        gbc.gridx--;

        /* Keyword field. */
        keywordField = new JTextField(11);
        //TODO: remember chosen keywords. https://docs.oracle.com/javase/tutorial/uiswing/components/combobox.html
        contents.add(keywordField, gbc);
        gbc.gridy++;
        gbc.gridy++;

        sitePass = new JLabel(TEXT_WHEN_NO_SITE_PASS);
        sitePass.setToolTipText("Click here to copy site pass to clipboard.");
        sitePass.setFont(new Font("Monospaced", Font.PLAIN, 16));
        sitePass.setVerticalAlignment(JLabel.CENTER);
        sitePass.addMouseListener(new ClickListener(this, SITE_PASS_ID));
        contents.add(sitePass, gbc);

        mainView.setFocusable(true);
        mainView.requestFocus();

        mainView.addMouseListener(inputEntropyListener);
        mainView.addMouseMotionListener(inputEntropyListener);
        mainView.addKeyListener(inputEntropyListener);

        keywordField.getDocument().addDocumentListener(new TextListener(this, TEXT_FIELD_KEYWORD_ID));
        keywordField.addKeyListener(inputEntropyListener);

        mainView.add(contents);
    }

    public void textFieldChanged(int id) {
        switch (id) {
            case TEXT_FIELD_MASTER_PASS_ID:
                if (locked && baoPass.decryptMasterKey(masterPassField.getText())) {
                    openLock();
                } else {
                    closeLock();
                }
                break;
            case TEXT_FIELD_KEYWORD_ID:
                try {
                    generateSitePass();
                } catch (Exception ex) {
                    System.err.println("Error generating site pass: " + ex.toString());
                }
                break;
            default:
                break;
        }
    }

    public void userClicked(int id) {
        switch (id) {
            case LOCK_ID:
                masterPassField.setText("");
                closeLock();
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
                break;
            case MENU_ABOUT_ID:
                System.out.println("moi");
                SwingUtilities.invokeLater(deselectAboutMenu);
                break;
            case MENU_SWITCH_KEY_ID:
                closeLock();
                changeView(FIRST_LAUNCH_VIEW_ID);
                break;
            default:
                break;
        }

    }

    Runnable deselectAboutMenu = new Runnable() {
        public void run() {
            aboutMenu.setSelected(false);
        }
    };

    private void closeLock() {
        if (!locked) {
            baoPass.forgetMasterKeyPlainText();
            keywordField.setText("");
            sitePass.setText(TEXT_WHEN_NO_SITE_PASS);
            locked = true;
            lockIcon.setIcon(closedLockIcon);
            lockIcon.setToolTipText(TOOLTIP_CLOSED_LOCK);
        }
    }

    private void openLock() {
        if (locked) {
            locked = false;
            lockIcon.setIcon(openLockIcon);
            lockIcon.setToolTipText(TOOLTIP_OPEN_LOCK);
        }
    }

    private void sitePassClicked() {
        sitePass.setText("Copied");
    }

    public void generateSitePass() throws Exception {
        String kw = keywordField.getText();
        if (kw.isEmpty()) {
            sitePass.setText(TEXT_WHEN_NO_SITE_PASS);
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
