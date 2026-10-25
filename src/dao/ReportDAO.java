package dao;

import database.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
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



    public DefaultTableModel unitOccupancyDetailed() throws SQLException {
        return run("SELECT u.unit_no, u.unit_type, u.monthly_rent, u.status, "
                + "COALESCE(t.first_name || ' ' || t.last_name, '-') AS current_tenant, c.end_date "
                + "FROM UNITS u "
                + "LEFT JOIN CONTRACTS c ON c.unit_id = u.unit_id AND c.status = 'Active' "
                + "LEFT JOIN TENANTS t ON t.tenant_id = c.tenant_id ORDER BY u.unit_no");
    }

    /** Q: Who do I contact, and where do they live? */
    public DefaultTableModel tenantDirectory() throws SQLException {
        return run("SELECT t.first_name || ' ' || t.last_name AS tenant, t.contact_no, t.email, "
                + "COALESCE(u.unit_no, '-') AS unit_no, c.end_date "
                + "FROM TENANTS t "
                + "LEFT JOIN CONTRACTS c ON c.tenant_id = t.tenant_id AND c.status = 'Active' "
                + "LEFT JOIN UNITS u ON u.unit_id = c.unit_id ORDER BY t.last_name, t.first_name");
    }

   
    public DefaultTableModel paymentsForMonth(String billingMonth) throws SQLException {
        String sql = "SELECT t.first_name || ' ' || t.last_name AS tenant, u.unit_no, p.payment_date, p.amount, "
                + "p.payment_method, p.status, us.full_name AS received_by "
                + "FROM PAYMENTS p "
                + "INNER JOIN CONTRACTS c ON p.contract_id = c.contract_id "
                + "INNER JOIN TENANTS t ON c.tenant_id = t.tenant_id "
                + "INNER JOIN UNITS u ON c.unit_id = u.unit_id "
                + "INNER JOIN USERS us ON p.user_id = us.user_id "
                + "WHERE p.billing_month = ? AND p.status <> 'Voided' "
                + "ORDER BY p.payment_date, t.last_name";
        return runWith(sql, ps -> ps.setString(1, billingMonth));
    }

   
    public DefaultTableModel rentOwed(String billingMonth) throws SQLException {
        YearMonth ym = YearMonth.parse(billingMonth);
        String sql = "SELECT x.tenant, x.contact_no, x.unit_no, x.monthly_rent, x.paid, x.balance, "
                + "CASE WHEN x.paid = 0 THEN 'Unpaid' ELSE 'Partial' END AS pay_status "
                + "FROM (SELECT t.first_name || ' ' || t.last_name AS tenant, t.contact_no, u.unit_no, c.monthly_rent, "
                + "COALESCE((SELECT SUM(p.amount) FROM PAYMENTS p WHERE p.contract_id = c.contract_id "
                + "AND p.billing_month = ? AND p.status <> 'Voided'), 0) AS paid, "
                + "c.monthly_rent - COALESCE((SELECT SUM(p2.amount) FROM PAYMENTS p2 WHERE p2.contract_id = c.contract_id "
                + "AND p2.billing_month = ? AND p2.status <> 'Voided'), 0) AS balance "
                + "FROM CONTRACTS c "
                + "INNER JOIN TENANTS t ON c.tenant_id = t.tenant_id "
                + "INNER JOIN UNITS u ON c.unit_id = u.unit_id "
                + "WHERE c.status = 'Active' AND c.start_date <= ? AND c.end_date >= ?) x "
                + "WHERE x.balance > 0 ORDER BY x.balance DESC, x.unit_no";
        return runWith(sql, ps -> {
            ps.setString(1, billingMonth);
            ps.setString(2, billingMonth);
            ps.setDate(3, java.sql.Date.valueOf(ym.atEndOfMonth()));
            ps.setDate(4, java.sql.Date.valueOf(ym.atDay(1)));
        });
    }

  
    public DefaultTableModel openMaintenanceByPriority() throws SQLException {
        DefaultTableModel m = run("SELECT x.unit_no, x.tenant, x.description, x.priority, x.date_reported, x.status "
                + "FROM (SELECT u.unit_no, COALESCE(t.first_name || ' ' || t.last_name, '-') AS tenant, m.description, "
                + "m.priority, m.date_reported, m.status, "
                + "CASE m.priority WHEN 'Urgent' THEN 1 WHEN 'High' THEN 2 WHEN 'Medium' THEN 3 ELSE 4 END AS rk "
                + "FROM MAINTENANCE_REQUESTS m "
                + "INNER JOIN UNITS u ON m.unit_id = u.unit_id "
                + "LEFT JOIN TENANTS t ON m.tenant_id = t.tenant_id "
                + "WHERE m.status IN ('Open','In Progress')) x ORDER BY x.rk, x.date_reported");
        addDaysColumn(m, 4, "days_open", false);
        return m;
    }

    
    public DefaultTableModel leasesEndingSoon() throws SQLException {
        String sql = "SELECT t.first_name || ' ' || t.last_name AS tenant, t.contact_no, u.unit_no, c.monthly_rent, c.end_date "
                + "FROM CONTRACTS c INNER JOIN TENANTS t ON c.tenant_id = t.tenant_id "
                + "INNER JOIN UNITS u ON c.unit_id = u.unit_id "
                + "WHERE c.status = 'Active' AND c.end_date BETWEEN CURRENT_DATE AND ? ORDER BY c.end_date";
        DefaultTableModel m = runWith(sql, ps -> ps.setDate(1, java.sql.Date.valueOf(LocalDate.now().plusDays(30))));
        addDaysColumn(m, 4, "days_left", true);
        return m;
    }

    
    private void addDaysColumn(DefaultTableModel m, int dateCol, String name, boolean untilDate) {
        Object[] days = new Object[m.getRowCount()];
        for (int i = 0; i < days.length; i++) {
            Object d = m.getValueAt(i, dateCol);
            LocalDate date = (d instanceof java.sql.Date) ? ((java.sql.Date) d).toLocalDate() : null;
            if (date == null) { days[i] = "-"; continue; }
            days[i] = untilDate
                    ? java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), date)
                    : java.time.temporal.ChronoUnit.DAYS.between(date, LocalDate.now());
        }
        m.addColumn(name, days);
    }

    private interface Binder { void bind(PreparedStatement ps) throws SQLException; }

    private DefaultTableModel runWith(String sql, Binder b) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            b.bind(ps);
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
