package com.fulfilment.application.monolith.fulfillment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;

import jakarta.ws.rs.core.Response;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FulfillmentResourceUnitTest {

    @Mock
    private FulfillmentService fulfillmentService;

    @Mock
    private FulfillmentRepository fulfillmentRepository;

    @InjectMocks
    private FulfillmentResource fulfillmentResource;

    @Test
    void testCreateFulfillment_Success() {
        // Arrange
        FulfillmentResource.FulfillmentRequest request = new FulfillmentResource.FulfillmentRequest();
        request.productId = 1L;
        request.warehouseBusinessUnitCode = "WH-001";
        request.storeId = 1L;
        request.allocatedStock = 50;
        request.maxCapacity = 100;

        FulfillmentAssociation association = new FulfillmentAssociation(
            new Product(), new DbWarehouse(), new Store(), 50, 100);
        when(fulfillmentService.createFulfillmentAssociation(any(), any(), any(), eq(50), eq(100)))
            .thenReturn(association);

        // Act
        Response response = fulfillmentResource.createFulfillment(request);

        // Assert
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertEquals(association, response.getEntity());
        verify(fulfillmentService).createFulfillmentAssociation(any(Product.class), any(DbWarehouse.class), any(Store.class), eq(50), eq(100));
    }

    @Test
    void testCreateFulfillment_IllegalArgumentException() {
        // Arrange
        FulfillmentResource.FulfillmentRequest request = new FulfillmentResource.FulfillmentRequest();
        request.productId = 1L;
        request.warehouseBusinessUnitCode = "WH-001";
        request.storeId = 1L;
        request.allocatedStock = 50;
        request.maxCapacity = 100;

        when(fulfillmentService.createFulfillmentAssociation(any(), any(), any(), eq(50), eq(100)))
            .thenThrow(new IllegalArgumentException("Test exception"));

        // Act
        Response response = fulfillmentResource.createFulfillment(request);

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertTrue(response.getEntity() instanceof FulfillmentResource.ErrorResponse);
        FulfillmentResource.ErrorResponse error = (FulfillmentResource.ErrorResponse) response.getEntity();
        assertEquals("Test exception", error.message);
        assertEquals("IllegalArgumentException", error.type);
    }

    @Test
    void testGetFulfillmentsByProduct() {
        // Arrange
        List<FulfillmentAssociation> associations = Arrays.asList(
            new FulfillmentAssociation(new Product(), new DbWarehouse(), new Store(), 50, 100));
        when(fulfillmentService.getFulfillmentsByProduct(1L)).thenReturn(associations);

        // Act
        List<FulfillmentAssociation> result = fulfillmentResource.getFulfillmentsByProduct(1L);

        // Assert
        assertEquals(associations, result);
        verify(fulfillmentService).getFulfillmentsByProduct(1L);
    }

    @Test
    void testGetFulfillmentsByWarehouse() {
        // Arrange
        List<FulfillmentAssociation> associations = Arrays.asList(
            new FulfillmentAssociation(new Product(), new DbWarehouse(), new Store(), 50, 100));
        when(fulfillmentService.getFulfillmentsByWarehouse("WH-001")).thenReturn(associations);

        // Act
        List<FulfillmentAssociation> result = fulfillmentResource.getFulfillmentsByWarehouse("WH-001");

        // Assert
        assertEquals(associations, result);
        verify(fulfillmentService).getFulfillmentsByWarehouse("WH-001");
    }

    @Test
    void testGetFulfillmentsByStore() {
        // Arrange
        List<FulfillmentAssociation> associations = Arrays.asList(
            new FulfillmentAssociation(new Product(), new DbWarehouse(), new Store(), 50, 100));
        when(fulfillmentService.getFulfillmentsByStore(1L)).thenReturn(associations);

        // Act
        List<FulfillmentAssociation> result = fulfillmentResource.getFulfillmentsByStore(1L);

        // Assert
        assertEquals(associations, result);
        verify(fulfillmentService).getFulfillmentsByStore(1L);
    }

    @Test
    void testDeleteFulfillmentAssociation() {
        // Act
        Response response = fulfillmentResource.deleteFulfillment(1L);

        // Assert
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verify(fulfillmentService).deleteFulfillmentAssociation(1L);
    }

    @Test
    void testCleanupTestData() {
        // Act
        Response response = fulfillmentResource.cleanupTestData();

        // Assert
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        verify(fulfillmentRepository).deleteAll();
    }

    @Test
    void testErrorResponseConstructor() {
        // Act
        FulfillmentResource.ErrorResponse error = new FulfillmentResource.ErrorResponse("Test message");

        // Assert
        assertEquals("Test message", error.message);
        assertEquals("IllegalArgumentException", error.type);
    }
}
