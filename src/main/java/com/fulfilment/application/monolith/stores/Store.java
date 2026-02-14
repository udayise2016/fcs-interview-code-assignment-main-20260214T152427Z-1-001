package com.fulfilment.application.monolith.stores;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

/**
 * Store entity representing a store in the system.
 */
@Entity
@Cacheable
public class Store extends PanacheEntity {

  /** Maximum name length. */
  private static final int MAX_NAME_LENGTH = 40;

  /** The store name. */
  @Column(length = MAX_NAME_LENGTH, unique = true)
  private String name;

  /** The quantity of products in stock. */
  private int quantityProductsInStock;

  /** Default constructor. */
  public Store() { }

  /**
   * Constructs a new Store with the specified name.
   *
   * @param name the store name
   */
  public Store(final String name) {
    this.name = name;
  }

  /**
   * Gets the store name.
   *
   * @return the store name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the store name.
   *
   * @param name the store name
   */
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * Gets the quantity of products in stock.
   *
   * @return the quantity of products in stock
   */
  public int getQuantityProductsInStock() {
    return quantityProductsInStock;
  }

  /**
   * Sets the quantity of products in stock.
   *
   * @param quantityProductsInStock the quantity of products in stock
   */
  public void setQuantityProductsInStock(
      final int quantityProductsInStock) {
    this.quantityProductsInStock = quantityProductsInStock;
  }
}
