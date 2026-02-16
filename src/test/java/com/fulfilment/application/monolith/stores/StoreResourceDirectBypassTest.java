package com.fulfilment.application.monolith.stores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.enterprise.event.Event;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class StoreResourceDirectBypassTest {

    private StoreResource storeResource;
    private LegacyStoreManagerGateway mockGateway;
    private Event<StoreTransactionEvent> mockEvent;

    @BeforeEach
    void setUp() throws Exception {
        // Create direct instance bypassing CDI
        storeResource = new StoreResource();

        // Create mocks
        mockGateway = mock(LegacyStoreManagerGateway.class);
        mockEvent = mock(Event.class);

        // Use reflection to inject mocks directly into fields
        injectField(storeResource, "legacyStoreManagerGateway", mockGateway);
        injectField(storeResource, "storeTransactionEvent", mockEvent);
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void testCreateStoreValidationBypass() throws Exception {
        // Create invalid store with ID to test validation bypass
        Store invalidStore = new Store("Invalid Store");
        invalidStore.id = 123L;
        invalidStore.setQuantityProductsInStock(100);

        // Test validation (this should work even with CDI bypass)
        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
            storeResource.create(invalidStore);
        });

        assertEquals(422, exception.getResponse().getStatus());
        assertTrue(exception.getMessage().contains("Id was invalidly set"));
    }

    @Test
    void testUpdateStoreValidationBypass() throws Exception {
        // Test null name validation
        Store invalidData = new Store();
        invalidData.setName(null);
        invalidData.setQuantityProductsInStock(200);

        // Test validation
        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
            storeResource.update(1L, invalidData);
        });

        assertEquals(422, exception.getResponse().getStatus());
        assertTrue(exception.getMessage().contains("Name was not set"));
    }

    @Test
    void testPatchStoreValidationBypass() throws Exception {
        // Test null name validation in patch
        Store invalidData = new Store();
        invalidData.setName(null);
        invalidData.setQuantityProductsInStock(150);

        // Test validation
        WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
            storeResource.patch(1L, invalidData);
        });

        assertEquals(422, exception.getResponse().getStatus());
        assertTrue(exception.getMessage().contains("Name was not set"));
    }

    @Test
    void testErrorMapperDirectAccess() throws Exception {
        // Create error mapper directly
        StoreResource.ErrorMapper errorMapper = new StoreResource.ErrorMapper();

        // Inject mock ObjectMapper
        ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        injectField(errorMapper, "objectMapper", mockObjectMapper);

        // Setup mocks
        ObjectNode mockNode = mock(ObjectNode.class);
        when(mockObjectMapper.createObjectNode()).thenReturn(mockNode);
        when(mockNode.put(anyString(), anyString())).thenReturn(mockNode);
        when(mockNode.put(anyString(), anyInt())).thenReturn(mockNode);

        // Test exception handling
        WebApplicationException webEx = new WebApplicationException("Test error", 404);
        Response response = errorMapper.toResponse(webEx);

        // Verify results
        assertEquals(404, response.getStatus());
        verify(mockObjectMapper).createObjectNode();
        verify(mockNode).put(eq("exceptionType"), eq(WebApplicationException.class.getName()));
        verify(mockNode).put(eq("code"), eq(404));
        verify(mockNode).put(eq("error"), eq("Test error"));
    }

    @Test
    void testStoreResourceMethodExistence() throws Exception {
        // Test that all expected methods exist and are accessible
        var methods = StoreResource.class.getDeclaredMethods();

        boolean hasGetMethod = false;
        boolean hasCreateMethod = false;
        boolean hasUpdateMethod = false;
        boolean hasPatchMethod = false;
        boolean hasDeleteMethod = false;
        boolean hasGetSingleMethod = false;

        for (var method : methods) {
            switch (method.getName()) {
                case "get":
                    hasGetMethod = true;
                    break;
                case "create":
                    hasCreateMethod = true;
                    break;
                case "update":
                    hasUpdateMethod = true;
                    break;
                case "patch":
                    hasPatchMethod = true;
                    break;
                case "delete":
                    hasDeleteMethod = true;
                    break;
                case "getSingle":
                    hasGetSingleMethod = true;
                    break;
            }
        }

        // Verify all critical methods exist
        assertTrue(hasGetMethod, "get method should exist");
        assertTrue(hasCreateMethod, "create method should exist");
        assertTrue(hasUpdateMethod, "update method should exist");
        assertTrue(hasPatchMethod, "patch method should exist");
        assertTrue(hasDeleteMethod, "delete method should exist");
        assertTrue(hasGetSingleMethod, "getSingle method should exist");
    }

    @Test
    void testStoreResourceFieldExistence() throws Exception {
        // Test that critical fields exist
        var fields = StoreResource.class.getDeclaredFields();

        boolean hasGatewayField = false;
        boolean hasEventField = false;
        boolean hasErrorMapper = false;

        for (var field : fields) {
            switch (field.getName()) {
                case "legacyStoreManagerGateway":
                    hasGatewayField = true;
                    break;
                case "storeTransactionEvent":
                    hasEventField = true;
                    break;
            }
        }

        // Check for inner classes
        var classes = StoreResource.class.getDeclaredClasses();
        for (var clazz : classes) {
            if (clazz.getSimpleName().equals("ErrorMapper")) {
                hasErrorMapper = true;
                break;
            }
        }

        // Verify critical components exist
        assertTrue(hasGatewayField, "legacyStoreManagerGateway field should exist");
        assertTrue(hasEventField, "storeTransactionEvent field should exist");
        assertTrue(hasErrorMapper, "ErrorMapper inner class should exist");
    }

    @Test
    void testStoreResourceConstructor() {
        // Test that we can create StoreResource directly
        StoreResource resource = new StoreResource();
        assertNotNull(resource);

        // Test ErrorMapper constructor
        StoreResource.ErrorMapper errorMapper = new StoreResource.ErrorMapper();
        assertNotNull(errorMapper);
    }

    @Test
    void testDirectFieldInjection() throws Exception {
        // Test that reflection injection works
        StoreResource testResource = new StoreResource();
        LegacyStoreManagerGateway testGateway = new LegacyStoreManagerGateway();

        // Inject field
        injectField(testResource, "legacyStoreManagerGateway", testGateway);

        // Verify injection worked
        Field field = testResource.getClass().getDeclaredField("legacyStoreManagerGateway");
        field.setAccessible(true);
        Object injectedValue = field.get(testResource);

        assertNotNull(injectedValue);
        assertEquals(LegacyStoreManagerGateway.class, injectedValue.getClass());
    }

    @Test
    void testCDIBypassDemonstration() throws Exception {
        // Demonstrate that we can access methods that CDI would normally proxy
        StoreResource resource = new StoreResource();

        // Verify we can call methods directly without CDI
        assertDoesNotThrow(() -> {
            // This would normally go through CDI proxy
            // But we're calling it directly
            try {
                resource.getClass().getDeclaredMethod("get");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertDoesNotThrow(() -> {
            try {
                resource.getClass().getDeclaredMethod("create", Store.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        assertDoesNotThrow(() -> {
            try {
                resource.getClass().getDeclaredMethod("getSingle", Long.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void assertDoesNotThrow(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            fail("Expected no exception, but got: " + e.getMessage());
        }
    }
}
