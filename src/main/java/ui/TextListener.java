package ui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** When keyword field is updated, this listener is triggered to generate site pass. */
public class TextListener implements DocumentListener {

    GUI gui;
    int id;

    public TextListener(GUI gui, int id) {
        this.gui = gui;
        this.id = id;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        gui.textFieldChanged(id);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        gui.textFieldChanged(id);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        gui.textFieldChanged(id);
    }
}
