package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class LocationGateway implements LocationResolver {

  /** List of predefined locations. */
  private static final List<Location> LOCATIONS = new ArrayList<>();

  static {
    LOCATIONS.add(new Location("ZWOLLE-001", 1, 40));
    LOCATIONS.add(new Location("ZWOLLE-002", 2, 50));
    LOCATIONS.add(new Location("AMSTERDAM-001", 5, 100));
    LOCATIONS.add(new Location("AMSTERDAM-002", 3, 75));
    LOCATIONS.add(new Location("TILBURG-001", 1, 40));
    LOCATIONS.add(new Location("HELMOND-001", 1, 45));
    LOCATIONS.add(new Location("EINDHOVEN-001", 2, 70));
    LOCATIONS.add(new Location("VETSBY-001", 1, 90));
  }

  @Override
  public Location resolveByIdentifier(String identifier) {
    if (identifier == null || identifier.trim().isEmpty()) {
      return null;
    }
    
    return LOCATIONS.stream()
        .filter(location -> location.identification.equals(identifier))
        .findFirst()
        .orElse(null);
  }
}
