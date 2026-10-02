package argha.example.notification_processing_system.service;

import argha.example.notification_processing_system.entity.Job;
import argha.example.notification_processing_system.entity.type.JobStatus;
import argha.example.notification_processing_system.queue.RedisQueueService;
import argha.example.notification_processing_system.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetryService {

    private final JobRepository jobRepository;
    private final JobProcessingService jobProcessingService;
    private final RedisQueueService redisQueueService;

    @Transactional
    public boolean retryJob(Long jobId) {
        return jobProcessingService.retryJob(jobId);
    }

    @Transactional
    public int retryAllFailedJobs() {
        List<Job> failedJobs = new ArrayList<>(jobRepository.findByStatus(JobStatus.FAILED));
        List<Job> dlqJobs = jobRepository.findByStatus(JobStatus.DEAD_LETTER);
        failedJobs.addAll(dlqJobs);

        int count = 0;
        for (Job job : failedJobs) {
            job.setStatus(JobStatus.PENDING);
            job.setAvailableAt(LocalDateTime.now());
            job.setLast_error(null);
            jobRepository.save(job);
            redisQueueService.enqueue(job.getJobId());
            count++;
        }
        log.info("Retried {} failed/DLQ jobs", count);
        return count;
    }

    public long calculateExponentialBackoffSeconds(int attemptCount) {
        return (long) Math.pow(2, Math.max(1, attemptCount));
    }
}
