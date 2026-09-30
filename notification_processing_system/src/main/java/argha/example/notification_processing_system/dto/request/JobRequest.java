package argha.example.notification_processing_system.dto.request;

import argha.example.notification_processing_system.entity.type.JobStatus;
import argha.example.notification_processing_system.entity.type.NotificationType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JobRequest {
    private String jobType;
    private Long notificationId;
    private JobStatus status;
    private int attempts;
    private int priority;

    public JobRequest(String jobType, Long notificationId, JobStatus status, int attempts, int priority) {
        this.jobType = jobType;
        this.notificationId = notificationId;
        this.status = status;
        this.attempts = attempts;
        this.priority = priority;
    }
}
