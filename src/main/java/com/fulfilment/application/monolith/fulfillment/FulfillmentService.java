package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.fulfillment.FulfillmentAssociation;
import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class FulfillmentService {

    @Inject
    FulfillmentRepository fulfillmentRepository;

    // Constraints
    private static final int MAX_WAREHOUSES_PER_PRODUCT_STORE = 2;
    private static final int MAX_WAREHOUSES_PER_STORE = 3;
    private static final int MAX_PRODUCT_TYPES_PER_WAREHOUSE = 5;

    @Transactional
    public FulfillmentAssociation createFulfillmentAssociation(Product product, DbWarehouse warehouse, Store store, Integer allocatedStock, Integer maxCapacity) {
        // Validate constraints
        validateConstraints(product, warehouse, store);

        FulfillmentAssociation association = new FulfillmentAssociation(product, warehouse, store, allocatedStock, maxCapacity);
        fulfillmentRepository.persist(association);
        return association;
    }

    @Transactional
    public List<FulfillmentAssociation> getFulfillmentsByProduct(Long productId) {
        return fulfillmentRepository.findByProduct(productId);
    }

    @Transactional
    public List<FulfillmentAssociation> getFulfillmentsByWarehouse(String warehouseBusinessUnitCode) {
        return fulfillmentRepository.findByWarehouse(warehouseBusinessUnitCode);
    }

    @Transactional
    public List<FulfillmentAssociation> getFulfillmentsByStore(Long storeId) {
        return fulfillmentRepository.findByStore(storeId);
    }

    @Transactional
    public void deleteFulfillmentAssociation(Long associationId) {
        fulfillmentRepository.deleteById(associationId);
    }

    private void validateConstraints(Product product, DbWarehouse warehouse, Store store) {
        // Constraint 1: Each Product can be fulfilled by max 2 different Warehouses per Store
        long productStoreCount = fulfillmentRepository.countByProductAndStore(product.id, store.id);
        if (productStoreCount >= MAX_WAREHOUSES_PER_PRODUCT_STORE) {
            throw new IllegalArgumentException("Product " + product.name + " can be fulfilled by maximum " + 
                MAX_WAREHOUSES_PER_PRODUCT_STORE + " warehouses per store. Current: " + productStoreCount);
        }

        // Constraint 2: Each Store can be fulfilled by max 3 different Warehouses
        long storeCount = fulfillmentRepository.countByStore(store.id);
        if (storeCount >= MAX_WAREHOUSES_PER_STORE) {
            throw new IllegalArgumentException("Store " + store.getName() + " can be fulfilled by maximum " + 
                MAX_WAREHOUSES_PER_STORE + " warehouses. Current: " + storeCount);
        }

        // Constraint 3: Each Warehouse can store max 5 types of Products
        long warehouseProductCount = fulfillmentRepository.countByWarehouse(warehouse.businessUnitCode);
        if (warehouseProductCount >= MAX_PRODUCT_TYPES_PER_WAREHOUSE) {
            throw new IllegalArgumentException("Warehouse " + warehouse.businessUnitCode + " can store maximum " + 
                MAX_PRODUCT_TYPES_PER_WAREHOUSE + " product types. Current: " + warehouseProductCount);
        }

        // Check for existing association
        List<FulfillmentAssociation> existing = fulfillmentRepository.findByProductAndStore(product.id, store.id);
        boolean warehouseAlreadyAssociated = existing.stream()
            .anyMatch(assoc -> assoc.warehouse.businessUnitCode.equals(warehouse.businessUnitCode));
        
        if (warehouseAlreadyAssociated) {
            throw new IllegalArgumentException("Warehouse " + warehouse.businessUnitCode + 
                " is already associated with product " + product.name + " for store " + store.getName());
        }
    }
}
