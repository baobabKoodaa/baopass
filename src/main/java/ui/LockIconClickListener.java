package ui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static ui.GUI.getPoint;

public class LockIconClickListener implements MouseListener {

    GUI gui;

    public LockIconClickListener(GUI gui) {
        this.gui = gui;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        System.out.println("WOOT");
        gui.lockClicked();
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
