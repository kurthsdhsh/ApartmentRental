package util;

import java.awt.*;
import java.math.BigDecimal;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.ComboItem;

/** Small helpers so every module frame builds its screen the same way. Added by Member 1 (new class, nothing renamed). */
public class FormUtil {

    public static JPanel form(String title) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Style.BG);
        p.setBorder(BorderFactory.createTitledBorder(title));
        return p;
    }

    public static void addRow(JPanel p, int row, int col, String label, Component field) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;
        gc.gridx = col; gc.gridy = row;
        JLabel l = new JLabel(label);
        l.setFont(Style.LABEL);
        p.add(l, gc);
        gc.gridx = col + 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        p.add(field, gc);
    }

    /** Puts the action buttons in a column at the right of the form. */
    public static void addButtons(JPanel form, int rows, JButton... buttons) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 4; gc.gridy = 0; gc.gridheight = rows; gc.insets = new Insets(4, 12, 4, 4);
        gc.anchor = GridBagConstraints.NORTH;
        JPanel col = new JPanel(new GridLayout(buttons.length, 1, 0, 6));
        col.setOpaque(false);
        for (JButton b : buttons) {
            Style.styleButton(b);
            if ("Delete".equals(b.getText())) b.setBackground(Style.DANGER);
            col.add(b);
        }
        form.add(col, gc);
    }

    public static JPanel searchBar(String label, JTextField txt, JComboBox<String> filter, JButton btn) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.setOpaque(false);
        p.add(new JLabel(label));
        p.add(txt);
        if (filter != null) { p.add(new JLabel("Status:")); p.add(filter); }
        Style.styleButton(btn);
        p.add(btn);
        return p;
    }

    public static DefaultTableModel tableModel(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    public static JTable table(DefaultTableModel m) {
        JTable t = new JTable(m);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setRowHeight(24);
        return t;
    }

    /** Standard screen: title, form on top, search bar + table underneath. */
    public static void layout(JFrame f, String title, JPanel form, JPanel searchBar, JTable table,
                              JLabel info, int w, int h) {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        root.setBackground(Style.BG);
        JLabel t = new JLabel(title);
        t.setFont(Style.TITLE);
        t.setForeground(Style.PRIMARY);
        root.add(t, BorderLayout.NORTH);
        JPanel tp = new JPanel(new BorderLayout(4, 4));
        tp.setOpaque(false);
        tp.add(searchBar, BorderLayout.NORTH);
        tp.add(new JScrollPane(table), BorderLayout.CENTER);
        tp.add(info, BorderLayout.SOUTH);
        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setOpaque(false);
        center.add(form, BorderLayout.NORTH);
        center.add(tp, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);
        f.setContentPane(root);
        f.setSize(w, h);
        f.setLocationRelativeTo(null);
    }

    public static void warn(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Validation", JOptionPane.WARNING_MESSAGE);
    }

    public static void info(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(Component parent, String msg, String title) {
        return JOptionPane.showConfirmDialog(parent, msg, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static void selectCombo(JComboBox<ComboItem> cmb, int id) {
        for (int i = 0; i < cmb.getItemCount(); i++) {
            if (cmb.getItemAt(i).getId() == id) { cmb.setSelectedIndex(i); return; }
        }
        if (cmb.getItemCount() > 0) cmb.setSelectedIndex(0);
    }

    public static String money(BigDecimal v) { return v == null ? "" : String.format("%,.2f", v); }
}
