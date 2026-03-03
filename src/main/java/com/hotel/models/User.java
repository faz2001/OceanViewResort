package com.hotel.models;

public class User {
    public int    id;
    public String fullName;
    public String username;
    public String email;
    public String role;    // admin | staff
    public String status;  // active | inactive

    public User() {}

    public User(int id, String fullName, String username,
                String email, String role, String status) {
        this.id       = id;
        this.fullName = fullName;
        this.username = username;
        this.email    = email;
        this.role     = role;
        this.status   = status;
    }

    /** Returns a JSON object (without password). */
    public String toJson() {
        return "{\"id\":" + id
             + ",\"fullName\":\"" + escape(fullName) + "\""
             + ",\"username\":\"" + escape(username) + "\""
             + ",\"email\":\""    + escape(email)    + "\""
             + ",\"role\":\""     + escape(role)     + "\""
             + ",\"status\":\""   + escape(status)   + "\"}";
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\\","\\\\").replace("\"","\\\"");
    }
}
