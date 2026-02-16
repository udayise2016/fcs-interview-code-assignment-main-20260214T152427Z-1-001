package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class StoreAdditionalCoverageTest {

    @BeforeEach
    @Transactional
    void setUp() {
        Store.deleteAll();
    }

    @Test
    void testStoreResourceErrorMapper() {
        // Test error mapper with invalid JSON
        given()
            .contentType(ContentType.JSON)
            .body("{invalid json}")
            .when()
            .post("/store")
            .then()
            .statusCode(400) // JSON parsing error returns 400
            .body("exceptionType", notNullValue());
    }

    @Test
    void testStoreResourceCreateWithMaxValues() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Max Value Store\",\"quantityProductsInStock\":" + Integer.MAX_VALUE + "}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .body("name", equalTo("Max Value Store"))
            .body("quantityProductsInStock", equalTo(Integer.MAX_VALUE));
    }

    @Test
    void testStoreResourceCreateWithMinValues() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Min Value Store\",\"quantityProductsInStock\":" + Integer.MIN_VALUE + "}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .body("name", equalTo("Min Value Store"))
            .body("quantityProductsInStock", equalTo(Integer.MIN_VALUE));
    }

    @Test
    void testStoreResourceCreateWithZeroStock() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Zero Stock Store\",\"quantityProductsInStock\":0}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .body("name", equalTo("Zero Stock Store"))
            .body("quantityProductsInStock", equalTo(0));
    }

    @Test
    void testStoreResourceCreateWithNegativeStock() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Negative Stock Store\",\"quantityProductsInStock\":-100}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .body("name", equalTo("Negative Stock Store"))
            .body("quantityProductsInStock", equalTo(-100));
    }

    @Test
    void testStoreResourceCreateWithSpecialCharacters() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Special!@#$%^&*()Store\",\"quantityProductsInStock\":100}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .body("name", equalTo("Special!@#$%^&*()Store"))
            .body("quantityProductsInStock", equalTo(100));
    }

    @Test
    void testStoreResourceCreateWithUnicodeCharacters() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"商店测试🏪\",\"quantityProductsInStock\":200}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .body("name", equalTo("商店测试🏪"))
            .body("quantityProductsInStock", equalTo(200));
    }

    @Test
    void testStoreResourceCreateWithSpacesName() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"   \",\"quantityProductsInStock\":50}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .body("name", equalTo("   "))
            .body("quantityProductsInStock", equalTo(50));
    }

    @Test
    void testStoreResourceCreateWithEmptyName() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"\",\"quantityProductsInStock\":75}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .body("name", equalTo(""))
            .body("quantityProductsInStock", equalTo(75));
    }

    @Test
    void testStoreResourceCreateWithMaxLengthName() {
        String maxLengthName = "A".repeat(40);
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"" + maxLengthName + "\",\"quantityProductsInStock\":100}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .body("name", equalTo(maxLengthName))
            .body("quantityProductsInStock", equalTo(100));
    }

    @Test
    void testStoreResourceUpdateWithEmptyName() {
        // Create a store first
        Long storeId = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Original Store\",\"quantityProductsInStock\":100}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        // Update with empty name
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"\",\"quantityProductsInStock\":200}")
            .when()
            .put("/store/" + storeId)
            .then()
            .statusCode(200)
            .body("name", equalTo(""))
            .body("quantityProductsInStock", equalTo(200));
    }

    @Test
    void testStoreResourceUpdateWithNegativeStock() {
        // Create a store first
        Long storeId = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Original Store\",\"quantityProductsInStock\":100}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        // Update with negative stock
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Updated Store\",\"quantityProductsInStock\":-200}")
            .when()
            .put("/store/" + storeId)
            .then()
            .statusCode(200)
            .body("name", equalTo("Updated Store"))
            .body("quantityProductsInStock", equalTo(-200));
    }

    @Test
    void testStoreResourceUpdateWithMaxIntStock() {
        // Create a store first
        Long storeId = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Original Store\",\"quantityProductsInStock\":100}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        // Update with max int stock
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Max Stock Store\",\"quantityProductsInStock\":" + Integer.MAX_VALUE + "}")
            .when()
            .put("/store/" + storeId)
            .then()
            .statusCode(200)
            .body("name", equalTo("Max Stock Store"))
            .body("quantityProductsInStock", equalTo(Integer.MAX_VALUE));
    }

    @Test
    void testStoreResourcePatchOnlyName() {
        // Create a store first
        Long storeId = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Original Store\",\"quantityProductsInStock\":100}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        // Patch only name (note: the patch logic requires both name and quantity)
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Patched Store\",\"quantityProductsInStock\":200}")
            .when()
            .patch("/store/" + storeId)
            .then()
            .statusCode(200)
            .body("name", equalTo("Patched Store"))
            .body("quantityProductsInStock", equalTo(200));
    }

    @Test
    void testStoreResourcePatchOnlyStock() {
        // Create a store first
        Long storeId = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Original Store\",\"quantityProductsInStock\":100}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        // Patch only stock (note: the patch logic requires both name and quantity)
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Original Store\",\"quantityProductsInStock\":300}")
            .when()
            .patch("/store/" + storeId)
            .then()
            .statusCode(200)
            .body("name", equalTo("Original Store"))
            .body("quantityProductsInStock", equalTo(300));
    }

    @Test
    void testStoreResourcePatchWithZeroStock() {
        // Create a store first
        Long storeId = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Original Store\",\"quantityProductsInStock\":100}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        // Patch with zero stock
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Zero Stock Store\",\"quantityProductsInStock\":0}")
            .when()
            .patch("/store/" + storeId)
            .then()
            .statusCode(200)
            .body("name", equalTo("Zero Stock Store"))
            .body("quantityProductsInStock", equalTo(0));
    }

    @Test
    void testStoreResourcePatchWithNegativeStock() {
        // Create a store first
        Long storeId = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Original Store\",\"quantityProductsInStock\":100}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        // Patch with negative stock
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Negative Stock Store\",\"quantityProductsInStock\":-50}")
            .when()
            .patch("/store/" + storeId)
            .then()
            .statusCode(200)
            .body("name", equalTo("Negative Stock Store"))
            .body("quantityProductsInStock", equalTo(-50));
    }

    @Test
    void testStoreResourceErrorHandling() {
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
        given()
            .contentType(ContentType.JSON)
            .body("{\"quantityProductsInStock\":200}")
            .when()
            .put("/store/999999")
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
            .patch("/store/999999")
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
}
