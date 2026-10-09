package dao;

import database.DatabaseConnection;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.ComboItem;
import model.MaintenanceRequest;

/** All SQL for MAINTENANCE_REQUESTS. Throws SQLException; the frame shows the message. */
public class MaintenanceDAO {

    // JOIN: INNER for unit (required), LEFT for tenant (optional)
    private static final String SELECT_BASE =
            "SELECT m.request_id, m.unit_id, m.tenant_id, u.unit_no, "
          + "COALESCE(t.first_name || ' ' || t.last_name, '-') AS tenant, "
          + "m.description, m.priority, m.date_reported, m.status, m.date_resolved, m.repair_cost "
          + "FROM MAINTENANCE_REQUESTS m "
          + "INNER JOIN UNITS u ON m.unit_id = u.unit_id "
          + "LEFT JOIN TENANTS t ON m.tenant_id = t.tenant_id ";

    private static final String ORDER = " ORDER BY m.date_reported DESC, m.request_id DESC";

    public boolean add(MaintenanceRequest r) throws SQLException {
        String sql = "INSERT INTO MAINTENANCE_REQUESTS "
                + "(unit_id, tenant_id, description, priority, date_reported, status, date_resolved, repair_cost) "
                + "VALUES (?,?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, r);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(MaintenanceRequest r) throws SQLException {
        String sql = "UPDATE MAINTENANCE_REQUESTS SET unit_id=?, tenant_id=?, description=?, priority=?, "
                + "date_reported=?, status=?, date_resolved=?, repair_cost=? WHERE request_id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, r);
            ps.setInt(9, r.getRequestId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM MAINTENANCE_REQUESTS WHERE request_id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public MaintenanceRequest getById(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BASE + "WHERE m.request_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<MaintenanceRequest> getAll() throws SQLException {
        List<MaintenanceRequest> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BASE + ORDER);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<MaintenanceRequest> getByStatus(String status) throws SQLException {
        List<MaintenanceRequest> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BASE + "WHERE m.status = ?" + ORDER)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** Convention method: keyword only. */
    public List<MaintenanceRequest> search(String keyword) throws SQLException {
        return search(keyword, "All");
    }

    /** Keyword matches unit no, tenant name or description. statusFilter = "All" or a real status. */
    public List<MaintenanceRequest> search(String keyword, String statusFilter) throws SQLException {
        String k = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        boolean filter = statusFilter != null && !"All".equals(statusFilter);
        String sql = SELECT_BASE
                + "WHERE (LOWER(u.unit_no) LIKE ? OR LOWER(m.description) LIKE ? "
                + "OR LOWER(COALESCE(t.first_name || ' ' || t.last_name, '')) LIKE ?)"
                + (filter ? " AND m.status = ?" : "")
                + ORDER;
        List<MaintenanceRequest> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, k);
            ps.setString(2, k);
            ps.setString(3, k);
            if (filter) ps.setString(4, statusFilter);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    // ---- combo box sources (small own SELECTs so this module does not wait for Member 3) ----

    public List<ComboItem> getUnitItems() throws SQLException {
        List<ComboItem> items = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT unit_id, unit_no FROM UNITS ORDER BY unit_no");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) items.add(new ComboItem(rs.getInt(1), rs.getString(2)));
        }
        return items;
    }

    public List<ComboItem> getTenantItems() throws SQLException {
        List<ComboItem> items = new ArrayList<>();
        String sql = "SELECT tenant_id, first_name || ' ' || last_name AS full_name "
                + "FROM TENANTS ORDER BY last_name, first_name";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) items.add(new ComboItem(rs.getInt(1), rs.getString(2)));
        }
        return items;
    }

    // ---- helpers ----

    private void bind(PreparedStatement ps, MaintenanceRequest r) throws SQLException {
        ps.setInt(1, r.getUnitId());
        if (r.getTenantId() == 0) ps.setNull(2, Types.INTEGER); else ps.setInt(2, r.getTenantId());
        ps.setString(3, r.getDescription());
        ps.setString(4, r.getPriority());
        ps.setDate(5, java.sql.Date.valueOf(r.getDateReported()));
        ps.setString(6, r.getStatus());
        if (r.getDateResolved() == null) ps.setNull(7, Types.DATE);
        else ps.setDate(7, java.sql.Date.valueOf(r.getDateResolved()));
        ps.setBigDecimal(8, r.getRepairCost() == null ? BigDecimal.ZERO : r.getRepairCost());
    }

    /** One place that builds a MaintenanceRequest from a row of SELECT_BASE. */
    private MaintenanceRequest map(ResultSet rs) throws SQLException {
        MaintenanceRequest r = new MaintenanceRequest();
        r.setRequestId(rs.getInt("request_id"));
        r.setUnitId(rs.getInt("unit_id"));
        r.setTenantId(rs.getInt("tenant_id"));          // NULL becomes 0 = none
        r.setUnitNo(rs.getString("unit_no"));
        r.setTenantName(rs.getString("tenant"));
        r.setDescription(rs.getString("description"));
        r.setPriority(rs.getString("priority"));
        r.setDateReported(rs.getDate("date_reported").toLocalDate());
        r.setStatus(rs.getString("status"));
        Date resolved = rs.getDate("date_resolved");
        r.setDateResolved(resolved == null ? null : resolved.toLocalDate());
        r.setRepairCost(rs.getBigDecimal("repair_cost"));
        return r;
    }
}
