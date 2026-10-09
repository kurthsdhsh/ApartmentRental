package database;

import java.sql.*;
import util.PasswordUtil;

public class DatabaseInitializer {
    /** Called from Main before showing the login screen. Creates admin/admin123 if USERS is empty. */
    public static void ensureDefaultAdmin() throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM USERS")) {
            rs.next();
            if (rs.getInt(1) == 0) {
                String sql = "INSERT INTO USERS (username,password,full_name,role,status) VALUES (?,?,?,?,?)";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, "admin");
                    ps.setString(2, PasswordUtil.hash("admin123"));
                    ps.setString(3, "System Administrator");
                    ps.setString(4, "Admin");
                    ps.setString(5, "Active");
                    ps.executeUpdate();
                }
            }
        }
    }
}
