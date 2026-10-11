package dao;

import database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.User;
import util.PasswordUtil;

public class UserDAO {
    private static final String BASE = "SELECT user_id, username, full_name, role, status FROM USERS ";

    /** Returns the User, or null if no match or the account is Inactive. */
    public User authenticate(String username, String plainPassword) throws SQLException {
        String sql = BASE + "WHERE username = ? AND password = ? AND status = 'Active'";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.setString(2, PasswordUtil.hash(plainPassword));
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    /** u.getPassword() holds the PLAIN password; it is hashed here. */
    public boolean add(User u) throws SQLException {
        String sql = "INSERT INTO USERS (username, password, full_name, role, status) VALUES (?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getUsername().trim());
            ps.setString(2, PasswordUtil.hash(u.getPassword()));
            ps.setString(3, u.getFullName().trim());
            ps.setString(4, u.getRole());
            ps.setString(5, u.getStatus());
            return ps.executeUpdate() > 0;
        }
    }

    /** Blank password = keep the old one. */
    public boolean update(User u) throws SQLException {
        boolean newPw = u.getPassword() != null && !u.getPassword().isEmpty();
        String sql = "UPDATE USERS SET username=?, full_name=?, role=?, status=?"
                + (newPw ? ", password=?" : "") + " WHERE user_id=?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getUsername().trim());
            ps.setString(2, u.getFullName().trim());
            ps.setString(3, u.getRole());
            ps.setString(4, u.getStatus());
            int i = 5;
            if (newPw) ps.setString(i++, PasswordUtil.hash(u.getPassword()));
            ps.setInt(i, u.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM USERS WHERE user_id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public User getById(int id) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(BASE + "WHERE user_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? map(rs) : null; }
        }
    }

    public List<User> getAll() throws SQLException { return search(""); }

    public List<User> search(String keyword) throws SQLException {
        String k = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        String sql = BASE + "WHERE LOWER(username) LIKE ? OR LOWER(full_name) LIKE ? ORDER BY username";
        List<User> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, k);
            ps.setString(2, k);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    /** Number of Admin accounts (used to protect the last Admin). */
    public int countAdmins() throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM USERS WHERE role = 'Admin'");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setUsername(rs.getString("username"));
        u.setFullName(rs.getString("full_name"));
        u.setRole(rs.getString("role"));
        u.setStatus(rs.getString("status"));
        return u;
    }
}
