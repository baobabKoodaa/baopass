package ui;

import app.Main;
import crypto.EntropyCollector;
import app.BaoPass;
import crypto.PBKDF2;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static crypto.Utils.charArrayEquals;
import static crypto.Utils.wipe;

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
    private static final int MENU_HIDE_SITE_PASS_ID = 10;
    private static final int BUTTON_OK_NOTIFICATION_ID = 11;
    private static final int TEXT_FIELD_CREATE_MPW1_ID = 12;
    private static final int TEXT_FIELD_CREATE_MPW2_ID = 13;
    private static final int BUTTON_ENCRYPT_ID = 14;
    private static final int MENU_CHANGE_MPW_ID = 15;
    private static final int BUTTON_PERFORM_MPW_CHANGE_ID = 16;
    private static final int BUTTON_CANCEL_MPW_CHANGE_ID = 17;

    /* IDs for different GUI views. */
    public static final String MAIN_VIEW_ID = "MAIN_VIEW_ID";
    public static final String FIRST_LAUNCH_VIEW_ID = "FIRST_LAUNCH_VIEW_ID";
    public static final String NOTIFICATION_VIEW_ID = "NOTIFICATION_VIEW_ID";
    public static final String NEW_KEY_VIEW_ID = "NEW_KEY_VIEW_ID";
    public static final String CHANGE_MPW_VIEW_ID = "CHANGE_MPW_VIEW_ID";
    private String currentViewId = FIRST_LAUNCH_VIEW_ID;
    private String nextViewId = FIRST_LAUNCH_VIEW_ID;

    /* Static texts displayed to user. */
    public static final String TEXT_WHEN_NO_SITE_PASS = " "; // layout breaks if space is removed
    public static final String TOOLTIP_OPEN_LOCK = "Click here to lock down.";
    public static final String TOOLTIP_CLOSED_LOCK = "Please insert master password to decrypt keyfile.";

    /* Static parameters. */
    public static final int SECONDS_TO_KEEP_SITE_PASS_IN_CLIPBOARD = 10;

    /* Service for scheduled events. */
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private JFrame frame;
    private JPanel viewHolder;
    private JPanel mainView;
    private JPanel firstLaunchView;
    private JPanel newKeyView;
    private JPanel changeMPWView;

    private JPanel notificationView;
    private JLabel notificationText;
    private JButton buttonOkNotification;

    private CardLayout cardLayout;
    private Dimension dimension;
    private Font regularFont;

    private BaoPass baoPass;
    private EntropyCollector entropyCollector;
    private EntropyListener inputEntropyListener;
    private JLabel sitePass;

    private JMenuItem menuSwitchKey;
    private JMenuItem menuChangeMPW;
    private JCheckBoxMenuItem menuHideSitePass;
    private JMenu aboutMenu;

    private JLabel lockIcon;
    private boolean locked;
    private ImageIcon closedLockIcon;
    private ImageIcon openLockIcon;

    private JTextField keywordField;

    private JPasswordField MPW_FIELD_IN_MAIN_VIEW;
    private JPasswordField MPW1_FIELD_IN_NEW_KEY_VIEW;
    private JPasswordField MPW2_FIELD_IN_NEW_KEY_VIEW;
    private JPasswordField MPW1_FIELD_IN_CHANGE_MPW_VIEW;
    private JPasswordField MPW2_FIELD_IN_CHANGE_MPW_VIEW;
    private JPasswordField MPW_OLD_FIELD_IN_CHANGE_MPW_VIEW;

    private JButton buttonEncryptNewKey;
    private JButton buttonEncryptOldKey;
    private JButton buttonCancelMPWChange;

    private String hashOfWhatWeSetClipboardTo;

    public GUI(BaoPass baoPass, EntropyCollector entropyCollector, String initialView) throws Exception {
        this.baoPass = baoPass;
        this.entropyCollector = entropyCollector;
        this.inputEntropyListener = new EntropyListener(entropyCollector);
        initFrame();

        /* Create GUI contents */
        createMenu();
        createMainView();
        createFirstLaunchView();
        createNotificationView();
        createNewKeyView();
        createChangeMPWView();

        packFrame(initialView);
    }

    private void sitePassClicked() {
        copySitePassToClipboard();
        sitePass.setText("Copied");
    }

    private void openLock() {
        if (locked) {
            locked = false;
            lockIcon.setIcon(openLockIcon);
            lockIcon.setToolTipText(TOOLTIP_OPEN_LOCK);
        }
    }

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





    /** Actions to take at the beginning of constructing the GUI. */
    private void initFrame() {
        entropyCollector.collect(System.nanoTime());
        dimension = new Dimension(300, 145);
        regularFont = new Font("Tahoma", Font.PLAIN, 12);
        frame = new JFrame("BaoPass");
        loadAppIcon();
        viewHolder = new JPanel();
        cardLayout = new CardLayout();
        viewHolder.setLayout(cardLayout);
    }

    /** Actions to take at the end of constructing the GUI. */
    private void packFrame(String initialView) {
        changeView(initialView);
        frame.add(viewHolder);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        entropyCollector.collect(System.nanoTime());
    }

    private void createNotificationView() {
        //notificationView = new JPanel(new BorderLayout());
        notificationView = new JPanel(new GridBagLayout());

        JPanel inc2 = new JPanel(new BorderLayout());

        notificationView.setPreferredSize(dimension);
        //notificationView.setLayout(new GridBagLayout());
        //notificationView.setLayout(new BorderLayout(50, 50));

        JPanel textPane = new JPanel();
        notificationText = new JLabel("Placeholder.");
        notificationText.setHorizontalAlignment(JLabel.CENTER);
        notificationText.setFont(regularFont);
        textPane.add(notificationText);

        JPanel inception = new JPanel();
        JPanel buttonPane = new JPanel();
        buttonOkNotification = createButton("OK");
        buttonOkNotification.addActionListener(new ClickListener(this, BUTTON_OK_NOTIFICATION_ID));
        buttonPane.add(buttonOkNotification);
        inception.add(buttonPane);
        viewHolder.add(notificationView, NOTIFICATION_VIEW_ID);

        inc2.add(textPane, BorderLayout.PAGE_START);
        inc2.add(inception, BorderLayout.CENTER);
        notificationView.add(inc2);

        addEntropyListeners(notificationView);
    }

    private void addEntropyListeners(JPanel view) {
        view.addMouseListener(inputEntropyListener);
        view.addMouseMotionListener(inputEntropyListener);
        view.addKeyListener(inputEntropyListener);
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(regularFont);
        menuSwitchKey = new JMenuItem("Switch active keyfile");
        menuSwitchKey.setFont(regularFont);
        menuSwitchKey.addActionListener(new ClickListener(this, MENU_SWITCH_KEY_ID));
        fileMenu.add(menuSwitchKey);

        menuChangeMPW = new JMenuItem("Change master password");
        menuChangeMPW.setFont(regularFont);
        menuChangeMPW.addActionListener(new ClickListener(this, MENU_CHANGE_MPW_ID));
        fileMenu.add(menuChangeMPW);

        JMenu optionsMenu = new JMenu("Options");
        optionsMenu.setFont(regularFont);

        menuHideSitePass = new JCheckBoxMenuItem("Hide site pass");
        menuHideSitePass.setFont(regularFont);
        menuHideSitePass.addActionListener(new ClickListener(this, MENU_HIDE_SITE_PASS_ID));
        optionsMenu.add(menuHideSitePass);

        aboutMenu = new JMenu("About");
        aboutMenu.setFont(regularFont);
        aboutMenu.addMouseListener(new ClickListener(this, MENU_ABOUT_ID));

        menuBar.add(fileMenu);
        menuBar.add(optionsMenu);
        menuBar.add(aboutMenu);
        frame.setJMenuBar(menuBar);
    }

    private void createFirstLaunchView() throws IOException {
        firstLaunchView = new JPanel();
        firstLaunchView.setPreferredSize(dimension);
        firstLaunchView.setLayout(new GridBagLayout());
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

        addEntropyListeners(firstLaunchView);
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

    private void createNewKeyView() {
        newKeyView = new JPanel();
        newKeyView.setPreferredSize(dimension);
        newKeyView.setLayout(new GridBagLayout());
        viewHolder.add(newKeyView, NEW_KEY_VIEW_ID);

        JPanel contents = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 3, 5, 3);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.ipady = 5;
        gbc.anchor = GridBagConstraints.EAST;

        /* Text labels. */
        JLabel labelMPW1 = new JLabel("Choose a password: ");
        labelMPW1.setFont(regularFont);
        contents.add(labelMPW1, gbc);
        gbc.gridy++;
        JLabel labelMPW2 = new JLabel("Retype password:");
        labelMPW2.setFont(regularFont);
        contents.add(labelMPW2, gbc);
        gbc.gridx++;
        gbc.gridy = 0;

        /* Create master password fields. */
        MPW1_FIELD_IN_NEW_KEY_VIEW = new JPasswordField(11);
        contents.add(MPW1_FIELD_IN_NEW_KEY_VIEW, gbc);
        gbc.gridy++;
        MPW2_FIELD_IN_NEW_KEY_VIEW = new JPasswordField(11);
        contents.add(MPW2_FIELD_IN_NEW_KEY_VIEW, gbc);
        gbc.gridy++;
        gbc.gridy++;

        buttonEncryptNewKey = createButton("Encrypt");
        buttonEncryptNewKey.addMouseListener(new ClickListener(this, BUTTON_ENCRYPT_ID));
        contents.add(buttonEncryptNewKey, gbc);

        addEntropyListeners(newKeyView);
        newKeyView.add(contents);
    }

    private void createChangeMPWView() {
        // old password
        // new password
        // retype
        // encrypt / cancel

        changeMPWView = new JPanel();
        changeMPWView.setPreferredSize(dimension);
        changeMPWView.setLayout(new GridBagLayout());
        viewHolder.add(changeMPWView, CHANGE_MPW_VIEW_ID);

        JPanel contents = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 3, 0, 3);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.ipady = 5;
        gbc.anchor = GridBagConstraints.EAST;

        /* Text labels. */
        JLabel labelMPW0 = new JLabel("Old password: ");
        labelMPW0.setFont(regularFont);
        contents.add(labelMPW0, gbc);
        gbc.gridy++;
        JLabel labelMPW1 = new JLabel("New password: ");
        labelMPW1.setFont(regularFont);
        contents.add(labelMPW1, gbc);
        gbc.gridy++;
        JLabel labelMPW2 = new JLabel("Retype password:");
        labelMPW2.setFont(regularFont);
        contents.add(labelMPW2, gbc);
        gbc.gridx++;
        gbc.gridy = 0;

        /* Old password field */
        MPW_OLD_FIELD_IN_CHANGE_MPW_VIEW = new JPasswordField(14);
        contents.add(MPW_OLD_FIELD_IN_CHANGE_MPW_VIEW, gbc);
        gbc.gridy++;

        /* Reuse "create MPW" fields from "New key view". */
        MPW1_FIELD_IN_CHANGE_MPW_VIEW = new JPasswordField(14);
        contents.add(MPW1_FIELD_IN_CHANGE_MPW_VIEW, gbc);
        gbc.gridy++;
        MPW2_FIELD_IN_CHANGE_MPW_VIEW = new JPasswordField(14);
        contents.add(MPW2_FIELD_IN_CHANGE_MPW_VIEW, gbc);
        gbc.gridy++;
        gbc.gridy++;

        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout());
        buttonCancelMPWChange = createButton("Cancel");
        buttonCancelMPWChange.addMouseListener(new ClickListener(this, BUTTON_CANCEL_MPW_CHANGE_ID));
        buttonEncryptOldKey = createButton("Change");
        buttonEncryptOldKey.addMouseListener(new ClickListener(this, BUTTON_PERFORM_MPW_CHANGE_ID));

        buttons.add(buttonCancelMPWChange);
        buttons.add(buttonEncryptOldKey);
        contents.add(buttons, gbc);

        addEntropyListeners(changeMPWView);
        changeMPWView.add(contents);
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
        MPW_FIELD_IN_MAIN_VIEW = new JPasswordField(11);
        MPW_FIELD_IN_MAIN_VIEW.getDocument().addDocumentListener(new TextListener(this, TEXT_FIELD_MASTER_PASS_ID));
        contents.add(MPW_FIELD_IN_MAIN_VIEW, gbc);
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

        keywordField.getDocument().addDocumentListener(new TextListener(this, TEXT_FIELD_KEYWORD_ID));

        addEntropyListeners(mainView);
        mainView.add(contents);
    }

    public void textFieldChanged(int id) {
        switch (id) {
            case TEXT_FIELD_MASTER_PASS_ID:
                if (locked && baoPass.decryptMasterKey(MPW_FIELD_IN_MAIN_VIEW.getText())) {
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
                MPW_FIELD_IN_MAIN_VIEW.setText("");
                closeLock();
                break;
            case SITE_PASS_ID:
                sitePassClicked();
                break;
            case BUTTON_NEW_MASTER_KEY_ID:
                if (baoPass.generateMasterKey() != null) {
                    notificationText.setText("<html>Your random keyfile has been<br>" +
                                                   "generated succesfully. Next you<br>" +
                                                   "will be asked to choose a master<br>" +
                                                   "password to encrypt the keyfile.");
                    changeView(NOTIFICATION_VIEW_ID);
                    nextViewId = NEW_KEY_VIEW_ID;
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
            case BUTTON_ENCRYPT_ID:
                char[] pw1 = MPW1_FIELD_IN_NEW_KEY_VIEW.getPassword();
                char[] pw2 = MPW2_FIELD_IN_NEW_KEY_VIEW.getPassword();
                if (!charArrayEquals(pw1, pw2)) {
                    System.out.println("not equals");
                    // TODO: complain
                }
                else if (baoPass.encryptMasterKey(pw1)) {
                    notificationText.setText( "<html>Your keyfile has been encrypted<br>" +
                                                    "succesfully and saved under filename<br>" +
                                                    "key1.baopass<br>");
                    System.out.println("Enrypted mpw with " + new String(pw1));
                    wipePasswordFields();
                    changeView(NOTIFICATION_VIEW_ID);
                    nextViewId = MAIN_VIEW_ID;
                } else {
                    // TODO: print error
                }
                break;
            case MENU_ABOUT_ID:
                notificationText.setText("<html>BaoPass by Baobab, unreleased<br>developer version. For updates,<br>visit https://baobab.fi/baopass");
                changeView(NOTIFICATION_VIEW_ID);
                SwingUtilities.invokeLater(deselectAboutMenu);
                break;
            case BUTTON_OK_NOTIFICATION_ID:
                changeView(nextViewId);
                break;
            case MENU_SWITCH_KEY_ID:
                closeLock();
                changeView(FIRST_LAUNCH_VIEW_ID);
                break;
            case MENU_CHANGE_MPW_ID:
                notificationText.setText("<html>Changing the master password<br>" +
                                               "means your keyfile will be<br>" +
                                               "encrypted with a new password.<br>" +
                                               "Your site passwords won't change.<br>");
                changeView(NOTIFICATION_VIEW_ID);
                nextViewId = CHANGE_MPW_VIEW_ID;
                break;
            case BUTTON_CANCEL_MPW_CHANGE_ID:
                wipePasswordFields();
                changeView(MAIN_VIEW_ID);
                break;
            case BUTTON_PERFORM_MPW_CHANGE_ID:
                //TODO: Check old pw, check new pws match, encrypt, file operations, verify.
                wipePasswordFields();
                changeView(MAIN_VIEW_ID);
                break;
            case MENU_HIDE_SITE_PASS_ID:
                //TODO: async ? save to preferences ?
                try {
                    generateSitePass();
                } catch (Exception ex) {
                    // TODO: ?
                }
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

    Runnable clearSitePassFromClipboard = new Runnable() {
        @Override
        public void run() {
            try {
                String hashOfClipboardNow = getClipboardHash(null);
                if (hashOfClipboardNow.equals(hashOfWhatWeSetClipboardTo)) {
                    /* Don't clear the clipboard if it contains something else than site pass! */
                    setClipboard("");
                }
            } catch (Exception ex) {
                /* Do nothing if we failed to access or clear the clipboard. */
            }
        }
    };

    public String getClipboardHash(String clipboardContent) throws IOException, UnsupportedFlavorException {
        if (clipboardContent == null) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboardContent = (String) clipboard.getData(DataFlavor.stringFlavor);
        }
        char[] chars = clipboardContent.toCharArray();
        return new String(PBKDF2.generateKey(chars, 1, 64).getEncoded());
    }

    public void setClipboard(String input) {
        StringSelection inputSelection = new StringSelection(input);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(inputSelection, null);
        try {
            hashOfWhatWeSetClipboardTo = getClipboardHash(input);
        } catch (Exception ex) {
            /* This never happens and if it does, it only affects our ability to clear the clipboard. */
        }
    }

    private void copySitePassToClipboard() {
        try {
            char[] sitePassChars = baoPass.generateSitePass(keywordField.getText().toCharArray());
            setClipboard(new String(sitePassChars));
            scheduler.schedule(clearSitePassFromClipboard, SECONDS_TO_KEEP_SITE_PASS_IN_CLIPBOARD, TimeUnit.SECONDS);
        } catch (Exception ex) {
            // TODO: error accessing clipboard (or generating site pass)
        }
    }

    public void generateSitePass() throws Exception {
        String kw = keywordField.getText();
        if (kw.isEmpty()) {
            sitePass.setText(TEXT_WHEN_NO_SITE_PASS);
        } else {
            char[] pass = baoPass.generateSitePass(kw.toCharArray());
            if (menuHideSitePass.getState()) {
                Arrays.fill(pass, '*');
            }
            sitePass.setText(new String(pass));
        }
    }

    public void repaint() {
        System.out.println("REPAINT WAS CALLED");
        mainView.repaint();
    }

    public static Point getPoint(MouseEvent e) {
        return new Point(e.getY(), e.getX());
    }

    public void wipePasswordFields() {
        MPW1_FIELD_IN_NEW_KEY_VIEW.setText("");
        MPW2_FIELD_IN_NEW_KEY_VIEW.setText("");
        MPW1_FIELD_IN_CHANGE_MPW_VIEW.setText("");
        MPW2_FIELD_IN_CHANGE_MPW_VIEW.setText("");
        MPW_FIELD_IN_MAIN_VIEW.setText("");
        MPW_OLD_FIELD_IN_CHANGE_MPW_VIEW.setText("");
    }

    private void changeView(String viewId) {
        aboutMenu.setEnabled(viewId.equals(NOTIFICATION_VIEW_ID) ? false : true);
        if (viewId.equals(FIRST_LAUNCH_VIEW_ID)) {
            wipePasswordFields();
        }
        boolean ifMainView = (viewId.equals(MAIN_VIEW_ID));
        menuSwitchKey.setEnabled(ifMainView);
        menuChangeMPW.setEnabled(ifMainView);

        nextViewId = currentViewId; /* Default assumption, sometimes overriden after calling this. */
        currentViewId = viewId;
        cardLayout.show(viewHolder, viewId);
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
