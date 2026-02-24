package com.fulfilment.application.monolith.warehouses.domain.validators;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class WarehouseValidator {

  @Inject
  WarehouseStore warehouseStore;

  @Inject
  LocationResolver locationResolver;

  public void validateBusinessUnitCode(Warehouse warehouse) {
    if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.trim().isEmpty()) {
      throw new IllegalArgumentException("Business unit code is required");
    }
  }

  public void validateWarehouseExists(Warehouse warehouse) {
    Warehouse existing = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (existing == null) {
      throw new IllegalArgumentException("Warehouse with business unit code " + 
          warehouse.businessUnitCode + " not found");
    }
  }

  public void validateWarehouseNotArchived(Warehouse warehouse) {
    Warehouse existing = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (existing != null && existing.archivedAt != null) {
      throw new IllegalArgumentException("Warehouse with business unit code " + 
          warehouse.businessUnitCode + " is already archived");
    }
  }

  public void validateLocation(Warehouse warehouse) {
    if (warehouse.location == null || warehouse.location.trim().isEmpty()) {
      throw new IllegalArgumentException("Location is required");
    }
  }

  public void validateLocationExists(Warehouse warehouse) {
    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw new IllegalArgumentException("Invalid location: " + warehouse.location);
    }
  }

  public void validateCapacity(Warehouse warehouse) {
    if (warehouse.capacity == null || warehouse.capacity <= 0) {
      throw new IllegalArgumentException("Capacity must be greater than 0");
    }
  }

  public void validateStock(Warehouse warehouse) {
    if (warehouse.stock == null || warehouse.stock < 0) {
      throw new IllegalArgumentException("Stock cannot be negative");
    }
  }

  public void validateStockNotExceedCapacity(Warehouse warehouse) {
    if (warehouse.stock > warehouse.capacity) {
      throw new IllegalArgumentException("Stock cannot exceed capacity");
    }
  }

  public void validateWarehouseCreationFeasibility(Location location, Warehouse existingWarehouse) {
    long existingWarehousesCount = warehouseStore.getAll().stream()
        .filter(w -> w.location.equals(location.identification) && w.archivedAt == null
            && !w.businessUnitCode.equals(existingWarehouse.businessUnitCode))
        .count();
    if (existingWarehousesCount >= location.maxNumberOfWarehouses) {
      throw new IllegalArgumentException("Maximum number of warehouses (" +
          location.maxNumberOfWarehouses + ") reached for location: " + location.identification);
    }
  }

  public void validateCapacityAgainstLocationLimits(Location location, Warehouse existingWarehouse, Warehouse newWarehouse) {
    int totalLocationCapacity = warehouseStore.getAll().stream()
        .filter(w -> w.location.equals(location.identification) && w.archivedAt == null
            && !w.businessUnitCode.equals(existingWarehouse.businessUnitCode))
        .mapToInt(w -> w.capacity)
        .sum();
    int newTotalCapacity = totalLocationCapacity + newWarehouse.capacity;
    if (newTotalCapacity > location.maxCapacity) {
      throw new IllegalArgumentException("Replacing warehouse would exceed location's maximum capacity. " +
          "Current: " + totalLocationCapacity + ", Replacing with: " + newWarehouse.capacity +
          ", Max allowed: " + location.maxCapacity);
    }
  }

  public void validateReplacementConstraints(Warehouse existing, Warehouse newWarehouse) {
    // Capacity Accommodation: Ensure the new warehouse's capacity can accommodate the stock from the warehouse being replaced
    if (newWarehouse.capacity < existing.stock) {
      throw new IllegalArgumentException("New warehouse capacity (" + newWarehouse.capacity + 
          ") cannot accommodate the stock from the warehouse being replaced (" + existing.stock + ")");
    }

    // Stock Matching: Confirm that the stock of the new warehouse matches the stock of the previous warehouse
    if (!newWarehouse.stock.equals(existing.stock)) {
      throw new IllegalArgumentException("New warehouse stock (" + newWarehouse.stock + 
          ") must match the stock of the previous warehouse (" + existing.stock + ")");
    }
  }
}
