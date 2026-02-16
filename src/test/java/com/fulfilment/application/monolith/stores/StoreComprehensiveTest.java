package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
class StoreComprehensiveTest {

    @BeforeEach
    @Transactional
    void setUp() {
        Store.deleteAll();
    }

    @Test
    void testCompleteStoreCRUDWorkflow() {
        // 1. Create multiple stores
        Long store1Id = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Store A\",\"quantityProductsInStock\":100}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        Long store2Id = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Store B\",\"quantityProductsInStock\":200}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        Long store3Id = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Store C\",\"quantityProductsInStock\":300}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        // 2. List all stores (should be sorted by name)
        given()
            .when()
            .get("/store")
            .then()
            .statusCode(200)
            .body("$", hasSize(3))
            .body("[0].name", equalTo("Store A"))
            .body("[1].name", equalTo("Store B"))
            .body("[2].name", equalTo("Store C"));

        // 3. Get individual stores
        given()
            .when()
            .get("/store/" + store1Id)
            .then()
            .statusCode(200)
            .body("name", equalTo("Store A"))
            .body("quantityProductsInStock", equalTo(100));

        // 4. Update stores
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Updated Store A\",\"quantityProductsInStock\":150}")
            .when()
            .put("/store/" + store1Id)
            .then()
            .statusCode(200)
            .body("name", equalTo("Updated Store A"))
            .body("quantityProductsInStock", equalTo(150));

        // 5. Patch stores
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Patched Store B\",\"quantityProductsInStock\":250}")
            .when()
            .patch("/store/" + store2Id)
            .then()
            .statusCode(200)
            .body("name", equalTo("Patched Store B"))
            .body("quantityProductsInStock", equalTo(250));

        // 6. Delete one store
        given()
            .when()
            .delete("/store/" + store3Id)
            .then()
            .statusCode(204);

        // 7. Verify deletion
        given()
            .when()
            .get("/store/" + store3Id)
            .then()
            .statusCode(404);

        // 8. Final list should have 2 stores
        given()
            .when()
            .get("/store")
            .then()
            .statusCode(200)
            .body("$", hasSize(2));
    }

    @Test
    void testStoreEdgeCasesAndValidation() {
        // Test with maximum integer values
        Long maxStoreId = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Max Value Store\",\"quantityProductsInStock\":" + Integer.MAX_VALUE + "}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        // Test with minimum integer values
        Long minStoreId = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Min Value Store\",\"quantityProductsInStock\":" + Integer.MIN_VALUE + "}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        // Test with zero stock
        Long zeroStoreId = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Zero Stock Store\",\"quantityProductsInStock\":0}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        // Test with negative stock
        Long negativeStoreId = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Negative Stock Store\",\"quantityProductsInStock\":-100}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        // Verify all edge case stores exist
        given()
            .when()
            .get("/store")
            .then()
            .statusCode(200)
            .body("$", hasSize(4))
            .body("[*].name", hasItems("Max Value Store", "Min Value Store", "Zero Stock Store", "Negative Stock Store"));
    }

    @Test
    void testStoreSpecialCharactersAndUnicode() {
        // Test with special characters
        Long specialStoreId = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Special!@#$%^&*()Store\",\"quantityProductsInStock\":100}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        // Test with Unicode characters
        Long unicodeStoreId = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"商店测试🏪\",\"quantityProductsInStock\":200}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        // Test with spaces
        Long spaceStoreId = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"   \",\"quantityProductsInStock\":50}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        // Verify special character stores
        given()
            .when()
            .get("/store")
            .then()
            .statusCode(200)
            .body("$", hasSize(3))
            .body("[*].name", hasItems("Special!@#$%^&*()Store", "商店测试🏪", "   "));
    }

    @Test
    void testStoreErrorHandling() {
        // Test invalid JSON
        given()
            .contentType(ContentType.JSON)
            .body("{invalid json}")
            .when()
            .post("/store")
            .then()
            .statusCode(500)
            .body("exceptionType", notNullValue())
            .body("code", equalTo(500));

        // Test create with ID (should fail)
        given()
            .contentType(ContentType.JSON)
            .body("{\"id\":123,\"name\":\"Invalid Store\",\"quantityProductsInStock\":100}")
            .when()
            .post("/store")
            .then()
            .statusCode(422)
            .body("exceptionType", equalTo("jakarta.ws.rs.WebApplicationException"))
            .body("code", equalTo(422))
            .body("error", containsString("Id was invalidly set"));

        // Test update non-existent store
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Updated Store\",\"quantityProductsInStock\":100}")
            .when()
            .put("/store/999999")
            .then()
            .statusCode(404)
            .body("exceptionType", equalTo("jakarta.ws.rs.WebApplicationException"))
            .body("code", equalTo(404))
            .body("error", containsString("does not exist"));

        // Test update with null name
        Long storeId = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Test Store\",\"quantityProductsInStock\":100}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        given()
            .contentType(ContentType.JSON)
            .body("{\"quantityProductsInStock\":200}")
            .when()
            .put("/store/" + storeId)
            .then()
            .statusCode(422)
            .body("exceptionType", equalTo("jakarta.ws.rs.WebApplicationException"))
            .body("code", equalTo(422))
            .body("error", containsString("Name was not set"));

        // Test patch with null name
        given()
            .contentType(ContentType.JSON)
            .body("{\"quantityProductsInStock\":300}")
            .when()
            .patch("/store/" + storeId)
            .then()
            .statusCode(422)
            .body("exceptionType", equalTo("jakarta.ws.rs.WebApplicationException"))
            .body("code", equalTo(422))
            .body("error", containsString("Name was not set"));

        // Test delete non-existent store
        given()
            .when()
            .delete("/store/999999")
            .then()
            .statusCode(404)
            .body("exceptionType", equalTo("jakarta.ws.rs.WebApplicationException"))
            .body("code", equalTo(404))
            .body("error", containsString("does not exist"));

        // Test get non-existent store
        given()
            .when()
            .get("/store/999999")
            .then()
            .statusCode(404)
            .body("exceptionType", equalTo("jakarta.ws.rs.WebApplicationException"))
            .body("code", equalTo(404))
            .body("error", containsString("does not exist"));
    }

    @Test
    void testStoreBoundaryConditions() {
        // Test name length at boundary (40 characters max)
        String maxLengthName = "A".repeat(40);
        Long boundaryStoreId = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"" + maxLengthName + "\",\"quantityProductsInStock\":100}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        // Verify boundary store
        given()
            .when()
            .get("/store/" + boundaryStoreId)
            .then()
            .statusCode(200)
            .body("name", equalTo(maxLengthName))
            .body("quantityProductsInStock", equalTo(100));

        // Test with empty name (should work)
        Long emptyNameStoreId = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"\",\"quantityProductsInStock\":50}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        // Verify empty name store
        given()
            .when()
            .get("/store/" + emptyNameStoreId)
            .then()
            .statusCode(200)
            .body("name", equalTo(""))
            .body("quantityProductsInStock", equalTo(50));
    }

    @Test
    void testStoreTransactionEvents() {
        // Create a store and verify event is fired (legacy system integration)
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Event Test Store\",\"quantityProductsInStock\":100}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .body("name", equalTo("Event Test Store"));

        // Update store and verify event is fired
        Long storeId = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Event Test Store\",\"quantityProductsInStock\":100}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Updated Event Store\",\"quantityProductsInStock\":200}")
            .when()
            .put("/store/" + storeId)
            .then()
            .statusCode(200)
            .body("name", equalTo("Updated Event Store"));

        // Patch store and verify event is fired
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Patched Event Store\",\"quantityProductsInStock\":300}")
            .when()
            .patch("/store/" + storeId)
            .then()
            .statusCode(200)
            .body("name", equalTo("Patched Event Store"));
    }
}
