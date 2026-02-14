package com.fulfilment.application.monolith.health;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

/**
 * Health check REST endpoint for the Warehouse Management System
 */
@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    @Inject
    WarehouseSystemHealthCheck healthCheck;

    @GET
    public Response getHealth() {
        if (healthCheck.isHealthy()) {
            return Response.ok(healthCheck.getHealthStatus()).build();
        } else {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(healthCheck.getHealthStatus())
                    .build();
        }
    }

    @GET
    @Path("/live")
    public Response liveness() {
        return Response.ok(Map.of("status", "UP")).build();
    }

    @GET
    @Path("/ready")
    public Response readiness() {
        if (healthCheck.isHealthy()) {
            return Response.ok(Map.of("status", "READY")).build();
        } else {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(Map.of("status", "NOT_READY"))
                    .build();
        }
    }

    @GET
    @Path("/metrics")
    public Response getMetrics() {
        return Response.ok(healthCheck.getHealthStatus()).build();
    }

    @GET
    @Path("/check/{component}")
    public Response checkComponent(@PathParam("component") String component) {
        Map<String, Object> health = healthCheck.getHealthStatus();
        
        switch (component.toLowerCase()) {
            case "memory":
                return Response.ok(health.get("memory")).build();
            case "cpu":
                return Response.ok(health.get("cpu")).build();
            case "business":
                return Response.ok(health.get("business_metrics")).build();
            default:
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Component not found: " + component))
                        .build();
        }
    }
}
