package com.fulfilment.application.monolith.fulfillment;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class FulfillmentRepository implements PanacheRepository<FulfillmentAssociation> {

    public List<FulfillmentAssociation> findByProduct(Long productId) {
        return find("product.id", productId).list();
    }

    public List<FulfillmentAssociation> findByWarehouse(String warehouseBusinessUnitCode) {
        return find("warehouse.businessUnitCode", warehouseBusinessUnitCode).list();
    }

    public List<FulfillmentAssociation> findByStore(Long storeId) {
        return find("store.id", storeId).list();
    }

    public List<FulfillmentAssociation> findByProductAndStore(Long productId, Long storeId) {
        return find("product.id = ?1 and store.id = ?2", productId, storeId).list();
    }

    public List<FulfillmentAssociation> findByWarehouseAndStore(String warehouseBusinessUnitCode, Long storeId) {
        return find("warehouse.businessUnitCode = ?1 and store.id = ?2", warehouseBusinessUnitCode, storeId).list();
    }

    public long countByWarehouse(String warehouseBusinessUnitCode) {
        return count("warehouse.businessUnitCode", warehouseBusinessUnitCode);
    }

    public long countByProductAndStore(Long productId, Long storeId) {
        return count("product.id = ?1 and store.id = ?2", productId, storeId);
    }

    public long countByStore(Long storeId) {
        return count("store.id", storeId);
    }
}
