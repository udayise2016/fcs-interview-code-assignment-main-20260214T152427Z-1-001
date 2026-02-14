package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

@QuarkusTest
class StoreResourceTest {

    @Test
    void testCreateListGetUpdatePatchDeleteStore() {
        String createBody = "{\"name\":\"Store-A\",\"quantityProductsInStock\":10}";

        // Create
        Long id = given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(createBody)
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getLong("id");

        // List
        given().when().get("/store").then().statusCode(200).body(containsString("Store-A"));

        // Get single
        given().when().get("/store/" + id).then().statusCode(200).body(containsString("Store-A"));

        // Update
        String updateBody = "{\"name\":\"Store-A2\",\"quantityProductsInStock\":20}";
        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(updateBody)
            .when()
            .put("/store/" + id)
            .then()
            .statusCode(200)
            .body(containsString("Store-A2"));

        // Patch
        String patchBody = "{\"name\":\"Store-A3\",\"quantityProductsInStock\":30}";
        given()
            .contentType(MediaType.APPLICATION_JSON)
            .body(patchBody)
            .when()
            .patch("/store/" + id)
            .then()
            .statusCode(200)
            .body(containsString("Store-A3"));

        // Delete
        given().when().delete("/store/" + id).then().statusCode(204);

        // Get deleted should 404
        given().when().get("/store/" + id).then().statusCode(404);
    }

    @Test
    void testGetStoreNotFound() {
        given().when().get("/store/999999").then().statusCode(404);
    }

    @Test
    void testCreateInvalidStore() {
        // id must be null
        String body = "{\"id\":1,\"name\":\"X\",\"quantityProductsInStock\":0}";
        given().contentType(MediaType.APPLICATION_JSON).body(body).when().post("/store").then().statusCode(422);
    }
}
