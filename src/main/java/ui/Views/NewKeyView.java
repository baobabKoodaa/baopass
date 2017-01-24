package ui.Views;

import app.BaoPassCore;
import ui.GUI;

import javax.swing.*;
import java.awt.*;

import static crypto.Utils.charArrayEquals;
import static crypto.Utils.wipe;

/** View where user is asked to choose a password to encrypt a newly generated keyfile. */
public class NewKeyView extends View {

    public static final String id = "NEW_KEY_VIEW_ID";
    private static final String TEXT_BUTTON_ENCRYPT = "Encrypt";

    private GUI gui;
    private BaoPassCore baoPassCore;

    JPasswordField MASTER_PASS_1;
    JPasswordField MASTER_PASS_2;
    JButton buttonEncryptNewKey;

    public NewKeyView(GUI gui, BaoPassCore baoPassCore) {
        this.gui = gui;
        this.baoPassCore = baoPassCore;
        setLayout(new GridBagLayout());
        JPanel contents = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 3, 5, 3);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.ipady = 5;
        gbc.anchor = GridBagConstraints.EAST;

        /* Text labels. */
        JLabel labelMPW1 = new JLabel("Choose a password: ");
        labelMPW1.setFont(gui.regularFont);
        contents.add(labelMPW1, gbc);
        gbc.gridy++;
        JLabel labelMPW2 = new JLabel("Retype password:");
        labelMPW2.setFont(gui.regularFont);
        contents.add(labelMPW2, gbc);
        gbc.gridx++;
        gbc.gridy = 0;

        /* Create master password fields. */
        MASTER_PASS_1 = new JPasswordField(11);
        contents.add(MASTER_PASS_1, gbc);
        gbc.gridy++;
        MASTER_PASS_2 = new JPasswordField(11);
        contents.add(MASTER_PASS_2, gbc);
        gbc.gridy++;
        gbc.gridy++;

        buttonEncryptNewKey = gui.createButton(TEXT_BUTTON_ENCRYPT, this);
        contents.add(buttonEncryptNewKey, gbc);

        add(contents);
    }

    public void performAction(String id) {
        if (TEXT_BUTTON_ENCRYPT.equals(id)) {
            char[] pw1 = MASTER_PASS_1.getPassword();
            char[] pw2 = MASTER_PASS_2.getPassword();
            if (!charArrayEquals(pw1, pw2)) {
                System.out.println("not equals");
                // TODO: complain
                return;
            }
            if (baoPassCore.encryptMasterKey(pw1)) {
                gui.notifyUser("<html>Your keyfile has been encrypted<br>" +
                        "succesfully and saved under filename<br>" +
                        "key1.baopass<br>");
                //TODO:wipePasswordFields();
                gui.nextViewId = MainView.id;
            } else {
                // TODO: print error
            }
            wipe(pw1);
            wipe(pw2);
            MASTER_PASS_1.setText("");
            MASTER_PASS_2.setText("");
        }
    }

    @Override
    public String getId() {
        return id;
    }
}
