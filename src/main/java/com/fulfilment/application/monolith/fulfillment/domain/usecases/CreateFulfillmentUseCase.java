package com.fulfilment.application.monolith.fulfillment.domain.usecases;

import com.fulfilment.application.monolith.fulfillment.FulfillmentAssociation;
import com.fulfilment.application.monolith.fulfillment.FulfillmentService;
import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CreateFulfillmentUseCase {

    @Inject
    FulfillmentService fulfillmentService;

    public FulfillmentAssociation execute(Product product, DbWarehouse warehouse, Store store, Integer allocatedStock, Integer maxCapacity) {
        // Validate business rules
        validateBusinessRules(product, warehouse, store, allocatedStock, maxCapacity);

        return fulfillmentService.createFulfillmentAssociation(product, warehouse, store, allocatedStock, maxCapacity);
    }

    private void validateBusinessRules(Product product, DbWarehouse warehouse, Store store, Integer allocatedStock, Integer maxCapacity) {
        // Validate allocated stock doesn't exceed warehouse capacity
        if (allocatedStock > warehouse.capacity) {
            throw new IllegalArgumentException("Allocated stock (" + allocatedStock + 
                ") cannot exceed warehouse capacity (" + warehouse.capacity + ")");
        }

        // Validate allocated stock doesn't exceed max capacity
        if (allocatedStock > maxCapacity) {
            throw new IllegalArgumentException("Allocated stock (" + allocatedStock + 
                ") cannot exceed max capacity (" + maxCapacity + ")");
        }

        // Validate max capacity doesn't exceed warehouse capacity
        if (maxCapacity > warehouse.capacity) {
            throw new IllegalArgumentException("Max capacity (" + maxCapacity + 
                ") cannot exceed warehouse capacity (" + warehouse.capacity + ")");
        }

        // Validate stock availability
        if (product.stock < allocatedStock) {
            throw new IllegalArgumentException("Product stock (" + product.stock + 
                ") is insufficient for allocation (" + allocatedStock + ")");
        }

        // Validate warehouse stock availability
        if (warehouse.stock < allocatedStock) {
            throw new IllegalArgumentException("Warehouse stock (" + warehouse.stock + 
                ") is insufficient for allocation (" + allocatedStock + ")");
        }
    }
}
