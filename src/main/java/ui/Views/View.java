package ui.Views;

import javax.swing.*;

/** Convenience class to facilitate switching between views and responding to user actions. */
public abstract class View extends JPanel {
    public abstract String getId();
    public abstract void performAction(String id);
}
