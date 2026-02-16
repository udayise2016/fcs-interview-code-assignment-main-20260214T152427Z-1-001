package com.fulfilment.application.monolith.stores;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreTransactionEventListenerUnitTest {

    @InjectMocks
    private StoreTransactionEventListener eventListener;

    @Mock
    private LegacyStoreManagerGateway mockGateway;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAfterTransactionCommitCreateAction() {
        Store store = new Store("Test Store");
        store.setQuantityProductsInStock(100);
        StoreTransactionEvent event = new StoreTransactionEvent(store, StoreTransactionEvent.Action.CREATE);

        eventListener.afterTransactionCommit(event);

        verify(mockGateway, times(1)).createStoreOnLegacySystem(store);
    }

    @Test
    void testAfterTransactionCommitUpdateAction() {
        Store store = new Store("Test Store");
        store.setQuantityProductsInStock(200);
        StoreTransactionEvent event = new StoreTransactionEvent(store, StoreTransactionEvent.Action.UPDATE);

        eventListener.afterTransactionCommit(event);

        verify(mockGateway, times(1)).updateStoreOnLegacySystem(store);
    }

    @Test
    void testAfterTransactionCommitNullStore() {
        StoreTransactionEvent event = new StoreTransactionEvent(null, StoreTransactionEvent.Action.CREATE);

        // Should handle null store gracefully
        assertDoesNotThrow(() -> {
            eventListener.afterTransactionCommit(event);
        });

        verify(mockGateway, times(1)).createStoreOnLegacySystem(null);
    }

    @Test
    void testAfterTransactionCommitNullEvent() {
        // Should handle null event gracefully - but it will throw NPE
        assertThrows(NullPointerException.class, () -> {
            eventListener.afterTransactionCommit(null);
        });

        verifyNoInteractions(mockGateway);
    }

    @Test
    void testAfterTransactionCommitWithNegativeStock() {
        Store store = new Store("Negative Stock Store");
        store.setQuantityProductsInStock(-50);
        StoreTransactionEvent event = new StoreTransactionEvent(store, StoreTransactionEvent.Action.CREATE);

        eventListener.afterTransactionCommit(event);

        verify(mockGateway, times(1)).createStoreOnLegacySystem(store);
    }

    @Test
    void testAfterTransactionCommitWithZeroStock() {
        Store store = new Store("Zero Stock Store");
        store.setQuantityProductsInStock(0);
        StoreTransactionEvent event = new StoreTransactionEvent(store, StoreTransactionEvent.Action.UPDATE);

        eventListener.afterTransactionCommit(event);

        verify(mockGateway, times(1)).updateStoreOnLegacySystem(store);
    }

    @Test
    void testAfterTransactionCommitWithMaxIntStock() {
        Store store = new Store("Max Stock Store");
        store.setQuantityProductsInStock(Integer.MAX_VALUE);
        StoreTransactionEvent event = new StoreTransactionEvent(store, StoreTransactionEvent.Action.CREATE);

        eventListener.afterTransactionCommit(event);

        verify(mockGateway, times(1)).createStoreOnLegacySystem(store);
    }

    @Test
    void testAfterTransactionCommitWithMinIntStock() {
        Store store = new Store("Min Stock Store");
        store.setQuantityProductsInStock(Integer.MIN_VALUE);
        StoreTransactionEvent event = new StoreTransactionEvent(store, StoreTransactionEvent.Action.UPDATE);

        eventListener.afterTransactionCommit(event);

        verify(mockGateway, times(1)).updateStoreOnLegacySystem(store);
    }

    @Test
    void testAfterTransactionCommitWithSpecialCharacters() {
        Store store = new Store("Special!@#$%^&*()Store");
        store.setQuantityProductsInStock(100);
        StoreTransactionEvent event = new StoreTransactionEvent(store, StoreTransactionEvent.Action.CREATE);

        eventListener.afterTransactionCommit(event);

        verify(mockGateway, times(1)).createStoreOnLegacySystem(store);
    }

    @Test
    void testAfterTransactionCommitWithUnicodeCharacters() {
        Store store = new Store("商店测试🏪");
        store.setQuantityProductsInStock(200);
        StoreTransactionEvent event = new StoreTransactionEvent(store, StoreTransactionEvent.Action.UPDATE);

        eventListener.afterTransactionCommit(event);

        verify(mockGateway, times(1)).updateStoreOnLegacySystem(store);
    }

    @Test
    void testAfterTransactionCommitWithEmptyName() {
        Store store = new Store("");
        store.setQuantityProductsInStock(50);
        StoreTransactionEvent event = new StoreTransactionEvent(store, StoreTransactionEvent.Action.CREATE);

        eventListener.afterTransactionCommit(event);

        verify(mockGateway, times(1)).createStoreOnLegacySystem(store);
    }

    @Test
    void testAfterTransactionCommitWithSpacesName() {
        Store store = new Store("   ");
        store.setQuantityProductsInStock(75);
        StoreTransactionEvent event = new StoreTransactionEvent(store, StoreTransactionEvent.Action.UPDATE);

        eventListener.afterTransactionCommit(event);

        verify(mockGateway, times(1)).updateStoreOnLegacySystem(store);
    }

    @Test
    void testAfterTransactionCommitMultipleEvents() {
        Store store1 = new Store("Store 1");
        store1.setQuantityProductsInStock(100);
        StoreTransactionEvent event1 = new StoreTransactionEvent(store1, StoreTransactionEvent.Action.CREATE);

        Store store2 = new Store("Store 2");
        store2.setQuantityProductsInStock(200);
        StoreTransactionEvent event2 = new StoreTransactionEvent(store2, StoreTransactionEvent.Action.UPDATE);

        eventListener.afterTransactionCommit(event1);
        eventListener.afterTransactionCommit(event2);

        verify(mockGateway, times(1)).createStoreOnLegacySystem(store1);
        verify(mockGateway, times(1)).updateStoreOnLegacySystem(store2);
    }

    @Test
    void testAfterTransactionCommitSameStoreMultipleActions() {
        Store store = new Store("Same Store");
        store.setQuantityProductsInStock(150);
        StoreTransactionEvent createEvent = new StoreTransactionEvent(store, StoreTransactionEvent.Action.CREATE);
        StoreTransactionEvent updateEvent = new StoreTransactionEvent(store, StoreTransactionEvent.Action.UPDATE);

        eventListener.afterTransactionCommit(createEvent);
        eventListener.afterTransactionCommit(updateEvent);

        verify(mockGateway, times(1)).createStoreOnLegacySystem(store);
        verify(mockGateway, times(1)).updateStoreOnLegacySystem(store);
    }

    private static void assertDoesNotThrow(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            fail("Expected no exception, but got: " + e.getMessage());
        }
    }
}
