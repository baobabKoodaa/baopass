package ui.Views;

import app.BaoPassCore;
import ui.GUI;
import util.Notifications;

import javax.swing.*;
import java.awt.*;

public class NotificationView extends View {

    public static final String id = "NOTIFICATION_VIEW";

    /* Dependencies. */
    GUI gui;
    BaoPassCore baoPassCore;

    /* Properties. */
    private JLabel notificationText;
    private JButton buttonOkNotification;

    public NotificationView(GUI gui, BaoPassCore baoPassCore) {
        this.gui = gui;
        this.baoPassCore = baoPassCore;

        setLayout(new GridBagLayout());
        JPanel inc2 = new JPanel(new BorderLayout());

        JPanel textPane = new JPanel();
        notificationText = new JLabel("Placeholder.");
        notificationText.setHorizontalAlignment(JLabel.CENTER);
        notificationText.setFont(gui.regularFont);
        textPane.add(notificationText);

        JPanel inception = new JPanel();
        JPanel buttonPane = new JPanel();
        buttonOkNotification = gui.createButton("OK", this);
        buttonPane.add(buttonOkNotification);
        inception.add(buttonPane);

        inc2.add(textPane, BorderLayout.PAGE_START);
        inc2.add(inception, BorderLayout.CENTER);
        add(inc2);
    }

    private void userClickedAboutMenu() {
        gui.notifyUser(Notifications.ABOUT);
        SwingUtilities.invokeLater(gui.menu.deselectAboutMenu);
    }

    private void displayNotification(String text) {
        notificationText.setText(text);
    }

    @Override
    public void performAction(String action) {
        if ("OK".equals(action)) {
            gui.nextView();
        } else if (gui.menu.ABOUT_ID.equals(action)) {
            userClickedAboutMenu();
        } else {
            displayNotification(action);
        }
    }

    @Override
    public String getId() {
        return id;
    }
}
