package ui.Views;

import app.CoreService;
import util.ErrorMessages;
import util.Utils;
import ui.GUI;

import javax.crypto.AEADBadTagException;
import javax.swing.*;
import java.awt.*;
import java.security.InvalidKeyException;

import static util.Utils.wipe;

public class ChangeMPWView extends View {

    public static final String id = "CHANGE_MPW_VIEW";

    /* Dependencies. */
    GUI gui;
    CoreService coreService;

    /* Properties. */
    private JPasswordField MASTER_PASS_OLD;
    private JPasswordField MASTER_PASS_NEW1;
    private JPasswordField MASTER_PASS_NEW2;
    private JButton buttonCancelMPWChange;
    private JButton buttonEncryptOldKey;

    public ChangeMPWView(GUI gui, CoreService coreService) {
        this.gui = gui;
        this.coreService = coreService;

        setLayout(new GridBagLayout());
        JPanel contents = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 3, 0, 3);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.ipady = 5;
        gbc.anchor = GridBagConstraints.EAST;

        /* Text labels. */
        JLabel labelMPW0 = new JLabel("Old password: ");
        labelMPW0.setFont(gui.regularFont);
        contents.add(labelMPW0, gbc);
        gbc.gridy++;
        JLabel labelMPW1 = new JLabel("New password: ");
        labelMPW1.setFont(gui.regularFont);
        contents.add(labelMPW1, gbc);
        gbc.gridy++;
        JLabel labelMPW2 = new JLabel("Retype password:");
        labelMPW2.setFont(gui.regularFont);
        contents.add(labelMPW2, gbc);
        gbc.gridx++;
        gbc.gridy = 0;

        /* Old password field. */
        MASTER_PASS_OLD = new JPasswordField(14);
        contents.add(MASTER_PASS_OLD, gbc);
        gbc.gridy++;

        /* New password fields. */
        MASTER_PASS_NEW1 = new JPasswordField(14);
        contents.add(MASTER_PASS_NEW1, gbc);
        gbc.gridy++;
        MASTER_PASS_NEW2 = new JPasswordField(14);
        contents.add(MASTER_PASS_NEW2, gbc);
        gbc.gridy++;
        gbc.gridy++;

        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout());
        buttonCancelMPWChange = gui.createButton("Cancel", this);
        buttonEncryptOldKey = gui.createButton("Change", this);
        buttons.add(buttonCancelMPWChange);
        buttons.add(buttonEncryptOldKey);
        contents.add(buttons, gbc);

        add(contents);
    }

    void performMasterPasswordChange() {
        char[] oldMPW = MASTER_PASS_OLD.getPassword();
        try {
            coreService.decryptMasterKey(oldMPW);
        } catch (InvalidKeyException ex) {
            gui.popupError(ErrorMessages.CRYPTO_EXPORT_RESTRICTIONS);
            return;
        } catch (AEADBadTagException ex) {
            gui.popupError(ErrorMessages.INVALID_OLD_MASTER_PASSWORD);
            return;
        } catch (Exception ex) {
            gui.popupError(ErrorMessages.INTERNAL_FAILURE);
            return;
        }

        char[] newMPW1 = MASTER_PASS_NEW1.getPassword();
        char[] newMPW2 = MASTER_PASS_NEW2.getPassword();
        if (!Utils.charArrayEquals(newMPW1, newMPW2)) {
            gui.popupError(ErrorMessages.PASSWORDS_DO_NOT_MATCH);
            return;
        }

        try {
            String result = coreService.changeMPW(newMPW1);
            gui.notifyUser(result);
        } catch (Exception ex) {
            gui.popupError(ErrorMessages.INTERNAL_FAILURE + ex.toString());
        }

        gui.setNextViewId(MainView.id);
        wipe(oldMPW);
        wipe(newMPW1);
        wipe(newMPW2);
        MASTER_PASS_NEW1.setText("");
        MASTER_PASS_NEW2.setText("");
        MASTER_PASS_OLD.setText("");
    }

    void cancelMasterPasswordChange() {
        gui.changeView(MainView.id);
        MASTER_PASS_NEW1.setText("");
        MASTER_PASS_NEW2.setText("");
        MASTER_PASS_OLD.setText("");
    }

    @Override
    public void performAction(String id) {
        switch (id) {
            case "Change":performMasterPasswordChange();break;
            case "Cancel":cancelMasterPasswordChange();break;
            default:break;
        }
    }

    @Override
    public String getId() {
        return id;
    }
}
