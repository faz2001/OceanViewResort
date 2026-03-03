package com.hotel.api;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class StaffApiTest extends ApiTestBase {

    @Test
    void getAllStaff_shouldReturnSuccessTrue_andArray() {
        given()
                .when().get("/api/staff")
                .then()
                .statusCode(200)
                .contentType(containsString("application/json"))
                .body("success", equalTo(true))
                .body("staff", notNullValue())
                .body("staff", instanceOf(java.util.List.class));
    }

    @Test
    void getStaff_invalidId_shouldReturn400() {
        given()
                .when().get("/api/staff/abc")
                .then()
                .statusCode(400)
                .contentType(containsString("application/json"))
                .body("success", equalTo(false))
                .body("message", equalTo("Invalid staff ID."));
    }

    @Test
    void getStaff_notFound_shouldReturn200_successFalse() {
        given()
                .when().get("/api/staff/999999")
                .then()
                .statusCode(200)
                .contentType(containsString("application/json"))
                .body("success", equalTo(false))
                .body("message", equalTo("Staff account not found."));
    }

    @Test
    void createStaff_shouldReturn201_onSuccess_or200_onValidationError() {
        // Use a unique username to reduce duplicate conflicts
        String uniqueUsername = "teststaff_" + System.currentTimeMillis();

        String body = "{"
                + "\"fullName\":\"Test Staff\","
                + "\"username\":\"" + uniqueUsername + "\","
                + "\"password\":\"pass123\","
                + "\"email\":\"" + uniqueUsername + "@example.com\","
                + "\"role\":\"staff\""
                + "}";

        given()
                .contentType("application/json")
                .body(body)
                .when().post("/api/staff")
                .then()
                .contentType(containsString("application/json"))
                .statusCode(anyOf(is(201), is(200)))
                .body("success", notNullValue())
                .body("message", notNullValue());
    }

    @Test
    void updateStaff_invalidId_shouldReturn400() {
        given()
                .contentType("application/json")
                .body("{\"fullName\":\"X\"}")
                .when().put("/api/staff/abc")
                .then()
                .statusCode(400)
                .contentType(containsString("application/json"))
                .body("success", equalTo(false))
                .body("message", equalTo("Invalid staff ID."));
    }

    @Test
    void deleteStaff_invalidId_shouldReturn400() {
        given()
                .when().delete("/api/staff/abc")
                .then()
                .statusCode(400)
                .contentType(containsString("application/json"))
                .body("success", equalTo(false))
                .body("message", equalTo("Invalid staff ID."));
    }
}