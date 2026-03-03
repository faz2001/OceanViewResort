package com.hotel.handlers;

import com.hotel.models.User;
import com.hotel.services.StaffService;
import com.hotel.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.*;

/**
 * /api/staff
 *
 * GET    /api/staff         → getAllStaff()   [View Staff]
 * GET    /api/staff/{id}    → getStaffById()  [View Staff]
 * POST   /api/staff         → createStaff()   [Create Staff]
 * PUT    /api/staff/{id}    → updateStaff()   [Update Staff]
 * DELETE /api/staff/{id}    → deleteStaff()   [Delete Staff]
 *
 * Implements Sequence 2 — Admin CRUD Staff Accounts
 */
public class StaffHandler extends BaseHandler {

    private final StaffService staffService = new StaffService();

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (handleOptions(ex)) return;

        String   method = ex.getRequestMethod().toUpperCase();
        String[] segs   = getPathSegments(ex);

        try {
            if ("GET".equals(method)) {
                if (segs.length == 0) getAllStaff(ex);       // list all
                else                  getStaff(ex, segs[0]); // by id
            } else if ("POST".equals(method)   && segs.length == 0) createStaff(ex);
            else if   ("PUT".equals(method)    && segs.length == 1) updateStaff(ex, segs[0]);
            else if   ("DELETE".equals(method) && segs.length == 1) deleteStaff(ex, segs[0]);
            else notFound(ex, "Unknown staff endpoint.");
        } catch (Exception e) {
            System.err.println("[StaffHandler] " + e.getMessage());
            serverError(ex, "Server error: " + e.getMessage());
        }
    }

    // ── [View All] ────────────────────────────────────────────────────────
    private void getAllStaff(HttpExchange ex) throws Exception {
        List<User> list = staffService.getAllStaff();
        JsonUtil.ArrBuilder arr = JsonUtil.arr();
        for (User u : list) arr.addRaw(u.toJson());
        ok(ex, JsonUtil.obj()
            .add("success", true)
            .addRaw("staff", arr.build())
            .build());
    }

    // ── [View One] alt [found] display(record) / [not found] showNotFound()
    private void getStaff(HttpExchange ex, String idStr) throws Exception {
        int id = parseId(idStr);
        if (id < 0) { badRequest(ex, "Invalid staff ID."); return; }

        User u = staffService.getStaffById(id);
        if (u == null) {
            // [not found] showNotFound()
            ok(ex, JsonUtil.obj()
                .add("success", false)
                .add("message", "Staff account not found.")
                .build());
        } else {
            // [found] display(record)
            ok(ex, JsonUtil.obj()
                .add("success", true)
                .addRaw("staff", u.toJson())
                .build());
        }
    }

    // ── [Create] StaffDB.save(staffData) → showSuccess() ─────────────────
    private void createStaff(HttpExchange ex) throws Exception {
        Map<String,String> p = JsonUtil.parse(readBody(ex));
        String err = staffService.createStaff(
            p.get("fullName"), p.get("username"),
            p.get("password"), p.get("email"),
            p.getOrDefault("role","staff"));

        if (err != null) {
            ok(ex, JsonUtil.obj().add("success",false).add("message",err).build());
        } else {
            // showSuccess()
            created(ex, JsonUtil.obj()
                .add("success", true)
                .add("message", "Staff account created successfully.")
                .build());
        }
    }

    // ── [Update] StaffDB.update(staffId, newData) → showSuccess() ────────
    private void updateStaff(HttpExchange ex, String idStr) throws Exception {
        int id = parseId(idStr);
        if (id < 0) { badRequest(ex, "Invalid staff ID."); return; }

        Map<String,String> p = JsonUtil.parse(readBody(ex));
        String err = staffService.updateStaff(id,
            p.get("fullName"),p.get("username"), p.get("email"),
            p.getOrDefault("role","staff"),
            p.getOrDefault("status","active"),
            p.get("password"));

        if (err != null) {
            ok(ex, JsonUtil.obj().add("success",false).add("message",err).build());
        } else {
            ok(ex, JsonUtil.obj()
                .add("success", true)
                .add("message", "Staff account updated successfully.")
                .build());
        }
    }

    // ── [Delete] opt confirmDelete → StaffDB.delete() → showDeleted() ────
    private void deleteStaff(HttpExchange ex, String idStr) throws Exception {
        int id = parseId(idStr);
        if (id < 0) { badRequest(ex, "Invalid staff ID."); return; }

        String err = staffService.deleteStaff(id);
        if (err != null) {
            ok(ex, JsonUtil.obj().add("success",false).add("message",err).build());
        } else {
            // showDeleted()
            ok(ex, JsonUtil.obj()
                .add("success", true)
                .add("message", "Staff account deleted.")
                .build());
        }
    }

    private static int parseId(String s) {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return -1; }
    }
}
