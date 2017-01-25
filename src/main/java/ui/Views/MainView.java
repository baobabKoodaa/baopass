package ui.Views;

import app.BaoPassCore;
import app.Main;
import crypto.PBKDF2;
import ui.Listeners.ClickListener;
import ui.GUI;
import ui.Listeners.TextListener;
import util.ErrorMessages;

import javax.crypto.AEADBadTagException;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainView extends View {

    /** Id for this View. */
    public static final String id = "MAIN_VIEW";

    /* Ids for various elements inside this View. */
    public static final String MPW_FIELD_ID = "MPW";
    public static final String KW_FIELD_ID = "KW";
    public static final String LOCK_ID = "LOCK";
    public static final String SITE_PASS_ID = "SITE_PASS";

    /* Static texts displayed to user. */
    public static final String TEXT_WHEN_NO_SITE_PASS = " "; /* layout breaks if space is removed */
    public static final String TOOLTIP_OPEN_LOCK = "Click here to lock down.";
    public static final String TOOLTIP_CLOSED_LOCK = "Please insert master password to decrypt keyfile.";
    public static final String TOOLTIP_SITE_PASS = "Click here to copy site pass to clipboard.";

    /* Static parameters. */
    public static final int SECONDS_TO_KEEP_SITE_PASS_IN_CLIPBOARD = 10;

    /* Service for scheduled events. */
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /* Dependencies. */
    GUI gui;
    BaoPassCore baoPassCore;

    /* Properties. */
    JPasswordField MPW;
    private JTextField keywordField;
    JLabel sitePass;

    private JLabel lockIcon;
    private boolean locked;
    private ImageIcon closedLockIcon;
    private ImageIcon openLockIcon;

    private String hashOfWhatWeSetClipboardTo;

    public MainView(GUI gui, BaoPassCore baoPassCore) throws IOException {
        this.gui = gui;
        this.baoPassCore = baoPassCore;

        setLayout(new GridBagLayout());
        JPanel contents = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,3,5,3);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.ipady = 5;
        gbc.anchor = GridBagConstraints.EAST;

        /* Text labels. */
        JLabel labelMasterPassword = new JLabel("Master password: ");
        labelMasterPassword.setFont(gui.regularFont);
        contents.add(labelMasterPassword, gbc);
        gbc.gridy++;
        JLabel labelKeyword = new JLabel("Keyword: ");
        labelKeyword.setFont(gui.regularFont);
        contents.add(labelKeyword, gbc);
        gbc.gridy++;
        contents.add(new JSeparator(SwingConstants.HORIZONTAL));
        gbc.gridy++;
        JLabel labelSitePass = new JLabel("Site password: ");
        labelSitePass.setVerticalAlignment(JLabel.BOTTOM);
        labelSitePass.setFont(gui.regularFont);
        contents.add(labelSitePass, gbc);
        gbc.gridx++;
        gbc.gridy = 0;
        //gbc.weightx = 1;
        //gbc.fill = GridBagConstraints.HORIZONTAL;

        /* Master pass field. */
        MPW = new JPasswordField(11);
        contents.add(MPW, gbc);
        gbc.gridx++;

        /* Set up clickable lock icon. */
        lockIcon = new JLabel();
        locked = true;
        try {
            String path = "lock-closed-25.png";
            closedLockIcon = new ImageIcon(ImageIO.read(Main.class.getClassLoader().getResourceAsStream(path)));
            path = "lock-open-25.png";
            openLockIcon = new ImageIcon(ImageIO.read(Main.class.getClassLoader().getResourceAsStream(path)));
            lockIcon.setIcon(closedLockIcon);
            lockIcon.addMouseListener(new ClickListener(this, LOCK_ID));
            lockIcon.setVerticalAlignment(JLabel.TOP);
        } catch (Exception ex) {
            /* Fail gracefully and simply don't show the lock icon. */
        }
        contents.add(lockIcon, gbc);
        gbc.gridy++;
        gbc.gridx--;

        /* Keyword field. */
        //TODO: remember chosen keywords. https://docs.oracle.com/javase/tutorial/uiswing/components/combobox.html
        keywordField = new JTextField(11);
        contents.add(keywordField, gbc);
        gbc.gridy++;
        gbc.gridy++;

        /* Generated site password. */
        sitePass = new JLabel(TEXT_WHEN_NO_SITE_PASS);
        sitePass.setToolTipText(TOOLTIP_SITE_PASS);
        sitePass.setFont(gui.monospaceFont);
        sitePass.setVerticalAlignment(JLabel.BOTTOM);
        sitePass.addMouseListener(new ClickListener(this, SITE_PASS_ID));
        contents.add(sitePass, gbc);

        MPW.getDocument().addDocumentListener(new TextListener(this, MPW_FIELD_ID));
        keywordField.getDocument().addDocumentListener(new TextListener(this, KW_FIELD_ID));

        add(contents);
    }

    private void sitePassClicked() {
        copySitePassToClipboard();
        sitePass.setText("Copied");
    }

    private void copySitePassToClipboard() {
        try {
            char[] sitePassChars = baoPassCore.generateSitePass(keywordField.getText().toCharArray());
            setClipboard(new String(sitePassChars));
            scheduler.schedule(clearSitePassFromClipboard, SECONDS_TO_KEEP_SITE_PASS_IN_CLIPBOARD, TimeUnit.SECONDS);
        } catch (Exception ex) {
            gui.popupError(ErrorMessages.CLIPBOARD_FAILURE);
        }
    }

    Runnable clearSitePassFromClipboard = new Runnable() {
        @Override
        public void run() {
            try {
                String hashOfClipboardNow = getClipboardHash();
                if (hashOfClipboardNow.equals(hashOfWhatWeSetClipboardTo)) {
                    /* Don't clear the clipboard if it contains something else than site pass! */
                    setClipboard("");
                }
            } catch (Exception ex) {
                /* Do nothing if we failed to access or clear the clipboard. */
            }
        }
    };

    public String getClipboardHash() throws IOException, UnsupportedFlavorException {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        String clipboardContent = (String) clipboard.getData(DataFlavor.stringFlavor);
        return getHash(clipboardContent);
    }
    public String getHash(String clipboardContent) throws IOException, UnsupportedFlavorException {
        char[] chars = clipboardContent.toCharArray();
        return new String(PBKDF2.generateKey(chars, 1, 64).getEncoded());
    }

    public void setClipboard(String input) {
        StringSelection inputSelection = new StringSelection(input);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(inputSelection, null);
        try {
            hashOfWhatWeSetClipboardTo = getHash(input);
        } catch (Exception ex) {
            /* This never happens and if it does, it only affects our ability to clear the clipboard later. */
        }
    }

    public void generateSitePass() throws Exception {
        String kw = keywordField.getText();
        if (kw.isEmpty()) {
            sitePass.setText(TEXT_WHEN_NO_SITE_PASS);
        } else {
            char[] pass = baoPassCore.generateSitePass(kw.toCharArray());
            if (gui.menu.hideSitePass.getState()) {
                Arrays.fill(pass, '*');
            }
            sitePass.setText(new String(pass));
        }
    }

    private void openLock() {
        if (locked) {
            locked = false;
            lockIcon.setIcon(openLockIcon);
            lockIcon.setToolTipText(TOOLTIP_OPEN_LOCK);
        }
    }

    public void closeLock(boolean clearMPW, boolean clearKW) {
        baoPassCore.forgetMasterKeyPlainText();
        if (clearMPW) MPW.setText("");
        if (clearKW) keywordField.setText("");
        if (!locked) {
            sitePass.setText(TEXT_WHEN_NO_SITE_PASS);
            locked = true;
            lockIcon.setIcon(closedLockIcon);
            lockIcon.setToolTipText(TOOLTIP_CLOSED_LOCK);
        }
    }

    /** When user inserts a character to MPW field, try to decrypt. */
    private void MPWfieldChanged() {
        char[] mpw = MPW.getPassword();
        try {
            baoPassCore.decryptMasterKey(mpw);
        } catch (InvalidKeyException ex) {
            gui.popupError(ErrorMessages.CRYPTO_EXPORT_RESTRICTIONS);
            return;
        } catch (AEADBadTagException ex) {
            /* Invalid password. Do not report error, we try to decrypt on every keystroke. */
            closeLock(false, false);
            return;
        } catch (Exception ex) {
            gui.popupError(ErrorMessages.INTERNAL_FAILURE);
            return;
        }
        /* Decryption succesful. */
        openLock();
        if (!keywordField.getText().isEmpty()) {
            /* If keyword field is not empty, generate site pass as well. */
            KWfieldChanged();
        }
    }

    /** When user inserts a character to KW field, try to generate site pass. */
    private void KWfieldChanged() {
        try {
            generateSitePass();
        } catch (Exception ex) {
            /* Probably incorrect master password. Don't report error, because
            *  some users may prefer to write keyword before master pass. */
            sitePass.setText(TEXT_WHEN_NO_SITE_PASS);
        }
    }

    @Override
    public void performAction(String id) {
        switch (id) {
            case MPW_FIELD_ID:MPWfieldChanged();break;
            case KW_FIELD_ID:KWfieldChanged();break;
            case LOCK_ID:closeLock(true, true);break;
            case SITE_PASS_ID:sitePassClicked();break;
            default:break;
        }
    }

    @Override
    public String getId() {
        return id;
    }
}
