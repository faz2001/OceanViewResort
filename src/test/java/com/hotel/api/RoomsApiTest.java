package com.hotel.api;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class RoomsApiTest extends ApiTestBase {

    @Test
    void getRooms_shouldReturnSuccessTrue_andRoomsArray() {
        given()
                .when().get("/api/rooms")
                .then()
                .statusCode(200)
                .contentType(containsString("application/json"))
                .body("success", equalTo(true))
                .body("rooms", notNullValue())
                .body("rooms", instanceOf(java.util.List.class));
    }

    @Test
    void updateRoom_withoutStatus_shouldReturn400() {
        given()
                .contentType("application/json")
                .body("{}")
                .when().put("/api/rooms/1")
                .then()
                .statusCode(400)
                .contentType(containsString("application/json"))
                .body("success", equalTo(false))
                .body("message", equalTo("Status is required."));
    }

    @Test
    void updateRoom_withInvalidStatus_shouldReturn400() {
        given()
                .contentType("application/json")
                .body("{\"status\":\"invalidstatus\"}")
                .when().put("/api/rooms/1")
                .then()
                .statusCode(400)
                .contentType(containsString("application/json"))
                .body("success", equalTo(false))
                .body("message", equalTo("Invalid status value."));
    }

    @Test
    void updateRoom_withValidStatus_shouldReturnSuccessField() {
        given()
                .contentType("application/json")
                .body("{\"status\":\"available\"}")
                .when().put("/api/rooms/1")
                .then()
                .statusCode(200)
                .contentType(containsString("application/json"))
                .body("success", notNullValue())
                .body("message", notNullValue());
    }

    @Test
    void unknownEndpoint_shouldReturn404() {
        given()
                .when().post("/api/rooms")
                .then()
                .statusCode(404)
                .contentType(containsString("application/json"))
                .body("success", equalTo(false))
                .body("message", containsString("Unknown rooms endpoint"));
    }
}