package util;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;

/** One look for all screens. */
public class Style {
    public static final Color PRIMARY = new Color(15, 76, 92), BG = new Color(245, 248, 250),
            SUCCESS = new Color(40, 140, 80), DANGER = new Color(190, 50, 50);
    public static final Font TITLE = new Font("Segoe UI", Font.BOLD, 20),
            LABEL = new Font("Segoe UI", Font.PLAIN, 13);

    public static void styleButton(JButton b) {
        b.setBackground(PRIMARY); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setFont(LABEL);
    }
}
