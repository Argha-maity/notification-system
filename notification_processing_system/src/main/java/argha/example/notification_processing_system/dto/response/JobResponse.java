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
    private String jobType;
    private JobStatus status;
    private int attemptCount;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
