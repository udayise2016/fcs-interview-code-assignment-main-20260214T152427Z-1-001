package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
public class WarehouseResourceIntegrationTest {

    @Inject
    WarehouseRepository warehouseRepository;

    @Test
    @Transactional
    void testListWarehouses() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "LIST-001";
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = 100;
        warehouse.stock = 50;
        warehouseRepository.create(warehouse);
        
        given()
            .when()
            .get("/warehouse")
            .then()
            .statusCode(200)
            .body("$", hasSize(greaterThan(0)));
    }

    @Test
    void testCreateWarehouse() {
        String requestBody = "{" +
            "\"businessUnitCode\":\"CREATE-001\"," +
            "\"location\":\"ZWOLLE-001\"," +
            "\"capacity\":30," +  // Within location max capacity
            "\"stock\":20" +
            "}";
        
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/warehouse")
            .then()
            .statusCode(200)
            .body("businessUnitCode", containsString("CREATE-001"))
            .body("location", containsString("ZWOLLE-001"))
            .body("capacity", equalTo(30))
            .body("stock", equalTo(20));
    }

    @Test
    @Transactional
    void testGetWarehouseById() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "GET-001";
        warehouse.location = "AMSTERDAM-001";
        warehouse.capacity = 200;
        warehouse.stock = 100;
        warehouseRepository.create(warehouse);
        
        given()
            .when()
            .get("/warehouse/GET-001")
            .then()
            .statusCode(200)
            .body("businessUnitCode", containsString("GET-001"))
            .body("location", containsString("AMSTERDAM-001"));
    }

    @Test
    @Transactional
    void testArchiveWarehouse() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "ARCHIVE-001";
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = 100;
        warehouse.stock = 50;
        warehouseRepository.create(warehouse);
        
        given()
            .when()
            .delete("/warehouse/ARCHIVE-001")
            .then()
            .statusCode(204);
    }

    @Test
    @Transactional
    void testReplaceWarehouse() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "REPLACE-001";
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = 100;
        warehouse.stock = 50;
        warehouseRepository.create(warehouse);
        
        String requestBody = "{" +
            "\"businessUnitCode\":\"IGNORED\"," +
            "\"location\":\"AMSTERDAM-001\"," +
            "\"capacity\":150," +
            "\"stock\":50" +
            "}";
        
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/warehouse/REPLACE-001/replacement")
            .then()
            .statusCode(200)
            .body("businessUnitCode", containsString("REPLACE-001"));
    }

    @Test
    void testCreateWarehouseInvalidCapacity() {
        String requestBody = "{" +
            "\"businessUnitCode\":\"INVALID-001\"," +
            "\"location\":\"ZWOLLE-001\"," +
            "\"capacity\":200," +  // Exceeds location max capacity
            "\"stock\":50" +
            "}";
        
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
            .post("/warehouse")
            .then()
            .statusCode(400)
            .body(containsString("\"status\":400"));
    }
}
