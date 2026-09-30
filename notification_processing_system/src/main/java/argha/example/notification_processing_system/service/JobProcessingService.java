package argha.example.notification_processing_system.service;

import argha.example.notification_processing_system.entity.Job;
import argha.example.notification_processing_system.entity.Notification;
import argha.example.notification_processing_system.entity.type.JobStatus;
import argha.example.notification_processing_system.entity.type.NotificationStatus;
import argha.example.notification_processing_system.repository.JobRepository;
import argha.example.notification_processing_system.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class JobProcessingService {
    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmailService emailService;

    public JobProcessingService(JobRepository jobRepository, NotificationRepository notificationRepository, EmailService emailService) {
        this.jobRepository = jobRepository;
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void processJob(Long jobId) {
        Job job = jobRepository.findById(jobId).orElse(null);
        if(job==null)
            throw new RuntimeException("Job Not Found");

        if(job.getStatus()== JobStatus.COMPLETED)
            return;

        if(job.getAttemptCount()>=job.getMaxAttempts()){
            job.setStatus(JobStatus.FAILED);
//            moveToDeadLetterQueue(job);
            return;
        }

        job.setStatus(JobStatus.PROCESSING);
        job.setAttemptCount(job.getAttemptCount()+1);
        jobRepository.save(job);

        try{
            Notification notification=job.getNotification();

            emailService.sendEmail(notification.getSubject(), notification.getMessage()); //also pass to

            job.setStatus(JobStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());

            jobRepository.save(job);
            notificationRepository.save(notification);
        }catch(Exception e){
            job.setStatus(JobStatus.PENDING);
            job.setLast_error(e.getMessage());

            long delaySeconds = (long) Math.pow(2, job.getAttemptCount());
            job.setAvailableAt(LocalDateTime.now().plusSeconds(delaySeconds));
            jobRepository.save(job);
        }
    }
}
