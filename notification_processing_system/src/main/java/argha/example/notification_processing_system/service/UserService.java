package argha.example.notification_processing_system.service;

import argha.example.notification_processing_system.dto.response.NotificationResponse;
import argha.example.notification_processing_system.entity.Notification;
import argha.example.notification_processing_system.entity.User;
import argha.example.notification_processing_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User findUserByUserId(Long userId){
        if(userId == null)
            throw new IllegalArgumentException("userId is null");

        return userRepository.findById(userId).orElse(null);
    }

    public List<NotificationResponse> getAllNotificationsByUserId(Long userId){
        if(userId == null)
            throw new IllegalArgumentException("userId is null");

        User user = userRepository.findById(userId).orElse(null);
        if(user == null)
            throw new IllegalArgumentException("User not found");

        List<Notification> notifications = user.getNotifications();
        if(notifications == null)
            throw new IllegalArgumentException("No notifications found");

        List<NotificationResponse> response = new ArrayList<>();
        for(Notification notification : notifications)
            response.add(new NotificationResponse(notification));

        return response;
    }
}
