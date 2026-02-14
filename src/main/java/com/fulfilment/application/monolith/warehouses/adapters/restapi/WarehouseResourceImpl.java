package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ArchiveWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ReplaceWarehouseUseCase;
import com.warehouse.api.WarehouseResource;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject private WarehouseRepository warehouseRepository;
  @Inject private CreateWarehouseUseCase createWarehouseUseCase;
  @Inject private ArchiveWarehouseUseCase archiveWarehouseUseCase;
  @Inject private ReplaceWarehouseUseCase replaceWarehouseUseCase;

  @Override
  public List<com.warehouse.api.beans.Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  public com.warehouse.api.beans.Warehouse createANewWarehouseUnit(@NotNull com.warehouse.api.beans.Warehouse data) {
    Warehouse domainWarehouse = toDomainWarehouse(data);
    createWarehouseUseCase.create(domainWarehouse);
    return toWarehouseResponse(domainWarehouse);
  }

  @Override
  public com.warehouse.api.beans.Warehouse getAWarehouseUnitByID(String id) {
    Warehouse domainWarehouse = warehouseRepository.findByBusinessUnitCode(id);
    if (domainWarehouse == null) {
      throw new IllegalArgumentException("Warehouse with business unit code " + id + " not found");
    }
    return toWarehouseResponse(domainWarehouse);
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    Warehouse domainWarehouse = new Warehouse();
    domainWarehouse.businessUnitCode = id;
    archiveWarehouseUseCase.archive(domainWarehouse);
  }

  @Override
  public com.warehouse.api.beans.Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull com.warehouse.api.beans.Warehouse data) {
    // Set the business unit code from the path parameter
    data.setBusinessUnitCode(businessUnitCode);
    
    Warehouse domainWarehouse = toDomainWarehouse(data);
    replaceWarehouseUseCase.replace(domainWarehouse);
    
    // Return the newly created warehouse
    Warehouse newWarehouse = warehouseRepository.findByBusinessUnitCode(businessUnitCode);
    return toWarehouseResponse(newWarehouse);
  }

  private com.warehouse.api.beans.Warehouse toWarehouseResponse(Warehouse warehouse) {
    var response = new com.warehouse.api.beans.Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }
  
  private Warehouse toDomainWarehouse(com.warehouse.api.beans.Warehouse apiWarehouse) {
    var domainWarehouse = new Warehouse();
    domainWarehouse.businessUnitCode = apiWarehouse.getBusinessUnitCode();
    domainWarehouse.location = apiWarehouse.getLocation();
    domainWarehouse.capacity = apiWarehouse.getCapacity();
    domainWarehouse.stock = apiWarehouse.getStock();
    
    return domainWarehouse;
  }
}
