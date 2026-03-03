package com.hotel.api;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.not;

class ApiSmokeTest extends ApiTestBase {

    @Test
    void routes_shouldNotReturn404() {
        given().when().get("/api/rates").then().statusCode(not(404));
        given().when().get("/api/rooms").then().statusCode(not(404));
        given().when().get("/api/staff").then().statusCode(not(404));
        given().when().get("/api/reservations").then().statusCode(not(404));
        given().when().get("/api/billing/1").then().statusCode(not(404));
        given().when().get("/api/ebill/1").then().statusCode(not(404));
    }

    @Test
    void login_shouldNotReturn404() {
        given().when().post("/api/auth/login").then().statusCode(not(404));
    }
}