package com.fulfilment.application.monolith.fulfillment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FulfillmentServiceUnitTest {

    @Mock
    private FulfillmentRepository fulfillmentRepository;

    @InjectMocks
    private FulfillmentService fulfillmentService;

    private Product product;
    private DbWarehouse warehouse;
    private Store store;

    @Test
    void testCreateFulfillmentAssociation_Success() {
        // Arrange
        product = new Product();
        product.id = 1L;
        product.name = "Test Product";

        warehouse = new DbWarehouse();
        warehouse.businessUnitCode = "WH-001";

        store = new Store();
        store.id = 1L;
        store.setName("Test Store");

        when(fulfillmentRepository.countByProductAndStore(1L, 1L)).thenReturn(0L);
        when(fulfillmentRepository.countByStore(1L)).thenReturn(0L);
        when(fulfillmentRepository.countByWarehouse("WH-001")).thenReturn(0L);
        when(fulfillmentRepository.findByProductAndStore(1L, 1L)).thenReturn(Collections.emptyList());

        // Act
        FulfillmentAssociation result = fulfillmentService.createFulfillmentAssociation(product, warehouse, store, 50, 100);

        // Assert
        assertNotNull(result);
        verify(fulfillmentRepository).persist(any(FulfillmentAssociation.class));
    }

    @Test
    void testCreateFulfillmentAssociation_ProductStoreConstraintViolation() {
        // Arrange
        product = new Product();
        product.id = 1L;
        product.name = "Test Product";

        warehouse = new DbWarehouse();
        warehouse.businessUnitCode = "WH-001";

        store = new Store();
        store.id = 1L;

        when(fulfillmentRepository.countByProductAndStore(1L, 1L)).thenReturn(2L); // Max is 2

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            fulfillmentService.createFulfillmentAssociation(product, warehouse, store, 50, 100));
        assertTrue(exception.getMessage().contains("can be fulfilled by maximum 2 warehouses per store"));
    }

    @Test
    void testCreateFulfillmentAssociation_StoreConstraintViolation() {
        // Arrange
        product = new Product();
        product.id = 1L;
        product.name = "Test Product";

        warehouse = new DbWarehouse();
        warehouse.businessUnitCode = "WH-001";

        store = new Store();
        store.id = 1L;
        store.setName("Test Store");

        when(fulfillmentRepository.countByProductAndStore(1L, 1L)).thenReturn(0L);
        when(fulfillmentRepository.countByStore(1L)).thenReturn(3L); // Max is 3

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            fulfillmentService.createFulfillmentAssociation(product, warehouse, store, 50, 100));
        assertTrue(exception.getMessage().contains("can be fulfilled by maximum 3 warehouses"));
    }

    @Test
    void testCreateFulfillmentAssociation_WarehouseProductConstraintViolation() {
        // Arrange
        product = new Product();
        product.id = 1L;
        product.name = "Test Product";

        warehouse = new DbWarehouse();
        warehouse.businessUnitCode = "WH-001";

        store = new Store();
        store.id = 1L;
        store.setName("Test Store");

        when(fulfillmentRepository.countByProductAndStore(1L, 1L)).thenReturn(0L);
        when(fulfillmentRepository.countByStore(1L)).thenReturn(0L);
        when(fulfillmentRepository.countByWarehouse("WH-001")).thenReturn(5L); // Max is 5

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            fulfillmentService.createFulfillmentAssociation(product, warehouse, store, 50, 100));
        assertTrue(exception.getMessage().contains("can store maximum 5 product types"));
    }

    @Test
    void testCreateFulfillmentAssociation_ExistingAssociationViolation() {
        // Arrange
        product = new Product();
        product.id = 1L;
        product.name = "Test Product";

        warehouse = new DbWarehouse();
        warehouse.businessUnitCode = "WH-001";

        store = new Store();
        store.id = 1L;
        store.setName("Test Store");

        FulfillmentAssociation existing = new FulfillmentAssociation(product, warehouse, store, 50, 100);
        when(fulfillmentRepository.countByProductAndStore(1L, 1L)).thenReturn(0L);
        when(fulfillmentRepository.countByStore(1L)).thenReturn(0L);
        when(fulfillmentRepository.countByWarehouse("WH-001")).thenReturn(0L);
        when(fulfillmentRepository.findByProductAndStore(1L, 1L)).thenReturn(Arrays.asList(existing));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            fulfillmentService.createFulfillmentAssociation(product, warehouse, store, 50, 100));
        assertTrue(exception.getMessage().contains("is already associated"));
    }

    @Test
    void testGetFulfillmentsByProduct() {
        // Arrange
        List<FulfillmentAssociation> associations = Arrays.asList(
            new FulfillmentAssociation(product, warehouse, store, 50, 100));
        when(fulfillmentRepository.findByProduct(1L)).thenReturn(associations);

        // Act
        List<FulfillmentAssociation> result = fulfillmentService.getFulfillmentsByProduct(1L);

        // Assert
        assertEquals(associations, result);
        verify(fulfillmentRepository).findByProduct(1L);
    }

    @Test
    void testGetFulfillmentsByWarehouse() {
        // Arrange
        List<FulfillmentAssociation> associations = Arrays.asList(
            new FulfillmentAssociation(product, warehouse, store, 50, 100));
        when(fulfillmentRepository.findByWarehouse("WH-001")).thenReturn(associations);

        // Act
        List<FulfillmentAssociation> result = fulfillmentService.getFulfillmentsByWarehouse("WH-001");

        // Assert
        assertEquals(associations, result);
        verify(fulfillmentRepository).findByWarehouse("WH-001");
    }

    @Test
    void testGetFulfillmentsByStore() {
        // Arrange
        List<FulfillmentAssociation> associations = Arrays.asList(
            new FulfillmentAssociation(product, warehouse, store, 50, 100));
        when(fulfillmentRepository.findByStore(1L)).thenReturn(associations);

        // Act
        List<FulfillmentAssociation> result = fulfillmentService.getFulfillmentsByStore(1L);

        // Assert
        assertEquals(associations, result);
        verify(fulfillmentRepository).findByStore(1L);
    }

    @Test
    void testDeleteFulfillmentAssociation() {
        // Act
        fulfillmentService.deleteFulfillmentAssociation(1L);

        // Assert
        verify(fulfillmentRepository).deleteById(1L);
    }
}
