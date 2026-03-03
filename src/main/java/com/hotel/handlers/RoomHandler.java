package com.hotel.handlers;

import com.hotel.models.Room;
import com.hotel.services.ReservationService;
import com.hotel.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.*;

/**
 * /api/rooms
 *
 * GET /api/rooms         → list all rooms with status
 * PUT /api/rooms/{id}    → update room status manually
 * GET /api/rates         → room types + rates (for reservation form dropdown)
 */
public class RoomHandler extends BaseHandler {

    private final ReservationService service = new ReservationService();

    @Override
    public void handle(HttpExchange ex) throws IOException {
        if (handleOptions(ex)) return;

        String   method = ex.getRequestMethod().toUpperCase();
        String[] segs   = getPathSegments(ex);

        try {
            if ("GET".equals(method)  && segs.length == 0) listRooms(ex);
            else if ("PUT".equals(method) && segs.length == 1) updateRoom(ex, segs[0]);
            else notFound(ex, "Unknown rooms endpoint.");
        } catch (Exception e) {
            System.err.println("[RoomHandler] " + e.getMessage());
            serverError(ex, "Server error: " + e.getMessage());
        }
    }

    private void listRooms(HttpExchange ex) throws Exception {
        List<Room> rooms = service.getAllRooms();
        JsonUtil.ArrBuilder arr = JsonUtil.arr();
        for (Room r : rooms) arr.addRaw(r.toJson());
        ok(ex, JsonUtil.obj()
            .add("success", true)
            .addRaw("rooms", arr.build())
            .build());
    }

    private void updateRoom(HttpExchange ex, String roomId) throws Exception {
        Map<String,String> p = JsonUtil.parse(readBody(ex));
        String status = p.getOrDefault("status","");
        if (status.isBlank()) { badRequest(ex,"Status is required."); return; }

        String valid[] = {"available","reserved","occupied","maintenance"};
        boolean ok = false;
        for (String v : valid) if (v.equals(status)) { ok = true; break; }
        if (!ok) { badRequest(ex,"Invalid status value."); return; }

        String err = service.updateRoomStatus(roomId, status);
        if (err != null) {
            this.ok(ex, JsonUtil.obj().add("success",false).add("message",err).build());
        } else {
            this.ok(ex, JsonUtil.obj()
                .add("success", true)
                .add("message", "Room " + roomId + " status updated to '" + status + "'.")
                .build());
        }
    }
}
