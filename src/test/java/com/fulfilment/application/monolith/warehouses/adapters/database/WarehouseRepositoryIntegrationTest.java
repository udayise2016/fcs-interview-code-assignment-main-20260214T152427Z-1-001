package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class WarehouseRepositoryIntegrationTest {

    @Inject
    WarehouseRepository warehouseRepository;

    @BeforeEach
    @Transactional
    void setUp() {
        warehouseRepository.getAll().forEach(warehouse -> {
            DbWarehouse dbWarehouse = warehouseRepository.find("businessUnitCode", warehouse.businessUnitCode).firstResult();
            if (dbWarehouse != null) {
                warehouseRepository.delete(dbWarehouse);
            }
        });
    }

    @Test
    @Transactional
    void testCreateWarehouse() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "TEST-001";
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = 100;
        warehouse.stock = 50;
        
        warehouseRepository.create(warehouse);
        
        List<Warehouse> all = warehouseRepository.getAll();
        assertEquals(1, all.size());
        Warehouse created = all.get(0);
        assertEquals("TEST-001", created.businessUnitCode);
        assertEquals("ZWOLLE-001", created.location);
        assertEquals(100, created.capacity);
        assertEquals(50, created.stock);
        assertNotNull(created.createdAt);
        assertNull(created.archivedAt);
    }

    @Test
    @Transactional
    void testFindById() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "FIND-001";
        warehouse.location = "AMSTERDAM-001";
        warehouse.capacity = 200;
        warehouse.stock = 100;
        
        warehouseRepository.create(warehouse);
        List<Warehouse> all = warehouseRepository.getAll();
        Warehouse created = all.get(0);
        
        Optional<Warehouse> found = warehouseRepository.find("businessUnitCode", created.businessUnitCode).firstResultOptional().map(DbWarehouse::toWarehouse);
        
        assertTrue(found.isPresent());
        assertEquals("FIND-001", found.get().businessUnitCode);
    }

    @Test
    @Transactional
    void testGetAll() {
        Warehouse warehouse1 = new Warehouse();
        warehouse1.businessUnitCode = "ALL-001";
        warehouse1.location = "ZWOLLE-001";
        warehouse1.capacity = 100;
        warehouse1.stock = 50;
        
        Warehouse warehouse2 = new Warehouse();
        warehouse2.businessUnitCode = "ALL-002";
        warehouse2.location = "AMSTERDAM-001";
        warehouse2.capacity = 200;
        warehouse2.stock = 100;
        
        warehouseRepository.create(warehouse1);
        warehouseRepository.create(warehouse2);
        
        List<Warehouse> all = warehouseRepository.getAll();
        
        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(w -> "ALL-001".equals(w.businessUnitCode)));
        assertTrue(all.stream().anyMatch(w -> "ALL-002".equals(w.businessUnitCode)));
    }

    @Test
    @Transactional
    void testUpdateWarehouse() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "UPDATE-001";
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = 100;
        warehouse.stock = 50;
        
        warehouseRepository.create(warehouse);
        List<Warehouse> all = warehouseRepository.getAll();
        Warehouse created = all.get(0);
        
        created.capacity = 150;
        created.stock = 75;
        
        warehouseRepository.update(created);
        
        List<Warehouse> updatedAll = warehouseRepository.getAll();
        Warehouse updated = updatedAll.get(0);
        assertEquals(150, updated.capacity);
        assertEquals(75, updated.stock);
    }

    @Test
    @Transactional
    void testRemoveWarehouse() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "REMOVE-001";
        warehouse.location = "ZWOLLE-001";
        warehouse.capacity = 100;
        warehouse.stock = 50;
        
        warehouseRepository.create(warehouse);
        List<Warehouse> all = warehouseRepository.getAll();
        Warehouse created = all.get(0);
        
        warehouseRepository.remove(created);
        
        List<Warehouse> afterRemove = warehouseRepository.getAll();
        assertEquals(0, afterRemove.size());
    }
}
