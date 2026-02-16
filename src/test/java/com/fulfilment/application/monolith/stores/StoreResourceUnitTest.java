package com.fulfilment.application.monolith.stores;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.enterprise.event.Event;
import jakarta.ws.rs.WebApplicationException;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreResourceUnitTest {

    private StoreResource storeResource;
    private LegacyStoreManagerGateway mockGateway;
    @SuppressWarnings("unchecked")
    private Event<StoreTransactionEvent> mockEvent;

    @BeforeEach
    void setUp() throws Exception {
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
    void testCreate_IdSet() {
        Store store = new Store("Test Store");
        store.id = 1L;

        WebApplicationException exception = assertThrows(WebApplicationException.class, () ->
            storeResource.create(store));
        assertEquals(422, exception.getResponse().getStatus());
    }

    @Test
    void testUpdate_NameNull() {
        Store updatedStore = new Store();
        updatedStore.setName(null);

        WebApplicationException exception = assertThrows(WebApplicationException.class, () ->
            storeResource.update(1L, updatedStore));
        assertEquals(422, exception.getResponse().getStatus());
    }

    @Test
    void testPatch_NameNull() {
        Store updatedStore = new Store();
        updatedStore.setName(null);

        WebApplicationException exception = assertThrows(WebApplicationException.class, () ->
            storeResource.patch(1L, updatedStore));
        assertEquals(422, exception.getResponse().getStatus());
    }
}
