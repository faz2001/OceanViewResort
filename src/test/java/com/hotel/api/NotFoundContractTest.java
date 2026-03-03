package com.hotel.api;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class NotFoundContractTest extends ApiTestBase {

    @Test
    void unknownEndpoint_shouldReturnJson404_withSuccessFalse() {
        given()
                .when().get("/api/auth/unknown")
                .then()
                .statusCode(404)
                .contentType(containsString("application/json"))
                .body("success", equalTo(false))
                .body("message", containsString("Unknown"));
    }
}