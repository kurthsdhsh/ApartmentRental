package database;

import java.sql.*;

/** Right-click, Run File. Prints table row counts so you know the DB + schema work. */
public class ConnectionTest {
    public static void main(String[] args) {
        String[] tables = {"USERS", "UNITS", "TENANTS", "CONTRACTS", "PAYMENTS", "MAINTENANCE_REQUESTS"};
        try (Connection c = DatabaseConnection.getConnection()) {
            System.out.println("Connected to " + c.getMetaData().getURL());
            for (String t : tables) {
                try (Statement st = c.createStatement();
                     ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + t)) {
                    rs.next();
                    System.out.println("  " + t + ": " + rs.getInt(1) + " rows");
                }
            }
        } catch (SQLException e) {
            System.out.println("FAILED: SQLState=" + e.getSQLState() + "  " + e.getMessage());
        }
    }
}
