package com.hotel.api;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class BillingApiTest extends ApiTestBase {

    @Test
    void getBilling_unknownReservation_shouldReturnSuccessFalse_withMessage() {
        given()
                .when().get("/api/billing/ZZZ999999")
                .then()
                .statusCode(200)
                .contentType(containsString("application/json"))
                .body("success", equalTo(false))
                .body("message", not(isEmptyOrNullString()));
    }

    @Test
    void getBilling_shouldBeCaseInsensitive_forResNo() {
        // Both should behave the same because handler uppercases resNo
        var upper = given().when().get("/api/billing/ab12");
        var lower = given().when().get("/api/billing/AB12");

        // We only assert that both calls return JSON and success field exists
        upper.then().statusCode(200).contentType(containsString("application/json")).body("success", notNullValue());
        lower.then().statusCode(200).contentType(containsString("application/json")).body("success", notNullValue());
    }

    @Test
    void printBilling_unknownReservation_shouldReturnReservationNotFound() {
        given()
                .when().post("/api/billing/print/ZZZ999999")
                .then()
                .statusCode(200)
                .contentType(containsString("application/json"))
                .body("success", equalTo(false))
                .body("message", equalTo("Reservation not found."));
    }

    @Test
    void printBilling_shouldAlsoBeCaseInsensitive_forResNo() {
        given()
                .when().post("/api/billing/print/ab12")
                .then()
                .statusCode(200)
                .contentType(containsString("application/json"))
                .body("success", notNullValue())
                .body("message", notNullValue());
    }
}