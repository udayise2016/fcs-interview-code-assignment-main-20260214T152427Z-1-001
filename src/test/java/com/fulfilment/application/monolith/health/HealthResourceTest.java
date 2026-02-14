package com.fulfilment.application.monolith.health;

import static io.restassured.RestAssured.given;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
class HealthResourceTest {

    @Test
    void testHealthEndpoint() {
        given().when().get("/health").then().statusCode(200);
    }

    @Test
    void testLivenessEndpoint() {
        given().when().get("/health/live").then().statusCode(200);
    }

    @Test
    void testReadinessEndpoint() {
        given().when().get("/health/ready").then().statusCode(200);
    }

    @Test
    void testMetricsEndpoint() {
        given().when().get("/health/metrics").then().statusCode(200);
    }

    @Test
    void testCheckComponentEndpoints() {
        given().when().get("/health/check/memory").then().statusCode(200);
        given().when().get("/health/check/cpu").then().statusCode(200);
        given().when().get("/health/check/business").then().statusCode(200);
        given().when().get("/health/check/unknown").then().statusCode(404);
    }
}
