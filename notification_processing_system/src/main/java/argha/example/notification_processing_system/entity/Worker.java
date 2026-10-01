package argha.example.notification_processing_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "workers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Worker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String workerId;

    @Column(nullable = false)
    private String workerName;

    @Column(nullable = false)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(nullable = false)
    @Builder.Default
    private long jobsProcessed = 0;

    @Column(nullable = false)
    @Builder.Default
    private int currentlyProcessing = 0;

    @Column
    private Long currentJobId;

    @Column(nullable = false)
    @Builder.Default
    private double cpuUsagePercent = 0;

    @Column(nullable = false)
    @Builder.Default
    private long memoryUsageMb = 0;

    @Column(nullable = false)
    @Builder.Default
    private long totalMemoryMb = 1024;

    @Column(nullable = false)
    @Builder.Default
    private double avgProcessingTimeSeconds = 0;

    @Column(nullable = false)
    @Builder.Default
    private double successRate = 0;

    @Column(nullable = false)
    @Builder.Default
    private long failedJobs = 0;

    @Column(nullable = false)
    @Builder.Default
    private long uptimeSeconds = 0;

    @Column
    private LocalDateTime lastHeartbeat;

    @Column
    private String version;

    @Column(nullable = false)
    @Builder.Default
    private int threadCount = 10;

    @Column(nullable = false)
    @Builder.Default
    private double throughput = 0;

    @Column(nullable = false)
    @Builder.Default
    private String healthStatus = "HEALTHY";

    @Column(nullable = false)
    @Builder.Default
    private long responseTimeMs = 0;

    @Column(nullable = false)
    @Builder.Default
    private double errorRate = 0;

    @Column(nullable = false)
    @Builder.Default
    private double avgRetryCount = 0;

    @Column
    private LocalDateTime startedAt;

    @Column(nullable = false)
    @Builder.Default
    private long networkLatencyMs = 0;

    @Column(nullable = false)
    @Builder.Default
    private long dbResponseTimeMs = 0;

    @Column(nullable = false)
    @Builder.Default
    private long redisResponseTimeMs = 0;

    @Column(nullable = false)
    @Builder.Default
    private long errorCount = 0;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    @Column
    private String instanceName;

    @Column(nullable = false)
    @Builder.Default
    private int availableThreads = 10;

    @Column(nullable = false)
    @Builder.Default
    private long jobsCompletedToday = 0;

    @Column(nullable = false)
    @Builder.Default
    private long jobsFailedToday = 0;

    @Column
    private String ipAddress;

    @Column
    private String hostname;

    @Column(nullable = false)
    @Builder.Default
    private int maxConcurrentJobs = 1;

    @Column(nullable = false)
    @Builder.Default
    private boolean acceptingJobs = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    /**
     * Calculate and update health status based on metrics
     * Called before saving to database
     */
    @PreUpdate
    @PrePersist
    public void updateHealthStatus() {
        if (!enabled) {
            this.healthStatus = "DOWN";
        } else if (this.status.equals("ACTIVE")) {
            if (this.errorRate < 5 && this.responseTimeMs < 200) {
                this.healthStatus = "HEALTHY";
            } else if (this.errorRate < 15 && this.responseTimeMs < 500) {
                this.healthStatus = "DEGRADED";
            } else {
                this.healthStatus = "UNHEALTHY";
            }
        } else {
            this.healthStatus = "DOWN";
        }

        // Update last heartbeat if we're calculating
        if (this.lastHeartbeat == null) {
            this.lastHeartbeat = LocalDateTime.now();
        }
    }
}
