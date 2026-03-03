package com.hotel.handlers;

import com.hotel.services.BillingService;
import com.hotel.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

/**
 * /api/billing
 *
 * GET  /api/billing/{resNo}        → calculateBill(resNo) — Sequence 4
 * POST /api/billing/print/{resNo}  → printBill(resNo)     — Sequence 4 opt
 */
public class BillingHandler extends BaseHandler {

    private final BillingService billing = new BillingService();

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (handleOptions(ex)) return;

        String   method = ex.getRequestMethod().toUpperCase();
        String[] segs   = getPathSegments(ex);

        try {
            // POST /api/billing/print/{resNo}
            if ("POST".equals(method) && segs.length >= 2 && "print".equals(segs[0])) {
                printBill(ex, segs[1]);

            // GET /api/billing/{resNo}
            } else if ("GET".equals(method) && segs.length >= 1) {
                calculateBill(ex, segs[0]);

            } else {
                notFound(ex, "Unknown billing endpoint.");
            }
        } catch (Exception e) {
            System.err.println("[BillingHandler] " + e.getMessage());
            serverError(ex, "Server error: " + e.getMessage());
        }
    }

    /**
     * Sequence 4:
     * BillingUI → BillingService.calculateBill(resNo)
     * → [found]   showBill(total)
     * → [not found] showNotFound()
     */
    private void calculateBill(HttpExchange ex, String resNo) throws Exception {
        Map<String,Object> bill = billing.calculateBill(resNo.toUpperCase());

        if (bill.containsKey("error")) {
            // [not found] showNotFound()
            ok(ex, JsonUtil.obj()
                .add("success", false)
                .add("message", (String) bill.get("error"))
                .build());
        } else {
            // [found] showBill(total)
            ok(ex, buildBillJson(bill, true));
        }
    }

    /**
     * Sequence 4 opt: BillingUI → BillingService.printBill(resNo) → printSuccess()
     */
    private void printBill(HttpExchange ex, String resNo) throws Exception {
        boolean ok = billing.printBill(resNo.toUpperCase());
        if (ok) {
            ok(ex, JsonUtil.obj()
                .add("success", true)
                .add("message", "Bill sent to printer.") // printSuccess()
                .build());
        } else {
            ok(ex, JsonUtil.obj()
                .add("success", false)
                .add("message", "Reservation not found.")
                .build());
        }
    }

    static String buildBillJson(Map<String,Object> b, boolean success) {
        JsonUtil.ObjBuilder o = JsonUtil.obj();
        o.add("success", success);
        for (Map.Entry<String,Object> e : b.entrySet()) {
            Object v = e.getValue();
            if (v instanceof Number) o.add(e.getKey(), (Number)v);
            else                     o.add(e.getKey(), String.valueOf(v));
        }
        return o.build();
    }
}
