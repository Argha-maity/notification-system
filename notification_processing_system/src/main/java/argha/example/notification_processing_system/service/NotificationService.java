package argha.example.notification_processing_system.service;

import argha.example.notification_processing_system.dto.request.JobRequest;
import argha.example.notification_processing_system.dto.request.NotificationRequest;
import argha.example.notification_processing_system.dto.response.NotificationResponse;
import argha.example.notification_processing_system.entity.Job;
import argha.example.notification_processing_system.entity.Notification;
import argha.example.notification_processing_system.entity.User;
import argha.example.notification_processing_system.entity.type.JobStatus;
import argha.example.notification_processing_system.entity.type.NotificationStatus;
import argha.example.notification_processing_system.repository.NotificationRepository;
import argha.example.notification_processing_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobService jobService;

    public NotificationResponse createNotificationService(NotificationRequest notificationRequest, User user){
        if(notificationRequest == null)
            throw new IllegalArgumentException("NotificationRequest is null");

        Notification notification=Notification.builder()
                .type(notificationRequest.getType())
                .channel(notificationRequest.getChannel())
                .subject(notificationRequest.getSubject())
                .message(notificationRequest.getMessage())
                .status(NotificationStatus.QUEUED)
                .user(user)
                .build();

        notificationRepository.save(notification);

        Job newJob=jobService.createNewJob(new JobRequest(
                                                            String.valueOf(notification.getType()),
                                                            notification.getNotificationId(),
                                                            JobStatus.PENDING,
                                                            1,
                                                            1
                                            ));

        notification.setJob(newJob);
        notificationRepository.save(notification);

        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .jobId(newJob.getJobId())
                .status(notification.getStatus())
                .type(notification.getType())
                .channel(notification.getChannel())
                .build();
    }

    public NotificationResponse getNotificationById(Long notificationId){
        try {
            if (notificationId == null)
                throw new IllegalArgumentException("notificationId is null");

            Optional<Notification> notification = notificationRepository.findById(notificationId);

            return NotificationResponse.builder()
                    .notificationId(notificationId)
                    .jobId(notification.get().getJob().getJobId())
                    .status(notification.get().getStatus())
                    .type(notification.get().getType())
                    .channel(notification.get().getChannel())
                    .build();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    public Notification findNotificationById(Long notificationId){
        if (notificationId == null)
            throw new IllegalArgumentException("notificationId is null");

        return notificationRepository.findById(notificationId).orElse(null);
    }

    public List<NotificationResponse> getAllNotifications(){
        List<Notification> notifications=notificationRepository.findAll();

        List<NotificationResponse> list=new ArrayList<>();
        for(Notification notification:notifications)
            list.add(new NotificationResponse(notification));

        return list;
    }

    public List<NotificationResponse> getNotificationsByUser(User user) {
        if(user == null)
            throw new IllegalArgumentException("user is null");

        List<Notification> notifications=notificationRepository.findAllNotificationByUser(user.getId());
        List<NotificationResponse> list=new ArrayList<>();

        for(Notification notification:notifications)
            list.add(new NotificationResponse(notification));

        return list;
    }
}
