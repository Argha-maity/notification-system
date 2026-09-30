package argha.example.notification_processing_system.repository;

import argha.example.notification_processing_system.entity.Notification;
import argha.example.notification_processing_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
//    @Query("SELECT n FROM Notification n WHERE n.user=:user")
//    List<Notification>  findAllNotificationByUser(@Param("user") User user);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId")
    List<Notification> findAllNotificationByUser(@Param("userId") Long userId);
}
