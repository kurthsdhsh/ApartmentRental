package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

/**
 * A small click-to-pick calendar for a date text field (plain Swing/AWT, no extra library).
 *
 * The field becomes read-only: the user clicks it (or the calendar icon) and picks a day.
 * The text the field holds is always yyyy-MM-dd, so existing code that reads it with
 * Validator.parseDate(...) keeps working.
 */
public final class DatePicker {

    private DatePicker() { }

    /**
     * @param field    the text field that will hold the date
     * @param minDate  returns the earliest day that may be picked, or null for no limit
     *                 (the supplier itself may be null)
     * @param onPicked runs after a date was picked or cleared (may be null)
     */
    public static void attach(JTextField field, Supplier<LocalDate> minDate, Runnable onPicked) {
        field.setEditable(false);
        field.setBackground(Color.WHITE);
        field.setDisabledTextColor(new Color(110, 95, 105));
        field.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        field.setToolTipText("Click to select a date");
        field.setBorder(BorderFactory.createCompoundBorder(field.getBorder(), new PickerBorder()));
        // a locked field (enabled = false) looks greyed out; an active one looks white
        field.addPropertyChangeListener("enabled", e ->
                field.setBackground(field.isEnabled() ? Color.WHITE : new Color(243, 238, 241)));
        field.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (field.isEnabled()) open(field, minDate, onPicked);
            }
        });
    }

    private static void open(JTextField field, Supplier<LocalDate> minDate, Runnable onPicked) {
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(Theme.PRIMARY, 1));
        popup.add(new CalendarPanel(field, minDate, onPicked, () -> popup.setVisible(false)));
        popup.show(field, 0, field.getHeight());
    }

    private static LocalDate parse(String s) {
        try {
            return LocalDate.parse(s.trim());
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    // ------------------------------------------------------------------ the calendar
    static final class CalendarPanel extends JPanel {
        private static final String[] DAYS = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};

        private final JTextField field;
        private final Supplier<LocalDate> minDate;
        private final Runnable onPicked, close;
        private final JLabel lblMonth = new JLabel("", SwingConstants.CENTER);
        private final JPanel grid = new JPanel(new GridLayout(0, 7, 2, 2));
        private YearMonth shown;

        CalendarPanel(JTextField field, Supplier<LocalDate> minDate, Runnable onPicked, Runnable close) {
            this.field = field;
            this.minDate = minDate;
            this.onPicked = onPicked;
            this.close = close;

            setLayout(new BorderLayout(0, 6));
            setBackground(Theme.CARD);
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            LocalDate current = parse(field.getText());
            LocalDate min = min();
            shown = YearMonth.from(current != null ? current : (min != null ? min : LocalDate.now()));

            add(header(), BorderLayout.NORTH);

            JPanel weekdays = new JPanel(new GridLayout(1, 7, 2, 2));
            weekdays.setOpaque(false);
            for (String d : DAYS) {
                JLabel l = new JLabel(d, SwingConstants.CENTER);
                l.setFont(Theme.BOLD);
                l.setForeground(Theme.PRIMARY_DARK);
                weekdays.add(l);
            }
            grid.setOpaque(false);
            JPanel center = new JPanel(new BorderLayout(0, 4));
            center.setOpaque(false);
            center.add(weekdays, BorderLayout.NORTH);
            center.add(grid, BorderLayout.CENTER);
            add(center, BorderLayout.CENTER);
            add(footer(), BorderLayout.SOUTH);
            rebuild();
        }

        private LocalDate min() {
            return minDate == null ? null : minDate.get();
        }

        private JPanel header() {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            left.setOpaque(false);
            left.add(nav("\u00AB", -12));
            left.add(nav("\u2039", -1));
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            right.setOpaque(false);
            right.add(nav("\u203A", 1));
            right.add(nav("\u00BB", 12));
            lblMonth.setFont(Theme.BOLD);
            lblMonth.setForeground(Theme.PRIMARY_DARK);
            p.add(left, BorderLayout.WEST);
            p.add(lblMonth, BorderLayout.CENTER);
            p.add(right, BorderLayout.EAST);
            return p;
        }

        private JButton nav(String text, int months) {
            JButton b = flat(text);
            b.setPreferredSize(new Dimension(28, 26));
            b.setFont(Theme.BOLD);
            b.addActionListener(e -> {
                shown = shown.plusMonths(months);
                rebuild();
            });
            return b;
        }

        private JPanel footer() {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
            p.setOpaque(false);
            LocalDate min = min();
            JButton today = flat("Today");
            today.setEnabled(min == null || !LocalDate.now().isBefore(min));
            today.addActionListener(e -> pick(LocalDate.now()));
            JButton clear = flat("Clear");
            clear.addActionListener(e -> {
                field.setText("");
                finish();
            });
            p.add(today);
            p.add(clear);
            return p;
        }

        private static JButton flat(String text) {
            JButton b = new JButton(text);
            b.setFont(Theme.BODY);
            b.setForeground(Theme.PRIMARY_DARK);
            b.setBorderPainted(false);
            b.setContentAreaFilled(false);
            b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            b.setMargin(new Insets(2, 4, 2, 4));     // the default margin is too wide and cuts the text to "..."
            return b;
        }

        private void rebuild() {
            grid.removeAll();
            lblMonth.setText(shown.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + shown.getYear());
            LocalDate selected = parse(field.getText());
            LocalDate min = min();
            LocalDate today = LocalDate.now();

            int offset = shown.atDay(1).getDayOfWeek().getValue() % 7;   // Sunday = 0
            for (int i = 0; i < offset; i++) grid.add(new JLabel());
            for (int d = 1; d <= shown.lengthOfMonth(); d++) {
                LocalDate date = shown.atDay(d);
                grid.add(new DayButton(date, date.equals(selected), date.equals(today),
                        min == null || !date.isBefore(min)));
            }
            for (int i = offset + shown.lengthOfMonth(); i < 42; i++) grid.add(new JLabel());   // constant height
            grid.revalidate();
            grid.repaint();
        }

        private void pick(LocalDate date) {
            field.setText(date.toString());      // always yyyy-MM-dd
            finish();
        }

        private void finish() {
            close.run();
            if (onPicked != null) onPicked.run();
        }

        private final class DayButton extends JButton {
            private final boolean selected, isToday;

            DayButton(LocalDate date, boolean selected, boolean isToday, boolean allowed) {
                super(String.valueOf(date.getDayOfMonth()));
                this.selected = selected;
                this.isToday = isToday;
                setPreferredSize(new Dimension(34, 28));
                setMargin(new Insets(0, 0, 0, 0));       // otherwise the day number is cut to "..."
                setFont(Theme.BODY);
                setBorderPainted(false);
                setContentAreaFilled(false);
                setFocusPainted(false);
                setOpaque(false);
                setRolloverEnabled(true);
                setEnabled(allowed);
                setForeground(selected ? Color.WHITE : (allowed ? Theme.TEXT : new Color(195, 185, 191)));
                if (allowed) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    addActionListener(e -> pick(date));
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (selected) {
                    g2.setColor(Theme.PRIMARY);
                    g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10);
                } else if (isEnabled() && getModel().isRollover()) {
                    g2.setColor(new Color(245, 225, 237));
                    g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 10, 10);
                }
                if (isToday && !selected) {
                    g2.setColor(Theme.PRIMARY);
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        }
    }

    // ------------------------------------------------------------------ icon + hint inside the field
    private static final class PickerBorder implements Border {
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 0, 26);       // keeps the text clear of the calendar icon
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.isEnabled() ? Theme.PRIMARY_DARK : new Color(175, 165, 171));
            int ix = x + w - 22, iy = y + (h - 15) / 2;
            g2.drawRoundRect(ix, iy + 2, 14, 12, 3, 3);                 // calendar body
            g2.fillRect(ix, iy + 2, 15, 4);                             // top bar
            g2.fillRect(ix + 3, iy, 2, 4);                              // rings
            g2.fillRect(ix + 9, iy, 2, 4);
            g2.fillRect(ix + 3, iy + 8, 2, 2);                          // days
            g2.fillRect(ix + 7, iy + 8, 2, 2);
            g2.fillRect(ix + 11, iy + 8, 2, 2);
            if (c instanceof JTextField && ((JTextField) c).getText().isEmpty()) {
                g2.setFont(c.getFont().deriveFont(Font.ITALIC));
                g2.setColor(new Color(160, 145, 155));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("Select date", x + 2, y + (h + fm.getAscent() - fm.getDescent()) / 2);
            }
            g2.dispose();
        }
    }
}
