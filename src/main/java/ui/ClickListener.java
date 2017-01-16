package ui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/** Separate instance of ClickListener for each clickable object. */
public class ClickListener implements MouseListener {

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
}
