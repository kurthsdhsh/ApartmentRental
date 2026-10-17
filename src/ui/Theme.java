package ui;

import java.awt.Color;
import java.awt.Font;


public final class Theme {

    private Theme() { }  


    public static final Color PRIMARY      = new Color(190, 92, 142);   // login buttons
    public static final Color PRIMARY_DARK = new Color(140, 40, 100);   // login title
    public static final Color SIDEBAR      = new Color(115, 28, 82);    // dark plum sidebar
    public static final Color LIGHT        = new Color(214, 150, 184);  // soft pink for small text on dark
    public static final Color PAGE_BG      = new Color(249, 241, 246);  // very light pink page
    public static final Color ROW_ALT      = new Color(252, 247, 250);  // every second table row
    public static final Color CARD         = Color.WHITE;
    public static final Color TEXT         = new Color(60, 40, 55);
    public static final Color MUTED        = new Color(130, 110, 125);
    public static final Color SUCCESS      = new Color(40, 140, 80);
    public static final Color WARNING      = new Color(205, 120, 30);
    public static final Color DANGER       = new Color(190, 50, 50);


    public static final Font TITLE  = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font BRAND  = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font H2     = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BOLD   = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font SMALL  = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font NAV    = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font NUMBER = new Font("Segoe UI", Font.BOLD, 30);
}