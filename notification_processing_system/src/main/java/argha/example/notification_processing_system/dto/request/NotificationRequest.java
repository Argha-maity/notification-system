package argha.example.notification_processing_system.dto.request;

import argha.example.notification_processing_system.entity.User;
import argha.example.notification_processing_system.entity.type.NotificationChannel;
import argha.example.notification_processing_system.entity.type.NotificationType;
import lombok.Data;

@Data
public class NotificationRequest {
    private NotificationType type;
    private NotificationChannel channel;
    private String subject;
    private String message;
}
