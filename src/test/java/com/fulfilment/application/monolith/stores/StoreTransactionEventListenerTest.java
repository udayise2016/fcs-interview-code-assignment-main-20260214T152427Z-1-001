package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class StoreTransactionEventListenerTest {

    @Inject
    StoreTransactionEventListener eventListener;

    @Test
    void testStoreTransactionEventCreateAction() {
        // Given
        StoreTransactionEvent.Action action = StoreTransactionEvent.Action.CREATE;

        // When & Then
        assertEquals("CREATE", action.name());
        assertNotNull(action.name());
    }

    @Test
    void testStoreTransactionEventUpdateAction() {
        // Given
        StoreTransactionEvent.Action action = StoreTransactionEvent.Action.UPDATE;

        // When & Then
        assertEquals("UPDATE", action.name());
        assertNotNull(action.name());
    }

    @Test
    void testStoreTransactionEventValues() {
        // When & Then
        StoreTransactionEvent.Action[] actions = StoreTransactionEvent.Action.values();
        assertEquals(2, actions.length);
        assertTrue(contains(actions, StoreTransactionEvent.Action.CREATE));
        assertTrue(contains(actions, StoreTransactionEvent.Action.UPDATE));
    }

    @Test
    void testStoreTransactionEventValueOf() {
        // When & Then
        assertEquals(StoreTransactionEvent.Action.CREATE, StoreTransactionEvent.Action.valueOf("CREATE"));
        assertEquals(StoreTransactionEvent.Action.UPDATE, StoreTransactionEvent.Action.valueOf("UPDATE"));
    }

    @Test
    void testStoreTransactionEventConstructorAndGetters() {
        // Given
        Store store = new Store("Test Store");
        store.setQuantityProductsInStock(1000);
        StoreTransactionEvent event = new StoreTransactionEvent(store, StoreTransactionEvent.Action.CREATE);

        // When & Then
        assertEquals(StoreTransactionEvent.Action.CREATE, event.getAction());
        assertEquals(store, event.getStore());
        assertEquals("Test Store", event.getStore().getName());
        assertEquals(1000, event.getStore().getQuantityProductsInStock());
    }

    @Test
    void testStoreConstructors() {
        // Given
        Store store1 = new Store();
        Store store2 = new Store("Test Store");

        // When & Then
        assertNotNull(store1);
        assertNotNull(store2);
        assertEquals("Test Store", store2.name);
    }

    @Test
    void testStoreFields() {
        // Given
        Store store = new Store();
        store.name = "Test Store";
        store.quantityProductsInStock = 500;

        // When & Then
        assertEquals("Test Store", store.name);
        assertEquals(500, store.quantityProductsInStock);
    }

    @Test
    void testLegacyStoreManagerGatewayCreateStore() {
        // Given
        LegacyStoreManagerGateway legacyGateway = new LegacyStoreManagerGateway();
        Store store = new Store("Test Store");
        store.quantityProductsInStock = 1000;

        // When & Then
        assertDoesNotThrow(() -> legacyGateway.createStoreOnLegacySystem(store));
    }

    @Test
    void testLegacyStoreManagerGatewayUpdateStore() {
        // Given
        LegacyStoreManagerGateway legacyGateway = new LegacyStoreManagerGateway();
        Store store = new Store("Test Store");
        store.quantityProductsInStock = 1000;

        // When & Then
        assertDoesNotThrow(() -> legacyGateway.updateStoreOnLegacySystem(store));
    }

    @Test
    void testLegacyStoreManagerGatewayWithNullStore() {
        // Given
        LegacyStoreManagerGateway legacyGateway = new LegacyStoreManagerGateway();

        // When & Then
        assertDoesNotThrow(() -> legacyGateway.createStoreOnLegacySystem(null));
        assertDoesNotThrow(() -> legacyGateway.updateStoreOnLegacySystem(null));
    }

    @Test
    void testLegacyStoreManagerGatewayWithEmptyStoreName() {
        // Given
        LegacyStoreManagerGateway legacyGateway = new LegacyStoreManagerGateway();
        Store store = new Store("");
        store.quantityProductsInStock = 1000;

        // When & Then
        assertDoesNotThrow(() -> legacyGateway.createStoreOnLegacySystem(store));
        assertDoesNotThrow(() -> legacyGateway.updateStoreOnLegacySystem(store));
    }

    @Test
    void testLegacyStoreManagerGatewayWithZeroStock() {
        // Given
        LegacyStoreManagerGateway legacyGateway = new LegacyStoreManagerGateway();
        Store store = new Store("Zero Stock Store");
        store.quantityProductsInStock = 0;

        // When & Then
        assertDoesNotThrow(() -> legacyGateway.createStoreOnLegacySystem(store));
        assertDoesNotThrow(() -> legacyGateway.updateStoreOnLegacySystem(store));
    }

    @Test
    void testLegacyStoreManagerGatewayWithLargeStock() {
        // Given
        LegacyStoreManagerGateway legacyGateway = new LegacyStoreManagerGateway();
        Store store = new Store("Large Stock Store");
        store.quantityProductsInStock = Integer.MAX_VALUE;

        // When & Then
        assertDoesNotThrow(() -> legacyGateway.createStoreOnLegacySystem(store));
        assertDoesNotThrow(() -> legacyGateway.updateStoreOnLegacySystem(store));
    }

    private boolean contains(StoreTransactionEvent.Action[] actions, StoreTransactionEvent.Action target) {
        for (StoreTransactionEvent.Action action : actions) {
            if (action == target) {
                return true;
            }
        }
        return false;
    }
}
