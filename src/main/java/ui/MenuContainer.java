package ui;

import app.CoreService;
import ui.Listeners.ClickListener;
import ui.Views.ChangeMPWView;
import ui.Views.FirstLaunchView;
import ui.Views.MainView;
import ui.Views.View;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** Top menu with options etc. */
public class MenuContainer extends JMenuBar {

    public static final String SWITCH_ACTIVE_KEYFILE = "Switch active keyfile";
    public static final String CHANGE_MASTER_PASSWORD = "Change master password";
    public static final String HIDE_SITE_PASS = "Hide site pass";

    /* Element Ids. */
    public static final String ABOUT_ID = "ABOUT";

    /* Under "File" menu */
    public JMenuItem switchKey;
    public JMenuItem changeMPW;
    /* Under "Options" menu */
    public JCheckBoxMenuItem hideSitePass;
    /* About */
    public JMenu aboutMenu;

    /* Dependencies. */
    GUI gui;
    CoreService baoPass;
    View notificationView;
    MainView mainView;

    public MenuContainer(GUI gui, CoreService baoPass, View notificationView, MainView mainView) {
        super();
        this.gui = gui;
        this.baoPass = baoPass;
        this.notificationView = notificationView;
        this.mainView = mainView;

        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(gui.regularFont);
        switchKey = createMenuItem(SWITCH_ACTIVE_KEYFILE);
        fileMenu.add(switchKey);
        changeMPW = createMenuItem(CHANGE_MASTER_PASSWORD);
        fileMenu.add(changeMPW);

        JMenu optionsMenu = new JMenu("Options");
        optionsMenu.setFont(gui.regularFont);

        hideSitePass = new JCheckBoxMenuItem(HIDE_SITE_PASS);
        hideSitePass.setFont(gui.regularFont);
        hideSitePass.addActionListener(new MenuActionListener(HIDE_SITE_PASS));
        optionsMenu.add(hideSitePass);

        aboutMenu = new JMenu("About");
        aboutMenu.setFont(gui.regularFont);
        aboutMenu.addMouseListener(new ClickListener(notificationView, ABOUT_ID));

        add(fileMenu);
        add(optionsMenu);
        add(aboutMenu);
    }

    private JMenuItem createMenuItem(String text) {
        JMenuItem out = new JMenuItem(text);
        out.setFont(gui.regularFont);
        out.addActionListener(new MenuActionListener(text));
        return out;
    }

    private void userToggledHiteSitePass() {
        try {
            mainView.generateSitePass();
            baoPass.setPreferenceHideSitePass(hideSitePass.getState());
        } catch (Exception ex) {
            /* Refresh failed. Do nothing. */
        }
    }

    private void userClickedChangeMasterPassword() {
        gui.notifyUser("<html>Changing the master password<br>" +
                "means your keyfile will be<br>" +
                "encrypted with a new password.<br>" +
                "Your site passwords won't change.<br>");
        gui.setNextViewId(ChangeMPWView.id);
        mainView.closeLock(true, true);
    }

    private void switchActiveKeyfile() {
        mainView.closeLock(true, true);
        gui.changeView(FirstLaunchView.id);
    }

    class MenuActionListener implements ActionListener {

        String id;

        public MenuActionListener(String id) {
            this.id = id;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            switch (id) {
                case SWITCH_ACTIVE_KEYFILE:switchActiveKeyfile();break;
                case CHANGE_MASTER_PASSWORD:userClickedChangeMasterPassword();break;
                case HIDE_SITE_PASS:userToggledHiteSitePass();break;
                default:break;
            }
        }
    }

    public Runnable deselectAboutMenu = new Runnable() {
        public void run() {
            aboutMenu.setSelected(false);
        }
    };

}
