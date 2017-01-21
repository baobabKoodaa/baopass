package ui;

import ui.Views.View;

import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/** Separate instance of ClickListener for each clickable object. */
public class ClickListener implements MouseListener, ActionListener {

    View view;
    String id;

    public ClickListener(View view, String id) {
        this.view = view;
        this.id = id;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        view.performAction(id);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        view.performAction(id);
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
