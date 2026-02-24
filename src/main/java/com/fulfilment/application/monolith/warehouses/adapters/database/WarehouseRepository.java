package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    if (warehouse.createdAt == null) {
      warehouse.createdAt = LocalDateTime.now();
    }
    DbWarehouse dbWarehouse = new DbWarehouse(warehouse);
    this.persistAndFlush(dbWarehouse);
    // Set the generated ID back to the domain warehouse
    warehouse.id = dbWarehouse.id.toString();
  }

  @Override
  @Transactional
  public void update(Warehouse warehouse) {
    DbWarehouse dbWarehouse;
    if (warehouse.id != null) {
      // Find by ID if available
      dbWarehouse = find("id", Long.parseLong(warehouse.id)).firstResult();
    } else {
      // Fallback to businessUnitCode
      dbWarehouse = find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    }
    
    if (dbWarehouse != null) {
      dbWarehouse.location = warehouse.location;
      dbWarehouse.capacity = warehouse.capacity;
      dbWarehouse.stock = warehouse.stock;
      dbWarehouse.createdAt = warehouse.createdAt;
      dbWarehouse.archivedAt = warehouse.archivedAt;
      getEntityManager().merge(dbWarehouse);
    }
  }

  @Override
  @Transactional
  public void remove(Warehouse warehouse) {
    DbWarehouse dbWarehouse = find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    if (dbWarehouse != null) {
      this.delete(dbWarehouse);
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse dbWarehouse = find("businessUnitCode", buCode).firstResult();
    return dbWarehouse != null ? dbWarehouse.toWarehouse() : null;
  }

  @Override
  public Warehouse findById(String id) {
    DbWarehouse dbWarehouse = find("id", id).firstResult();
    return dbWarehouse != null ? dbWarehouse.toWarehouse() : null;
  }
}
