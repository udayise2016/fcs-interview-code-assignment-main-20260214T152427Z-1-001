package com.fulfilment.application.monolith.warehouses.domain.validators;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WarehouseValidatorTest {

  @Mock
  WarehouseStore warehouseStore;

  @Mock
  LocationResolver locationResolver;

  WarehouseValidator validator;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    validator = new WarehouseValidator();
    validator.warehouseStore = warehouseStore;
    validator.locationResolver = locationResolver;
  }

  @Test
  void shouldValidateBusinessUnitCodeSuccessfully() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH001";
    
    assertDoesNotThrow(() -> validator.validateBusinessUnitCode(warehouse));
  }

  @Test
  void shouldThrowExceptionWhenBusinessUnitCodeIsNull() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = null;
    
    assertThrows(IllegalArgumentException.class, () -> validator.validateBusinessUnitCode(warehouse));
  }

  @Test
  void shouldThrowExceptionWhenBusinessUnitCodeIsEmpty() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "";
    
    assertThrows(IllegalArgumentException.class, () -> validator.validateBusinessUnitCode(warehouse));
  }

  @Test
  void shouldThrowExceptionWhenBusinessUnitCodeIsBlank() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "   ";
    
    assertThrows(IllegalArgumentException.class, () -> validator.validateBusinessUnitCode(warehouse));
  }

  @Test
  void shouldValidateLocationSuccessfully() {
    Warehouse warehouse = new Warehouse();
    warehouse.location = "ZWOLLE-001";
    
    assertDoesNotThrow(() -> validator.validateLocation(warehouse));
  }

  @Test
  void shouldThrowExceptionWhenLocationIsNull() {
    Warehouse warehouse = new Warehouse();
    warehouse.location = null;
    
    assertThrows(IllegalArgumentException.class, () -> validator.validateLocation(warehouse));
  }

  @Test
  void shouldThrowExceptionWhenLocationIsEmpty() {
    Warehouse warehouse = new Warehouse();
    warehouse.location = "";
    
    assertThrows(IllegalArgumentException.class, () -> validator.validateLocation(warehouse));
  }

  @Test
  void shouldValidateCapacitySuccessfully() {
    Warehouse warehouse = new Warehouse();
    warehouse.capacity = 1000;
    
    assertDoesNotThrow(() -> validator.validateCapacity(warehouse));
  }

  @Test
  void shouldThrowExceptionWhenCapacityIsNull() {
    Warehouse warehouse = new Warehouse();
    warehouse.capacity = null;
    
    assertThrows(IllegalArgumentException.class, () -> validator.validateCapacity(warehouse));
  }

  @Test
  void shouldThrowExceptionWhenCapacityIsZero() {
    Warehouse warehouse = new Warehouse();
    warehouse.capacity = 0;
    
    assertThrows(IllegalArgumentException.class, () -> validator.validateCapacity(warehouse));
  }

  @Test
  void shouldThrowExceptionWhenCapacityIsNegative() {
    Warehouse warehouse = new Warehouse();
    warehouse.capacity = -100;
    
    assertThrows(IllegalArgumentException.class, () -> validator.validateCapacity(warehouse));
  }

  @Test
  void shouldValidateStockSuccessfully() {
    Warehouse warehouse = new Warehouse();
    warehouse.stock = 500;
    
    assertDoesNotThrow(() -> validator.validateStock(warehouse));
  }

  @Test
  void shouldThrowExceptionWhenStockIsNull() {
    Warehouse warehouse = new Warehouse();
    warehouse.stock = null;
    
    assertThrows(IllegalArgumentException.class, () -> validator.validateStock(warehouse));
  }

  @Test
  void shouldThrowExceptionWhenStockIsNegative() {
    Warehouse warehouse = new Warehouse();
    warehouse.stock = -50;
    
    assertThrows(IllegalArgumentException.class, () -> validator.validateStock(warehouse));
  }

  @Test
  void shouldValidateStockNotExceedCapacitySuccessfully() {
    Warehouse warehouse = new Warehouse();
    warehouse.capacity = 1000;
    warehouse.stock = 500;
    
    assertDoesNotThrow(() -> validator.validateStockNotExceedCapacity(warehouse));
  }

  @Test
  void shouldThrowExceptionWhenStockExceedsCapacity() {
    Warehouse warehouse = new Warehouse();
    warehouse.capacity = 1000;
    warehouse.stock = 1500;
    
    assertThrows(IllegalArgumentException.class, () -> validator.validateStockNotExceedCapacity(warehouse));
  }

  @Test
  void shouldValidateWarehouseCreationFeasibilitySuccessfully() {
    Location location = new Location("ZWOLLE-001", 5, 10000);
    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "WH001";
    
    Warehouse warehouse1 = new Warehouse();
    warehouse1.businessUnitCode = "WH002";
    warehouse1.location = "ZWOLLE-001";
    warehouse1.archivedAt = null;
    
    List<Warehouse> warehouses = Arrays.asList(warehouse1);
    when(warehouseStore.getAll()).thenReturn(warehouses);
    
    assertDoesNotThrow(() -> validator.validateWarehouseCreationFeasibility(location, existingWarehouse));
  }

  @Test
  void shouldThrowExceptionWhenMaxWarehousesReached() {
    Location location = new Location("ZWOLLE-001", 1, 10000);
    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "WH001";
    
    Warehouse warehouse1 = new Warehouse();
    warehouse1.businessUnitCode = "WH002";
    warehouse1.location = "ZWOLLE-001";
    warehouse1.archivedAt = null;
    
    List<Warehouse> warehouses = Arrays.asList(warehouse1);
    when(warehouseStore.getAll()).thenReturn(warehouses);
    
    assertThrows(IllegalArgumentException.class, 
        () -> validator.validateWarehouseCreationFeasibility(location, existingWarehouse));
  }

  @Test
  void shouldValidateCapacityAgainstLocationLimitsSuccessfully() {
    Location location = new Location("ZWOLLE-001", 5, 10000);
    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "WH001";
    existingWarehouse.capacity = 2000;
    
    Warehouse newWarehouse = new Warehouse();
    newWarehouse.capacity = 3000;
    
    Warehouse warehouse1 = new Warehouse();
    warehouse1.businessUnitCode = "WH002";
    warehouse1.location = "ZWOLLE-001";
    warehouse1.capacity = 4000;
    warehouse1.archivedAt = null;
    
    List<Warehouse> warehouses = Arrays.asList(warehouse1);
    when(warehouseStore.getAll()).thenReturn(warehouses);
    
    assertDoesNotThrow(() -> validator.validateCapacityAgainstLocationLimits(location, existingWarehouse, newWarehouse));
  }

  @Test
  void shouldThrowExceptionWhenExceedsLocationMaxCapacity() {
    Location location = new Location("ZWOLLE-001", 5, 5000);
    Warehouse existingWarehouse = new Warehouse();
    existingWarehouse.businessUnitCode = "WH001";
    existingWarehouse.capacity = 2000;
    
    Warehouse newWarehouse = new Warehouse();
    newWarehouse.capacity = 4000;
    
    Warehouse warehouse1 = new Warehouse();
    warehouse1.businessUnitCode = "WH002";
    warehouse1.location = "ZWOLLE-001";
    warehouse1.capacity = 2000;
    warehouse1.archivedAt = null;
    
    List<Warehouse> warehouses = Arrays.asList(warehouse1);
    when(warehouseStore.getAll()).thenReturn(warehouses);
    
    assertThrows(IllegalArgumentException.class, 
        () -> validator.validateCapacityAgainstLocationLimits(location, existingWarehouse, newWarehouse));
  }

  @Test
  void shouldValidateReplacementConstraintsSuccessfully() {
    Warehouse existing = new Warehouse();
    existing.stock = 500;
    
    Warehouse newWarehouse = new Warehouse();
    newWarehouse.capacity = 1000;
    newWarehouse.stock = 500;
    
    assertDoesNotThrow(() -> validator.validateReplacementConstraints(existing, newWarehouse));
  }

  @Test
  void shouldThrowExceptionWhenNewWarehouseCapacityCannotAccommodateStock() {
    Warehouse existing = new Warehouse();
    existing.stock = 1500;
    
    Warehouse newWarehouse = new Warehouse();
    newWarehouse.capacity = 1000;
    newWarehouse.stock = 1500;
    
    assertThrows(IllegalArgumentException.class, 
        () -> validator.validateReplacementConstraints(existing, newWarehouse));
  }

  @Test
  void shouldThrowExceptionWhenNewWarehouseStockDoesNotMatchExistingStock() {
    Warehouse existing = new Warehouse();
    existing.stock = 500;
    
    Warehouse newWarehouse = new Warehouse();
    newWarehouse.capacity = 1000;
    newWarehouse.stock = 600;
    
    assertThrows(IllegalArgumentException.class, 
        () -> validator.validateReplacementConstraints(existing, newWarehouse));
  }
}
