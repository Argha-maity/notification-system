package argha.example.notification_processing_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobStatsResponse {
    private long total;
    private long pending;
    private long processing;
    private long completed;
    private long failed;
    private long deadLetterCount;
    private double successRate;
    private double avgProcessingTime;
    private int workersActive;
    private int queueSize;
    private double avgRetryCount;
    private String mostCommonError;
    private long lastUpdated;
    private long systemUptime;
    private double emailSuccessRate;
    private double smsSuccessRate;
    private double pushSuccessRate;
    private String databaseStatus;
    private String redisStatus;
    private String emailServiceStatus;
}
