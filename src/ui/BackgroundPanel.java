package ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class BackgroundPanel extends JPanel {

    private final Image image;

    /** @param resourcePath  */
    public BackgroundPanel(String resourcePath) {
        java.net.URL url = getClass().getResource(resourcePath);
        if (url == null) {
            System.err.println("BackgroundPanel: image not found: " + resourcePath);
            image = null;
        } else {
            image = new ImageIcon(url).getImage();
        }
        setBackground(Color.WHITE);   
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);      
        if (image != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            g2.dispose();
        }
    }
}