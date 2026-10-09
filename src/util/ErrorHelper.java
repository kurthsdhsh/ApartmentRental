package util;

import java.awt.Component;
import java.sql.SQLException;
import javax.swing.JOptionPane;

/** One place that turns a SQLException into a friendly message. */
public class ErrorHelper {
    public static void show(Component parent, SQLException e) {
        String st = e.getSQLState(), msg;
        if ("23505".equals(st)) msg = "Duplicate record. That value already exists.";
        else if ("23503".equals(st)) msg = "This record is linked to other records, so the action is not allowed.";
        else if ("23513".equals(st)) msg = "A value is not allowed by the database rules.";
        else if (st != null && st.startsWith("08"))
            msg = "Cannot connect to the database. Is the Derby server running?";
        else msg = "Database error: " + e.getMessage();
        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
