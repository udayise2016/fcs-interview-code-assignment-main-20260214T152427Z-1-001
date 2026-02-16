package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class StoreResourceExtendedTest {

    @Test
    void testStoreResourceErrorHandling() {
        // Test error mapper with invalid JSON
        given()
            .contentType(ContentType.JSON)
            .body("{invalid json}")
            .when()
            .post("/store")
            .then()
            .statusCode(500)
            .body("exceptionType", notNullValue())
            .body("code", equalTo(500));
    }

    @Test
    void testStoreResourceCreateWithEmptyName() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"\",\"quantityProductsInStock\":50}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .body("name", equalTo(""))
            .body("quantityProductsInStock", equalTo(50));
    }

    @Test
    void testStoreResourceCreateWithNegativeStock() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Negative Stock\",\"quantityProductsInStock\":-50}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .body("name", equalTo("Negative Stock"))
            .body("quantityProductsInStock", equalTo(-50));
    }

    @Test
    void testStoreResourceCreateWithZeroStock() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Zero Stock\",\"quantityProductsInStock\":0}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .body("name", equalTo("Zero Stock"))
            .body("quantityProductsInStock", equalTo(0));
    }

    @Test
    void testStoreResourceCreateWithMaxIntStock() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Max Stock\",\"quantityProductsInStock\":" + Integer.MAX_VALUE + "}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .body("name", equalTo("Max Stock"))
            .body("quantityProductsInStock", equalTo(Integer.MAX_VALUE));
    }

    @Test
    void testStoreResourceCreateWithMinIntStock() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Min Stock\",\"quantityProductsInStock\":" + Integer.MIN_VALUE + "}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .body("name", equalTo("Min Stock"))
            .body("quantityProductsInStock", equalTo(Integer.MIN_VALUE));
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
            .body("{\"name\":\"商店测试🏪\",\"quantityProductsInStock\":100}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .body("name", equalTo("商店测试🏪"))
            .body("quantityProductsInStock", equalTo(100));
    }

    @Test
    void testStoreResourceCreateWithSpacesName() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"   \",\"quantityProductsInStock\":100}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .body("name", equalTo("   "))
            .body("quantityProductsInStock", equalTo(100));
    }

    @Test
    void testStoreResourceCreateWithLongName() {
        String longName = "A".repeat(39); // Just under the limit
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"" + longName + "\",\"quantityProductsInStock\":100}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .body("name", equalTo(longName))
            .body("quantityProductsInStock", equalTo(100));
    }

    @Test
    void testStoreResourceUpdateWithEmptyName() {
        // Create a store first
        Long id = given()
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
            .put("/store/" + id)
            .then()
            .statusCode(200)
            .body("name", equalTo(""))
            .body("quantityProductsInStock", equalTo(200));
    }

    @Test
    void testStoreResourceUpdateWithNegativeStock() {
        // Create a store first
        Long id = given()
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
            .body("{\"name\":\"Updated Store\",\"quantityProductsInStock\":-100}")
            .when()
            .put("/store/" + id)
            .then()
            .statusCode(200)
            .body("name", equalTo("Updated Store"))
            .body("quantityProductsInStock", equalTo(-100));
    }

    @Test
    void testStoreResourcePatchOnlyName() {
        // Create a store first
        Long id = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Original Store\",\"quantityProductsInStock\":100}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        // Patch only name
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Patched Store\"}")
            .when()
            .patch("/store/" + id)
            .then()
            .statusCode(200)
            .body("name", equalTo("Patched Store"))
            .body("quantityProductsInStock", equalTo(100)); // Should remain unchanged
    }

    @Test
    void testStoreResourcePatchOnlyStock() {
        // Create a store first
        Long id = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Original Store\",\"quantityProductsInStock\":100}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        // Patch only stock
        given()
            .contentType(ContentType.JSON)
            .body("{\"quantityProductsInStock\":300}")
            .when()
            .patch("/store/" + id)
            .then()
            .statusCode(200)
            .body("name", equalTo("Original Store")) // Should remain unchanged
            .body("quantityProductsInStock", equalTo(300));
    }

    @Test
    void testStoreResourcePatchWithZeroStock() {
        // Create a store first
        Long id = given()
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
            .patch("/store/" + id)
            .then()
            .statusCode(200)
            .body("name", equalTo("Zero Stock Store"))
            .body("quantityProductsInStock", equalTo(0));
    }
}
