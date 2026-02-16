package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "fulfillment_association", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"product_id", "warehouse_id", "store_id"}, 
                              name = "uk_product_warehouse_store")
       })
@Cacheable
public class FulfillmentAssociation {

    @Id @GeneratedValue 
    public Long id;

    @ManyToOne
    public Product product;

    @ManyToOne
    public DbWarehouse warehouse;

    @ManyToOne
    public Store store;

    @Column(nullable = false)
    public Integer allocatedStock;

    @Column(nullable = false)
    public Integer maxCapacity;

    public FulfillmentAssociation() {}

    public FulfillmentAssociation(Product product, DbWarehouse warehouse, Store store, Integer allocatedStock, Integer maxCapacity) {
        this.product = product;
        this.warehouse = warehouse;
        this.store = store;
        this.allocatedStock = allocatedStock;
        this.maxCapacity = maxCapacity;
    }
}
