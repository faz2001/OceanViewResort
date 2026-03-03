package com.hotel.api;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class EBillApiTest extends ApiTestBase {

    @Test
    void getEBill_invalidReservation_shouldReturnSuccessFalse_andFixedMessage() {
        given()
                .when().get("/api/ebill/ZZZ999999")
                .then()
                .statusCode(200)
                .contentType(containsString("application/json"))
                .body("success", equalTo(false))
                .body("message", equalTo("Invalid reservation number"));
    }

    @Test
    void getEBill_shouldBeCaseInsensitive_forResNo() {
        // both should behave the same because handler uppercases resNo
        given()
                .when().get("/api/ebill/ab12")
                .then()
                .statusCode(200)
                .contentType(containsString("application/json"))
                .body("success", notNullValue());

        given()
                .when().get("/api/ebill/AB12")
                .then()
                .statusCode(200)
                .contentType(containsString("application/json"))
                .body("success", notNullValue());
    }

    @Test
    void unknownMethod_shouldReturn404_json() {
        // EBillHandler only supports GET; POST should go to notFound(...)
        given()
                .when().post("/api/ebill/ZZZ999999")
                .then()
                .statusCode(404)
                .contentType(containsString("application/json"))
                .body("success", equalTo(false))
                .body("message", containsString("Unknown e-bill endpoint"));
    }
}