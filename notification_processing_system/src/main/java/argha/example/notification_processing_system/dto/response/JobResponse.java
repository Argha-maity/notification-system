package argha.example.notification_processing_system.dto.response;

import argha.example.notification_processing_system.entity.type.JobStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobResponse {
    private Long jobId;
    private String notificationId;
    private String recipient;
    private String subject;
    private int maxAttempts;
    private String jobType;
    private JobStatus status;
    private int attemptCount;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String lastError;
    private Double processingTimeSeconds;
    private String workerId;
    private String priority;
    private String requestId;
    private Long userId;
    private LocalDateTime nextRetryAt;
    private String channel;
    private boolean inDeadLetterQueue;
    private String metadata;
    private String templateName;
    private Integer httpStatusCode;
    private String responseBody;
}
