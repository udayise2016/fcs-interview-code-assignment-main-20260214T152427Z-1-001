package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class StoreTransactionEventListenerIntegrationTest {

    @Inject
    StoreTransactionEventListener eventListener;

    @Inject
    LegacyStoreManagerGateway legacyGateway;

    private ByteArrayOutputStream testOut;

    @BeforeEach
    void setUp() {
        testOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));
    }

    @Test
    void testAfterTransactionCommit_CreateAction() {
        Store store = new Store("Create Test Store");
        store.setQuantityProductsInStock(100);
        
        StoreTransactionEvent event = new StoreTransactionEvent(store, StoreTransactionEvent.Action.CREATE);
        
        assertDoesNotThrow(() -> eventListener.afterTransactionCommit(event));
        
        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =Create Test Store ] [ items on stock =100 ]"));
    }

    @Test
    void testAfterTransactionCommit_UpdateAction() {
        Store store = new Store("Update Test Store");
        store.setQuantityProductsInStock(200);
        
        StoreTransactionEvent event = new StoreTransactionEvent(store, StoreTransactionEvent.Action.UPDATE);
        
        assertDoesNotThrow(() -> eventListener.afterTransactionCommit(event));
        
        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =Update Test Store ] [ items on stock =200 ]"));
    }

    @Test
    void testAfterTransactionCommit_NullEvent() {
        assertThrows(NullPointerException.class, () -> eventListener.afterTransactionCommit(null));
    }

    @Test
    void testAfterTransactionCommit_NullStore() {
        StoreTransactionEvent event = new StoreTransactionEvent(null, StoreTransactionEvent.Action.CREATE);
        
        assertDoesNotThrow(() -> eventListener.afterTransactionCommit(event));
        
        String output = testOut.toString();
        // Should handle null store gracefully and print stack trace
        assertTrue(output.contains("java.lang.NullPointerException") || output.contains("Exception"));
    }

    @Test
    void testAfterTransactionCommit_StoreWithEmptyName() {
        Store store = new Store("");
        store.setQuantityProductsInStock(25);
        
        StoreTransactionEvent event = new StoreTransactionEvent(store, StoreTransactionEvent.Action.CREATE);
        
        assertDoesNotThrow(() -> eventListener.afterTransactionCommit(event));
        
        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name = ] [ items on stock =25 ]"));
    }

    @Test
    void testAfterTransactionCommit_StoreWithNegativeStock() {
        Store store = new Store("Negative Stock");
        store.setQuantityProductsInStock(-10);
        
        StoreTransactionEvent event = new StoreTransactionEvent(store, StoreTransactionEvent.Action.UPDATE);
        
        assertDoesNotThrow(() -> eventListener.afterTransactionCommit(event));
        
        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =Negative Stock ] [ items on stock =-10 ]"));
    }

    @Test
    void testAfterTransactionCommit_StoreWithMaximumStock() {
        Store store = new Store("Max Stock Store");
        store.setQuantityProductsInStock(Integer.MAX_VALUE);
        
        StoreTransactionEvent event = new StoreTransactionEvent(store, StoreTransactionEvent.Action.CREATE);
        
        assertDoesNotThrow(() -> eventListener.afterTransactionCommit(event));
        
        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =Max Stock Store ] [ items on stock =" + Integer.MAX_VALUE + " ]"));
    }

    @Test
    void testAfterTransactionCommit_StoreWithMinimumStock() {
        Store store = new Store("Min Stock Store");
        store.setQuantityProductsInStock(Integer.MIN_VALUE);
        
        StoreTransactionEvent event = new StoreTransactionEvent(store, StoreTransactionEvent.Action.UPDATE);
        
        assertDoesNotThrow(() -> eventListener.afterTransactionCommit(event));
        
        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =Min Stock Store ] [ items on stock =" + Integer.MIN_VALUE + " ]"));
    }

    @Test
    void testAfterTransactionCommit_StoreWithSpecialCharacters() {
        Store store = new Store("Special!@#$%^&*()Store");
        store.setQuantityProductsInStock(123);
        
        StoreTransactionEvent event = new StoreTransactionEvent(store, StoreTransactionEvent.Action.CREATE);
        
        assertDoesNotThrow(() -> eventListener.afterTransactionCommit(event));
        
        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =Special!@#$%^&*()Store ] [ items on stock =123 ]"));
    }

    @Test
    void testAfterTransactionCommit_StoreWithUnicodeCharacters() {
        Store store = new Store("商店测试🏪");
        store.setQuantityProductsInStock(456);
        
        StoreTransactionEvent event = new StoreTransactionEvent(store, StoreTransactionEvent.Action.UPDATE);
        
        assertDoesNotThrow(() -> eventListener.afterTransactionCommit(event));
        
        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =商店测试🏪 ] [ items on stock =456 ]"));
    }

    @Test
    void testAfterTransactionCommit_StoreWithZeroStock() {
        Store store = new Store("Zero Stock Store");
        store.setQuantityProductsInStock(0);
        
        StoreTransactionEvent event = new StoreTransactionEvent(store, StoreTransactionEvent.Action.CREATE);
        
        assertDoesNotThrow(() -> eventListener.afterTransactionCommit(event));
        
        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =Zero Stock Store ] [ items on stock =0 ]"));
    }

    @Test
    void testAfterTransactionCommit_MultipleEventsSameStore() {
        Store store = new Store("Multi Event Store");
        store.setQuantityProductsInStock(100);
        
        StoreTransactionEvent createEvent = new StoreTransactionEvent(store, StoreTransactionEvent.Action.CREATE);
        StoreTransactionEvent updateEvent = new StoreTransactionEvent(store, StoreTransactionEvent.Action.UPDATE);
        
        // Process multiple events for the same store
        assertDoesNotThrow(() -> eventListener.afterTransactionCommit(createEvent));
        String createOutput = testOut.toString();
        testOut.reset();
        
        assertDoesNotThrow(() -> eventListener.afterTransactionCommit(updateEvent));
        String updateOutput = testOut.toString();
        
        // Both should process successfully
        assertTrue(createOutput.contains("Store created. [ name =Multi Event Store ] [ items on stock =100 ]"));
        assertTrue(updateOutput.contains("Store created. [ name =Multi Event Store ] [ items on stock =100 ]"));
    }

    @Test
    void testAfterTransactionCommit_MultipleEventsDifferentStores() {
        Store store1 = new Store("Store 1");
        store1.setQuantityProductsInStock(50);
        
        Store store2 = new Store("Store 2");
        store2.setQuantityProductsInStock(75);
        
        Store store3 = new Store("Store 3");
        store3.setQuantityProductsInStock(100);
        
        StoreTransactionEvent event1 = new StoreTransactionEvent(store1, StoreTransactionEvent.Action.CREATE);
        StoreTransactionEvent event2 = new StoreTransactionEvent(store2, StoreTransactionEvent.Action.UPDATE);
        StoreTransactionEvent event3 = new StoreTransactionEvent(store3, StoreTransactionEvent.Action.CREATE);
        
        // Process multiple events
        assertDoesNotThrow(() -> eventListener.afterTransactionCommit(event1));
        assertDoesNotThrow(() -> eventListener.afterTransactionCommit(event2));
        assertDoesNotThrow(() -> eventListener.afterTransactionCommit(event3));
        
        String output = testOut.toString();
        
        // Should contain all three stores
        assertTrue(output.contains("Store created. [ name =Store 1 ] [ items on stock =50 ]"));
        assertTrue(output.contains("Store created. [ name =Store 2 ] [ items on stock =75 ]"));
        assertTrue(output.contains("Store created. [ name =Store 3 ] [ items on stock =100 ]"));
    }

    @Test
    void testEventListenerDependencyInjection() {
        assertNotNull(eventListener);
        assertNotNull(legacyGateway);
        
        // Verify the event listener is properly configured
        // This test mainly ensures CDI injection is working
        assertTrue(eventListener instanceof StoreTransactionEventListener);
    }

    @Test
    void testAfterTransactionCommit_StoreNameLengthBoundary() {
        // Test with name exactly at maximum length (40 characters)
        String maxLengthName = "A".repeat(40);
        Store store = new Store(maxLengthName);
        store.setQuantityProductsInStock(200);
        
        StoreTransactionEvent event = new StoreTransactionEvent(store, StoreTransactionEvent.Action.CREATE);
        
        assertDoesNotThrow(() -> eventListener.afterTransactionCommit(event));
        
        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =" + maxLengthName + " ] [ items on stock =200 ]"));
    }

    @Test
    void testAfterTransactionCommit_StoreNameExceedsBoundary() {
        // Test with name exceeding maximum length (41 characters)
        String tooLongName = "A".repeat(41);
        Store store = new Store(tooLongName);
        store.setQuantityProductsInStock(300);
        
        StoreTransactionEvent event = new StoreTransactionEvent(store, StoreTransactionEvent.Action.CREATE);
        
        assertDoesNotThrow(() -> eventListener.afterTransactionCommit(event));
        
        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =" + tooLongName + " ] [ items on stock =300 ]"));
    }

    @Test
    void testAfterTransactionCommit_StoreWithSpacesName() {
        Store store = new Store("   ");
        store.setQuantityProductsInStock(150);
        
        StoreTransactionEvent event = new StoreTransactionEvent(store, StoreTransactionEvent.Action.UPDATE);
        
        assertDoesNotThrow(() -> eventListener.afterTransactionCommit(event));
        
        String output = testOut.toString();
        assertTrue(output.contains("Store created. [ name =   ] [ items on stock =150 ]"));
    }
}
