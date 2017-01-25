package ui.Views;

import app.BaoPassCore;
import ui.Listeners.ClickListener;
import ui.GUI;
import util.ErrorMessages;
import util.Notifications;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class FirstLaunchView extends View {

    public static final String id = "FIRST_LAUNCH_VIEW";

    GUI gui;
    BaoPassCore baoPassCore;

    public FirstLaunchView(GUI gui, BaoPassCore baoPassCore) {
        this.gui = gui;
        this.baoPassCore = baoPassCore;
        setLayout(new GridBagLayout());
        JPanel buttons = new JPanel(new GridLayout(4, 1));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weighty = 1;

        JLabel welcomeText = new JLabel("Welcome to BaoPass!");
        welcomeText.setHorizontalAlignment(JLabel.CENTER);
        welcomeText.setFont(gui.regularFont);
        buttons.add(welcomeText, gbc);
        JButton buttonCreate = gui.createButton("Create new master key", this);
        buttons.add(buttonCreate, gbc);
        JButton buttonLoad = gui.createButton("Load old master key", this);
        buttons.add(buttonLoad, gbc);
        JCheckBox checkBoxRemember = new JCheckBox("Remember this key");
        checkBoxRemember.setFont(gui.regularFont);
        checkBoxRemember.setHorizontalAlignment(JLabel.CENTER);
        if (baoPassCore.getPreferenceRememberKey()) {
            checkBoxRemember.doClick();
        }
        checkBoxRemember.addActionListener(new ClickListener(this, "Checkbox"));
        buttons.add(checkBoxRemember, BorderLayout.CENTER);

        add(buttons);
    }

    private void generateNewKey() {
        try {
            baoPassCore.generateMasterKey();
            gui.notifyUser(Notifications.SUCCESSFUL_KEY_GENERATION);
            gui.setNextViewId(NewKeyView.id);
        } catch (NoSuchAlgorithmException|UnsupportedEncodingException ex) {
            gui.popupError(ErrorMessages.INTERNAL_FAILURE + ex.toString());
        }
    }

    private void loadOldKey() {
        File file = askUserForFile();
        if (file == null) {
            /* User clicked cancel. */
            return;
        }
        if (baoPassCore.loadEncryptedMasterKey(file)) {
            gui.changeView(MainView.id);
            SwingUtilities.invokeLater(hackToImproveGUIResponsiveness);
        } else {
            gui.popupError(ErrorMessages.KEYFILE_LOAD_FAILED);
        }
    }

    /** The very first time decryption is called is a bit slow.
     *  This dummy call exists to burn that first-time slowness. */
    Runnable hackToImproveGUIResponsiveness = new Runnable() {
        @Override
        public void run() {
            try {
                baoPassCore.decryptMasterKey(new char[0]);
            } catch (Exception ex) {
                /* Expected decryption to fail due to invalid password. */
            }
        }
    };

    private File askUserForFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(baoPassCore.getConfigDirPath()));
        chooser.setDialogTitle("Choose encrypted keyfile");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(gui.getFrame()) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    @Override
    public void performAction(String id) {
        switch (id) {
            case "Create new master key":generateNewKey();break;
            case "Load old master key":loadOldKey();break;
            case "Checkbox":baoPassCore.flipPreferenceRememberKey();break;
            default:break;
        }
    }

    @Override
    public String getId() {
        return id;
    }
}
