package ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicButtonUI;


public class PillButtonUI extends BasicButtonUI {

    private final Color fill;

    private PillButtonUI(Color fill) {
        this.fill = fill;
    }

    public static void apply(AbstractButton b, Color fill, Color textColor) {
        b.setUI(new PillButtonUI(fill));
        b.setForeground(textColor);
        b.setOpaque(false);              // we paint the background ourselves
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setRolloverEnabled(true);
        b.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        AbstractButton b = (AbstractButton) c;
        Color color = fill;
        if (!b.isEnabled())                  color = new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), 110);  // faded
        else if (b.getModel().isPressed())   color = fill.darker();
        else if (b.getModel().isRollover())  color = fill.brighter();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), c.getHeight(), c.getHeight());
        g2.dispose();
        super.paint(g, c);               // draws the text on top
    }

    @Override
    protected void paintText(Graphics g, AbstractButton b, java.awt.Rectangle textRect, String text) {
        if (b.isEnabled()) {
            super.paintText(g, b, textRect, text);
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(b.getFont());
        Color fg = b.getForeground();
        g2.setColor(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 85));      // 85 = mostly transparent
        g2.drawString(text, textRect.x, textRect.y + g2.getFontMetrics().getAscent());
        g2.dispose();
    }
}