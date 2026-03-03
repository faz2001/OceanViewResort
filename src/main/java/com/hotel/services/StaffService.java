package com.hotel.services;

import com.hotel.db.DBConnection;
import com.hotel.models.User;

import java.sql.*;
import java.util.*;

/**
 * Sequence 2 — Admin CRUD Staff Accounts
 * manage(action, staffData)
 *   [Create]  → StaffDB.save(staffData)
 *   [Update]  → StaffDB.update(staffId, newData)
 *   [Delete]  → StaffDB.delete(staffId)
 *   [View]    → StaffDB.find(staffId) or findAll()
 */
public class StaffService {

    // ── [View All] StaffDB.find() → list ─────────────────────────────────
    public List<User> getAllStaff() throws SQLException {
        String sql = "SELECT id, fullName, username, email, role, status " +
                     "FROM dbo.Users ORDER BY id";
        List<User> list = new ArrayList<>();
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                User u = new User();
                u.id       = rs.getInt("id");
                u.fullName = rs.getString("fullName");
                u.username = rs.getString("username");
                u.email    = rs.getString("email");
                u.role     = rs.getString("role");
                u.status   = rs.getString("status");
                list.add(u);
            }
        }
        return list;
    }

    // ── [View One] StaffDB.find(staffId) ─────────────────────────────────
    public User getStaffById(int id) throws SQLException {
        String sql = "SELECT id, fullName, username, email, role, status " +
                     "FROM dbo.Users WHERE id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null; // [not found]
                User u = new User();
                u.id       = rs.getInt("id");
                u.fullName = rs.getString("fullName");
                u.username = rs.getString("username");
                u.email    = rs.getString("email");
                u.role     = rs.getString("role");
                u.status   = rs.getString("status");
                return u; // [found]
            }
        }
    }

    // ── [Create] StaffDB.save(staffData) ─────────────────────────────────
    public String createStaff(String fullName, String username, String password,
                              String email, String role) throws SQLException {
        if (fullName == null || fullName.isBlank()) return "Full name is required.";
        if (username == null || username.isBlank()) return "Username is required.";
        if (password == null || password.isBlank()) return "Password is required.";
        if (!role.equals("admin") && !role.equals("staff")) return "Invalid role.";

        // Check duplicate username
        String check = "SELECT COUNT(*) FROM dbo.Users WHERE username = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(check)) {
            ps.setString(1, username.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) return "Username '" + username + "' is already taken.";
            }
        }

        String sql = "INSERT INTO dbo.Users (fullName, username, password, email, role, status) " +
                     "VALUES (?, ?, ?, ?, ?, 'active')";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, fullName.trim());
            ps.setString(2, username.trim().toLowerCase());
            ps.setString(3, password.trim());
            ps.setString(4, email  == null ? "" : email.trim());
            ps.setString(5, role);
            ps.executeUpdate();
        }
        return null; // null = success
    }

    // ── [Update] StaffDB.update(staffId, newData) ─────────────────────────
    public String updateStaff(int id, String fullName, String username,
                              String email, String role, String status,
                              String password) throws SQLException {

        if (fullName == null || fullName.isBlank())
            return "Full name is required.";

        if (username == null || username.isBlank())
            return "Username is required.";

        username = username.trim().toLowerCase();

        // 🔎 Check duplicate username (excluding current user)
        String check = "SELECT COUNT(*) FROM dbo.Users WHERE username = ? AND id <> ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(check)) {
            ps.setString(1, username);
            ps.setInt(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0)
                    return "Username '" + username + "' is already taken.";
            }
        }

        if (password != null && !password.isBlank()) {

            String sql = "UPDATE dbo.Users " +
                    "SET fullName=?, username=?, email=?, role=?, status=?, password=? " +
                    "WHERE id=?";

            try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
                ps.setString(1, fullName.trim());
                ps.setString(2, username);
                ps.setString(3, email == null ? "" : email.trim());
                ps.setString(4, role);
                ps.setString(5, status);
                ps.setString(6, password.trim()); // plain text
                ps.setInt(7, id);

                int rows = ps.executeUpdate();
                if (rows == 0) return "Staff account not found.";
            }

        } else {

            String sql = "UPDATE dbo.Users " +
                    "SET fullName=?, username=?, email=?, role=?, status=? " +
                    "WHERE id=?";

            try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
                ps.setString(1, fullName.trim());
                ps.setString(2, username);
                ps.setString(3, email == null ? "" : email.trim());
                ps.setString(4, role);
                ps.setString(5, status);
                ps.setInt(6, id);

                int rows = ps.executeUpdate();
                if (rows == 0) return "Staff account not found.";
            }
        }

        return null; // success
    }

    // ── [Delete] opt(confirmDelete) → StaffDB.delete(staffId) ────────────
    public String deleteStaff(int id) throws SQLException {
        // Block deletion of last admin
        String countAdmin = "SELECT COUNT(*) FROM dbo.Users WHERE role='admin' AND status='active'";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(countAdmin)) {
            rs.next();
            if (rs.getInt(1) <= 1) {
                // Check if the account being deleted is an admin
                String chk = "SELECT role FROM dbo.Users WHERE id=?";
                try (PreparedStatement ps2 = DBConnection.getConnection().prepareStatement(chk)) {
                    ps2.setInt(1, id);
                    try (ResultSet rs2 = ps2.executeQuery()) {
                        if (rs2.next() && "admin".equals(rs2.getString(1))) {
                            return "Cannot delete the last active admin account.";
                        }
                    }
                }
            }
        }

        String sql = "DELETE FROM dbo.Users WHERE id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) return "Staff account not found.";
        }
        return null; // success → showDeleted()
    }
}
