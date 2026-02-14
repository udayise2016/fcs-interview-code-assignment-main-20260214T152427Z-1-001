package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class WarehouseRepositoryTest {

    @Inject
    WarehouseRepository warehouseRepository;

    @BeforeEach
    void setUp() {
        // Clean up test data
        warehouseRepository.getAll().forEach(warehouse -> warehouseRepository.remove(warehouse));
    }

    @Test
    void testCreateWarehouse() {
        // Given
        Warehouse warehouse = createTestWarehouse("WH001", "ZWOLLE-001", 100, 50);

        // When
        warehouseRepository.create(warehouse);

        // Then
        List<Warehouse> allWarehouses = warehouseRepository.getAll();
        assertEquals(1, allWarehouses.size());
        
        Warehouse created = allWarehouses.get(0);
        assertEquals("WH001", created.businessUnitCode);
        assertEquals("ZWOLLE-001", created.location);
        assertEquals(100, created.capacity);
        assertEquals(50, created.stock);
        assertNotNull(created.createdAt);
    }

    @Test
    void testGetAllWarehouses() {
        // Given
        Warehouse warehouse1 = createTestWarehouse("WH001", "ZWOLLE-001", 100, 50);
        Warehouse warehouse2 = createTestWarehouse("WH002", "AMSTERDAM-001", 200, 100);
        
        warehouseRepository.create(warehouse1);
        warehouseRepository.create(warehouse2);

        // When
        List<Warehouse> result = warehouseRepository.getAll();

        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(w -> "WH001".equals(w.businessUnitCode)));
        assertTrue(result.stream().anyMatch(w -> "WH002".equals(w.businessUnitCode)));
    }

    @Test
    void testGetAllWarehousesWhenEmpty() {
        // When
        List<Warehouse> result = warehouseRepository.getAll();

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindByBusinessUnitCode() {
        // Given
        Warehouse warehouse = createTestWarehouse("WH001", "ZWOLLE-001", 100, 50);
        warehouseRepository.create(warehouse);

        // When
        Warehouse result = warehouseRepository.findByBusinessUnitCode("WH001");

        // Then
        assertNotNull(result);
        assertEquals("WH001", result.businessUnitCode);
        assertEquals("ZWOLLE-001", result.location);
    }

    @Test
    void testFindByBusinessUnitCodeWhenNotFound() {
        // When
        Warehouse result = warehouseRepository.findByBusinessUnitCode("NONEXISTENT");

        // Then
        assertNull(result);
    }

    @Test
    void testUpdateWarehouse() {
        // Given
        Warehouse warehouse = createTestWarehouse("WH001", "ZWOLLE-001", 100, 50);
        warehouseRepository.create(warehouse);

        // Update warehouse
        warehouse.location = "AMSTERDAM-001";
        warehouse.capacity = 200;
        warehouse.stock = 150;

        // When
        warehouseRepository.update(warehouse);

        // Then
        Warehouse updated = warehouseRepository.findByBusinessUnitCode("WH001");
        assertNotNull(updated);
        assertEquals("AMSTERDAM-001", updated.location);
        assertEquals(200, updated.capacity);
        assertEquals(150, updated.stock);
    }

    @Test
    void testRemoveWarehouse() {
        // Given
        Warehouse warehouse = createTestWarehouse("WH001", "ZWOLLE-001", 100, 50);
        warehouseRepository.create(warehouse);

        // When
        warehouseRepository.remove(warehouse);

        // Then
        Warehouse removed = warehouseRepository.findByBusinessUnitCode("WH001");
        assertNull(removed);
        
        List<Warehouse> allWarehouses = warehouseRepository.getAll();
        assertTrue(allWarehouses.isEmpty());
    }

    @Test
    void testCreateMultipleWarehouses() {
        // Given
        Warehouse warehouse1 = createTestWarehouse("WH001", "ZWOLLE-001", 100, 50);
        Warehouse warehouse2 = createTestWarehouse("WH002", "AMSTERDAM-001", 200, 100);
        Warehouse warehouse3 = createTestWarehouse("WH003", "TILBURG-001", 150, 75);

        // When
        warehouseRepository.create(warehouse1);
        warehouseRepository.create(warehouse2);
        warehouseRepository.create(warehouse3);

        // Then
        List<Warehouse> allWarehouses = warehouseRepository.getAll();
        assertEquals(3, allWarehouses.size());
    }

    @Test
    void testUpdateNonExistentWarehouse() {
        // Given
        Warehouse warehouse = createTestWarehouse("WH001", "ZWOLLE-001", 100, 50);

        // When
        warehouseRepository.update(warehouse);

        // Then - should not throw exception, but warehouse won't be found
        assertNull(warehouseRepository.findByBusinessUnitCode("WH001"));
    }

    @Test
    void testRemoveNonExistentWarehouse() {
        // Given
        Warehouse warehouse = createTestWarehouse("WH001", "ZWOLLE-001", 100, 50);

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> warehouseRepository.remove(warehouse));
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
