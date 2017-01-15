package ui;

import crypto.EntropyCollector;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.*;

import static ui.GUI.getPoint;

public class EntropyListener implements ActionListener, MouseListener, MouseMotionListener, KeyListener, DocumentListener {

    EntropyCollector entropyCollector;
    GUI gui;

    public EntropyListener(GUI gui, EntropyCollector entropyCollector) {
        this.gui = gui;
        this.entropyCollector = entropyCollector;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        System.out.println("Key typed: " + e.getKeyChar());
    }

    @Override
    public void keyPressed(KeyEvent e) {
        entropyCollector.collect(System.nanoTime());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        entropyCollector.collect(System.nanoTime());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Action performed: " + e.paramString());
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Point point = getPoint(e);
        gui.userClickedOn(point);
        entropyCollector.collect(point);
        entropyCollector.collect(System.nanoTime());
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point point = getPoint(e);
        entropyCollector.collect(point);
        entropyCollector.collect(System.nanoTime());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        Point point = getPoint(e);
        entropyCollector.collect(point);
        entropyCollector.collect(System.nanoTime());
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Point point = getPoint(e);
        entropyCollector.collect(point);
        entropyCollector.collect(System.nanoTime());
    }



    @Override
    public void mouseDragged(MouseEvent e) {

    }


    @Override
    public void insertUpdate(DocumentEvent e) {
        System.out.println("updated");
        try {
            gui.generateSitePass();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        System.out.println("removed");
        try {
            gui.generateSitePass();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        System.out.println("changed");
    }
}
