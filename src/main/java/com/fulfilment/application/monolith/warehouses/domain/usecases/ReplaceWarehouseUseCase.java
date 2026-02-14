package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    // Validate that the warehouse to replace exists
    Warehouse existing = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (existing == null) {
      throw new IllegalArgumentException("Warehouse with business unit code " + 
          newWarehouse.businessUnitCode + " not found");
    }
    
    // Check if existing warehouse is already archived
    if (existing.archivedAt != null) {
      throw new IllegalArgumentException("Warehouse with business unit code " + 
          newWarehouse.businessUnitCode + " is already archived");
    }
    
    // Validate new warehouse data
    if (newWarehouse.location == null || newWarehouse.location.trim().isEmpty()) {
      throw new IllegalArgumentException("Location is required");
    }
    
    if (newWarehouse.capacity == null || newWarehouse.capacity <= 0) {
      throw new IllegalArgumentException("Capacity must be greater than 0");
    }
    
    if (newWarehouse.stock == null || newWarehouse.stock < 0) {
      throw new IllegalArgumentException("Stock cannot be negative");
    }
    
    if (newWarehouse.stock > newWarehouse.capacity) {
      throw new IllegalArgumentException("Stock cannot exceed capacity");
    }

    // Validate location exists
    Location location = locationResolver.resolveByIdentifier(newWarehouse.location);
    if (location == null) {
      throw new IllegalArgumentException("Invalid location: " + newWarehouse.location);
    }

    // Validate replacement-specific constraints
    validateReplacementConstraints(existing, newWarehouse);

    // Archive the existing warehouse
    existing.archivedAt = LocalDateTime.now();
    warehouseStore.update(existing);
    
    // Create the new warehouse with the same business unit code
    newWarehouse.createdAt = LocalDateTime.now();
    newWarehouse.archivedAt = null; // Ensure new warehouse is not archived
    warehouseStore.create(newWarehouse);
  }

  private void validateReplacementConstraints(Warehouse existing, Warehouse newWarehouse) {
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
