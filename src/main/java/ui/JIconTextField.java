package ui;

import app.Main;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;

/**
 * From https://gmigdos.wordpress.com/2010/03/30/java-a-custom-jtextfield-for-searching/
 * @author Georgios Migdos <cyberpython@gmail.com>
 */
public class JIconTextField extends JTextField {

    private Icon icon;
    private Insets dummyInsets;

    public JIconTextField(int columns) throws IOException {
        super(columns);
        this.icon = null;

        Border border = UIManager.getBorder("TextField.border");
        JTextField dummy = new JTextField();
        this.dummyInsets = border.getBorderInsets(dummy);

        //String path = "find-16x16.png";
        //InputStream is = Main.class.getClassLoader().getResourceAsStream(path);
        //setIcon(new ImageIcon(ImageIO.read(is)));
        setMargin(new Insets(2, 25, 2, 2));
    }

    public void setIcon(Icon icon){
        this.icon = icon;
    }

    public Icon getIcon(){
        return this.icon;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int textX = 2;

        if(this.icon!=null){
            int iconWidth = icon.getIconWidth();
            int iconHeight = icon.getIconHeight();
            int x = dummyInsets.left + 5;//this is our icon's x
            textX = x+iconWidth+2; //this is the x where text should start
            int y = (this.getHeight() - iconHeight)/2;
            icon.paintIcon(this, g, x, y);
        }


    }

}