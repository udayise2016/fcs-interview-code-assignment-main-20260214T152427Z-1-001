package com.fulfilment.application.monolith.health;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

public class WarehouseSystemHealthCheckTest {

    private WarehouseSystemHealthCheck healthCheck;

    @BeforeEach
    void setUp() {
        healthCheck = new WarehouseSystemHealthCheck();
    }

    @Test
    void testGetHealthStatus() {
        Map<String, Object> health = healthCheck.getHealthStatus();
        
        assertNotNull(health);
        assertTrue(health.containsKey("status"));
        assertEquals("UP", health.get("status"));
        assertTrue(health.containsKey("uptime"));
        assertTrue(health.containsKey("version"));
        assertTrue(health.containsKey("memory"));
        assertTrue(health.containsKey("cpu"));
        assertTrue(health.containsKey("business_metrics"));
    }

    @Test
    void testGetSystemMetrics() {
        Map<String, Object> metrics = healthCheck.getSystemMetrics();
        
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("heap_used"));
        assertTrue(metrics.containsKey("heap_max"));
        assertTrue(metrics.containsKey("non_heap_used"));
        assertTrue(metrics.containsKey("non_heap_max"));
        assertTrue(metrics.containsKey("processors"));
        assertTrue(metrics.containsKey("system_load_average"));
    }

    @Test
    void testGetBusinessMetrics() {
        Map<String, Object> metrics = healthCheck.getBusinessMetrics();
        
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("active_warehouses"));
        assertTrue(metrics.containsKey("pending_operations"));
        assertTrue(metrics.containsKey("error_rate"));
        assertTrue(metrics.containsKey("response_time"));
    }

    @Test
    void testGetApplicationVersion() {
        String version = healthCheck.getApplicationVersion();
        
        assertNotNull(version);
        assertFalse(version.isEmpty());
    }

    @Test
    void testHealthStatusValues() {
        Map<String, Object> health = healthCheck.getHealthStatus();
        
        assertEquals("UP", health.get("status"));
        assertTrue(health.get("uptime") instanceof String);
        assertTrue(health.get("version") instanceof String);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> memory = (Map<String, Object>) health.get("memory");
        assertTrue(memory.containsKey("heap_usage_percent"));
        assertTrue(memory.containsKey("non_heap_usage_percent"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> cpu = (Map<String, Object>) health.get("cpu");
        assertTrue(cpu.containsKey("available_processors"));
        assertTrue(cpu.containsKey("system_load_average"));
    }

    @Test
    void testSystemMetricsValues() {
        Map<String, Object> metrics = healthCheck.getSystemMetrics();
        
        assertTrue(metrics.get("heap_used") instanceof Long);
        assertTrue(metrics.get("heap_max") instanceof Long);
        assertTrue(metrics.get("non_heap_used") instanceof Long);
        assertTrue(metrics.get("non_heap_max") instanceof Long);
        assertTrue(metrics.get("processors") instanceof Integer);
        assertTrue(metrics.get("system_load_average") instanceof Double);
    }

    @Test
    void testBusinessMetricsValues() {
        Map<String, Object> metrics = healthCheck.getBusinessMetrics();
        
        assertTrue(metrics.get("active_warehouses") instanceof Integer);
        assertTrue(metrics.get("pending_operations") instanceof Integer);
        assertTrue(metrics.get("error_rate") instanceof Double);
        assertTrue(metrics.get("response_time") instanceof Double);
    }
}
