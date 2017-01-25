package ui.Listeners;

import ui.Views.View;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class TextListener implements DocumentListener {

    View view;
    String id;

    public TextListener(View view, String id) {
        this.view = view;
        this.id = id;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        view.performAction(id);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        view.performAction(id);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        view.performAction(id);
    }
}
