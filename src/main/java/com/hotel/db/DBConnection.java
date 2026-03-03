package com.hotel.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides a single JDBC connection to MS SQL Server.
 * Configure DB_HOST, DB_NAME, DB_USER, DB_PASS to match your environment.
 *
 * Driver requirement: mssql-jdbc-12.x.x.jre11.jar in backend/lib/
 * Download from: https://learn.microsoft.com/en-us/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server
 */
public class DBConnection {

    // ── Configuration ─────────────────────────────────────────────────────
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "53856";   // ← your SQL Server is listening here
    private static final String DB_NAME = "OceanViewResort";
    private static final String DB_USER = "hotel_app";   // recommended (not sa)
    private static final String DB_PASS = "123";  // use the exact password set in SQL Server// change to your SA password

    private static final String JDBC_URL =
        "jdbc:sqlserver://" + DB_HOST + ":" + DB_PORT
        + ";databaseName=" + DB_NAME
        + ";encrypt=false"
        + ";trustServerCertificate=true"
        + ";loginTimeout=30";

    // ── Singleton connection ───────────────────────────────────────────────
    private static Connection connection;

    private DBConnection() {}

    /**
     * Returns a live connection, re-creating it if closed or null.
     */
    public static synchronized Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
                System.out.println("[DB] Connected to " + DB_NAME + " on " + DB_HOST);
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                "MS SQL JDBC driver not found. Place mssql-jdbc-*.jre11.jar in backend/lib/", e);
        }
        return connection;
    }

    /**
     * Closes the singleton connection (call on application shutdown).
     */
    public static synchronized void close() {
        if (connection != null) {
            try { connection.close(); System.out.println("[DB] Connection closed."); }
            catch (SQLException ignored) {}
            connection = null;
        }
    }
}
