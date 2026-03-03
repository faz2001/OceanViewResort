package com.hotel;

import com.hotel.db.DBConnection;
import com.hotel.handlers.*;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {

    private static final int PORT = 8080;

    public static HttpServer startServer(int port) throws Exception {
        // optional: keep your DB verification messages
        try {
            DBConnection.getConnection();
            System.out.println("[Server] Database connection verified.");
        } catch (Exception e) {
            System.err.println("[Server] WARNING: Database not reachable: " + e.getMessage());
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/auth",         new AuthHandler());
        server.createContext("/api/staff",        new StaffHandler());
        server.createContext("/api/reservations", new ReservationHandler());
        server.createContext("/api/billing",      new BillingHandler());
        server.createContext("/api/ebill",        new EBillHandler());
        server.createContext("/api/rooms",        new RoomHandler());
        server.createContext("/api/rates",        new RateHandler());

        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();
        return server;
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = startServer(PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[Server] Shutting down...");
            server.stop(0);
            DBConnection.close();
            System.out.println("[Server] Closed.");
        }));
    }
}