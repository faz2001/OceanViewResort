package com.hotel.handlers;

import com.hotel.models.Reservation;
import com.hotel.services.ReservationService;
import com.hotel.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.*;

/**
 * /api/reservations
 *
 * GET    /api/reservations           → list all
 * GET    /api/reservations/{resNo}   → get one
 * POST   /api/reservations           → addReservation (Sequence 3)
 * PUT    /api/reservations/{resNo}   → update status
 * DELETE /api/reservations/{resNo}   → cancel
 */
public class ReservationHandler extends BaseHandler {

    private final ReservationService service = new ReservationService();

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (handleOptions(ex)) return;

        String   method = ex.getRequestMethod().toUpperCase();
        String[] segs   = getPathSegments(ex);

        try {
            if ("GET".equals(method)) {
                if (segs.length == 0) listAll(ex);
                else                  getOne(ex, segs[0]);
            } else if ("POST".equals(method)   && segs.length == 0) add(ex);
            else if   ("PUT".equals(method)    && segs.length >= 1) updateStatus(ex, segs[0]);
            else if   ("DELETE".equals(method) && segs.length >= 1) cancel(ex, segs[0]);
            else notFound(ex, "Unknown reservations endpoint.");
        } catch (Exception e) {
            System.err.println("[ReservationHandler] " + e.getMessage());
            serverError(ex, "Server error: " + e.getMessage());
        }
    }

    // ── GET all ───────────────────────────────────────────────────────────
    private void listAll(HttpExchange ex) throws Exception {
        List<Reservation> list = service.getAllReservations();
        JsonUtil.ArrBuilder arr = JsonUtil.arr();
        for (Reservation r : list) arr.addRaw(r.toJson());
        ok(ex, JsonUtil.obj()
            .add("success", true)
            .addRaw("reservations", arr.build())
            .build());
    }

    // ── GET one ───────────────────────────────────────────────────────────
    private void getOne(HttpExchange ex, String resNo) throws Exception {
        Reservation r = service.getByResNo(resNo);
        if (r == null) {
            notFound(ex, "Reservation '" + resNo + "' not found.");
        } else {
            ok(ex, JsonUtil.obj()
                .add("success", true)
                .addRaw("reservation", r.toJson())
                .build());
        }
    }

    // ── POST — addReservation (Sequence 3) ────────────────────────────────
    private void add(HttpExchange ex) throws Exception {
        Map<String,String> p = JsonUtil.parse(readBody(ex));

        int createdBy = 0;
        try { createdBy = Integer.parseInt(p.getOrDefault("createdBy","0")); }
        catch (NumberFormatException ignored) {}

        Map<String,Object> result = service.addReservation(
            p.get("guestName"),    p.get("guestEmail"),
            p.get("guestContact"), p.get("roomId"),
            p.get("checkIn"),      p.get("checkOut"),
            createdBy);

        if (result.containsKey("error")) {
            ok(ex, JsonUtil.obj()
                .add("success", false)
                .add("message", (String) result.get("error"))
                .build());
        } else if (result.containsKey("conflict")) {
            // showConflict()
            ok(ex, JsonUtil.obj()
                .add("success", false)
                .add("conflict", true)
                .add("message", (String) result.get("conflict"))
                .build());
        } else {
            // showConfirmation(resNo)
            created(ex, JsonUtil.obj()
                .add("success", true)
                .add("message", "Reservation confirmed.")
                .add("resNo",   (String) result.get("resNo"))
                .add("total",   result.get("total").toString())
                .build());
        }
    }

    // ── PUT — update status ───────────────────────────────────────────────
    private void updateStatus(HttpExchange ex, String resNo) throws Exception {
        Map<String,String> p  = JsonUtil.parse(readBody(ex));
        String newStatus       = p.getOrDefault("status","");
        if (newStatus.isBlank()) { badRequest(ex,"Status is required."); return; }

        String err = service.updateStatus(resNo, newStatus);
        if (err != null) {
            ok(ex, JsonUtil.obj().add("success",false).add("message",err).build());
        } else {
            ok(ex, JsonUtil.obj()
                .add("success", true)
                .add("message", "Reservation status updated to '" + newStatus + "'.")
                .build());
        }
    }

    // ── DELETE — cancel ───────────────────────────────────────────────────
    private void cancel(HttpExchange ex, String resNo) throws Exception {
        String err = service.cancelReservation(resNo);
        if (err != null) {
            ok(ex, JsonUtil.obj().add("success",false).add("message",err).build());
        } else {
            ok(ex, JsonUtil.obj()
                .add("success", true)
                .add("message", "Reservation '" + resNo + "' cancelled.")
                .build());
        }
    }
}
