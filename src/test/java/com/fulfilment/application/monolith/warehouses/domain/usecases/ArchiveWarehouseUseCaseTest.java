package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ArchiveWarehouseUseCaseTest {

  private ArchiveWarehouseUseCase archiveWarehouseUseCase;
  private TestWarehouseStore warehouseStore;

  @BeforeEach
  void setUp() {
    warehouseStore = new TestWarehouseStore();
    archiveWarehouseUseCase = new ArchiveWarehouseUseCase(warehouseStore);
  }

  @Test
  void shouldArchiveWarehouseWhenValidDataProvided() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH001";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 1000;
    warehouse.stock = 500;
    warehouse.createdAt = LocalDateTime.now().minusDays(1);

    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "WH001";
    existingWarehouse.location = "ZWOLLE-001";
    existingWarehouse.capacity = 1000;
    existingWarehouse.stock = 500;
    existingWarehouse.createdAt = LocalDateTime.now().minusDays(1);
    warehouseStore.warehouseToReturn = existingWarehouse;

    // When
    assertDoesNotThrow(() -> archiveWarehouseUseCase.archive(warehouse));

    // Then
    assertNotNull(warehouseStore.updatedWarehouse);
    assertEquals("WH001", warehouseStore.updatedWarehouse.businessUnitCode);
    assertNotNull(warehouseStore.updatedWarehouse.archivedAt);
    assertTrue(warehouseStore.updatedWarehouse.archivedAt.isAfter(LocalDateTime.now().minusMinutes(1)));
  }

  @Test
  void shouldThrowExceptionWhenWarehouseNotFound() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "NONEXISTENT";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 1000;
    warehouse.stock = 500;

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> archiveWarehouseUseCase.archive(warehouse)
    );
    assertEquals("Warehouse with business unit code NONEXISTENT not found", exception.getMessage());
    assertNull(warehouseStore.updatedWarehouse);
  }

  @Test
  void shouldThrowExceptionWhenWarehouseAlreadyArchived() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH001";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 1000;
    warehouse.stock = 500;

    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "WH001";
    existingWarehouse.location = "ZWOLLE-001";
    existingWarehouse.capacity = 1000;
    existingWarehouse.stock = 500;
    existingWarehouse.archivedAt = LocalDateTime.now().minusDays(1);
    warehouseStore.warehouseToReturn = existingWarehouse;

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> archiveWarehouseUseCase.archive(warehouse)
    );
    assertEquals("Warehouse with business unit code WH001 is already archived", exception.getMessage());
    assertNull(warehouseStore.updatedWarehouse);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   ", "\t", "\n"})
  void shouldThrowExceptionWhenBusinessUnitCodeIsBlank(String businessUnitCode) {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = businessUnitCode;
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 1000;
    warehouse.stock = 500;

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> archiveWarehouseUseCase.archive(warehouse)
    );
    assertEquals("Warehouse with business unit code " + businessUnitCode + " not found", exception.getMessage());
    assertNull(warehouseStore.updatedWarehouse);
  }

  @Test
  void shouldThrowExceptionWhenBusinessUnitCodeIsNull() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = null;
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 1000;
    warehouse.stock = 500;

    // When & Then
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> archiveWarehouseUseCase.archive(warehouse)
    );
    assertEquals("Warehouse with business unit code null not found", exception.getMessage());
    assertNull(warehouseStore.updatedWarehouse);
  }

  @Test
  void shouldPreserveWarehouseDataDuringArchiving() {
    // Given
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH001";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 1000;
    warehouse.stock = 500;
    warehouse.createdAt = LocalDateTime.now().minusDays(5);

    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "WH001";
    existingWarehouse.location = "ZWOLLE-001";
    existingWarehouse.capacity = 1000;
    existingWarehouse.stock = 500;
    existingWarehouse.createdAt = LocalDateTime.now().minusDays(5);
    warehouseStore.warehouseToReturn = existingWarehouse;

    // When
    archiveWarehouseUseCase.archive(warehouse);

    // Then
    Warehouse archived = warehouseStore.updatedWarehouse;
    assertNotNull(archived);
    assertEquals("WH001", archived.businessUnitCode);
    assertEquals("ZWOLLE-001", archived.location);
    assertEquals(1000, archived.capacity);
    assertEquals(500, archived.stock);
    assertEquals(existingWarehouse.createdAt, archived.createdAt);
    assertNotNull(archived.archivedAt);
  }

  @Test
  void shouldHandleMultipleArchivingOperations() {
    // Given
    Warehouse warehouse1 = new Warehouse();
    warehouse1.businessUnitCode = "WH001";
    warehouse1.location = "ZWOLLE-001";
    warehouse1.capacity = 1000;
    warehouse1.stock = 500;

    Warehouse warehouse2 = new Warehouse();
    warehouse2.businessUnitCode = "WH002";
    warehouse2.location = "AMSTERDAM-001";
    warehouse2.capacity = 2000;
    warehouse2.stock = 1000;

    Warehouse existing1 = new Warehouse();
    existing1.businessUnitCode = "WH001";
    existing1.location = "ZWOLLE-001";
    existing1.capacity = 1000;
    existing1.stock = 500;

    Warehouse existing2 = new Warehouse();
    existing2.businessUnitCode = "WH002";
    existing2.location = "AMSTERDAM-001";
    existing2.capacity = 2000;
    existing2.stock = 1000;

    // When
    warehouseStore.warehouseToReturn = existing1;
    archiveWarehouseUseCase.archive(warehouse1);

    warehouseStore.warehouseToReturn = existing2;
    archiveWarehouseUseCase.archive(warehouse2);

    // Then
    assertEquals(2, warehouseStore.updatedWarehouses.size());
    assertTrue(warehouseStore.updatedWarehouses.stream().allMatch(w -> w.archivedAt != null));
  }

  // Test implementation of WarehouseStore for testing
  private static class TestWarehouseStore implements WarehouseStore {
    Warehouse updatedWarehouse;
    Warehouse warehouseToReturn;
    java.util.List<Warehouse> updatedWarehouses = new java.util.ArrayList<>();

    @Override
    public java.util.List<Warehouse> getAll() {
      return java.util.Collections.emptyList();
    }

    @Override
    public void create(Warehouse warehouse) {
      // Not used in this test
    }

    @Override
    public void update(Warehouse warehouse) {
      updatedWarehouse = warehouse;
      updatedWarehouses.add(warehouse);
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
}
