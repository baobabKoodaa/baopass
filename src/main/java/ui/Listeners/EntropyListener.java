package ui.Listeners;

import crypto.EntropyCollector;
import util.Point;

import java.awt.event.*;

import static ui.GUI.getPoint;

/** Listener to pass entropy from user actions onto EntropyCollector. */
public class EntropyListener implements ActionListener, MouseListener, MouseMotionListener, KeyListener {

    EntropyCollector entropyCollector;

    public EntropyListener(EntropyCollector entropyCollector) {
        this.entropyCollector = entropyCollector;
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
    public void mouseClicked(MouseEvent e) {
        Point point = getPoint(e);
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
    public void mouseMoved(MouseEvent e) {
        Point point = getPoint(e);
        entropyCollector.collect(point);
        entropyCollector.collect(System.nanoTime());
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }
}
