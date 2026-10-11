package dao;

import database.DatabaseConnection;
import java.math.BigDecimal;
import java.sql.*;

/** All dashboard count queries. The Dashboard frame never writes SQL. */
public class DashboardDAO {
    public int totalUnits() throws SQLException { return count("SELECT COUNT(*) FROM UNITS"); }
    public int occupiedUnits() throws SQLException { return count("SELECT COUNT(*) FROM UNITS WHERE status = 'Occupied'"); }
    public int availableUnits() throws SQLException { return count("SELECT COUNT(*) FROM UNITS WHERE status = 'Available'"); }
    public int activeContracts() throws SQLException { return count("SELECT COUNT(*) FROM CONTRACTS WHERE status = 'Active'"); }
    public int openMaintenance() throws SQLException {
        return count("SELECT COUNT(*) FROM MAINTENANCE_REQUESTS WHERE status IN ('Open','In Progress')");
    }

    /** billingMonth is yyyy-MM, for example java.time.YearMonth.now().toString(). */
    public BigDecimal rentCollected(String billingMonth) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM PAYMENTS WHERE billing_month = ? AND status <> 'Voided'";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, billingMonth);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getBigDecimal(1); }
        }
    }

    private int count(String sql) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
