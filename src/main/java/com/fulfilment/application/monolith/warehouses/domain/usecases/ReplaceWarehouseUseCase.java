package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.domain.validators.WarehouseValidator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  @Inject
  WarehouseStore warehouseStore;

  @Inject
  LocationResolver locationResolver;

  @Inject
  WarehouseValidator validator;

  // Constructor for testing
  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver, WarehouseValidator validator) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
    this.validator = validator;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    // Validate business unit code is present (moved before warehouse exists check)
    validator.validateBusinessUnitCode(newWarehouse);

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
    validator.validateLocation(newWarehouse);
    validator.validateCapacity(newWarehouse);
    validator.validateStock(newWarehouse);
    validator.validateStockNotExceedCapacity(newWarehouse);

    // Validate location exists
    Location location = locationResolver.resolveByIdentifier(newWarehouse.location);
    if (location == null) {
      throw new IllegalArgumentException("Invalid location: " + newWarehouse.location);
    }

    // Validate warehouse creation feasibility (max warehouses per location)
    validator.validateWarehouseCreationFeasibility(location, existing);

    // Validate capacity and stock against location limits
    validator.validateCapacityAgainstLocationLimits(location, existing, newWarehouse);

    // Validate replacement-specific constraints
    validator.validateReplacementConstraints(existing, newWarehouse);

    // Archive the existing warehouse
    existing.archivedAt = LocalDateTime.now();
    warehouseStore.update(existing);
    
    // Create the new warehouse with the same business unit code
    newWarehouse.createdAt = LocalDateTime.now();
    newWarehouse.archivedAt = null; // Ensure new warehouse is not archived
    warehouseStore.create(newWarehouse);
  }
}
