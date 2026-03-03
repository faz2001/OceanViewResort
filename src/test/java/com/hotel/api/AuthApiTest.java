package com.hotel.api;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class AuthApiTest extends ApiTestBase {

    @Test
    void login_missingFields_shouldReturnSuccessFalse_andMessage() {
        given()
                .contentType("application/json")
                .body("{\"username\":\"\",\"password\":\"\"}")
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .contentType(containsString("application/json"))
                .body("success", equalTo(false))
                .body("message", equalTo("Username and password are required."));
    }

    @Test
    void login_invalidCredentials_shouldReturnSuccessFalse() {
        given()
                .contentType("application/json")
                .body("{\"username\":\"admin\",\"password\":\"wrong\"}")
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .contentType(containsString("application/json"))
                .body("success", equalTo(false))
                .body("message", containsString("Invalid credentials"));
    }

    @Test
    void login_validAdmin_shouldReturnSuccessTrue_andUserObject() {
        given()
                .contentType("application/json")
                .body("{\"username\":\"admin\",\"password\":\"admin123\"}")
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .contentType(containsString("application/json"))
                .body("success", equalTo(true))
                .body("message", equalTo("Login successful."))
                .body("user", notNullValue()); // user is embedded as an object
    }

    @Test
    void staffLogin_validCredentials_shouldSucceed() {
        given()
                .contentType("application/json")
                .body("{\"username\":\"staff\",\"password\":\"staff123\"}")
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .contentType(containsString("application/json"))
                .body("success", equalTo(true))
                .body("message", equalTo("Login successful."))
                .body("user", notNullValue());
    }

    @Test
    void login_correctUsername_wrongPassword_shouldFail() {
        given()
                .contentType("application/json")
                .body("{\"username\":\"admin\",\"password\":\"wrong\"}")
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("success", equalTo(false))
                .body("message", containsString("Invalid credentials"));
    }

    @Test
    void login_wrongUsername_correctPassword_shouldFail() {
        given()
                .contentType("application/json")
                .body("{\"username\":\"wrong\",\"password\":\"admin123\"}")
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("success", equalTo(false))
                .body("message", containsString("Invalid credentials"));
    }
}