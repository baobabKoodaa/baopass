package ui;

import crypto.EntropyCollector;

import java.awt.event.*;

import static com.sun.java.accessibility.util.AWTEventMonitor.addKeyListener;

public class Listener implements ActionListener, MouseListener, MouseMotionListener, KeyListener {

    EntropyCollector entropyCollector;
    private GUI gui;

    public Listener(GUI gui, EntropyCollector entropyCollector) {
        this.gui = gui;
        this.entropyCollector = entropyCollector;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        System.out.println("Key typed: " + e.getKeyChar());
    }

    @Override
    public void keyPressed(KeyEvent e) {
        entropyCollector.feed(System.nanoTime());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        entropyCollector.feed(System.nanoTime());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Action performed: " + e.paramString());
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Point point = getPoint(e);
        gui.userClickedOn(point);
        entropyCollector.feed(point);
        entropyCollector.feed(System.nanoTime());
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point point = getPoint(e);
        entropyCollector.feed(point);
        entropyCollector.feed(System.nanoTime());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        Point point = getPoint(e);
        entropyCollector.feed(point);
        entropyCollector.feed(System.nanoTime());
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
        entropyCollector.feed(point);
        entropyCollector.feed(System.nanoTime());
    }

    public Point getPoint(MouseEvent e) {
        return new Point(e.getY(), e.getX());
    }


    @Override
    public void mouseDragged(MouseEvent e) {

    }


}
