package com.fulfilment.application.monolith.products;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class ProductResourceTest {

    @Test
    public void testCreateProduct() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"TEST-PRODUCT\"}")
            .when()
            .post("/product")
            .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", containsString("TEST-PRODUCT"));
    }

    @Test
    public void testListProducts() {
        given()
            .when()
            .get("/product")
            .then()
            .statusCode(200);
    }

    @Test
    public void testGetProductById() {
        String productId = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"GET-TEST\"}")
            .when()
            .post("/product")
            .then()
            .extract()
            .path("id")
            .toString();

        given()
            .when()
            .get("/product/" + productId)
            .then()
            .statusCode(200)
            .body("name", containsString("GET-TEST"));
    }

    @Test
    public void testDeleteProduct() {
        String productId = given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"DELETE-TEST\"}")
            .when()
            .post("/product")
            .then()
            .extract()
            .path("id")
            .toString();

        given()
            .when()
            .delete("/product/" + productId)
            .then()
            .statusCode(204);
    }
}
