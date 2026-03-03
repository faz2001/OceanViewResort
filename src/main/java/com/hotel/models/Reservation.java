package com.hotel.models;

public class Reservation {
    public int    id;
    public String resNo;
    public String guestName;
    public String guestEmail;
    public String guestContact;
    public String roomId;
    public String roomType;
    public String checkIn;
    public String checkOut;
    public int    nights;
    public double ratePerNight;
    public double subtotal;
    public double tax;
    public double total;
    public String status;  // confirmed | checked_in | checked_out | cancelled
    public int    createdBy;
    public String createdAt;

    public Reservation() {}

    public String toJson() {
        return "{"
             + "\"id\":"           + id + ","
             + "\"resNo\":\""      + e(resNo)        + "\","
             + "\"guestName\":\""  + e(guestName)    + "\","
             + "\"guestEmail\":\"" + e(guestEmail)   + "\","
             + "\"guestContact\":\"" + e(guestContact) + "\","
             + "\"roomId\":\""     + e(roomId)       + "\","
             + "\"roomType\":\""   + e(roomType)     + "\","
             + "\"checkIn\":\""    + e(checkIn)      + "\","
             + "\"checkOut\":\""   + e(checkOut)     + "\","
             + "\"nights\":"       + nights          + ","
             + "\"ratePerNight\":" + String.format("%.2f", ratePerNight) + ","
             + "\"subtotal\":"     + String.format("%.2f", subtotal)     + ","
             + "\"tax\":"          + String.format("%.2f", tax)          + ","
             + "\"total\":"        + String.format("%.2f", total)        + ","
             + "\"status\":\""     + e(status)       + "\","
             + "\"createdAt\":\""  + e(createdAt)    + "\""
             + "}";
    }

    private static String e(String s) {
        return s == null ? "" : s.replace("\\","\\\\").replace("\"","\\\"");
    }
}
