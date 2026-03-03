package com.hotel.api;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class CorsOptionsTest extends ApiTestBase {

    @Test
    void options_preflight_shouldReturn204_andCorsHeaders() {
        given()
                .when().options("/api/rates")
                .then()
                .statusCode(204)
                .header("Access-Control-Allow-Origin", equalTo("*"))
                .header("Access-Control-Allow-Methods", containsString("GET"))
                .header("Access-Control-Allow-Headers", containsString("Content-Type"));
    }
}