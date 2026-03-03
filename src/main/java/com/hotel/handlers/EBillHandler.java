package com.hotel.handlers;

import com.hotel.services.BillingService;
import com.hotel.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Map;

/**
 * /api/ebill
 *
 * GET /api/ebill/{resNo}
 * Implements Sequence 5 — Guest View E-Bill
 * EBillUI → BillingService.viewEBill(resNo)
 *   → [found]   displayEBill(total)
 *   → [invalid] showError("Invalid reservation number")
 */
public class EBillHandler extends BaseHandler {

    private final BillingService billing = new BillingService();

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (handleOptions(ex)) return;

        String   method = ex.getRequestMethod().toUpperCase();
        String[] segs   = getPathSegments(ex);

        try {
            if ("GET".equals(method) && segs.length >= 1) {
                viewEBill(ex, segs[0]);
            } else {
                notFound(ex, "Unknown e-bill endpoint.");
            }
        } catch (Exception e) {
            System.err.println("[EBillHandler] " + e.getMessage());
            serverError(ex, "Server error: " + e.getMessage());
        }
    }

    /**
     * Step 2: EBillUI → BillingService.viewEBill(resNo)
     */
    private void viewEBill(HttpExchange ex, String resNo) throws Exception {
        // Steps 3+4: BillingService → ReservationDB.find(resNo) → record/null
        Map<String,Object> bill = billing.viewEBill(resNo.toUpperCase());

        if (bill.containsKey("error")) {
            // [invalid/not found] showError("Invalid reservation number")
            ok(ex, JsonUtil.obj()
                .add("success", false)
                .add("message", "Invalid reservation number")
                .build());
        } else {
            // [found] computeTotal() → displayEBill(total)
            ok(ex, BillingHandler.buildBillJson(bill, true));
        }
    }
}
