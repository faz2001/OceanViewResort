package com.hotel.api;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class ReservationsApiTest extends ApiTestBase {

    @Test
    void getAllReservations_shouldReturnSuccessTrue_andArray() {
        given()
                .when().get("/api/reservations")
                .then()
                .statusCode(200)
                .contentType(containsString("application/json"))
                .body("success", equalTo(true))
                .body("reservations", notNullValue())
                .body("reservations", instanceOf(java.util.List.class));
    }

    @Test
    void getOne_unknownReservation_shouldReturn404() {
        given()
                .when().get("/api/reservations/ZZZ999999")
                .then()
                .statusCode(404)
                .contentType(containsString("application/json"))
                .body("success", equalTo(false))
                .body("message", containsString("not found"));
    }

    @Test
    void addReservation_missingFields_shouldReturnSuccessFalse() {
        given()
                .contentType("application/json")
                .body("{}")
                .when().post("/api/reservations")
                .then()
                .statusCode(200)
                .contentType(containsString("application/json"))
                .body("success", equalTo(false))
                .body("message", notNullValue());
    }

    @Test
    void updateStatus_withoutStatus_shouldReturn400() {
        given()
                .contentType("application/json")
                .body("{}")
                .when().put("/api/reservations/ZZZ999999")
                .then()
                .statusCode(400)
                .contentType(containsString("application/json"))
                .body("success", equalTo(false))
                .body("message", equalTo("Status is required."));
    }

    @Test
    void cancel_unknownReservation_shouldReturnSuccessFalse() {
        given()
                .when().delete("/api/reservations/ZZZ999999")
                .then()
                .statusCode(200)
                .contentType(containsString("application/json"))
                .body("success", equalTo(false))
                .body("message", notNullValue());
    }
}