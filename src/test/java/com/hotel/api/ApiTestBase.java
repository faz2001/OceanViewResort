package com.hotel.api;

import com.hotel.Main;
import com.sun.net.httpserver.HttpServer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public abstract class ApiTestBase {

    private static HttpServer server;
    protected static final int PORT = 8080;

    @BeforeAll
    static void boot() throws Exception {
        server = Main.startServer(PORT);
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = PORT;
    }

    @AfterAll
    static void shutdown() {
        if (server != null) server.stop(0);
    }
}