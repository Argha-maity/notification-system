package argha.example.notification_processing_system.service;

import argha.example.notification_processing_system.dto.request.JobRequest;
import argha.example.notification_processing_system.dto.response.JobAttemptResponse;
import argha.example.notification_processing_system.dto.response.JobResponse;
import argha.example.notification_processing_system.entity.Job;
import argha.example.notification_processing_system.entity.JobAttempt;
import argha.example.notification_processing_system.entity.Notification;
import argha.example.notification_processing_system.entity.type.JobStatus;
import argha.example.notification_processing_system.queue.RedisQueueService;
import argha.example.notification_processing_system.repository.JobRepository;
import argha.example.notification_processing_system.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private RedisQueueService redisQueueService;

    @Transactional
    public Job createNewJob(JobRequest request) {
        Long notificationId=request.getNotificationId();
        Notification notification=notificationRepository.findById(notificationId).orElse(null);
        if(notification==null)
            throw new IllegalArgumentException("Notification id not found");

        Job job=Job.builder()
                .jobType(request.getJobType())
                .status(request.getStatus())
                .priority(1)
                .attemptCount(1)
                .maxAttempts(3)
                .idempotencyKey(UUID.randomUUID().toString())
                .last_error("")
                .notification(notification)
                .build();

        Job jobCreated=jobRepository.save(job);

        try {//prevent from being orphaned by using @Transactional
            redisQueueService.enqueue(jobCreated.getJobId());
        }catch(Exception e){
            throw new RuntimeException("Failed to enqueue job", e);
        }

        return jobCreated;
    }

    public JobResponse getJobById(Long id){
        Job job=jobRepository.findById(id).orElse(null);
        if(job==null)
            throw new IllegalArgumentException("Job id not found");

        return JobResponse.builder()
                .jobId(id)
                .jobType(job.getJobType())
                .status(job.getStatus())
                .attemptCount(job.getAttemptCount())
                .createdAt(job.getCreatedAt())
                .completedAt(job.getCompletedAt())
                .build();
    }

    public List<JobAttemptResponse> getAttemptsOfJob(Long jobId){
        Job job=jobRepository.findById(jobId).orElse(null);
        if(job==null)
            throw new IllegalArgumentException("Job id not found");

        List<JobAttempt> jobAttempts=job.getJobAttempts();
        List<JobAttemptResponse> attempts=new ArrayList<>();

        for(JobAttempt jobAttempt:jobAttempts)
            attempts.add(new JobAttemptResponse(jobAttempt));

        return attempts;
    }
}
