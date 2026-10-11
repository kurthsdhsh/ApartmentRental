package dao;

import database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Tenant;

public class TenantDAO {
    // LEFT JOIN to the ACTIVE contract shows the tenant's current unit
    private static final String BASE =
            "SELECT t.tenant_id, t.first_name, t.last_name, t.contact_no, t.email, t.address, "
          + "COALESCE(u.unit_no, '-') AS current_unit "
          + "FROM TENANTS t "
          + "LEFT JOIN CONTRACTS c ON c.tenant_id = t.tenant_id AND c.status = 'Active' "
          + "LEFT JOIN UNITS u ON u.unit_id = c.unit_id ";
    private static final String ORDER = " ORDER BY t.last_name, t.first_name";

    public boolean add(Tenant t) throws SQLException {
        String sql = "INSERT INTO TENANTS (first_name, last_name, contact_no, email, address) VALUES (?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, t);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(Tenant t) throws SQLException {
        String sql = "UPDATE TENANTS SET first_name=?, last_name=?, contact_no=?, email=?, address=? WHERE tenant_id=?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, t);
            ps.setInt(6, t.getTenantId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM TENANTS WHERE tenant_id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Tenant getById(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + "WHERE t.tenant_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    public List<Tenant> getAll() throws SQLException {
        List<Tenant> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + ORDER); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Tenant> search(String keyword) throws SQLException {
        String k = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        String sql = BASE + "WHERE LOWER(t.first_name || ' ' || t.last_name) LIKE ? OR t.contact_no LIKE ? "
                + "OR LOWER(COALESCE(t.email, '')) LIKE ?" + ORDER;
        List<Tenant> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, k); ps.setString(2, k); ps.setString(3, k);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    /** Same first name + last name + contact number (excludeId = 0 when adding). */
    public boolean isDuplicate(String first, String last, String contact, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM TENANTS WHERE LOWER(first_name)=? AND LOWER(last_name)=? "
                + "AND contact_no=? AND tenant_id <> ?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, first.trim().toLowerCase());
            ps.setString(2, last.trim().toLowerCase());
            ps.setString(3, contact.trim());
            ps.setInt(4, excludeId);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1) > 0; }
        }
    }

    /** True when the tenant has any contract or maintenance request (delete protection). */
    public boolean hasHistory(int tenantId) throws SQLException {
        return count("SELECT COUNT(*) FROM CONTRACTS WHERE tenant_id = ?", tenantId) > 0
            || count("SELECT COUNT(*) FROM MAINTENANCE_REQUESTS WHERE tenant_id = ?", tenantId) > 0;
    }

    private int count(String sql, int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }

    private void bind(PreparedStatement ps, Tenant t) throws SQLException {
        ps.setString(1, t.getFirstName());
        ps.setString(2, t.getLastName());
        ps.setString(3, t.getContactNo());
        if (t.getEmail() == null || t.getEmail().trim().isEmpty()) ps.setNull(4, Types.VARCHAR);
        else ps.setString(4, t.getEmail().trim());
        if (t.getAddress() == null || t.getAddress().trim().isEmpty()) ps.setNull(5, Types.VARCHAR);
        else ps.setString(5, t.getAddress().trim());
    }

    private Tenant map(ResultSet rs) throws SQLException {
        Tenant t = new Tenant();
        t.setTenantId(rs.getInt("tenant_id"));
        t.setFirstName(rs.getString("first_name"));
        t.setLastName(rs.getString("last_name"));
        t.setContactNo(rs.getString("contact_no"));
        t.setEmail(rs.getString("email"));
        t.setAddress(rs.getString("address"));
        t.setCurrentUnit(rs.getString("current_unit"));
        return t;
    }
}
