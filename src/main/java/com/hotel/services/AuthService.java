package com.hotel.services;

import com.hotel.db.DBConnection;
import com.hotel.models.User;

import java.security.MessageDigest;
import java.sql.*;

/**
 * Sequence 1 — Staff / Admin Login
 * AuthService.login(username, password)
 *   → UserDB.findUser(username)
 *   → returns User on success, null on fail
 */
public class AuthService {

    /**
     * Validates credentials.
     * @return User object on success; null if not found, inactive, or bad password.
     */
    public User login(String username, String password) throws SQLException {
        if (username == null || password == null || username.isBlank() || password.isBlank())
            return null;

        String sql =
                "SELECT id, fullName, username, password, email, role, status " +
                        "FROM dbo.Users " +
                        "WHERE username = ? AND password = ? AND status = 'active'";

        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, username.trim().toLowerCase());
            ps.setString(2, password.trim()); // plain text compare

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                User user = new User();
                user.id       = rs.getInt("id");
                user.fullName = rs.getString("fullName");
                user.username = rs.getString("username");
                user.email    = rs.getString("email");
                user.role     = rs.getString("role");
                user.status   = rs.getString("status");
                return user;
            }
        }
    }
    // ── SHA-256 helper ────────────────────────────────────────────────────
    public static String sha256(String input) {
        try {
            MessageDigest md  = MessageDigest.getInstance("SHA-256");
            byte[]         b  = md.digest(input.getBytes("UTF-8"));
            StringBuilder  sb = new StringBuilder();
            for (byte bv : b) sb.append(String.format("%02x", bv));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 error", e);
        }
    }
}
