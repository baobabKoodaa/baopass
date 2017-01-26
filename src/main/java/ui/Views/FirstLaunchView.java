package ui.Views;

import app.CoreService;
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
    CoreService coreService;

    public FirstLaunchView(GUI gui, CoreService coreService) {
        this.gui = gui;
        this.coreService = coreService;
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
        if (coreService.getPreferenceRememberKey()) {
            checkBoxRemember.doClick();
        }
        checkBoxRemember.addActionListener(new ClickListener(this, "Checkbox"));
        buttons.add(checkBoxRemember, BorderLayout.CENTER);

        add(buttons);
    }

    private void generateNewKey() {
        try {
            coreService.generateMasterKey();
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
        if (coreService.loadEncryptedMasterKey(file)) {
            gui.changeView(MainView.id);
            SwingUtilities.invokeLater(gui.hackToImproveGUIResponsiveness);
        } else {
            gui.popupError(ErrorMessages.KEYFILE_LOAD_FAILED);
        }
    }

    private File askUserForFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(coreService.getConfigDirPath()));
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
            case "Checkbox":
                coreService.flipPreferenceRememberKey();break;
            default:break;
        }
    }

    @Override
    public String getId() {
        return id;
    }
}
