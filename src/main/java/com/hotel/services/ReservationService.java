package com.hotel.services;

import com.hotel.db.DBConnection;
import com.hotel.models.Reservation;
import com.hotel.models.Room;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Sequence 3 — Staff Add Reservation
 * addReservation(details)
 *   → ReservationDB.checkConflict(roomId, dates)
 *   → [no conflict] ReservationDB.save(details) → saved(resNo)
 */
public class ReservationService {

    // ── Get all reservations ──────────────────────────────────────────────
    public List<Reservation> getAllReservations() throws SQLException {
        String sql = "SELECT id,resNo,guestName,guestEmail,guestContact,roomId,roomType," +
                     "CONVERT(varchar,checkIn,23) AS checkIn," +
                     "CONVERT(varchar,checkOut,23) AS checkOut," +
                     "nights,ratePerNight,subtotal,tax,total,status," +
                     "createdBy,CONVERT(varchar,createdAt,120) AS createdAt " +
                     "FROM dbo.Reservations ORDER BY createdAt DESC";
        List<Reservation> list = new ArrayList<>();
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ── Get one reservation by resNo ──────────────────────────────────────
    public Reservation getByResNo(String resNo) throws SQLException {
        String sql = "SELECT id,resNo,guestName,guestEmail,guestContact,roomId,roomType," +
                     "CONVERT(varchar,checkIn,23) AS checkIn," +
                     "CONVERT(varchar,checkOut,23) AS checkOut," +
                     "nights,ratePerNight,subtotal,tax,total,status," +
                     "createdBy,CONVERT(varchar,createdAt,120) AS createdAt " +
                     "FROM dbo.Reservations WHERE resNo=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, resNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    // ── Add reservation (Sequence 3) ──────────────────────────────────────
    public Map<String,Object> addReservation(String guestName, String guestEmail,
                                             String guestContact, String roomId,
                                             String checkIn,  String checkOut,
                                             int    createdBy) throws SQLException {
        Map<String,Object> result = new LinkedHashMap<>();

        // Basic validation
        if (guestName  == null || guestName.isBlank())  { result.put("error","Guest name is required."); return result; }
        if (guestEmail == null || guestEmail.isBlank()) { result.put("error","Guest email is required."); return result; }
        if (roomId     == null || roomId.isBlank())     { result.put("error","Room selection is required."); return result; }
        if (checkIn    == null || checkOut == null)     { result.put("error","Check-in and check-out dates are required."); return result; }

        LocalDate ci, co;
        try {
            ci = LocalDate.parse(checkIn);
            co = LocalDate.parse(checkOut);
        } catch (Exception e) { result.put("error","Invalid date format. Use YYYY-MM-DD."); return result; }
        if (!co.isAfter(ci)) { result.put("error","Check-out must be after check-in."); return result; }

        // Verify room exists and is not under maintenance
        Room room = getRoomById(roomId);
        if (room == null)                           { result.put("error","Room '" + roomId + "' does not exist."); return result; }
        if ("maintenance".equals(room.status))      { result.put("error","Room " + room.roomNumber + " is under maintenance and cannot be booked."); return result; }

        // Step 3: ReservationDB.checkConflict(roomId, dates) ──────────────
        String conflictSql =
            "SELECT COUNT(*) FROM dbo.Reservations " +
            "WHERE roomId=? AND status NOT IN ('checked_out','cancelled') " +
            "AND checkIn < ? AND checkOut > ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(conflictSql)) {
            ps.setString(1, roomId);
            ps.setString(2, checkOut);
            ps.setString(3, checkIn);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    // Step 4: conflictYes → showConflict()
                    result.put("conflict", "Room " + room.roomNumber + " (" + room.roomType +
                               ") is already reserved for the selected dates.");
                    return result;
                }
            }
        }

        // [no conflict] — compute bill
        long   nights   = ChronoUnit.DAYS.between(ci, co);
        double subtotal = nights * room.ratePerNight;
        double tax      = subtotal * 0.12;
        double total    = subtotal + tax;

        // Generate resNo: RES-YYYYMMDD-NNN
        String dateStr = checkIn.replace("-","");
        String resNo   = generateResNo(dateStr);

        // Step 1 (no conflict): ReservationDB.save(details)
        String insertSql =
            "INSERT INTO dbo.Reservations " +
            "(resNo,guestName,guestEmail,guestContact,roomId,roomType," +
            " checkIn,checkOut,nights,ratePerNight,subtotal,tax,total,status,createdBy) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,'confirmed',?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(insertSql)) {
            ps.setString (1, resNo);
            ps.setString (2, guestName.trim());
            ps.setString (3, guestEmail.trim());
            ps.setString (4, guestContact == null ? "" : guestContact.trim());
            ps.setString (5, roomId);
            ps.setString (6, room.roomType);
            ps.setString (7, checkIn);
            ps.setString (8, checkOut);
            ps.setInt    (9, (int) nights);
            ps.setDouble (10, room.ratePerNight);
            ps.setDouble (11, subtotal);
            ps.setDouble (12, tax);
            ps.setDouble (13, total);
            ps.setInt    (14, createdBy);
            ps.executeUpdate();
        }

        // Mark room reserved
        updateRoomStatus(roomId, "reserved");

        // Step 2: saved(resNo) → showConfirmation(resNo)
        result.put("resNo",  resNo);
        result.put("total",  total);
        return result;
    }

    // ── Update status (check-in / check-out / cancel) ─────────────────────
    public String updateStatus(String resNo, String newStatus) throws SQLException {
        String sql = "UPDATE dbo.Reservations SET status=? WHERE resNo=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, resNo);
            int rows = ps.executeUpdate();
            if (rows == 0) return "Reservation not found.";
        }
        // Sync room status
        Reservation r = getByResNo(resNo);
        if (r != null) {
            if ("checked_in".equals(newStatus))  updateRoomStatus(r.roomId, "occupied");
            if ("checked_out".equals(newStatus)) updateRoomStatus(r.roomId, "available");
            if ("cancelled".equals(newStatus))   updateRoomStatus(r.roomId, "available");
        }
        return null; // success
    }

    // ── Cancel (delete) reservation ───────────────────────────────────────
    public String cancelReservation(String resNo) throws SQLException {
        return updateStatus(resNo, "cancelled");
    }

    // ── Get all rooms ─────────────────────────────────────────────────────
    public List<Room> getAllRooms() throws SQLException {
        String sql = "SELECT id,roomNumber,roomType,ratePerNight,status,description " +
                     "FROM dbo.Rooms ORDER BY roomNumber";
        List<Room> list = new ArrayList<>();
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Room rm = new Room();
                rm.id          = rs.getString("id");
                rm.roomNumber  = rs.getString("roomNumber");
                rm.roomType    = rs.getString("roomType");
                rm.ratePerNight= rs.getDouble("ratePerNight");
                rm.status      = rs.getString("status");
                rm.description = rs.getString("description");
                list.add(rm);
            }
        }
        return list;
    }

    public Room getRoomById(String id) throws SQLException {
        String sql = "SELECT id,roomNumber,roomType,ratePerNight,status,description " +
                     "FROM dbo.Rooms WHERE id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Room rm = new Room();
                rm.id          = rs.getString("id");
                rm.roomNumber  = rs.getString("roomNumber");
                rm.roomType    = rs.getString("roomType");
                rm.ratePerNight= rs.getDouble("ratePerNight");
                rm.status      = rs.getString("status");
                rm.description = rs.getString("description");
                return rm;
            }
        }
    }

    public String updateRoomStatus(String roomId, String status) throws SQLException {
        String sql = "UPDATE dbo.Rooms SET status=? WHERE id=?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, roomId);
            ps.executeUpdate();
        }
        return null;
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private String generateResNo(String dateStr) throws SQLException {
        String sql = "SELECT COUNT(*) FROM dbo.Reservations WHERE resNo LIKE ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, "RES-" + dateStr + "-%");
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int seq = rs.getInt(1) + 1;
                return String.format("RES-%s-%03d", dateStr, seq);
            }
        }
    }

    private static Reservation mapRow(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.id           = rs.getInt("id");
        r.resNo        = rs.getString("resNo");
        r.guestName    = rs.getString("guestName");
        r.guestEmail   = rs.getString("guestEmail");
        r.guestContact = rs.getString("guestContact");
        r.roomId       = rs.getString("roomId");
        r.roomType     = rs.getString("roomType");
        r.checkIn      = rs.getString("checkIn");
        r.checkOut     = rs.getString("checkOut");
        r.nights       = rs.getInt("nights");
        r.ratePerNight = rs.getDouble("ratePerNight");
        r.subtotal     = rs.getDouble("subtotal");
        r.tax          = rs.getDouble("tax");
        r.total        = rs.getDouble("total");
        r.status       = rs.getString("status");
        r.createdAt    = rs.getString("createdAt");
        return r;
    }
}
