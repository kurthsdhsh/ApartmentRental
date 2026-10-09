package util;

import java.sql.*;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/** Turns any ResultSet into a non-editable table model (used by Reports). */
public class TableHelper {
    public static DefaultTableModel toModel(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int n = md.getColumnCount();
        Vector<String> cols = new Vector<>();
        for (int i = 1; i <= n; i++) cols.add(md.getColumnLabel(i));
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        while (rs.next()) {
            Object[] row = new Object[n];
            for (int i = 1; i <= n; i++) row[i - 1] = rs.getObject(i);
            m.addRow(row);
        }
        return m;
    }
}
