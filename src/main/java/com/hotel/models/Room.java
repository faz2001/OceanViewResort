package com.hotel.models;

public class Room {
    public String id;          // R101
    public String roomNumber;
    public String roomType;
    public double ratePerNight;
    public String status;      // available | reserved | occupied | maintenance
    public String description;

    public Room() {}

    public String toJson() {
        return "{"
             + "\"id\":\""           + e(id)          + "\","
             + "\"roomNumber\":\""   + e(roomNumber)  + "\","
             + "\"roomType\":\""     + e(roomType)    + "\","
             + "\"ratePerNight\":"   + String.format("%.2f", ratePerNight) + ","
             + "\"status\":\""       + e(status)      + "\","
             + "\"description\":\""  + e(description) + "\""
             + "}";
    }

    private static String e(String s) {
        return s == null ? "" : s.replace("\\","\\\\").replace("\"","\\\"");
    }
}
