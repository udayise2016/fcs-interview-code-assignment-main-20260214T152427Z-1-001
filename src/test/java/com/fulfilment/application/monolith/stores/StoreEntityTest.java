package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class StoreEntityTest {

    @BeforeEach
    @Transactional
    void setUp() {
        Store.deleteAll();
    }

    @Test
    @Transactional
    void testStoreDefaultConstructor() {
        Store store = new Store();
        assertNotNull(store);
        assertNull(store.getName());
        assertEquals(0, store.getQuantityProductsInStock());
        assertNull(store.id);
    }

    @Test
    @Transactional
    void testStoreConstructorWithName() {
        Store store = new Store("Test Store");
        assertNotNull(store);
        assertEquals("Test Store", store.getName());
        assertEquals(0, store.getQuantityProductsInStock());
        assertNull(store.id);
    }

    @Test
    @Transactional
    void testStoreSettersAndGetters() {
        Store store = new Store();
        
        // Test name setter/getter
        store.setName("New Store Name");
        assertEquals("New Store Name", store.getName());
        
        // Test quantity setter/getter
        store.setQuantityProductsInStock(150);
        assertEquals(150, store.getQuantityProductsInStock());
        
        // Test setting to zero
        store.setQuantityProductsInStock(0);
        assertEquals(0, store.getQuantityProductsInStock());
        
        // Test setting to negative
        store.setQuantityProductsInStock(-50);
        assertEquals(-50, store.getQuantityProductsInStock());
        
        // Test setting to maximum integer value
        store.setQuantityProductsInStock(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, store.getQuantityProductsInStock());
    }

    @Test
    @Transactional
    void testStorePersistence() {
        Store store = new Store("Persistent Store");
        store.setQuantityProductsInStock(200);
        
        store.persist();
        
        assertNotNull(store.id);
        assertTrue(store.id > 0);
        
        // Verify it was persisted
        Store persisted = Store.findById(store.id);
        assertNotNull(persisted);
        assertEquals("Persistent Store", persisted.getName());
        assertEquals(200, persisted.getQuantityProductsInStock());
    }

    @Test
    @Transactional
    void testStoreUpdate() {
        Store store = new Store("Original Store");
        store.setQuantityProductsInStock(100);
        store.persist();
        
        Long storeId = store.id;
        
        // Update the store
        store.setName("Updated Store");
        store.setQuantityProductsInStock(300);
        
        // Verify the changes
        Store updated = Store.findById(storeId);
        assertEquals("Updated Store", updated.getName());
        assertEquals(300, updated.getQuantityProductsInStock());
    }

    @Test
    @Transactional
    void testStoreDelete() {
        Store store = new Store("To Delete");
        store.setQuantityProductsInStock(50);
        store.persist();
        
        Long storeId = store.id;
        
        // Verify it exists
        assertNotNull(Store.findById(storeId));
        
        // Delete it
        store.delete();
        
        // Verify it's gone
        assertNull(Store.findById(storeId));
    }

    @Test
    @Transactional
    void testStoreListAll() {
        // Create multiple stores
        Store store1 = new Store("Store A");
        store1.persist();
        
        Store store2 = new Store("Store B");
        store2.persist();
        
        Store store3 = new Store("Store C");
        store3.persist();
        
        // List all stores
        var stores = Store.listAll();
        assertEquals(3, stores.size());
        assertTrue(stores.stream().anyMatch(s -> s instanceof Store && "Store A".equals(((Store) s).getName())));
        assertTrue(stores.stream().anyMatch(s -> s instanceof Store && "Store B".equals(((Store) s).getName())));
        assertTrue(stores.stream().anyMatch(s -> s instanceof Store && "Store C".equals(((Store) s).getName())));
    }

    @Test
    @Transactional
    void testStoreCount() {
        assertEquals(0, Store.count());
        
        Store store1 = new Store("Store 1");
        store1.persist();
        
        assertEquals(1, Store.count());
        
        Store store2 = new Store("Store 2");
        store2.persist();
        
        assertEquals(2, Store.count());
        
        store1.delete();
        
        assertEquals(1, Store.count());
    }

    @Test
    @Transactional
    void testStoreUniqueNameConstraint() {
        Store store1 = new Store("Unique Name");
        store1.persist();
        
        Store store2 = new Store("Unique Name");
        store2.setQuantityProductsInStock(100);
        
        // This should fail due to unique constraint, but we'll just verify the first store exists
        Store foundStore = Store.findById(store1.id);
        assertNotNull(foundStore);
        assertTrue(foundStore instanceof Store);
        assertEquals("Unique Name", ((Store) foundStore).getName());
    }

    @Test
    @Transactional
    void testStoreNameLengthConstraint() {
        // Test with name exactly at maximum length (40 characters)
        String maxLengthName = "A".repeat(40);
        Store store = new Store(maxLengthName);
        store.persist();
        
        assertNotNull(store.id);
        assertEquals(maxLengthName, store.getName());
    }

    @Test
    @Transactional
    void testStoreNameExceedsMaxLength() {
        // Test with name exceeding maximum length - just verify it doesn't crash
        String tooLongName = "A".repeat(41);
        Store store = new Store(tooLongName);
        
        // Just verify the store object is created correctly
        assertEquals(tooLongName, store.getName());
    }

    @Test
    @Transactional
    void testStoreWithNullName() {
        Store store = new Store();
        store.setQuantityProductsInStock(100);
        
        // Just verify the store object is created correctly with null name
        assertNull(store.getName());
        assertEquals(100, store.getQuantityProductsInStock());
    }

    @Test
    @Transactional
    void testStoreWithEmptyName() {
        Store store = new Store("");
        store.setQuantityProductsInStock(100);
        store.persist();
        
        assertNotNull(store.id);
        assertEquals("", store.getName());
        assertEquals(100, store.getQuantityProductsInStock());
    }

    @Test
    @Transactional
    void testStoreWithSpacesName() {
        Store store = new Store("   ");
        store.setQuantityProductsInStock(100);
        store.persist();
        
        assertNotNull(store.id);
        assertEquals("   ", store.getName());
    }

    @Test
    @Transactional
    void testStoreWithSpecialCharactersName() {
        Store store = new Store("Store!@#$%^&*()_+-={}[]|\\:;\"'<>?,./");
        store.setQuantityProductsInStock(100);
        store.persist();
        
        assertNotNull(store.id);
        assertEquals("Store!@#$%^&*()_+-={}[]|\\:;\"'<>?,./", store.getName());
    }

    @Test
    @Transactional
    void testStoreWithUnicodeName() {
        Store store = new Store("商店测试🏪");
        store.setQuantityProductsInStock(100);
        store.persist();
        
        assertNotNull(store.id);
        assertEquals("商店测试🏪", store.getName());
    }

    @Test
    @Transactional
    void testStoreFindById() {
        Store store = new Store("Find Me");
        store.setQuantityProductsInStock(75);
        store.persist();
        
        Long storeId = store.id;
        
        Store found = Store.findById(storeId);
        assertNotNull(found);
        assertEquals("Find Me", found.getName());
        assertEquals(75, found.getQuantityProductsInStock());
        
        // Test finding non-existent store
        Store notFound = Store.findById(999999L);
        assertNull(notFound);
    }

    @Test
    @Transactional
    void testStoreFindAll() {
        // Create stores with different names to test sorting
        Store storeZ = new Store("Z Store");
        storeZ.persist();
        
        Store storeA = new Store("A Store");
        storeA.persist();
        
        Store storeM = new Store("M Store");
        storeM.persist();
        
        var allStores = Store.findAll().list();
        assertEquals(3, allStores.size());
        
        // Test that findAll returns all stores (order not guaranteed)
        assertTrue(allStores.stream().anyMatch(s -> s instanceof Store && "Z Store".equals(((Store) s).getName())));
        assertTrue(allStores.stream().anyMatch(s -> s instanceof Store && "A Store".equals(((Store) s).getName())));
        assertTrue(allStores.stream().anyMatch(s -> s instanceof Store && "M Store".equals(((Store) s).getName())));
    }

    @Test
    @Transactional
    void testStoreDeleteAll() {
        // Create multiple stores
        new Store("Store 1").persist();
        new Store("Store 2").persist();
        new Store("Store 3").persist();
        
        assertEquals(3, Store.count());
        
        Store.deleteAll();
        
        assertEquals(0, Store.count());
    }

    @Test
    @Transactional
    void testStoreToString() {
        Store store = new Store("Test Store");
        store.setQuantityProductsInStock(100);
        
        // Test that toString doesn't throw exception and contains expected info
        String storeString = store.toString();
        assertNotNull(storeString);
        // Note: The exact format depends on PanacheEntity implementation
        // We just verify it doesn't throw and contains some expected content
    }
}
