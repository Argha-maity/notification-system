package argha.example.notification_processing_system.controller;

import argha.example.notification_processing_system.dto.response.NotificationResponse;
import argha.example.notification_processing_system.security.UserPrincipal;
import argha.example.notification_processing_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/{userId}/notifications")
    public ResponseEntity<?> getUserNotificationHistory(@AuthenticationPrincipal UserPrincipal userPrincipal, @PathVariable Long userId){
        if(userPrincipal == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not logged in");

        List<NotificationResponse> response =userService.getAllNotificationsByUserId(userId);
        if(response == null || response.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No notifications found");

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
