package ui.Views;

import app.BaoPassCore;
import ui.ClickListener;
import ui.GUI;

import javax.swing.*;
import java.awt.*;
import java.io.File;

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
        if (baoPassCore.generateMasterKey() != null) {
            gui.notifyUser("<html>Your random keyfile has been<br>" +
                    "generated succesfully. Next you<br>" +
                    "will be asked to choose a master<br>" +
                    "password to encrypt the keyfile.");
            gui.setNextViewId(NewKeyView.id);
        } else {
            // TODO
        }
    }

    private void loadOldKey() {
        if (baoPassCore.loadEncryptedMasterKey(askUserForFile())) {
            gui.changeView(MainView.id);
            SwingUtilities.invokeLater(hackToImproveGUIResponsiveness);
        } else {
            // TODO
        }
    }

    /** The very first time decryption is called is a bit slow.
     *  This dummy call exists to burn that first-time slowness. */
    Runnable hackToImproveGUIResponsiveness = new Runnable() {
        @Override
        public void run() {
            baoPassCore.decryptMasterKey(new char[0]);
        }
    };

    private File askUserForFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File("."));
        chooser.setDialogTitle("Choose encrypted keyfile");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        if (chooser.showOpenDialog(gui.getFrame()) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void performAction(String id) {
        switch (id) {
            case "Create new master key":generateNewKey();break;
            case "Load old master key":loadOldKey();break;
            case "Checkbox":
                baoPassCore.flipPreferenceRememberKey();break;
            default:break;
        }
    }
}
