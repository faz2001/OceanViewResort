package com.hotel.services;

import com.hotel.db.DBConnection;
import com.hotel.models.Reservation;

import java.sql.*;
import java.util.*;

/**
 * Sequence 4 — Staff Calculate Bill + Print Bill
 * Sequence 5 — Guest View E-Bill (viewEBill)
 *
 * calculateBill(resNo)
 *   → ReservationDB.find(resNo)
 *   → [found] computeNights() + getRate() + computeTotal()
 *   → showBill(total)
 *
 * printBill(resNo)      → printSuccess()
 * viewEBill(resNo)      → displayEBill(total) or showError("Invalid reservation number")
 */
public class BillingService {

    private final ReservationService resService = new ReservationService();

    /**
     * Calculates bill for a given resNo.
     * Returns a map with all bill fields, or map with "error" key if not found.
     */
    public Map<String, Object> calculateBill(String resNo) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();

        // Step 3: BillingService → ReservationDB: find(resNo)
        Reservation r = resService.getByResNo(resNo);

        // Step 4: record / null
        if (r == null) {
            // [not found] showNotFound()
            result.put("error", "Reservation '" + resNo + "' not found.");
            return result;
        }

        // [found]
        // Step 1: computeNights() — already stored in DB, verify
        int nights = r.nights;

        // Step 2: getRate() — already stored
        double rate = r.ratePerNight;

        // Step 3: computeTotal() — recalculate to ensure accuracy
        double subtotal = nights * rate;
        double tax      = Math.round(subtotal * 0.12 * 100.0) / 100.0;
        double total    = subtotal + tax;

        // Step 4: showBill(total)
        result.put("resNo",        r.resNo);
        result.put("guestName",    r.guestName);
        result.put("guestEmail",   r.guestEmail);
        result.put("guestContact", r.guestContact);
        result.put("roomId",       r.roomId);
        result.put("roomType",     r.roomType);
        result.put("checkIn",      r.checkIn);
        result.put("checkOut",     r.checkOut);
        result.put("nights",       nights);
        result.put("ratePerNight", String.format("%.2f", rate));
        result.put("subtotal",     String.format("%.2f", subtotal));
        result.put("tax",          String.format("%.2f", tax));
        result.put("total",        String.format("%.2f", total));
        result.put("status",       r.status);
        return result;
    }

    /**
     * Sequence 4 opt: printBill(resNo) → printSuccess()
     * The actual printing is handled by the browser (window.print).
     * This method logs the print action and confirms success.
     */
    public boolean printBill(String resNo) throws SQLException {
        // Verify reservation exists
        Reservation r = resService.getByResNo(resNo);
        if (r == null) return false;
        // In a production system, this would log to a print queue table
        System.out.println("[PRINT] Bill printed for " + resNo + " — guest: " + r.guestName);
        return true; // printSuccess()
    }

    /**
     * Sequence 5 — Guest View E-Bill (reservation number only)
     * viewEBill(resNo) — same as calculateBill but called from the public e-bill portal
     */
    public Map<String, Object> viewEBill(String resNo) throws SQLException {
        // Delegates to calculateBill (same logic)
        Map<String, Object> bill = calculateBill(resNo);

        // If not found → showError("Invalid reservation number")
        if (bill.containsKey("error")) {
            Map<String, Object> errResult = new LinkedHashMap<>();
            errResult.put("error", "Invalid reservation number");
            return errResult;
        }

        // [found] → displayEBill(total)
        return bill;
    }
}
