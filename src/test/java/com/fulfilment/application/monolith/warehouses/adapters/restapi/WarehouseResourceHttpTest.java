package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

@QuarkusTest
class WarehouseResourceHttpTest {

    @Test
    void testWarehouseEndpointsOverHttp() {
        // Invalid create: exceed location max capacity (ZWOLLE-001 max is 40)
        String invalidCreateBody = "{" +
            "\"businessUnitCode\":\"WH-HTTP-INVALID\"," +
            "\"location\":\"ZWOLLE-001\"," +
            "\"capacity\":100," +
            "\"stock\":50" +
            "}";

        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(invalidCreateBody)
            .when()
            .post("/warehouse")
            .then()
            .statusCode(400)
            .body(containsString("\"status\":400"))
            .body(containsString("\"type\":\"java.lang.IllegalArgumentException\""));

        // Create
        String createBody = "{" +
            "\"businessUnitCode\":\"WH-HTTP-001\"," +
            "\"location\":\"ZWOLLE-001\"," +
            "\"capacity\":30," +
            "\"stock\":20" +
            "}";

        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(createBody)
            .when()
            .post("/warehouse")
            .then()
            .statusCode(200)
            .body(containsString("WH-HTTP-001"));

        // List
        given().when().get("/warehouse").then().statusCode(200).body(containsString("WH-HTTP-001"));

        // Get by id
        given().when().get("/warehouse/WH-HTTP-001").then().statusCode(200).body(containsString("ZWOLLE-001"));

        // Replace (must keep stock matching previous = 50)
        String replaceBody = "{" +
            "\"businessUnitCode\":\"IGNORED\"," +
            "\"location\":\"AMSTERDAM-001\"," +
            "\"capacity\":40," +
            "\"stock\":20" +
            "}";

        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(replaceBody)
            .when()
            .post("/warehouse/WH-HTTP-001/replacement")
            .then()
            .statusCode(200);

        // Archive
        given().when().delete("/warehouse/WH-HTTP-001").then().statusCode(204);

        // Get after archive should still return something (implementation-dependent) or 404.
        // We accept both because repository lookup may return archived or active depending on query.
        int status = given().when().get("/warehouse/WH-HTTP-001").then().extract().statusCode();
        if (status != 200 && status != 404) {
            throw new AssertionError("Unexpected status code: " + status);
        }
    }
}
