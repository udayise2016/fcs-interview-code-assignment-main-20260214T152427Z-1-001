package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class CreateWarehouseUseCaseTest {

  private CreateWarehouseUseCase createWarehouseUseCase;
  private TestWarehouseStore warehouseStore;
  private TestLocationResolver locationResolver;

  @BeforeEach
  void setUp() {
    warehouseStore = new TestWarehouseStore();
    locationResolver = new TestLocationResolver();
    createWarehouseUseCase = new CreateWarehouseUseCase(warehouseStore, locationResolver);
  }

  @Test
  void shouldCreateWarehouseWhenValidDataProvided() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH001";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 30; // Within location max capacity of 40
    warehouse.stock = 20;

    // When
    assertDoesNotThrow(() -> createWarehouseUseCase.create(warehouse));

    // Then
    assertNotNull(warehouseStore.createdWarehouse);
    assertEquals("WH001", warehouseStore.createdWarehouse.businessUnitCode);
    assertEquals("ZWOLLE-001", warehouseStore.createdWarehouse.location);
    assertEquals(30, warehouseStore.createdWarehouse.capacity);
    assertEquals(20, warehouseStore.createdWarehouse.stock);
    assertNotNull(warehouseStore.createdWarehouse.createdAt);
  }

  @Test
  void shouldThrowExceptionWhenBusinessUnitCodeExists() {
    // Given
    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "WH001";
    existingWarehouse.location = "ZWOLLE-001";
    existingWarehouse.capacity = 500;
    existingWarehouse.stock = 250;
    warehouseStore.warehouseToReturn = existingWarehouse;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH001";
    newWarehouse.location = "ZWOLLE-001";
    newWarehouse.capacity = 1000;
    newWarehouse.stock = 500;

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> createWarehouseUseCase.create(newWarehouse)
    );
    assertEquals("Warehouse with business unit code WH001 already exists", exception.getMessage());
    assertNull(warehouseStore.createdWarehouse);
  }

  @Test
  void shouldThrowExceptionWhenBusinessUnitCodeIsEmpty() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 1000;
    warehouse.stock = 500;

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> createWarehouseUseCase.create(warehouse)
    );
    assertEquals("Business unit code is required", exception.getMessage());
    assertNull(warehouseStore.createdWarehouse);
  }

  @Test
  void shouldThrowExceptionWhenLocationIsInvalid() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH001";
    warehouse.location = "INVALID-LOCATION";
    warehouse.capacity = 1000;
    warehouse.stock = 500;

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> createWarehouseUseCase.create(warehouse)
    );
    assertEquals("Invalid location: INVALID-LOCATION", exception.getMessage());
    assertNull(warehouseStore.createdWarehouse);
  }

  @Test
  void shouldAllowReuseOfArchivedBusinessUnitCode() {
    // Given
    Warehouse archivedWarehouse = new Warehouse();
    archivedWarehouse.businessUnitCode = "WH001";
    archivedWarehouse.archivedAt = LocalDateTime.now().minusDays(1);
    warehouseStore.warehouseToReturn = archivedWarehouse;

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH001";
    newWarehouse.location = "ZWOLLE-001";
    newWarehouse.capacity = 30; // Within location max capacity of 40
    newWarehouse.stock = 20;

    // When
    assertDoesNotThrow(() -> createWarehouseUseCase.create(newWarehouse));

    // Then
    assertNotNull(warehouseStore.createdWarehouse);
    assertEquals("WH001", warehouseStore.createdWarehouse.businessUnitCode);
  }

  @Test
  void shouldThrowExceptionWhenMaxWarehousesReached() {
    // Given
    // Set up location with max 1 warehouse
    Location location = new Location("ZWOLLE-001", 1, 40);
    locationResolver.locationToReturn = location;

    // Add existing warehouse to this location
    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "WH001";
    existingWarehouse.location = "ZWOLLE-001";
    existingWarehouse.capacity = 20;
    existingWarehouse.stock = 10;
    warehouseStore.existingWarehouses.add(existingWarehouse);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH002";
    newWarehouse.location = "ZWOLLE-001";
    newWarehouse.capacity = 1000;
    newWarehouse.stock = 500;

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> createWarehouseUseCase.create(newWarehouse)
    );
    assertEquals("Maximum number of warehouses (1) reached for location: ZWOLLE-001", exception.getMessage());
    assertNull(warehouseStore.createdWarehouse);
  }

  @Test
  void shouldThrowExceptionWhenLocationCapacityExceeded() {
    // Given
    // Set up location with max capacity 40
    Location location = new Location("ZWOLLE-001", 5, 40);
    locationResolver.locationToReturn = location;

    // Add existing warehouse using 30 capacity
    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "WH001";
    existingWarehouse.location = "ZWOLLE-001";
    existingWarehouse.capacity = 30;
    existingWarehouse.stock = 15;
    warehouseStore.existingWarehouses.add(existingWarehouse);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH002";
    newWarehouse.location = "ZWOLLE-001";
    newWarehouse.capacity = 20; // This would exceed max capacity (30 + 20 > 40)
    newWarehouse.stock = 10;

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> createWarehouseUseCase.create(newWarehouse)
    );
    assertEquals("Adding warehouse would exceed location's maximum capacity. Current: 30, Adding: 20, Max allowed: 40", exception.getMessage());
    assertNull(warehouseStore.createdWarehouse);
  }

  // Test implementation of WarehouseStore for testing
  private static class TestWarehouseStore implements WarehouseStore {
    Warehouse createdWarehouse;
    Warehouse warehouseToReturn;
    java.util.List<Warehouse> existingWarehouses = new java.util.ArrayList<>();

    @Override
    public java.util.List<Warehouse> getAll() {
      return existingWarehouses;
    }

    @Override
    public void create(Warehouse warehouse) {
      this.createdWarehouse = warehouse;
      // Ensure createdAt is set
      if (this.createdWarehouse.createdAt == null) {
        this.createdWarehouse.createdAt = java.time.LocalDateTime.now();
      }
    }

    @Override
    public void update(Warehouse warehouse) {
      // Not used in this test
    }

    @Override
    public void remove(Warehouse warehouse) {
      // Not used in this test
    }

    @Override
    public Warehouse findByBusinessUnitCode(String buCode) {
      return warehouseToReturn;
    }

    @Override
    public Warehouse findById(String id) {
      return warehouseToReturn;
    }
  }

  // Test implementation of LocationResolver for testing
  private static class TestLocationResolver implements LocationResolver {
    Location locationToReturn;

    @Override
    public Location resolveByIdentifier(String identifier) {
      if ("INVALID-LOCATION".equals(identifier)) {
        return null;
      }
      // Return a default location for valid identifiers
      if (locationToReturn != null) {
        return locationToReturn;
      }
      return new Location(identifier, 5, 100);
    }
  }
}
