package database;

import java.sql.*;

/** Single place for the JDBC URL. Derby server must be running. */
public class DatabaseConnection {
    private static final String URL = "jdbc:derby://localhost:1527/apt_db";
    private static final String USER = "app";
    private static final String PASSWORD = "app";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}