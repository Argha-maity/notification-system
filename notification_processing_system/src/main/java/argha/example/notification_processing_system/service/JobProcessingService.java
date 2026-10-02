package argha.example.notification_processing_system.service;

import argha.example.notification_processing_system.entity.DeadLetterJob;
import argha.example.notification_processing_system.entity.Job;
import argha.example.notification_processing_system.entity.Notification;
import argha.example.notification_processing_system.entity.type.JobStatus;
import argha.example.notification_processing_system.entity.type.NotificationStatus;
import argha.example.notification_processing_system.queue.RedisQueueService;
import argha.example.notification_processing_system.repository.JobRepository;
import argha.example.notification_processing_system.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobProcessingService {

    private final JobRepository jobRepository;
    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final RedisQueueService redisQueueService;

    @Transactional
    public void processJob(Long jobId) {
        Job job = jobRepository.findById(jobId).orElse(null);
        if (job == null) {
            log.error("Job not found: {}", jobId);
            return;
        }

        if (job.getStatus() == JobStatus.COMPLETED) {
            log.info("Job {} is already completed, skipping", jobId);
            return;
        }

        if (job.getAttemptCount() >= job.getMaxAttempts()) {
            moveToDeadLetterQueue(job);
            return;
        }

        long startTime = System.currentTimeMillis();
        job.setStatus(JobStatus.PROCESSING);
        job.setAttemptCount(job.getAttemptCount() + 1);
        jobRepository.save(job);

        try {
            Notification notification = job.getNotification();
            String recipient = null;
            if (notification != null && notification.getUser() != null) {
                recipient = notification.getUser().getEmail();
            }
            if (recipient == null || recipient.isBlank()) {
                recipient = "goku87880@gmail.com";
            }

            String subject = (notification != null && notification.getSubject() != null) ? notification.getSubject() : "Notification Job #" + jobId;
            String message = (notification != null && notification.getMessage() != null) ? notification.getMessage() : "Notification message content";

            emailService.sendEmail(recipient, subject, message);

            double processingDuration = (System.currentTimeMillis() - startTime) / 1000.0;
            job.setProcessingTime(processingDuration);
            job.setStatus(JobStatus.COMPLETED);
            job.setCompletedAt(LocalDateTime.now());
            job.setLast_error(null);

            if (notification != null) {
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
                notificationRepository.save(notification);
            }

            jobRepository.save(job);
            log.info("Job {} processed successfully in {}s", jobId, processingDuration);
        } catch (Exception e) {
            log.error("Failed to process job {}: {}", jobId, e.getMessage());
            double processingDuration = (System.currentTimeMillis() - startTime) / 1000.0;
            job.setProcessingTime(processingDuration);
            job.setLast_error(e.getMessage());

            if (job.getAttemptCount() >= job.getMaxAttempts()) {
                moveToDeadLetterQueue(job);
            } else {
                job.setStatus(JobStatus.PENDING);
                long delaySeconds = (long) Math.pow(2, job.getAttemptCount());
                job.setAvailableAt(LocalDateTime.now().plusSeconds(delaySeconds));
                jobRepository.save(job);
            }
        }
    }

    private void moveToDeadLetterQueue(Job job) {
        job.setStatus(JobStatus.DEAD_LETTER);
        DeadLetterJob dlq = job.getDeadLetterJob();
        if (dlq == null) {
            dlq = DeadLetterJob.builder()
                    .job(job)
                    .reason(job.getLast_error() != null ? job.getLast_error() : "Max attempts reached")
                    .failedAt(LocalDateTime.now())
                    .build();
            job.setDeadLetterJob(dlq);
        } else {
            dlq.setReason(job.getLast_error() != null ? job.getLast_error() : "Max attempts reached");
            dlq.setFailedAt(LocalDateTime.now());
        }
        jobRepository.save(job);
        redisQueueService.enqueueDeadLetter(job.getJobId());
        log.warn("Job {} reached max attempts ({}) and was moved to dead letter queue", job.getJobId(), job.getMaxAttempts());
    }

    @Transactional
    public boolean retryJob(Long jobId) {
        Optional<Job> optionalJob = jobRepository.findById(jobId);
        if (optionalJob.isEmpty()) {
            log.warn("Retry failed: job {} not found", jobId);
            return false;
        }

        Job job = optionalJob.get();
        job.setStatus(JobStatus.PENDING);
        job.setAvailableAt(LocalDateTime.now());
        job.setLast_error(null);
        jobRepository.save(job);

        redisQueueService.enqueue(job.getJobId());
        log.info("Job {} re-enqueued for retry", jobId);
        return true;
    }
}
