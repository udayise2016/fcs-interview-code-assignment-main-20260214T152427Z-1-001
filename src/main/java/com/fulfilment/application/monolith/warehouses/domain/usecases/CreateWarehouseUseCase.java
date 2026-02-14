package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    // Validate business unit code uniqueness
    Warehouse existing = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (existing != null && existing.archivedAt == null) {
      throw new IllegalArgumentException("Warehouse with business unit code " + 
          warehouse.businessUnitCode + " already exists");
    }
    
    // Validate required fields
    if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.trim().isEmpty()) {
      throw new IllegalArgumentException("Business unit code is required");
    }
    
    if (warehouse.location == null || warehouse.location.trim().isEmpty()) {
      throw new IllegalArgumentException("Location is required");
    }
    
    if (warehouse.capacity == null || warehouse.capacity <= 0) {
      throw new IllegalArgumentException("Capacity must be greater than 0");
    }
    
    if (warehouse.stock == null || warehouse.stock < 0) {
      throw new IllegalArgumentException("Stock cannot be negative");
    }
    
    if (warehouse.stock > warehouse.capacity) {
      throw new IllegalArgumentException("Stock cannot exceed capacity");
    }

    // Validate location exists
    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw new IllegalArgumentException("Invalid location: " + warehouse.location);
    }

    // Validate warehouse creation feasibility
    validateWarehouseCreationFeasibility(location, warehouse);

    // Validate capacity and stock against location limits
    validateCapacityAndStockAgainstLocationLimits(location, warehouse);

    // if all went well, create the warehouse
    warehouseStore.create(warehouse);
  }

  private void validateWarehouseCreationFeasibility(Location location, Warehouse warehouse) {
    // Count existing warehouses at this location (excluding archived ones)
    long existingWarehousesCount = warehouseStore.getAll().stream()
        .filter(w -> w.location.equals(location.identification) && w.archivedAt == null)
        .count();
    
    if (existingWarehousesCount >= location.maxNumberOfWarehouses) {
      throw new IllegalArgumentException("Maximum number of warehouses (" + 
          location.maxNumberOfWarehouses + ") reached for location: " + location.identification);
    }
  }

  private void validateCapacityAndStockAgainstLocationLimits(Location location, Warehouse warehouse) {
    // Calculate total capacity at this location (excluding archived warehouses)
    int totalLocationCapacity = warehouseStore.getAll().stream()
        .filter(w -> w.location.equals(location.identification) && w.archivedAt == null)
        .mapToInt(w -> w.capacity)
        .sum();
    
    int newTotalCapacity = totalLocationCapacity + warehouse.capacity;
    
    if (newTotalCapacity > location.maxCapacity) {
      throw new IllegalArgumentException("Adding warehouse would exceed location's maximum capacity. " +
          "Current: " + totalLocationCapacity + ", Adding: " + warehouse.capacity + 
          ", Max allowed: " + location.maxCapacity);
    }
  }
}
