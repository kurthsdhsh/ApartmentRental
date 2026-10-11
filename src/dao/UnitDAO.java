package dao;

import database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Unit;

public class UnitDAO {
    private static final String BASE = "SELECT unit_id, unit_no, unit_type, floor_no, monthly_rent, status FROM UNITS ";

    public boolean add(Unit u) throws SQLException {
        String sql = "INSERT INTO UNITS (unit_no, unit_type, floor_no, monthly_rent, status) VALUES (?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getUnitNo());
            ps.setString(2, u.getUnitType());
            ps.setInt(3, u.getFloorNo());
            ps.setBigDecimal(4, u.getMonthlyRent());
            ps.setString(5, u.getStatus());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(Unit u) throws SQLException {
        String sql = "UPDATE UNITS SET unit_no=?, unit_type=?, floor_no=?, monthly_rent=?, status=? WHERE unit_id=?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getUnitNo());
            ps.setString(2, u.getUnitType());
            ps.setInt(3, u.getFloorNo());
            ps.setBigDecimal(4, u.getMonthlyRent());
            ps.setString(5, u.getStatus());
            ps.setInt(6, u.getUnitId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM UNITS WHERE unit_id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Unit getById(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + "WHERE unit_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    public List<Unit> getAll() throws SQLException {
        return query(BASE + "ORDER BY unit_no");
    }

    /** Member 4 depends on this. */
    public List<Unit> getAvailableUnits() throws SQLException {
        return query(BASE + "WHERE status = 'Available' ORDER BY unit_no");
    }

    public List<Unit> search(String keyword) throws SQLException { return search(keyword, "All"); }

    public List<Unit> search(String keyword, String statusFilter) throws SQLException {
        String k = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        boolean filter = statusFilter != null && !"All".equals(statusFilter);
        String sql = BASE + "WHERE (LOWER(unit_no) LIKE ? OR LOWER(unit_type) LIKE ?)"
                + (filter ? " AND status = ?" : "") + " ORDER BY unit_no";
        List<Unit> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, k);
            ps.setString(2, k);
            if (filter) ps.setString(3, statusFilter);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    /** True when the unit has any contract or maintenance request (delete protection). */
    public boolean hasHistory(int unitId) throws SQLException {
        return count("SELECT COUNT(*) FROM CONTRACTS WHERE unit_id = ?", unitId) > 0
            || count("SELECT COUNT(*) FROM MAINTENANCE_REQUESTS WHERE unit_id = ?", unitId) > 0;
    }

    private int count(String sql, int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1); }
        }
    }

    private List<Unit> query(String sql) throws SQLException {
        List<Unit> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private Unit map(ResultSet rs) throws SQLException {
        Unit u = new Unit();
        u.setUnitId(rs.getInt("unit_id"));
        u.setUnitNo(rs.getString("unit_no"));
        u.setUnitType(rs.getString("unit_type"));
        u.setFloorNo(rs.getInt("floor_no"));
        u.setMonthlyRent(rs.getBigDecimal("monthly_rent"));
        u.setStatus(rs.getString("status"));
        return u;
    }
}
