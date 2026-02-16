package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class StoreResourceAdditionalTest {

    @Inject
    StoreResource storeResource;

    @BeforeEach
    @Transactional
    void setUp() {
        Store.deleteAll();
    }

    @Test
    void testStoreResourceGetAll() {
        // Create some test stores
        new Store("Store A").persist();
        new Store("Store B").persist();
        new Store("Store C").persist();

        // Test the resource directly
        var stores = storeResource.get();
        assertEquals(3, stores.size());
        assertTrue(stores.stream().anyMatch(s -> s.getName().equals("Store A")));
        assertTrue(stores.stream().anyMatch(s -> s.getName().equals("Store B")));
        assertTrue(stores.stream().anyMatch(s -> s.getName().equals("Store C")));
    }

    @Test
    void testStoreResourceGetAllEmpty() {
        // Test with no stores
        var stores = storeResource.get();
        assertEquals(0, stores.size());
    }

    @Test
    void testStoreResourceGetSingle() {
        // Create a test store
        Store store = new Store("Test Store");
        store.setQuantityProductsInStock(100);
        store.persist();

        // Test getting single store
        Store found = storeResource.getSingle(store.id);
        assertNotNull(found);
        assertEquals("Test Store", found.getName());
        assertEquals(100, found.getQuantityProductsInStock());
    }

    @Test
    void testStoreResourceGetSingleNotFound() {
        // Test getting non-existent store
        assertThrows(Exception.class, () -> {
            storeResource.getSingle(999999L);
        });
    }

    @Test
    void testStoreResourceCreate() {
        Store newStore = new Store("New Store");
        newStore.setQuantityProductsInStock(50);

        var response = storeResource.create(newStore);
        assertEquals(201, response.getStatus());
        Store created = (Store) response.getEntity();
        assertNotNull(created.id);
        assertEquals("New Store", created.getName());
        assertEquals(50, created.getQuantityProductsInStock());
    }

    @Test
    void testStoreResourceCreateWithId() {
        Store newStore = new Store("Invalid Store");
        newStore.id = 123L; // This should cause an error
        newStore.setQuantityProductsInStock(50);

        assertThrows(Exception.class, () -> {
            storeResource.create(newStore);
        });
    }

    @Test
    void testStoreResourceUpdate() {
        // Create a store
        Store store = new Store("Original Store");
        store.setQuantityProductsInStock(100);
        store.persist();

        // Update it
        Store update = new Store("Updated Store");
        update.setQuantityProductsInStock(200);

        Store updated = storeResource.update(store.id, update);
        assertEquals("Updated Store", updated.getName());
        assertEquals(200, updated.getQuantityProductsInStock());
    }

    @Test
    void testStoreResourceUpdateNotFound() {
        Store update = new Store("Updated Store");
        update.setQuantityProductsInStock(200);

        assertThrows(Exception.class, () -> {
            storeResource.update(999999L, update);
        });
    }

    @Test
    void testStoreResourceUpdateNullName() {
        // Create a store
        Store store = new Store("Original Store");
        store.setQuantityProductsInStock(100);
        store.persist();

        // Try to update with null name
        Store update = new Store();
        update.setName(null);
        update.setQuantityProductsInStock(200);

        assertThrows(Exception.class, () -> {
            storeResource.update(store.id, update);
        });
    }

    @Test
    void testStoreResourcePatch() {
        // Create a store
        Store store = new Store("Original Store");
        store.setQuantityProductsInStock(100);
        store.persist();

        // Patch it
        Store patch = new Store("Patched Store");
        patch.setQuantityProductsInStock(150);

        Store patched = storeResource.patch(store.id, patch);
        assertEquals("Patched Store", patched.getName());
        assertEquals(150, patched.getQuantityProductsInStock());
    }

    @Test
    void testStoreResourcePatchNotFound() {
        Store patch = new Store("Patched Store");
        patch.setQuantityProductsInStock(150);

        assertThrows(Exception.class, () -> {
            storeResource.patch(999999L, patch);
        });
    }

    @Test
    void testStoreResourcePatchNullName() {
        // Create a store
        Store store = new Store("Original Store");
        store.setQuantityProductsInStock(100);
        store.persist();

        // Try to patch with null name
        Store patch = new Store();
        patch.setName(null);
        patch.setQuantityProductsInStock(150);

        assertThrows(Exception.class, () -> {
            storeResource.patch(store.id, patch);
        });
    }

    @Test
    void testStoreResourceDelete() {
        // Create a store
        Store store = new Store("To Delete");
        store.setQuantityProductsInStock(100);
        store.persist();

        // Delete it
        storeResource.delete(store.id);

        // Verify it's gone
        assertNull(Store.findById(store.id));
    }

    @Test
    void testStoreResourceDeleteNotFound() {
        assertThrows(Exception.class, () -> {
            storeResource.delete(999999L);
        });
    }

    @Test
    void testStoreResourceErrorMapper() {
        // Test the error mapper by triggering an exception
        StoreResource.ErrorMapper errorMapper = new StoreResource.ErrorMapper();
        
        // Test with WebApplicationException
        jakarta.ws.rs.WebApplicationException webEx = 
            new jakarta.ws.rs.WebApplicationException("Test error", 404);
        
        var response = errorMapper.toResponse(webEx);
        assertEquals(404, response.getStatus());
        
        // Test with generic exception
        Exception genericEx = new Exception("Generic error");
        response = errorMapper.toResponse(genericEx);
        assertEquals(500, response.getStatus());
    }
}
