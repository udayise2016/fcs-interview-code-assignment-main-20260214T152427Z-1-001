package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReplaceWarehouseUseCaseTest {

  private ReplaceWarehouseUseCase replaceWarehouseUseCase;
  private TestWarehouseStore warehouseStore;
  private TestLocationResolver locationResolver;

  @BeforeEach
  void setUp() {
    warehouseStore = new TestWarehouseStore();
    locationResolver = new TestLocationResolver();
    replaceWarehouseUseCase = new ReplaceWarehouseUseCase(warehouseStore, locationResolver);
  }

  @Test
  void shouldReplaceWarehouseWhenValidDataProvided() {
    // Given
    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "WH001";
    existingWarehouse.location = "ZWOLLE-001";
    existingWarehouse.capacity = 1000;
    existingWarehouse.stock = 500;
    existingWarehouse.createdAt = LocalDateTime.now().minusDays(5);
    warehouseStore.warehouseToReturn = existingWarehouse;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH001";
    newWarehouse.location = "ZWOLLE-001";
    newWarehouse.capacity = 800;
    newWarehouse.stock = 500;

    // When
    assertDoesNotThrow(() -> replaceWarehouseUseCase.replace(newWarehouse));

    // Then
    assertNotNull(warehouseStore.updatedWarehouse);
    assertNotNull(warehouseStore.createdWarehouse);
    
    // Verify old warehouse is archived
    assertEquals("WH001", warehouseStore.updatedWarehouse.businessUnitCode);
    assertNotNull(warehouseStore.updatedWarehouse.archivedAt);
    
    // Verify new warehouse is created
    assertEquals("WH001", warehouseStore.createdWarehouse.businessUnitCode);
    assertEquals("ZWOLLE-001", warehouseStore.createdWarehouse.location);
    assertEquals(800, warehouseStore.createdWarehouse.capacity);
    assertEquals(500, warehouseStore.createdWarehouse.stock);
    assertNotNull(warehouseStore.createdWarehouse.createdAt);
    assertNull(warehouseStore.createdWarehouse.archivedAt);
  }

  @Test
  void shouldThrowExceptionWhenWarehouseNotFound() {
    // Given
    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "NONEXISTENT";
    newWarehouse.location = "ZWOLLE-001";
    newWarehouse.capacity = 1000;
    newWarehouse.stock = 500;

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> replaceWarehouseUseCase.replace(newWarehouse)
    );
    assertEquals("Warehouse with business unit code NONEXISTENT not found", exception.getMessage());
    assertNull(warehouseStore.updatedWarehouse);
    assertNull(warehouseStore.createdWarehouse);
  }

  @Test
  void shouldThrowExceptionWhenWarehouseAlreadyArchived() {
    // Given
    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "WH001";
    existingWarehouse.location = "ZWOLLE-001";
    existingWarehouse.capacity = 1000;
    existingWarehouse.stock = 500;
    existingWarehouse.archivedAt = LocalDateTime.now().minusDays(1);
    warehouseStore.warehouseToReturn = existingWarehouse;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH001";
    newWarehouse.location = "ZWOLLE-001";
    newWarehouse.capacity = 1000;
    newWarehouse.stock = 500;

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> replaceWarehouseUseCase.replace(newWarehouse)
    );
    assertEquals("Warehouse with business unit code WH001 is already archived", exception.getMessage());
    assertNull(warehouseStore.updatedWarehouse);
    assertNull(warehouseStore.createdWarehouse);
  }

  @Test
  void shouldThrowExceptionWhenLocationIsInvalid() {
    // Given
    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "WH001";
    existingWarehouse.location = "ZWOLLE-001";
    existingWarehouse.capacity = 1000;
    existingWarehouse.stock = 500;
    warehouseStore.warehouseToReturn = existingWarehouse;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH001";
    newWarehouse.location = "INVALID-LOCATION";
    newWarehouse.capacity = 1000;
    newWarehouse.stock = 500;

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> replaceWarehouseUseCase.replace(newWarehouse)
    );
    assertEquals("Invalid location: INVALID-LOCATION", exception.getMessage());
    assertNull(warehouseStore.updatedWarehouse);
    assertNull(warehouseStore.createdWarehouse);
  }

  @Test
  void shouldThrowExceptionWhenCapacityCannotAccommodateStock() {
    // Given
    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "WH001";
    existingWarehouse.location = "ZWOLLE-001";
    existingWarehouse.capacity = 1000;
    existingWarehouse.stock = 800;
    warehouseStore.warehouseToReturn = existingWarehouse;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH001";
    newWarehouse.location = "ZWOLLE-001";
    newWarehouse.capacity = 500; // Less than existing stock
    newWarehouse.stock = 800;

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> replaceWarehouseUseCase.replace(newWarehouse)
    );
    assertEquals("Stock cannot exceed capacity", exception.getMessage());
    assertNull(warehouseStore.updatedWarehouse);
    assertNull(warehouseStore.createdWarehouse);
  }

  @Test
  void shouldThrowExceptionWhenStockDoesNotMatch() {
    // Given
    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "WH001";
    existingWarehouse.location = "ZWOLLE-001";
    existingWarehouse.capacity = 1000;
    existingWarehouse.stock = 500;
    warehouseStore.warehouseToReturn = existingWarehouse;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH001";
    newWarehouse.location = "ZWOLLE-001";
    newWarehouse.capacity = 1000;
    newWarehouse.stock = 600; // Different from existing stock

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> replaceWarehouseUseCase.replace(newWarehouse)
    );
    assertEquals("New warehouse stock (600) must match the stock of the previous warehouse (500)", exception.getMessage());
    assertNull(warehouseStore.updatedWarehouse);
    assertNull(warehouseStore.createdWarehouse);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   ", "\t", "\n"})
  void shouldThrowExceptionWhenLocationIsEmpty(String location) {
    // Given
    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "WH001";
    existingWarehouse.location = "ZWOLLE-001";
    existingWarehouse.capacity = 1000;
    existingWarehouse.stock = 500;
    warehouseStore.warehouseToReturn = existingWarehouse;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH001";
    newWarehouse.location = location;
    newWarehouse.capacity = 1000;
    newWarehouse.stock = 500;

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> replaceWarehouseUseCase.replace(newWarehouse)
    );
    assertEquals("Location is required", exception.getMessage());
    assertNull(warehouseStore.updatedWarehouse);
    assertNull(warehouseStore.createdWarehouse);
  }

  @Test
  void shouldThrowExceptionWhenCapacityIsInvalid() {
    // Given
    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "WH001";
    existingWarehouse.location = "ZWOLLE-001";
    existingWarehouse.capacity = 1000;
    existingWarehouse.stock = 500;
    warehouseStore.warehouseToReturn = existingWarehouse;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH001";
    newWarehouse.location = "ZWOLLE-001";
    newWarehouse.capacity = 0; // Invalid capacity
    newWarehouse.stock = 500;

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> replaceWarehouseUseCase.replace(newWarehouse)
    );
    assertEquals("Capacity must be greater than 0", exception.getMessage());
    assertNull(warehouseStore.updatedWarehouse);
    assertNull(warehouseStore.createdWarehouse);
  }

  @Test
  void shouldThrowExceptionWhenStockIsNegative() {
    // Given
    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "WH001";
    existingWarehouse.location = "ZWOLLE-001";
    existingWarehouse.capacity = 1000;
    existingWarehouse.stock = 500;
    warehouseStore.warehouseToReturn = existingWarehouse;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH001";
    newWarehouse.location = "ZWOLLE-001";
    newWarehouse.capacity = 1000;
    newWarehouse.stock = -1; // Negative stock

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> replaceWarehouseUseCase.replace(newWarehouse)
    );
    assertEquals("Stock cannot be negative", exception.getMessage());
    assertNull(warehouseStore.updatedWarehouse);
    assertNull(warehouseStore.createdWarehouse);
  }

  @Test
  void shouldThrowExceptionWhenStockExceedsCapacity() {
    // Given
    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "WH001";
    existingWarehouse.location = "ZWOLLE-001";
    existingWarehouse.capacity = 1000;
    existingWarehouse.stock = 500;
    warehouseStore.warehouseToReturn = existingWarehouse;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH001";
    newWarehouse.location = "ZWOLLE-001";
    newWarehouse.capacity = 400;
    newWarehouse.stock = 500; // Stock exceeds capacity

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> replaceWarehouseUseCase.replace(newWarehouse)
    );
    assertEquals("Stock cannot exceed capacity", exception.getMessage());
    assertNull(warehouseStore.updatedWarehouse);
    assertNull(warehouseStore.createdWarehouse);
  }

  @Test
  void shouldThrowExceptionWhenBusinessUnitCodeIsNull() {
    // Given - warehouseStore returns null when businessUnitCode is null (no match)
    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = null;
    newWarehouse.location = "ZWOLLE-001";
    newWarehouse.capacity = 1000;
    newWarehouse.stock = 500;

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> replaceWarehouseUseCase.replace(newWarehouse)
    );
    assertTrue(exception.getMessage().contains("not found") || exception.getMessage().contains("Business unit code is required"));
  }

  @Test
  void shouldThrowExceptionWhenBusinessUnitCodeIsEmpty() {
    // Given
    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "WH001";
    existingWarehouse.location = "ZWOLLE-001";
    existingWarehouse.capacity = 1000;
    existingWarehouse.stock = 500;
    warehouseStore.warehouseToReturn = existingWarehouse;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "   ";
    newWarehouse.location = "ZWOLLE-001";
    newWarehouse.capacity = 1000;
    newWarehouse.stock = 500;

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> replaceWarehouseUseCase.replace(newWarehouse)
    );
    assertEquals("Business unit code is required", exception.getMessage());
  }

  @Test
  void shouldThrowExceptionWhenMaxWarehousesReachedAtLocation() {
    // Given
    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "WH001";
    existingWarehouse.location = "ZWOLLE-001";
    existingWarehouse.capacity = 1000;
    existingWarehouse.stock = 500;
    warehouseStore.warehouseToReturn = existingWarehouse;

    Warehouse otherWarehouse = new Warehouse();
    otherWarehouse.businessUnitCode = "WH002";
    otherWarehouse.location = "ZWOLLE-001";
    otherWarehouse.capacity = 500;
    otherWarehouse.stock = 100;
    warehouseStore.allWarehouses.add(otherWarehouse);

    locationResolver.maxNumberOfWarehouses = 1;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH001";
    newWarehouse.location = "ZWOLLE-001";
    newWarehouse.capacity = 800;
    newWarehouse.stock = 500;

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> replaceWarehouseUseCase.replace(newWarehouse)
    );
    assertTrue(exception.getMessage().contains("Maximum number of warehouses"));
  }

  @Test
  void shouldThrowExceptionWhenExceedsLocationMaxCapacity() {
    // Given
    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "WH001";
    existingWarehouse.location = "ZWOLLE-001";
    existingWarehouse.capacity = 1000;
    existingWarehouse.stock = 100;
    warehouseStore.warehouseToReturn = existingWarehouse;

    Warehouse otherWarehouse = new Warehouse();
    otherWarehouse.businessUnitCode = "WH002";
    otherWarehouse.location = "ZWOLLE-001";
    otherWarehouse.capacity = 800;
    otherWarehouse.stock = 100;
    warehouseStore.allWarehouses.add(otherWarehouse);

    locationResolver.maxCapacity = 900;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH001";
    newWarehouse.location = "ZWOLLE-001";
    newWarehouse.capacity = 200; // other(800) + new(200) = 1000 > maxCapacity(900)
    newWarehouse.stock = 100;   // matches existing stock, does not exceed capacity

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> replaceWarehouseUseCase.replace(newWarehouse)
    );
    assertTrue(exception.getMessage().contains("exceed location's maximum capacity"));
  }

  @Test
  void shouldPreserveBusinessUnitCodeDuringReplacement() {
    // Given
    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "WH001";
    existingWarehouse.location = "ZWOLLE-001";
    existingWarehouse.capacity = 1000;
    existingWarehouse.stock = 500;
    warehouseStore.warehouseToReturn = existingWarehouse;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH001";
    newWarehouse.location = "ZWOLLE-001";
    newWarehouse.capacity = 1200;
    newWarehouse.stock = 500;

    // When
    replaceWarehouseUseCase.replace(newWarehouse);

    // Then
    assertEquals("WH001", warehouseStore.updatedWarehouse.businessUnitCode);
    assertEquals("WH001", warehouseStore.createdWarehouse.businessUnitCode);
  }

  // Test implementation of WarehouseStore for testing
  private static class TestWarehouseStore implements WarehouseStore {
    Warehouse updatedWarehouse;
    Warehouse createdWarehouse;
    Warehouse warehouseToReturn;
    List<Warehouse> allWarehouses = new ArrayList<>();

    @Override
    public List<Warehouse> getAll() {
      return allWarehouses;
    }

    @Override
    public void create(Warehouse warehouse) {
      createdWarehouse = warehouse;
    }

    @Override
    public void update(Warehouse warehouse) {
      updatedWarehouse = warehouse;
    }

    @Override
    public void remove(Warehouse warehouse) {
      // Not used in this test
    }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
      return warehouseToReturn;
    }
  }

  // Test implementation of LocationResolver for testing
  private static class TestLocationResolver implements LocationResolver {
    Location locationToReturn;
    int maxNumberOfWarehouses = 5;
    int maxCapacity = 10000;

    @Override
    public Location resolveByIdentifier(String identifier) {
      if ("INVALID-LOCATION".equals(identifier)) {
        return null;
      }
      if (locationToReturn != null) {
        return locationToReturn;
      }
      return new Location(identifier, maxNumberOfWarehouses, maxCapacity);
    }
  }
}
