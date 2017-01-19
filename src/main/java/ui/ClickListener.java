package ui;

import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/** Separate instance of ClickListener for each clickable object. */
public class ClickListener implements MouseListener, ActionListener, MenuListener {

    GUI gui;
    int id;

    public ClickListener(GUI gui, int id) {
        this.gui = gui;
        this.id = id;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        gui.userClicked(id);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        /* For checkboxes and menu items, etc. */
        gui.userClicked(id);
    }

    @Override
    public void menuSelected(MenuEvent e) {
        System.out.println(e.toString());
        gui.userClicked(id);
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void menuDeselected(MenuEvent e) {

    }

    @Override
    public void menuCanceled(MenuEvent e) {

    }
}
