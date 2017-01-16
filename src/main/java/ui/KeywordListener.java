package ui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** When keyword field is updated, this listener is triggered to generate site pass. */
public class KeywordListener implements DocumentListener {

    GUI gui;

    public KeywordListener(GUI gui) {
        this.gui = gui;
    }

    private void askGuiToGenerateSitePass() {
        try {
            gui.generateSitePass();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        askGuiToGenerateSitePass();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        askGuiToGenerateSitePass();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        askGuiToGenerateSitePass();
    }
}
