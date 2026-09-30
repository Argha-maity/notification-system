package argha.example.notification_processing_system.entity;

import argha.example.notification_processing_system.entity.type.JobStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "jobs")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id", unique = true, nullable = false)
    private Long jobId;

    @Column(name = "job_type")
    private String jobType;
    private JobStatus status;
    private int priority;

    @Column(name = "attempt_count")
    private int attemptCount;

    @Column(name = "max_attempts")
    private int maxAttempts;

    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @Column(name = "available_at")
    private LocalDateTime availableAt;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    private String last_error;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updated_at;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "notification_id")
    private Notification notification;

    @OneToMany(mappedBy = "job")
    @JsonIgnore
    private List<JobAttempt> jobAttempts;

    @OneToOne(mappedBy = "job", cascade = CascadeType.ALL)
    private DeadLetterJob deadLetterJob;
}
