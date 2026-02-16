package com.fulfilment.application.monolith.products;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductResourceUnitTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductResource productResource;

    @Test
    void testGet() {
        // Arrange
        List<Product> products = Arrays.asList(new Product("Test Product"));
        when(productRepository.listAll(any(Sort.class))).thenReturn(products);

        // Act
        List<Product> result = productResource.get();

        // Assert
        assertEquals(products, result);
        verify(productRepository).listAll(any(Sort.class));
    }

    @Test
    void testGetSingle_Found() {
        // Arrange
        Product product = new Product("Test Product");
        product.id = 1L;
        when(productRepository.findById(1L)).thenReturn(product);

        // Act
        Product result = productResource.getSingle(1L);

        // Assert
        assertEquals(product, result);
    }

    @Test
    void testGetSingle_NotFound() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(null);

        // Act & Assert
        WebApplicationException exception = assertThrows(WebApplicationException.class, () ->
            productResource.getSingle(1L));
        assertEquals(404, exception.getResponse().getStatus());
    }

    @Test
    void testCreate_Success() {
        // Arrange
        Product product = new Product("New Product");
        product.name = "New Product";
        doNothing().when(productRepository).persist(product);

        // Act
        Response response = productResource.create(product);

        // Assert
        assertEquals(201, response.getStatus());
        assertEquals(product, response.getEntity());
        verify(productRepository).persist(product);
    }

    @Test
    void testCreate_IdSet() {
        // Arrange
        Product product = new Product("New Product");
        product.id = 1L;

        // Act & Assert
        WebApplicationException exception = assertThrows(WebApplicationException.class, () ->
            productResource.create(product));
        assertEquals(422, exception.getResponse().getStatus());
    }

    @Test
    void testUpdate_Success() {
        // Arrange
        Product existing = new Product("Old Name");
        existing.id = 1L;
        existing.name = "Old Name";
        existing.description = "Old Desc";
        existing.price = BigDecimal.valueOf(10.0);
        existing.stock = 5;

        Product update = new Product();
        update.name = "New Name";
        update.description = "New Desc";
        update.price = BigDecimal.valueOf(20.0);
        update.stock = 10;

        when(productRepository.findById(1L)).thenReturn(existing);

        // Act
        Product result = productResource.update(1L, update);

        // Assert
        assertEquals("New Name", result.name);
        assertEquals("New Desc", result.description);
        assertEquals(BigDecimal.valueOf(20.0), result.price);
        assertEquals(10, result.stock);
        verify(productRepository).persist(existing);
    }

    @Test
    void testUpdate_NameNull() {
        // Arrange
        Product update = new Product();
        update.name = null;

        // Act & Assert
        WebApplicationException exception = assertThrows(WebApplicationException.class, () ->
            productResource.update(1L, update));
        assertEquals(422, exception.getResponse().getStatus());
    }

    @Test
    void testUpdate_NotFound() {
        // Arrange
        Product update = new Product("New Name");
        when(productRepository.findById(1L)).thenReturn(null);

        // Act & Assert
        WebApplicationException exception = assertThrows(WebApplicationException.class, () ->
            productResource.update(1L, update));
        assertEquals(404, exception.getResponse().getStatus());
    }

    @Test
    void testDelete_Success() {
        // Arrange
        Product product = new Product("Test Product");
        when(productRepository.findById(1L)).thenReturn(product);

        // Act
        Response response = productResource.delete(1L);

        // Assert
        assertEquals(204, response.getStatus());
        verify(productRepository).delete(product);
    }

    @Test
    void testDelete_NotFound() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(null);

        // Act & Assert
        WebApplicationException exception = assertThrows(WebApplicationException.class, () ->
            productResource.delete(1L));
        assertEquals(404, exception.getResponse().getStatus());
    }
}
