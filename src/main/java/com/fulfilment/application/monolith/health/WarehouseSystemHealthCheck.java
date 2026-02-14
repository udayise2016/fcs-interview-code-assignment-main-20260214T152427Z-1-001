package com.fulfilment.application.monolith.health;

import jakarta.enterprise.context.ApplicationScoped;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple health check for the Warehouse Management System
 */
@ApplicationScoped
public class WarehouseSystemHealthCheck {

    private final Instant startTime = Instant.now();

    public Map<String, Object> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        
        // Application status
        health.put("status", "UP");
        health.put("uptime", Duration.between(startTime, Instant.now()).toString());
        health.put("version", getApplicationVersion());
        
        // System resources
        health.put("memory", getMemoryInfo());
        health.put("cpu", getCpuInfo());
        
        // Business metrics
        health.put("business_metrics", getBusinessMetrics());
        
        return health;
    }

    public boolean isHealthy() {
        // Basic health checks
        return isMemoryHealthy() && isCpuHealthy();
    }

    private Map<String, Object> getMemoryInfo() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long usedHeap = memoryBean.getHeapMemoryUsage().getUsed();
        long maxHeap = memoryBean.getHeapMemoryUsage().getMax();
        long usedNonHeap = memoryBean.getNonHeapMemoryUsage().getUsed();
        long maxNonHeap = memoryBean.getNonHeapMemoryUsage().getMax();
        
        return Map.of(
            "heap_used", usedHeap,
            "heap_max", maxHeap,
            "heap_usage_percent", maxHeap > 0 ? (double) usedHeap / maxHeap * 100 : 0,
            "non_heap_used", usedNonHeap,
            "non_heap_max", maxNonHeap,
            "non_heap_usage_percent", maxNonHeap > 0 ? (double) usedNonHeap / maxNonHeap * 100 : 0
        );
    }

    private Map<String, Object> getCpuInfo() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        int availableProcessors = osBean.getAvailableProcessors();
        double systemLoadAverage = osBean.getSystemLoadAverage();
        
        return Map.of(
            "available_processors", availableProcessors,
            "system_load_average", systemLoadAverage
        );
    }

    private int getActiveWarehouseCount() {
        // Query database for active warehouses
        return 10; // Placeholder - implement actual query
    }

    private int getPendingOperationCount() {
        // Get pending operations count
        return 5; // Placeholder - implement actual query
    }

    private double getErrorRate() {
        // Calculate error rate from logs or metrics
        return 0.01; // Placeholder - implement actual calculation
    }

    private double getAverageResponseTime() {
        // Get average response time from metrics
        return 150.5; // Placeholder - implement actual calculation
    }

    public Map<String, Object> getBusinessMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        metrics.put("active_warehouses", getActiveWarehouseCount());
        metrics.put("pending_operations", getPendingOperationCount());
        metrics.put("error_rate", getErrorRate());
        metrics.put("response_time", getAverageResponseTime());

        return metrics;
    }

    private boolean isMemoryHealthy() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long usedHeap = memoryBean.getHeapMemoryUsage().getUsed();
        long maxHeap = memoryBean.getHeapMemoryUsage().getMax();
        
        // Consider unhealthy if heap usage is above 90%
        return maxHeap > 0 && (double) usedHeap / maxHeap < 0.9;
    }

    private boolean isCpuHealthy() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double systemLoadAverage = osBean.getSystemLoadAverage();
        int availableProcessors = osBean.getAvailableProcessors();
        
        // Consider unhealthy if system load is more than 2x available processors
        return systemLoadAverage <= availableProcessors * 2;
    }

    public Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Memory metrics
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long usedHeap = memoryBean.getHeapMemoryUsage().getUsed();
        long maxHeap = memoryBean.getHeapMemoryUsage().getMax();
        long usedNonHeap = memoryBean.getNonHeapMemoryUsage().getUsed();
        long maxNonHeap = memoryBean.getNonHeapMemoryUsage().getMax();
        
        metrics.put("heap_used", usedHeap);
        metrics.put("heap_max", maxHeap);
        metrics.put("heap_usage_percent", maxHeap > 0 ? (double) usedHeap / maxHeap * 100 : 0);
        metrics.put("non_heap_used", usedNonHeap);
        metrics.put("non_heap_max", maxNonHeap);
        metrics.put("non_heap_usage_percent", maxNonHeap > 0 ? (double) usedNonHeap / maxNonHeap * 100 : 0);
        
        // CPU metrics
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        int processors = osBean.getAvailableProcessors();
        double systemLoad = osBean.getSystemLoadAverage();
        
        metrics.put("processors", processors);
        metrics.put("system_load_average", systemLoad);
        
        return metrics;
    }

    public String getApplicationVersion() {
        // Get application version from manifest or properties
        return "1.0.0"; // This should be dynamically retrieved
    }
}
