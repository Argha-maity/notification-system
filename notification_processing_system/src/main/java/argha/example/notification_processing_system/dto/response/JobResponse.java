package argha.example.notification_processing_system.dto.response;

import argha.example.notification_processing_system.entity.type.JobStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class JobResponse {
    private Long jobId;
    private String jobType;
    private JobStatus status;
    private int attemptCount;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
