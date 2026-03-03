package com.hotel.handlers;

import com.hotel.models.Room;
import com.hotel.services.ReservationService;
import com.hotel.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.*;

/**
 * GET /api/rates
 * Returns available rooms and their rates for the reservation form dropdown.
 * Only returns rooms that are NOT under maintenance.
 */
public class RateHandler extends BaseHandler {

    private final ReservationService service = new ReservationService();

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (handleOptions(ex)) return;
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            notFound(ex, "Method not allowed."); return;
        }
        try {
            List<Room> all = service.getAllRooms();
            JsonUtil.ArrBuilder arr = JsonUtil.arr();
            for (Room r : all) {
                if (!"maintenance".equals(r.status)) {
                    arr.addRaw(JsonUtil.obj()
                        .add("id",           r.id)
                        .add("roomNumber",   r.roomNumber)
                        .add("roomType",     r.roomType)
                        .add("rate",         r.ratePerNight)
                        .add("status",       r.status)
                        .build());
                }
            }
            ok(ex, JsonUtil.obj()
                .add("success", true)
                .addRaw("rates", arr.build())
                .build());
        } catch (Exception e) {
            System.err.println("[RateHandler] " + e.getMessage());
            serverError(ex, "Server error: " + e.getMessage());
        }
    }
}
