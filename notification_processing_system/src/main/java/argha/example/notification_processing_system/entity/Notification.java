package argha.example.notification_processing_system.entity;

import argha.example.notification_processing_system.entity.type.NotificationChannel;
import argha.example.notification_processing_system.entity.type.NotificationStatus;
import argha.example.notification_processing_system.entity.type.NotificationType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id", unique = true, nullable = false)
    private Long notificationId;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;
    private String subject;
    private String message;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private java.util.List<Job> jobs;

    public Job getJob() {
        return (jobs != null && !jobs.isEmpty()) ? jobs.get(jobs.size() - 1) : null;
    }

    public void setJob(Job job) {
        if (this.jobs == null) {
            this.jobs = new java.util.ArrayList<>();
        }
        this.jobs.add(job);
    }
}
