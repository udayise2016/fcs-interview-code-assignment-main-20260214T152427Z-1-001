package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
class StoreResourceIntegrationTest {

    private Long testStoreId;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up any existing test data
        Store.deleteAll();
        
        // Create a test store for use in tests
        testStoreId = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Test Store\",\"quantityProductsInStock\":100}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");
    }

    @Test
    void testGetAllStores_EmptyList() {
        // Clean up and test empty list
        Store.deleteAll();
        
        given()
            .when()
            .get("/store")
            .then()
            .statusCode(200)
            .body("$", hasSize(0));
    }

    @Test
    void testGetAllStores_WithStores() {
        given()
            .when()
            .get("/store")
            .then()
            .statusCode(200)
            .body("$", hasSize(greaterThan(0)))
            .body("[0].name", equalTo("Test Store"))
            .body("[0].quantityProductsInStock", equalTo(100));
    }

    @Test
    void testGetAllStores_SortedByName() {
        // Create additional stores with different names
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"A Store\",\"quantityProductsInStock\":50}")
            .when()
            .post("/store")
            .then()
            .statusCode(201);

        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Z Store\",\"quantityProductsInStock\":150}")
            .when()
            .post("/store")
            .then()
            .statusCode(201);

        given()
            .when()
            .get("/store")
            .then()
            .statusCode(200)
            .body("$", hasSize(3))
            .body("[0].name", equalTo("A Store"))
            .body("[1].name", equalTo("Test Store"))
            .body("[2].name", equalTo("Z Store"));
    }

    @Test
    void testGetSingleStore_Success() {
        given()
            .when()
            .get("/store/" + testStoreId)
            .then()
            .statusCode(200)
            .body("id", equalTo(testStoreId.intValue()))
            .body("name", equalTo("Test Store"))
            .body("quantityProductsInStock", equalTo(100));
    }

    @Test
    void testGetSingleStore_NotFound() {
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
    void testCreateStore_Success() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"New Store\",\"quantityProductsInStock\":200}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .body("name", equalTo("New Store"))
            .body("quantityProductsInStock", equalTo(200))
            .body("id", notNullValue());
    }

    @Test
    void testCreateStore_WithIdInBody() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"id\":123,\"name\":\"Invalid Store\",\"quantityProductsInStock\":50}")
            .when()
            .post("/store")
            .then()
            .statusCode(422)
            .body("exceptionType", equalTo("jakarta.ws.rs.WebApplicationException"))
            .body("code", equalTo(422))
            .body("error", containsString("Id was invalidly set"));
    }

    @Test
    void testCreateStore_DuplicateName() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Test Store\",\"quantityProductsInStock\":50}")
            .when()
            .post("/store")
            .then()
            .statusCode(500); // Database constraint violation
    }

    @Test
    void testCreateStore_EmptyName() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"\",\"quantityProductsInStock\":50}")
            .when()
            .post("/store")
            .then()
            .statusCode(201); // Should succeed - empty name is allowed by constraints
    }

    @Test
    void testCreateStore_NullName() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"quantityProductsInStock\":50}")
            .when()
            .post("/store")
            .then()
            .statusCode(500); // Database constraint violation
    }

    @Test
    void testCreateStore_NegativeStock() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Negative Stock Store\",\"quantityProductsInStock\":-10}")
            .when()
            .post("/store")
            .then()
            .statusCode(201); // Should succeed - negative stock is allowed
    }

    @Test
    void testUpdateStore_Success() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Updated Store\",\"quantityProductsInStock\":300}")
            .when()
            .put("/store/" + testStoreId)
            .then()
            .statusCode(200)
            .body("id", equalTo(testStoreId.intValue()))
            .body("name", equalTo("Updated Store"))
            .body("quantityProductsInStock", equalTo(300));
    }

    @Test
    void testUpdateStore_NotFound() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Updated Store\",\"quantityProductsInStock\":300}")
            .when()
            .put("/store/999999")
            .then()
            .statusCode(404)
            .body("exceptionType", equalTo("jakarta.ws.rs.WebApplicationException"))
            .body("code", equalTo(404))
            .body("error", containsString("does not exist"));
    }

    @Test
    void testUpdateStore_NullName() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"quantityProductsInStock\":300}")
            .when()
            .put("/store/" + testStoreId)
            .then()
            .statusCode(422)
            .body("exceptionType", equalTo("jakarta.ws.rs.WebApplicationException"))
            .body("code", equalTo(422))
            .body("error", containsString("Name was not set"));
    }

    @Test
    void testUpdateStore_EmptyName() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"\",\"quantityProductsInStock\":300}")
            .when()
            .put("/store/" + testStoreId)
            .then()
            .statusCode(200)
            .body("name", equalTo(""))
            .body("quantityProductsInStock", equalTo(300));
    }

    @Test
    void testPatchStore_Success() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Patched Store\",\"quantityProductsInStock\":400}")
            .when()
            .patch("/store/" + testStoreId)
            .then()
            .statusCode(200)
            .body("id", equalTo(testStoreId.intValue()))
            .body("name", equalTo("Patched Store"))
            .body("quantityProductsInStock", equalTo(400));
    }

    @Test
    void testPatchStore_NotFound() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Patched Store\",\"quantityProductsInStock\":400}")
            .when()
            .patch("/store/999999")
            .then()
            .statusCode(404)
            .body("exceptionType", equalTo("jakarta.ws.rs.WebApplicationException"))
            .body("code", equalTo(404))
            .body("error", containsString("does not exist"));
    }

    @Test
    void testPatchStore_NullName() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"quantityProductsInStock\":400}")
            .when()
            .patch("/store/" + testStoreId)
            .then()
            .statusCode(422)
            .body("exceptionType", equalTo("jakarta.ws.rs.WebApplicationException"))
            .body("code", equalTo(422))
            .body("error", containsString("Name was not set"));
    }

    @Test
    void testPatchStore_OnlyName() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Name Only Patch\"}")
            .when()
            .patch("/store/" + testStoreId)
            .then()
            .statusCode(200)
            .body("name", equalTo("Name Only Patch"))
            .body("quantityProductsInStock", equalTo(100)); // Should remain unchanged
    }

    @Test
    void testPatchStore_OnlyStock() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"quantityProductsInStock\":500}")
            .when()
            .patch("/store/" + testStoreId)
            .then()
            .statusCode(200)
            .body("name", equalTo("Test Store")) // Should remain unchanged
            .body("quantityProductsInStock", equalTo(500));
    }

    @Test
    void testPatchStore_ZeroStock() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"Zero Stock\",\"quantityProductsInStock\":0}")
            .when()
            .patch("/store/" + testStoreId)
            .then()
            .statusCode(200)
            .body("name", equalTo("Zero Stock"))
            .body("quantityProductsInStock", equalTo(0));
    }

    @Test
    void testDeleteStore_Success() {
        given()
            .when()
            .delete("/store/" + testStoreId)
            .then()
            .statusCode(204);

        // Verify store is deleted
        given()
            .when()
            .get("/store/" + testStoreId)
            .then()
            .statusCode(404);
    }

    @Test
    void testDeleteStore_NotFound() {
        given()
            .when()
            .delete("/store/999999")
            .then()
            .statusCode(404)
            .body("exceptionType", equalTo("jakarta.ws.rs.WebApplicationException"))
            .body("code", equalTo(404))
            .body("error", containsString("does not exist"));
    }

    @Test
    void testErrorMapper_GenericException() {
        // Test with invalid JSON to trigger a generic exception
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
    void testErrorMapper_WithoutMessage() {
        // This test verifies the error mapper handles exceptions without messages
        given()
            .when()
            .get("/store/invalid-id") // This will cause a NumberFormatException in the path param
            .then()
            .statusCode(404);
    }

    @Test
    void testStoreNameMaxLength() {
        // Test with name exactly at the maximum length (40 characters)
        String maxLengthName = "A".repeat(40);
        
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"" + maxLengthName + "\",\"quantityProductsInStock\":100}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .body("name", equalTo(maxLengthName));
    }

    @Test
    void testStoreNameExceedsMaxLength() {
        // Test with name exceeding maximum length
        String tooLongName = "A".repeat(41);
        
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"" + tooLongName + "\",\"quantityProductsInStock\":100}")
            .when()
            .post("/store")
            .then()
            .statusCode(500); // Database constraint violation
    }
}
