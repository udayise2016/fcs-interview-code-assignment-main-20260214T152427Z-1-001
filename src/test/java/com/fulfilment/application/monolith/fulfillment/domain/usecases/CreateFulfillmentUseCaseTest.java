package com.fulfilment.application.monolith.fulfillment.domain.usecases;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fulfilment.application.monolith.fulfillment.FulfillmentAssociation;
import com.fulfilment.application.monolith.fulfillment.FulfillmentService;
import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateFulfillmentUseCaseTest {

    @Mock
    private FulfillmentService fulfillmentService;

    @InjectMocks
    private CreateFulfillmentUseCase useCase;

    private Product product;
    private DbWarehouse warehouse;
    private Store store;

    @Test
    void testExecute_Success() {
        // Arrange
        product = new Product("Test Product");
        product.id = 1L;
        product.stock = 100;

        warehouse = new DbWarehouse();
        warehouse.businessUnitCode = "WH-001";
        warehouse.capacity = 200;
        warehouse.stock = 150;

        store = new Store("Test Store");
        store.id = 1L;

        FulfillmentAssociation association = new FulfillmentAssociation(product, warehouse, store, 50, 100);
        when(fulfillmentService.createFulfillmentAssociation(product, warehouse, store, 50, 100)).thenReturn(association);

        // Act
        FulfillmentAssociation result = useCase.execute(product, warehouse, store, 50, 100);

        // Assert
        assertEquals(association, result);
        verify(fulfillmentService).createFulfillmentAssociation(product, warehouse, store, 50, 100);
    }

    @Test
    void testExecute_AllocatedStockExceedsWarehouseCapacity() {
        // Arrange
        product = new Product("Test Product");
        product.stock = 100;

        warehouse = new DbWarehouse();
        warehouse.capacity = 50; // Low capacity

        store = new Store("Test Store");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            useCase.execute(product, warehouse, store, 60, 100)); // allocated > capacity
        assertTrue(exception.getMessage().contains("cannot exceed warehouse capacity"));
    }

    @Test
    void testExecute_AllocatedStockExceedsMaxCapacity() {
        // Arrange
        product = new Product("Test Product");
        product.stock = 100;

        warehouse = new DbWarehouse();
        warehouse.capacity = 200;

        store = new Store("Test Store");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            useCase.execute(product, warehouse, store, 150, 100)); // allocated > maxCapacity
        assertTrue(exception.getMessage().contains("cannot exceed max capacity"));
    }

    @Test
    void testExecute_MaxCapacityExceedsWarehouseCapacity() {
        // Arrange
        product = new Product("Test Product");
        product.stock = 100;

        warehouse = new DbWarehouse();
        warehouse.capacity = 100;

        store = new Store("Test Store");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            useCase.execute(product, warehouse, store, 50, 150)); // maxCapacity > warehouse capacity
        assertTrue(exception.getMessage().contains("Max capacity"));
    }

    @Test
    void testExecute_ProductStockInsufficient() {
        // Arrange
        product = new Product("Test Product");
        product.stock = 30; // Low stock

        warehouse = new DbWarehouse();
        warehouse.capacity = 200;
        warehouse.stock = 150;

        store = new Store("Test Store");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            useCase.execute(product, warehouse, store, 50, 100)); // allocated > product stock
        assertTrue(exception.getMessage().contains("Product stock"));
    }

    @Test
    void testExecute_WarehouseStockInsufficient() {
        // Arrange
        product = new Product("Test Product");
        product.stock = 100;

        warehouse = new DbWarehouse();
        warehouse.capacity = 200;
        warehouse.stock = 30; // Low stock

        store = new Store("Test Store");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            useCase.execute(product, warehouse, store, 50, 100)); // allocated > warehouse stock
        assertTrue(exception.getMessage().contains("Warehouse stock"));
    }
}
