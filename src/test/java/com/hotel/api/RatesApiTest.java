package com.hotel.api;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class RatesApiTest extends ApiTestBase {

    @Test
    void getRates_shouldReturnSuccessTrue_andRatesArray() {
        given()
                .when().get("/api/rates")
                .then()
                .statusCode(200)
                .contentType(containsString("application/json"))
                .body("success", equalTo(true))
                .body("rates", notNullValue())
                .body("rates", instanceOf(java.util.List.class));
    }

    @Test
    void getRates_eachRateItem_shouldContainExpectedFields() {
        given()
                .when().get("/api/rates")
                .then()
                .statusCode(200)
                .contentType(containsString("application/json"))
                // If rates array is empty, this test still passes because it only checks structure when items exist
                .body("rates.size()", greaterThanOrEqualTo(0))
                .body("rates", everyItem(allOf(
                        hasKey("id"),
                        hasKey("roomNumber"),
                        hasKey("roomType"),
                        hasKey("rate"),
                        hasKey("status")
                )));
    }

    @Test
    void getRates_shouldNotReturnMaintenanceRooms() {
        given()
                .when().get("/api/rates")
                .then()
                .statusCode(200)
                // asserts no item's status equals "maintenance"
                .body("rates.status", not(hasItem("maintenance")));
    }

    @Test
    void nonGetMethod_shouldReturn404_methodNotAllowed() {
        given()
                .when().post("/api/rates")
                .then()
                .statusCode(404)
                .contentType(containsString("application/json"))
                .body("success", equalTo(false))
                .body("message", containsString("Method not allowed."));
    }
}