package argha.example.notification_processing_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkerStatsResponse {
    private String workerId;
    private String status;
    private long jobsProcessed;
    private int currentlyProcessing;
    private Long currentJobId;
    private double cpuUsagePercent;
    private long memoryUsageMb;
    private long totalMemoryMb;
    private double avgProcessingTimeSeconds;
    private double successRate;
    private long failedJobs;
    private long uptimeSeconds;
    private LocalDateTime lastHeartbeat;
    private String version;
    private int threadCount;
    private double throughput;
    private String healthStatus;
    private long responseTimeMs;
    private double errorRate;
    private double avgRetryCount;
    private LocalDateTime startedAt;
    private long networkLatencyMs;
    private long dbResponseTimeMs;
    private long redisResponseTimeMs;
    private long errorCount;
    private String lastError;
    private String instanceName;
    private int availableThreads;
    private long jobsCompletedToday;
    private long jobsFailedToday;
}
