package dao;

import database.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import javax.swing.table.DefaultTableModel;
import util.TableHelper;

/** Every report is just one SQL string turned into a table model by TableHelper. */
public class ReportDAO {

    public DefaultTableModel unitOccupancy() throws SQLException {
        return run("SELECT u.unit_no, u.unit_type, u.monthly_rent, u.status, "
                + "COALESCE(t.first_name || ' ' || t.last_name, '-') AS current_tenant "
                + "FROM UNITS u "
                + "LEFT JOIN CONTRACTS c ON c.unit_id = u.unit_id AND c.status = 'Active' "
                + "LEFT JOIN TENANTS t ON t.tenant_id = c.tenant_id ORDER BY u.unit_no");
    }

    public DefaultTableModel tenantList() throws SQLException {
        return run("SELECT t.tenant_id, t.first_name || ' ' || t.last_name AS tenant, t.contact_no, t.email, "
                + "COALESCE(u.unit_no, '-') AS active_unit "
                + "FROM TENANTS t "
                + "LEFT JOIN CONTRACTS c ON c.tenant_id = t.tenant_id AND c.status = 'Active' "
                + "LEFT JOIN UNITS u ON u.unit_id = c.unit_id ORDER BY t.last_name, t.first_name");
    }

    public DefaultTableModel monthlyCollection() throws SQLException {
        return run("SELECT billing_month, COUNT(*) AS payment_count, SUM(amount) AS total_collected "
                + "FROM PAYMENTS WHERE status <> 'Voided' GROUP BY billing_month ORDER BY billing_month DESC");
    }

    /** billingMonth is yyyy-MM. */
    public DefaultTableModel overdueRent(String billingMonth) throws SQLException {
        String sql = "SELECT c.contract_id, t.first_name || ' ' || t.last_name AS tenant, u.unit_no, c.monthly_rent "
                + "FROM CONTRACTS c "
                + "INNER JOIN TENANTS t ON c.tenant_id = t.tenant_id "
                + "INNER JOIN UNITS u ON c.unit_id = u.unit_id "
                + "WHERE c.status = 'Active' AND NOT EXISTS (SELECT 1 FROM PAYMENTS p "
                + "WHERE p.contract_id = c.contract_id AND p.billing_month = ? AND p.status = 'Paid') "
                + "ORDER BY u.unit_no";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, billingMonth);
            try (ResultSet rs = ps.executeQuery()) { return TableHelper.toModel(rs); }
        }
    }

    public DefaultTableModel openMaintenance() throws SQLException {
        return run("SELECT m.request_id, u.unit_no, COALESCE(t.first_name || ' ' || t.last_name, '-') AS tenant, "
                + "m.description, m.priority, m.date_reported, m.status "
                + "FROM MAINTENANCE_REQUESTS m "
                + "INNER JOIN UNITS u ON m.unit_id = u.unit_id "
                + "LEFT JOIN TENANTS t ON m.tenant_id = t.tenant_id "
                + "WHERE m.status IN ('Open','In Progress') ORDER BY m.date_reported");
    }

    public DefaultTableModel expiringSoon() throws SQLException {
        String sql = "SELECT c.contract_id, t.first_name || ' ' || t.last_name AS tenant, u.unit_no, c.end_date "
                + "FROM CONTRACTS c INNER JOIN TENANTS t ON c.tenant_id = t.tenant_id "
                + "INNER JOIN UNITS u ON c.unit_id = u.unit_id "
                + "WHERE c.status = 'Active' AND c.end_date BETWEEN CURRENT_DATE AND ? ORDER BY c.end_date";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(LocalDate.now().plusDays(30)));
            try (ResultSet rs = ps.executeQuery()) { return TableHelper.toModel(rs); }
        }
    }

    private DefaultTableModel run(String sql) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            return TableHelper.toModel(rs);
        }
    }
}
