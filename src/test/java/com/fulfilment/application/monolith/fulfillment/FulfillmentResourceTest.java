package com.fulfilment.application.monolith.fulfillment;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
public class FulfillmentResourceTest {

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up existing data
        given()
            .when()
            .delete("/fulfillment/cleanup")
            .then()
            .statusCode(204);
    }

    @Test
    void testCreateFulfillmentAssociation_Success() {
        String requestBody = "{" +
            "\"productId\": 1," +
            "\"warehouseBusinessUnitCode\": \"WH-001\"," +
            "\"storeId\": 1," +
            "\"allocatedStock\": 50," +
            "\"maxCapacity\": 100" +
            "}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/fulfillment")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("allocatedStock", containsString("50"))
            .body("maxCapacity", containsString("100"));
    }

    @Test
    void testCreateFulfillmentAssociation_ConstraintViolation() {
        // First create a valid association
        String validRequest = "{" +
            "\"productId\": 1," +
            "\"warehouseBusinessUnitCode\": \"WH-001\"," +
            "\"storeId\": 1," +
            "\"allocatedStock\": 30," +
            "\"maxCapacity\": 50" +
            "}";

        given()
            .contentType(ContentType.JSON)
            .body(validRequest)
            .when()
            .post("/fulfillment")
            .then()
            .statusCode(201);

        // Try to create duplicate association - should fail
        String duplicateRequest = "{" +
            "\"productId\": 1," +
            "\"warehouseBusinessUnitCode\": \"WH-001\"," +
            "\"storeId\": 1," +
            "\"allocatedStock\": 20," +
            "\"maxCapacity\": 40" +
            "}";

        given()
            .contentType(ContentType.JSON)
            .body(duplicateRequest)
            .when()
            .post("/fulfillment")
            .then()
            .statusCode(400)
            .body("message", containsString("already associated"));
    }

    @Test
    void testGetFulfillmentsByProduct() {
        // First create an association
        String requestBody = "{" +
            "\"productId\": 1," +
            "\"warehouseBusinessUnitCode\": \"WH-001\"," +
            "\"storeId\": 1," +
            "\"allocatedStock\": 50," +
            "\"maxCapacity\": 100" +
            "}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/fulfillment")
            .then()
            .statusCode(201);

        // Get fulfillments by product
        given()
            .when()
            .get("/fulfillment/product/1")
            .then()
            .statusCode(200)
            .body("$", hasSize(1))
            .body("[0].productId", containsString("1"));
    }

    @Test
    void testGetFulfillmentsByWarehouse() {
        // First create an association
        String requestBody = "{" +
            "\"productId\": 1," +
            "\"warehouseBusinessUnitCode\": \"WH-001\"," +
            "\"storeId\": 1," +
            "\"allocatedStock\": 50," +
            "\"maxCapacity\": 100" +
            "}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/fulfillment")
            .then()
            .statusCode(201);

        // Get fulfillments by warehouse
        given()
            .when()
            .get("/fulfillment/warehouse/WH-001")
            .then()
            .statusCode(200)
            .body("$", hasSize(1))
            .body("[0].warehouseBusinessUnitCode", containsString("WH-001"));
    }

    @Test
    void testGetFulfillmentsByStore() {
        // First create an association
        String requestBody = "{" +
            "\"productId\": 1," +
            "\"warehouseBusinessUnitCode\": \"WH-001\"," +
            "\"storeId\": 1," +
            "\"allocatedStock\": 50," +
            "\"maxCapacity\": 100" +
            "}";

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/fulfillment")
            .then()
            .statusCode(201);

        // Get fulfillments by store
        given()
            .when()
            .get("/fulfillment/store/1")
            .then()
            .statusCode(200)
            .body("$", hasSize(1))
            .body("[0].storeId", containsString("1"));
    }

    @Test
    void testDeleteFulfillmentAssociation() {
        // First create an association
        String requestBody = "{" +
            "\"productId\": 1," +
            "\"warehouseBusinessUnitCode\": \"WH-001\"," +
            "\"storeId\": 1," +
            "\"allocatedStock\": 50," +
            "\"maxCapacity\": 100" +
            "}";

        Long associationId = given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/fulfillment")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Delete the association
        given()
            .when()
            .delete("/fulfillment/" + associationId)
            .then()
            .statusCode(204);

        // Verify it's gone
        given()
            .when()
            .get("/fulfillment/product/1")
            .then()
            .statusCode(200)
            .body("$", hasSize(0));
    }
}
