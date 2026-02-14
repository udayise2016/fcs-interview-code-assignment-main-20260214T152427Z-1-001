package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ArchiveWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ReplaceWarehouseUseCase;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class WarehouseResourceImplTest {

    @Inject
    WarehouseResourceImpl warehouseResource;

    @Inject
    WarehouseRepository warehouseRepository;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        warehouseRepository.getAll().forEach(warehouse -> warehouseRepository.remove(warehouse));
    }

    @Test
    void testListAllWarehousesUnits() {
        // Given
        Warehouse domainWarehouse1 = createTestWarehouse("WH001", "ZWOLLE-001", 100, 50);
        Warehouse domainWarehouse2 = createTestWarehouse("WH002", "AMSTERDAM-001", 200, 100);
        
        warehouseRepository.create(domainWarehouse1);
        warehouseRepository.create(domainWarehouse2);

        // When
        List<com.warehouse.api.beans.Warehouse> result = warehouseResource.listAllWarehousesUnits();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        com.warehouse.api.beans.Warehouse apiWarehouse1 = result.stream()
            .filter(w -> "WH001".equals(w.getBusinessUnitCode()))
            .findFirst()
            .orElse(null);
        assertNotNull(apiWarehouse1);
        assertEquals("WH001", apiWarehouse1.getBusinessUnitCode());
        assertEquals("ZWOLLE-001", apiWarehouse1.getLocation());
        assertEquals(100, apiWarehouse1.getCapacity());
        assertEquals(50, apiWarehouse1.getStock());
    }

    @Test
    void testListAllWarehousesUnitsWhenEmpty() {
        // When
        List<com.warehouse.api.beans.Warehouse> result = warehouseResource.listAllWarehousesUnits();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCreateANewWarehouseUnit() {
        // Given
        com.warehouse.api.beans.Warehouse apiWarehouse = new com.warehouse.api.beans.Warehouse();
        apiWarehouse.setBusinessUnitCode("WH001");
        apiWarehouse.setLocation("ZWOLLE-001");
        apiWarehouse.setCapacity(30);
        apiWarehouse.setStock(20);

        // When
        com.warehouse.api.beans.Warehouse result = warehouseResource.createANewWarehouseUnit(apiWarehouse);

        // Then
        assertNotNull(result);
        assertEquals("WH001", result.getBusinessUnitCode());
        assertEquals("ZWOLLE-001", result.getLocation());
        assertEquals(30, result.getCapacity());
        assertEquals(20, result.getStock());

        // Verify warehouse was created in repository
        Warehouse createdWarehouse = warehouseRepository.findByBusinessUnitCode("WH001");
        assertNotNull(createdWarehouse);
        assertEquals("WH001", createdWarehouse.businessUnitCode);
        assertEquals("ZWOLLE-001", createdWarehouse.location);
    }

    @Test
    void testCreateANewWarehouseUnitWithInvalidData() {
        // Given
        com.warehouse.api.beans.Warehouse apiWarehouse = new com.warehouse.api.beans.Warehouse();
        apiWarehouse.setBusinessUnitCode(""); // Invalid empty code
        apiWarehouse.setLocation("ZWOLLE-001");
        apiWarehouse.setCapacity(100);
        apiWarehouse.setStock(50);

        // When & Then
        assertThrows(Exception.class, () -> warehouseResource.createANewWarehouseUnit(apiWarehouse));
    }

    @Test
    void testArchiveAWarehouseUnitByID() {
        // Given
        Warehouse warehouse = createTestWarehouse("WH001", "ZWOLLE-001", 100, 50);
        warehouseRepository.create(warehouse);

        // When
        warehouseResource.archiveAWarehouseUnitByID("WH001");

        // Then
        Warehouse archivedWarehouse = warehouseRepository.findByBusinessUnitCode("WH001");
        assertNotNull(archivedWarehouse);
        assertNotNull(archivedWarehouse.archivedAt);
    }

    @Test
    void testArchiveAWarehouseUnitByIDWhenNotFound() {
        // When & Then
        assertThrows(Exception.class, () -> warehouseResource.archiveAWarehouseUnitByID("NONEXISTENT"));
    }

    @Test
    void testGetAWarehouseUnitByID() {
        // Given
        Warehouse warehouse = createTestWarehouse("WH001", "ZWOLLE-001", 100, 50);
        warehouseRepository.create(warehouse);

        // When
        com.warehouse.api.beans.Warehouse result = warehouseResource.getAWarehouseUnitByID("WH001");

        // Then
        assertNotNull(result);
        assertEquals("WH001", result.getBusinessUnitCode());
        assertEquals("ZWOLLE-001", result.getLocation());
        assertEquals(100, result.getCapacity());
        assertEquals(50, result.getStock());
    }

    @Test
    void testGetAWarehouseUnitByIDWhenNotFound() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> warehouseResource.getAWarehouseUnitByID("NONEXISTENT"));
    }

    @Test
    void testReplaceTheCurrentActiveWarehouse() {
        // Given
        Warehouse existingWarehouse = createTestWarehouse("WH001", "ZWOLLE-001", 100, 50);
        warehouseRepository.create(existingWarehouse);

        com.warehouse.api.beans.Warehouse newWarehouseData = new com.warehouse.api.beans.Warehouse();
        newWarehouseData.setBusinessUnitCode("WH001");
        newWarehouseData.setLocation("AMSTERDAM-001");
        newWarehouseData.setCapacity(200);
        newWarehouseData.setStock(100);

        // When
        com.warehouse.api.beans.Warehouse result = warehouseResource.replaceTheCurrentActiveWarehouse("WH001", newWarehouseData);

        // Then
        assertNotNull(result);
        assertEquals("WH001", result.getBusinessUnitCode());
        assertEquals("AMSTERDAM-001", result.getLocation());
        assertEquals(200, result.getCapacity());
        assertEquals(100, result.getStock());

        // Verify old warehouse was archived
        Warehouse oldWarehouse = warehouseRepository.getAll().stream()
            .filter(w -> "WH001".equals(w.businessUnitCode) && w.archivedAt != null)
            .findFirst()
            .orElse(null);
        assertNotNull(oldWarehouse);
        assertNotNull(oldWarehouse.archivedAt);
    }

    @Test
    void testReplaceTheCurrentActiveWarehouseWhenNotFound() {
        // Given
        com.warehouse.api.beans.Warehouse newWarehouseData = new com.warehouse.api.beans.Warehouse();
        newWarehouseData.setBusinessUnitCode("WH001");
        newWarehouseData.setLocation("AMSTERDAM-001");
        newWarehouseData.setCapacity(200);
        newWarehouseData.setStock(100);

        // When & Then
        assertThrows(Exception.class, () -> warehouseResource.replaceTheCurrentActiveWarehouse("NONEXISTENT", newWarehouseData));
    }

    @Test
    void testReplaceTheCurrentActiveWarehouseWithInvalidCapacity() {
        // Given
        Warehouse existingWarehouse = createTestWarehouse("WH001", "ZWOLLE-001", 100, 50);
        warehouseRepository.create(existingWarehouse);

        com.warehouse.api.beans.Warehouse newWarehouseData = new com.warehouse.api.beans.Warehouse();
        newWarehouseData.setBusinessUnitCode("WH001");
        newWarehouseData.setLocation("AMSTERDAM-001");
        newWarehouseData.setCapacity(25); // Less than existing stock
        newWarehouseData.setStock(50);

        // When & Then
        assertThrows(Exception.class, () -> warehouseResource.replaceTheCurrentActiveWarehouse("WH001", newWarehouseData));
    }

    private Warehouse createTestWarehouse(String businessUnitCode, String location, int capacity, int stock) {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = businessUnitCode;
        warehouse.location = location;
        warehouse.capacity = capacity;
        warehouse.stock = stock;
        warehouse.createdAt = LocalDateTime.now();
        return warehouse;
    }
}
