package ui;

import app.Main;
import crypto.EntropyCollector;
import app.BaoPass;
import crypto.Utils;
import ui.Views.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;

public class GUI {

    public String currentViewId = FirstLaunchView.id;
    public String nextViewId = FirstLaunchView.id;

    private JFrame frame;
    private JPanel cardLayoutViewHolder;
    private HashMap<String, View> viewMapper;

    private CardLayout cardLayout;
    private Dimension dimension;
    public Font regularFont;

    public BaoPass baoPass;
    EntropyCollector entropyCollector;
    EntropyListener inputEntropyListener;

    public MenuContainer menu;

    public GUI(BaoPass baoPass, EntropyCollector entropyCollector, String initialViewId) throws Exception {
        this.baoPass = baoPass;
        this.entropyCollector = entropyCollector;
        this.inputEntropyListener = new EntropyListener(entropyCollector);
        initFrame();

        /* Create GUI contents */
        setUpView(new MainView        (this, baoPass));
        setUpView(new FirstLaunchView (this, baoPass));
        setUpView(new NewKeyView      (this, baoPass));
        setUpView(new ChangeMPWView   (this, baoPass));
        setUpView(new NotificationView(this, baoPass));
        createMenu();

        packFrame(initialViewId);
    }

    /** Actions to take at the beginning of constructing the GUI. */
    private void initFrame() {
        entropyCollector.collect(System.nanoTime());
        dimension = new Dimension(300, 145);
        regularFont = new Font("Tahoma", Font.PLAIN, 12);
        frame = new JFrame("BaoPass");
        loadAppIcon();
        cardLayoutViewHolder = new JPanel();
        cardLayout = new CardLayout();
        cardLayoutViewHolder.setLayout(cardLayout);
        viewMapper = new HashMap<String, View>();
    }

    /** Actions to take at the end of constructing the GUI. */
    private void packFrame(String initialView) {
        changeView(initialView);
        frame.add(cardLayoutViewHolder);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        entropyCollector.collect(System.nanoTime());
    }

    /** Called for each view when constructing the GUI. */
    private void setUpView(View view) {
        view.setPreferredSize(dimension);
        cardLayoutViewHolder.add(view, view.getId());
        viewMapper.put(view.getId(), view);
        addEntropyListeners(view);
    }

    /** Collect entropy from user actions. */
    private void addEntropyListeners(JPanel view) {
        view.addMouseListener(inputEntropyListener);
        view.addMouseMotionListener(inputEntropyListener);
        view.addKeyListener(inputEntropyListener);
    }

    public void notifyUser(String text) {
        View notificationView = viewMapper.get(NotificationView.id);
        notificationView.performAction(text);
        changeView(notificationView);
    }

    private void createMenu() {
        View notificationView = viewMapper.get(NotificationView.id);
        MainView mainView = (MainView) viewMapper.get(MainView.id);
        menu = new MenuContainer(this, notificationView, mainView);
        frame.setJMenuBar(menu);
    }

    public JButton createButton(String text, View view) {
        JButton button = new JButton(text);
        button.setBackground(new Color(142, 68, 173));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Tahoma", Font.BOLD, 12));
        button.addMouseMotionListener(inputEntropyListener);
        button.addMouseListener(inputEntropyListener);
        ClickListener clickListener = new ClickListener(view, text);
        button.addMouseListener(clickListener);
        return button;
    }

    public void nextView() {
        changeView(nextViewId);
    }

    public void setNextViewId(String id) {
        nextViewId = id;
    }

    public void changeView(View view) {
        changeView(view.getId());
    }

    public void changeView(String desiredViewId) {
        boolean ifMainView = (desiredViewId.equals(MainView.id));
        menu.switchKey.setEnabled(ifMainView);
        menu.changeMPW.setEnabled(ifMainView);
        if (!currentViewId.equals(desiredViewId)) {
            /* Default assumption is to set nextView as the view where user was.
               Sometimes overridden after calling this method. */
            nextViewId = currentViewId;
            /* We don't do this when currentView and desiredView are the same,
               because the user could get stuck inside the "About" view. */
        }
        currentViewId = desiredViewId;
        cardLayout.show(cardLayoutViewHolder, desiredViewId);
    }

    /** Try to load icon for the app, use default icon if something goes wrong. */
    private void loadAppIcon() {
        try {
            Image small = ImageIO.read(Main.class.getClassLoader().getResourceAsStream("B-25.png"));
            frame.setIconImage(small);
        } catch (Exception e) {
            /* Use default icon. */
        }
    }

    public static Point getPoint(MouseEvent e) {
        return new Point(e.getY(), e.getX());
    }

    public JFrame getFrame() {
        return frame;
    }
}
