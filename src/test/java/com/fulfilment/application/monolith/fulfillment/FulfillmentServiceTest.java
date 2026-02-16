package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class FulfillmentServiceTest {

    @Inject
    FulfillmentService fulfillmentService;

    @Inject
    FulfillmentRepository fulfillmentRepository;

    @Inject
    WarehouseRepository warehouseRepository;

    private Product testProduct;
    private DbWarehouse testWarehouse;
    private Store testStore;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up existing data
        fulfillmentRepository.deleteAll();
        warehouseRepository.deleteAll();
        
        // Setup test data
        testProduct = new Product();
        testProduct.id = 1L;
        testProduct.name = "Test Product";
        testProduct.stock = 100;

        testWarehouse = new DbWarehouse();
        testWarehouse.businessUnitCode = "WH-001";
        testWarehouse.location = "ZWOLLE-001";
        testWarehouse.capacity = 200;
        testWarehouse.stock = 150;
        warehouseRepository.persist(testWarehouse);

        testStore = new Store();
        testStore.id = 1L;
        testStore.setName("Test Store");
    }

    @Test
    @Transactional
    void testCreateFulfillmentAssociation_Success() {
        FulfillmentAssociation association = fulfillmentService.createFulfillmentAssociation(
            testProduct, testWarehouse, testStore, 50, 100);

        assertNotNull(association.id);
        assertEquals(testProduct, association.product);
        assertEquals(testWarehouse, association.warehouse);
        assertEquals(testStore, association.store);
        assertEquals(50, association.allocatedStock);
        assertEquals(100, association.maxCapacity);
    }

    @Test
    @Transactional
    void testConstraint_MaxWarehousesPerProductStore() {
        // Create first association
        fulfillmentService.createFulfillmentAssociation(testProduct, testWarehouse, testStore, 30, 50);

        // Create second warehouse
        DbWarehouse warehouse2 = new DbWarehouse();
        warehouse2.businessUnitCode = "WH-002";
        warehouse2.location = "AMSTERDAM-001";
        warehouse2.capacity = 200;
        warehouse2.stock = 150;
        warehouseRepository.persist(warehouse2);

        // Create second association - should succeed
        fulfillmentService.createFulfillmentAssociation(testProduct, warehouse2, testStore, 20, 40);

        // Create third warehouse
        DbWarehouse warehouse3 = new DbWarehouse();
        warehouse3.businessUnitCode = "WH-003";
        warehouse3.location = "ROTTERDAM-001";
        warehouse3.capacity = 200;
        warehouse3.stock = 150;
        warehouseRepository.persist(warehouse3);

        // Try to create third association - should fail
        assertThrows(IllegalArgumentException.class, () -> {
            fulfillmentService.createFulfillmentAssociation(testProduct, warehouse3, testStore, 10, 20);
        });
    }

    @Test
    @Transactional
    void testConstraint_MaxWarehousesPerStore() {
        // Create 3 different warehouses for the same store
        DbWarehouse warehouse1 = testWarehouse;
        DbWarehouse warehouse2 = new DbWarehouse();
        warehouse2.businessUnitCode = "WH-002";
        warehouse2.location = "AMSTERDAM-001";
        warehouse2.capacity = 200;
        warehouse2.stock = 150;
        warehouseRepository.persist(warehouse2);

        DbWarehouse warehouse3 = new DbWarehouse();
        warehouse3.businessUnitCode = "WH-003";
        warehouse3.location = "ROTTERDAM-001";
        warehouse3.capacity = 200;
        warehouse3.stock = 150;
        warehouseRepository.persist(warehouse3);

        // Create 3 associations - should succeed
        fulfillmentService.createFulfillmentAssociation(testProduct, warehouse1, testStore, 30, 50);
        fulfillmentService.createFulfillmentAssociation(testProduct, warehouse2, testStore, 20, 40);
        fulfillmentService.createFulfillmentAssociation(testProduct, warehouse3, testStore, 10, 20);

        // Try to create 4th warehouse
        DbWarehouse warehouse4 = new DbWarehouse();
        warehouse4.businessUnitCode = "WH-004";
        warehouse4.location = "UTRECHT-001";
        warehouse4.capacity = 200;
        warehouse4.stock = 150;
        warehouseRepository.persist(warehouse4);

        // Should fail due to max 3 warehouses per store constraint
        assertThrows(IllegalArgumentException.class, () -> {
            fulfillmentService.createFulfillmentAssociation(testProduct, warehouse4, testStore, 5, 10);
        });
    }

    @Test
    @Transactional
    void testConstraint_MaxProductTypesPerWarehouse() {
        // Create 5 different products for the same warehouse
        for (int i = 1; i <= 5; i++) {
            Product product = new Product();
            product.id = (long) i;
            product.name = "Product " + i;
            product.stock = 100;

            Store store = new Store();
            store.id = (long) i;
            store.setName("Store " + i);

            fulfillmentService.createFulfillmentAssociation(product, testWarehouse, store, 20, 40);
        }

        // Try to create 6th product type for the same warehouse
        Product product6 = new Product();
        product6.id = 6L;
        product6.name = "Product 6";
        product6.stock = 100;

        Store store6 = new Store();
        store6.id = 6L;
        store6.setName("Store 6");

        // Should fail due to max 5 product types per warehouse constraint
        assertThrows(IllegalArgumentException.class, () -> {
            fulfillmentService.createFulfillmentAssociation(product6, testWarehouse, store6, 10, 20);
        });
    }

    @Test
    @Transactional
    void testGetFulfillmentsByProduct() {
        fulfillmentService.createFulfillmentAssociation(testProduct, testWarehouse, testStore, 50, 100);

        List<FulfillmentAssociation> fulfillments = fulfillmentService.getFulfillmentsByProduct(testProduct.id);
        assertEquals(1, fulfillments.size());
        assertEquals(testProduct.id, fulfillments.get(0).product.id);
    }

    @Test
    @Transactional
    void testGetFulfillmentsByWarehouse() {
        fulfillmentService.createFulfillmentAssociation(testProduct, testWarehouse, testStore, 50, 100);

        List<FulfillmentAssociation> fulfillments = fulfillmentService.getFulfillmentsByWarehouse(testWarehouse.businessUnitCode);
        assertEquals(1, fulfillments.size());
        assertEquals(testWarehouse.businessUnitCode, fulfillments.get(0).warehouse.businessUnitCode);
    }

    @Test
    @Transactional
    void testGetFulfillmentsByStore() {
        fulfillmentService.createFulfillmentAssociation(testProduct, testWarehouse, testStore, 50, 100);

        List<FulfillmentAssociation> fulfillments = fulfillmentService.getFulfillmentsByStore(testStore.id);
        assertEquals(1, fulfillments.size());
        assertEquals(testStore.id, fulfillments.get(0).store.id);
    }

    @Test
    @Transactional
    void testDeleteFulfillmentAssociation() {
        FulfillmentAssociation association = fulfillmentService.createFulfillmentAssociation(
            testProduct, testWarehouse, testStore, 50, 100);

        fulfillmentService.deleteFulfillmentAssociation(association.id);

        List<FulfillmentAssociation> fulfillments = fulfillmentService.getFulfillmentsByProduct(testProduct.id);
        assertEquals(0, fulfillments.size());
    }
}
