package ui;

import javax.swing.*;
import java.awt.*;

public class Button extends JButton {

    public Button(String text) {
        super(text);
        super.setBackground(new Color(142, 68, 173));
        super.setForeground(Color.WHITE);
        super.setFocusPainted(false);
        super.setFont(new Font("Tahoma", Font.BOLD, 12));
    }
}
