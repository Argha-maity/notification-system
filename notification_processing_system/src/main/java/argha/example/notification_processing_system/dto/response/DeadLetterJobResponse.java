package argha.example.notification_processing_system.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeadLetterJobResponse {
    private Long jobId;
    private String notificationId;
    private String type;
    private String recipient;
    private String subject;
    private int attemptCount;
    private int maxAttempts;
    private String lastError;
    private String errorStackTrace;
    private LocalDateTime createdAt;
    private LocalDateTime movedToDlqAt;
    private LocalDateTime lastAttemptedAt;
    private Long userId;
    private String priority;
    private String requestId;
    private String dlqReason;
    private String templateName;
    private String metadata;
    private boolean canRetry;
    private Long timeInDlqHours;
    private String attemptErrorLog;
}
