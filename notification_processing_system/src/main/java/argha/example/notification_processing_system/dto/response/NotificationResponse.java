package argha.example.notification_processing_system.dto.response;

import argha.example.notification_processing_system.entity.Job;
import argha.example.notification_processing_system.entity.Notification;
import argha.example.notification_processing_system.entity.type.NotificationChannel;
import argha.example.notification_processing_system.entity.type.NotificationStatus;
import argha.example.notification_processing_system.entity.type.NotificationType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long notificationId;
    private Long jobId;
    private NotificationType type;
    private NotificationChannel channel;
    private NotificationStatus status;

    public NotificationResponse(Notification notification){
        this.notificationId = notification.getNotificationId();
        this.jobId=notification.getJob().getJobId();
        this.type=notification.getType();
        this.channel=notification.getChannel();
        this.status=notification.getStatus();
    }
}
