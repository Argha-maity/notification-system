package argha.example.notification_processing_system.controller;

import argha.example.notification_processing_system.dto.request.NotificationRequest;
import argha.example.notification_processing_system.dto.response.NotificationResponse;
import argha.example.notification_processing_system.entity.Notification;
import argha.example.notification_processing_system.entity.User;
import argha.example.notification_processing_system.security.CustomUserDetailsService;
import argha.example.notification_processing_system.security.UserPrincipal;
import argha.example.notification_processing_system.service.NotificationService;
import argha.example.notification_processing_system.service.UserService;
import org.aspectj.weaver.ast.Not;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @PostMapping()
    public ResponseEntity<?> createNotification(@AuthenticationPrincipal UserPrincipal userDetails, @RequestBody NotificationRequest request){
        if(userDetails == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");

        User user=userDetails.getUser();

        if(user==null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");

        NotificationResponse response=notificationService.createNotificationService(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<?> getNotification(@AuthenticationPrincipal UserPrincipal userDetails, @PathVariable Long notificationId){
        User currentUser = userDetails.getUser();
        Notification notification = notificationService.findNotificationById(notificationId);

        if(notification == null) {
            return ResponseEntity.notFound().build();
        }

        if(!notification.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Not authorized to access this notification");
        }

        return ResponseEntity.ok(new NotificationResponse(notification));
    }

    @GetMapping()
    public ResponseEntity<?> getAllNotifications(@AuthenticationPrincipal UserPrincipal userDetails){
        if(userDetails == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");

        User currentUser = userDetails.getUser();
        List<NotificationResponse> notifications=notificationService.getNotificationsByUser(currentUser);
        if(notifications==null || notifications.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No notifications found");
        return ResponseEntity.status(HttpStatus.OK).body(notifications);
    }
}
