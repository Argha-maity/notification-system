package argha.example.notification_processing_system.dto.response;

import argha.example.notification_processing_system.entity.JobAttempt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobAttemptResponse {
    private int attemptNumber;
    private String status;
    private String errorMessage;

    public JobAttemptResponse(JobAttempt jobAttempt){
        this.attemptNumber = jobAttempt.getAttemptNumber();
        this.status = jobAttempt.getStatus();
        this.errorMessage = jobAttempt.getErrorMessage();
    }
}
