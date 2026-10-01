package argha.example.notification_processing_system.service;

import argha.example.notification_processing_system.dto.request.JobRequest;
import argha.example.notification_processing_system.dto.response.JobAttemptResponse;
import argha.example.notification_processing_system.dto.response.JobResponse;
import argha.example.notification_processing_system.dto.response.JobStatsResponse;
import argha.example.notification_processing_system.entity.Job;
import argha.example.notification_processing_system.entity.JobAttempt;
import argha.example.notification_processing_system.entity.Notification;
import argha.example.notification_processing_system.entity.type.JobStatus;
import argha.example.notification_processing_system.queue.RedisQueueService;
import argha.example.notification_processing_system.repository.JobRepository;
import argha.example.notification_processing_system.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
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

    @Transactional(readOnly = true)
    public JobStatsResponse getJobStats(){
        try{
            long total = jobRepository.count();
            long pending = jobRepository.countByStatus(JobStatus.PENDING);
            long processing = jobRepository.countByStatus(JobStatus.PROCESSING);
            long completed = jobRepository.countByStatus(JobStatus.COMPLETED);
            long failed = jobRepository.countByStatus(JobStatus.FAILED);
            long deadLetterCount = jobRepository.countByStatus(JobStatus.DEAD_LETTER);

            // Calculate success rate
            double successRate = total > 0 ? (completed / (double) total) * 100 : 0;

            // Calculate average processing time (only for completed jobs)
            Double avgProcessingTime = jobRepository.getAverageProcessingTime();
            avgProcessingTime = avgProcessingTime != null ? avgProcessingTime : 0.0;

            // Get queue size (pending + processing)
            int queueSize = (int) (pending + processing);

            // Get channel success rates
            double emailSuccessRate = calculateChannelSuccessRate("EMAIL");
            double smsSuccessRate = calculateChannelSuccessRate("SMS");
            double pushSuccessRate = calculateChannelSuccessRate("PUSH");

            return JobStatsResponse.builder()
                    .total(total)
                    .pending(pending)
                    .processing(processing)
                    .completed(completed)
                    .failed(failed)
                    .deadLetterCount(deadLetterCount)
                    .successRate(successRate)
                    .avgProcessingTime(avgProcessingTime)
                    .workersActive(5) // TODO: Get from worker registry
                    .queueSize(queueSize)
                    .avgRetryCount(calculateAverageRetryCount())
                    .mostCommonError(getMostCommonError())
                    .lastUpdated(System.currentTimeMillis())
                    .systemUptime(0) // TODO: Calculate from app start time
                    .emailSuccessRate(emailSuccessRate)
                    .smsSuccessRate(smsSuccessRate)
                    .pushSuccessRate(pushSuccessRate)
                    .databaseStatus("HEALTHY")
                    .redisStatus("HEALTHY")
                    .emailServiceStatus("HEALTHY")
                    .build();
        }catch (Exception e){
            log.error("Error calculating job statistics", e);
            throw new RuntimeException("Failed to calculate job statistics", e);
        }
    }

    @Transactional
    public boolean deleteJob(Long jobId){
//        log.info("Deleting job ID: {}", jobId);
        try {
            if (jobRepository.existsById(jobId)) {
                jobRepository.deleteById(jobId);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error deleting job", e);
            throw new RuntimeException("Failed to delete job", e);
        }
    }

    @Transactional(readOnly = true)
    public Object getJobTimeline(int hours) {
//        log.info("Fetching job timeline for last {} hours", hours);
        try {
            LocalDateTime startTime = LocalDateTime.now().minus(hours, ChronoUnit.HOURS);
            List<Job> jobs = jobRepository.findByCreatedAtAfter(startTime);

            // Group by hour and status
            Map<String, Map<String, Long>> timeline = new HashMap<>();

            jobs.forEach(job -> {
                String hourKey = job.getCreatedAt().toString().substring(0, 13);
                timeline.computeIfAbsent(hourKey, k -> new HashMap<>())
                        .merge(String.valueOf(job.getStatus()), 1L, Long::sum);
            });

            return timeline;
        } catch (Exception e) {
            log.error("Error fetching job timeline", e);
            throw new RuntimeException("Failed to fetch job timeline", e);
        }
    }

    @Transactional(readOnly = true)
    public Object getProcessingTimeAnalytics(){
        try{
            List<Job> completedJobs = jobRepository.findByStatus(JobStatus.COMPLETED);

            Map<String, Object> analytics = new HashMap<>();
            analytics.put("min", completedJobs.stream()
                    .map(Job::getProcessingTime)
                    .filter(Objects::nonNull)
                    .mapToDouble(Double::doubleValue)
                    .min()
                    .orElse(0.0));

            analytics.put("max", completedJobs.stream()
                    .map(Job::getProcessingTime)
                    .filter(Objects::nonNull)
                    .mapToDouble(Double::doubleValue)
                    .max()
                    .orElse(0.0));

            analytics.put("avg", completedJobs.stream()
                    .map(Job::getProcessingTime)
                    .filter(Objects::nonNull)
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0));

            return analytics;
        }catch (Exception e){
            log.error("Error fetching processing time analytics", e);
            throw new RuntimeException("Failed to fetch analytics", e);
        }
    }

    private double calculateChannelSuccessRate(String channel){
        long total = jobRepository.countByJobType(channel);
        if (total == 0) return 0;
        long completed = jobRepository.countByJobTypeAndStatus(channel, JobStatus.COMPLETED);
        return (completed / (double) total) * 100;
    }

    private double calculateAverageRetryCount() {
        List<Job> allJobs = jobRepository.findAll();
        return allJobs.stream()
                .mapToInt(Job::getAttemptCount)
                .average()
                .orElse(0);
    }

    private String getMostCommonError() {
        List<Job> failedJobs = jobRepository.findByStatus(JobStatus.FAILED);
        return failedJobs.stream()
                .filter(j -> j.getLast_error() != null)
                .collect(Collectors.groupingByConcurrent(
                        Job::getLast_error,
                        Collectors.counting()))
                .entrySet().stream()
                .max((a, b) -> Long.compare(a.getValue(), b.getValue()))
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}
