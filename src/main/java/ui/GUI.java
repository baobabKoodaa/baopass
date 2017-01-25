package ui;

import app.Main;
import crypto.EntropyCollector;
import app.BaoPassCore;
import ui.Listeners.ClickListener;
import ui.Listeners.EntropyListener;
import ui.Views.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class GUI {

    /* Dependencies. */
    BaoPassCore baoPassCore;
    EntropyCollector entropyCollector;
    EntropyListener inputEntropyListener;

    /* Properties. */
    private JFrame frame;
    private JPanel cardLayoutViewHolder;
    private HashMap<String, View> viewMapper;
    private CardLayout cardLayout;
    private Dimension dimension;
    public String currentViewId = FirstLaunchView.id;
    public String nextViewId = FirstLaunchView.id;
    public Font regularFont;
    public Font monospaceFont;
    public MenuContainer menu;

    public GUI(BaoPassCore baoPassCore, EntropyCollector entropyCollector, String initialViewId) throws Exception {
        this.baoPassCore = baoPassCore;
        this.entropyCollector = entropyCollector;
        this.inputEntropyListener = new EntropyListener(entropyCollector);
        initFonts();
        initFrame();

        /* Create GUI contents */
        setUpView(new MainView        (this, baoPassCore));
        setUpView(new FirstLaunchView (this, baoPassCore));
        setUpView(new NewKeyView      (this, baoPassCore));
        setUpView(new ChangeMPWView   (this, baoPassCore));
        setUpView(new NotificationView(this, baoPassCore));
        createMenu();

        packFrame(initialViewId);
    }

    private void initFonts() {
        loadFontFromFile("tahoma.ttf");
        loadFontFromFile("SourceCodePro-Medium.ttf");
        regularFont = new Font("Tahoma", Font.PLAIN, 12);
        monospaceFont = new Font("Source Code Pro Medium", Font.TRUETYPE_FONT, 16);
    }

    private void loadFontFromFile(String fontPath) {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            InputStream is = Main.class.getClassLoader().getResourceAsStream(fontPath);
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, is));
        } catch (Exception ex) {
            /* If we fail at loading a font from file, the user may
             * have the font installed anyway. If they don't, we
             * silently fallback to system's default font. */
        }
    }

    /** Actions to take at the beginning of constructing the GUI. */
    private void initFrame() {
        entropyCollector.collect(System.nanoTime());
        dimension = new Dimension(300, 145);
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
        SwingUtilities.invokeLater(hackToImproveGUIResponsiveness);
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

    private void createMenu() {
        View notificationView = viewMapper.get(NotificationView.id);
        MainView mainView = (MainView) viewMapper.get(MainView.id);
        menu = new MenuContainer(this, baoPassCore, notificationView, mainView);
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

    public void notifyUser(String text) {
        View notificationView = viewMapper.get(NotificationView.id);
        notificationView.performAction(text);
        changeView(NotificationView.id);
    }

    public void nextView() {
        changeView(nextViewId);
    }

    public void setNextViewId(String id) {
        nextViewId = id;
    }

    public void changeView(String desiredViewId) {
        boolean ifMainView = (desiredViewId.equals(MainView.id));
        menu.switchKey.setEnabled(ifMainView);
        menu.changeMPW.setEnabled(ifMainView);
        if (!currentViewId.equals(desiredViewId)) {
            /* Default assumption is to set nextView as the view where user was.
               Can be set to something else after calling this method. */
            nextViewId = currentViewId;
            /* We don't do this when currentView and desiredView are the same,
               because the user could get stuck inside the "About" view.
               That's why this is inside the if block. */
        }
        currentViewId = desiredViewId;
        cardLayout.show(cardLayoutViewHolder, desiredViewId);
    }

    private void loadAppIcon() {
        try {
            java.util.List<Image> list = new ArrayList<>();
            if (isWindows()) {
                /* On Windows 25x25 looks smoothest in typical use cases,
                 * but JRE won't choose it if other sizes are available. */
                addIconToList("B-25.png", list);
            }
            else {
                addIconToList("B-25.png", list);
                addIconToList("B-26.png", list);
                addIconToList("B-32.png", list);
                addIconToList("B-50.png", list);
                addIconToList("B-52.png", list);
                addIconToList("B-64.png", list);
            }
            frame.setIconImages(list);
        } catch (Exception ex) {
            /* Fallback to default icon. */
        }
    }

    private void addIconToList(String iconPath, java.util.List<Image> list) throws IOException {
        Image icon = ImageIO.read(Main.class.getClassLoader().getResourceAsStream(iconPath));
        list.add(icon);
    }

    public void popupError(String msg) {
        JOptionPane.showMessageDialog(frame, msg);
    }

    public static util.Point getPoint(MouseEvent e) {
        return new util.Point(e.getY(), e.getX());
    }

    public JFrame getFrame() {
        return frame;
    }

    public static String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isUnix() {
        return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));
    }

    public static boolean isWindows() {
        return (OS.contains("win"));
    }

    /** The very first time decryption is called is a bit slow (is Java lazy-loading the keyfile?)
     *  This dummy call exists to burn that first-time slowness. */
    public Runnable hackToImproveGUIResponsiveness = new Runnable() {
        @Override
        public void run() {
            try {
                baoPassCore.decryptMasterKey(new char[0]);
            } catch (Exception ex) {
                /* Expected decryption to fail due to invalid password. */
            }
        }
    };
}
