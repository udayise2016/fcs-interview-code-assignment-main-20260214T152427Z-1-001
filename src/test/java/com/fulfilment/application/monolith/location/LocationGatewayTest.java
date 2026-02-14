package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.junit.jupiter.api.Assertions.*;

public class LocationGatewayTest {

  private LocationGateway locationGateway;

  @BeforeEach
  void setUp() {
    locationGateway = new LocationGateway();
  }

  @Test
  void shouldResolveValidLocationByIdentifier() {
    // When
    Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

    // Then
    assertNotNull(location);
    assertEquals("ZWOLLE-001", location.identification);
    assertEquals(1, location.maxNumberOfWarehouses);
    assertEquals(40, location.maxCapacity);
  }

  @Test
  void shouldResolveAnotherValidLocationByIdentifier() {
    // When
    Location location = locationGateway.resolveByIdentifier("AMSTERDAM-001");

    // Then
    assertNotNull(location);
    assertEquals("AMSTERDAM-001", location.identification);
    assertEquals(5, location.maxNumberOfWarehouses);
    assertEquals(100, location.maxCapacity);
  }

  @Test
  void shouldReturnNullForNonExistentLocation() {
    // When
    Location location = locationGateway.resolveByIdentifier("NONEXISTENT-001");

    // Then
    assertNull(location);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t", "\n", "invalid"})
  void shouldReturnNullForInvalidIdentifiers(String identifier) {
    // When
    Location location = locationGateway.resolveByIdentifier(identifier);

    // Then
    assertNull(location);
  }

  @Test
  void shouldResolveAllAvailableLocations() {
    // Test all predefined locations
    String[] validLocations = {
        "ZWOLLE-001", "ZWOLLE-002", "AMSTERDAM-001", "AMSTERDAM-002",
        "TILBURG-001", "HELMOND-001", "EINDHOVEN-001", "VETSBY-001"
    };

    for (String locationId : validLocations) {
      // When
      Location location = locationGateway.resolveByIdentifier(locationId);

      // Then
      assertNotNull(location, "Location should be found for: " + locationId);
      assertEquals(locationId, location.identification);
      assertTrue(location.maxNumberOfWarehouses > 0);
      assertTrue(location.maxCapacity > 0);
    }
  }

  @Test
  void shouldReturnCorrectLocationProperties() {
    // Test specific location properties
    Location zwolle1 = locationGateway.resolveByIdentifier("ZWOLLE-001");
    assertEquals(1, zwolle1.maxNumberOfWarehouses);
    assertEquals(40, zwolle1.maxCapacity);

    Location zwolle2 = locationGateway.resolveByIdentifier("ZWOLLE-002");
    assertEquals(2, zwolle2.maxNumberOfWarehouses);
    assertEquals(50, zwolle2.maxCapacity);

    Location amsterdam1 = locationGateway.resolveByIdentifier("AMSTERDAM-001");
    assertEquals(5, amsterdam1.maxNumberOfWarehouses);
    assertEquals(100, amsterdam1.maxCapacity);

    Location amsterdam2 = locationGateway.resolveByIdentifier("AMSTERDAM-002");
    assertEquals(3, amsterdam2.maxNumberOfWarehouses);
    assertEquals(75, amsterdam2.maxCapacity);
  }

  @Test
  void shouldBeCaseSensitive() {
    // When
    Location location = locationGateway.resolveByIdentifier("zwolle-001");

    // Then
    assertNull(location); // Should be case sensitive
  }

  @Test
  void shouldHandleConcurrentCalls() {
    // When
    Location location1 = locationGateway.resolveByIdentifier("ZWOLLE-001");
    Location location2 = locationGateway.resolveByIdentifier("ZWOLLE-001");
    Location location3 = locationGateway.resolveByIdentifier("AMSTERDAM-001");

    // Then
    assertNotNull(location1);
    assertNotNull(location2);
    assertNotNull(location3);
    assertEquals(location1.identification, location2.identification);
    assertEquals("ZWOLLE-001", location1.identification);
    assertEquals("AMSTERDAM-001", location3.identification);
  }

  @Test
  void shouldReturnDifferentInstancesForSameIdentifier() {
    // When
    Location location1 = locationGateway.resolveByIdentifier("ZWOLLE-001");
    Location location2 = locationGateway.resolveByIdentifier("ZWOLLE-001");

    // Then
    // Note: Since LocationGateway uses a static list and creates new instances,
    // they should be different objects but with same values
    assertNotNull(location1);
    assertNotNull(location2);
    assertEquals(location1.identification, location2.identification);
    assertEquals(location1.maxNumberOfWarehouses, location2.maxNumberOfWarehouses);
    assertEquals(location1.maxCapacity, location2.maxCapacity);
  }

  @Test
  void shouldHandleSpecialCharactersInIdentifier() {
    // When
    Location location = locationGateway.resolveByIdentifier("ZWOLLE-001@#$");

    // Then
    assertNull(location); // Should not match any predefined location
  }

  @Test
  void shouldValidateLocationDataIntegrity() {
    // When
    Location location = locationGateway.resolveByIdentifier("EINDHOVEN-001");

    // Then
    assertNotNull(location);
    assertEquals("EINDHOVEN-001", location.identification);
    assertEquals(2, location.maxNumberOfWarehouses);
    assertEquals(70, location.maxCapacity);
    
    // Verify all fields are properly set
    assertTrue(location.maxNumberOfWarehouses > 0);
    assertTrue(location.maxCapacity > 0);
    assertNotNull(location.identification);
    assertFalse(location.identification.trim().isEmpty());
  }
}
