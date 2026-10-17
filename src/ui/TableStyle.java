package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;

public final class TableStyle {

    private TableStyle() { }

    public static void apply(JTable t) {
        t.setRowHeight(32);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setDefaultRenderer(new HeaderRenderer());
        t.getTableHeader().setPreferredSize(new Dimension(0, 34));
        CellRenderer cell = new CellRenderer();
        for (int i = 0; i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setCellRenderer(cell);
        }
        java.awt.Container p = t.getParent();                
        if (p != null && p.getParent() instanceof JScrollPane) {
            JScrollPane sp = (JScrollPane) p.getParent();
            sp.setBorder(BorderFactory.createEmptyBorder());
            sp.setViewportBorder(BorderFactory.createEmptyBorder());
            sp.getViewport().setBackground(Theme.CARD);
        }
    }

    private static class HeaderRenderer extends DefaultTableCellRenderer {
        HeaderRenderer() {
            setOpaque(true);
            setHorizontalAlignment(LEFT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, false, false, row, col);
            setBackground(Theme.PRIMARY_DARK);
            setForeground(Color.WHITE);
            setFont(Theme.BOLD);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            return this;
        }
    }


    private static class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, false, row, col);
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            setFont(Theme.BODY);
            setHorizontalAlignment(LEFT);
            if (sel) {
                setBackground(Theme.PRIMARY);
                setForeground(Color.WHITE);
            } else {
                setBackground(row % 2 == 0 ? Theme.CARD : Theme.ROW_ALT);
                setForeground(Theme.TEXT);
                String s = v == null ? "" : v.toString();
                if ("Active".equals(s)) {
                    setFont(Theme.BOLD);
                    setForeground(Theme.SUCCESS);
                } else if ("Inactive".equals(s)) {
                    setFont(Theme.BOLD);
                    setForeground(Theme.DANGER);
                } else if ("Admin".equals(s)) {
                    setFont(Theme.BOLD);
                    setForeground(Theme.PRIMARY_DARK);
                }
            }
            return this;
        }
    }
}
