package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void archive(Warehouse warehouse) {
    // Validate that warehouse exists
    Warehouse existing = warehouseStore.findById(warehouse.id);
    if (existing == null) {
      throw new IllegalArgumentException("Warehouse with id " + warehouse.id + " not found");
    }
    
    // Check if warehouse is already archived
    if (existing.archivedAt != null) {
      throw new IllegalArgumentException("Warehouse with id " + warehouse.id + " is already archived");
    }
    
    // Set archive timestamp
    existing.archivedAt = LocalDateTime.now();
    
    warehouseStore.update(existing);
  }
}
