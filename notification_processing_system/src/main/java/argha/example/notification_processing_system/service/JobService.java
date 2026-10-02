package argha.example.notification_processing_system.service;

import argha.example.notification_processing_system.dto.request.JobRequest;
import argha.example.notification_processing_system.dto.response.DeadLetterJobResponse;
import argha.example.notification_processing_system.dto.response.JobAttemptResponse;
import argha.example.notification_processing_system.dto.response.JobResponse;
import argha.example.notification_processing_system.dto.response.JobStatsResponse;
import argha.example.notification_processing_system.entity.DeadLetterJob;
import argha.example.notification_processing_system.entity.Job;
import argha.example.notification_processing_system.entity.JobAttempt;
import argha.example.notification_processing_system.entity.Notification;
import argha.example.notification_processing_system.entity.type.JobStatus;
import argha.example.notification_processing_system.queue.RedisQueueService;
import argha.example.notification_processing_system.repository.JobRepository;
import argha.example.notification_processing_system.repository.NotificationRepository;
import argha.example.notification_processing_system.repository.WorkerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobRepository jobRepository;
    private final NotificationRepository notificationRepository;
    private final RedisQueueService redisQueueService;
    private final WorkerRepository workerRepository;

    @Transactional
    public Job createNewJob(JobRequest request) {
        Long notificationId = request.getNotificationId();
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification == null)
            throw new IllegalArgumentException("Notification id not found");

        Job job = Job.builder()
                .jobType(request.getJobType())
                .status(request.getStatus() != null ? request.getStatus() : JobStatus.PENDING)
                .priority(request.getPriority() > 0 ? request.getPriority() : 1)
                .attemptCount(0)
                .maxAttempts(request.getAttempts() > 0 ? request.getAttempts() : 5)
                .idempotencyKey(UUID.randomUUID().toString())
                .last_error("")
                .notification(notification)
                .build();

        Job jobCreated = jobRepository.save(job);

        try {
            redisQueueService.enqueue(jobCreated.getJobId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to enqueue job", e);
        }

        return jobCreated;
    }

    public JobResponse getJobById(Long id) {
        Job job = jobRepository.findById(id).orElse(null);
        if (job == null)
            throw new IllegalArgumentException("Job id not found");

        return convertToJobResponse(job);
    }

    public List<JobAttemptResponse> getAttemptsOfJob(Long jobId) {
        Job job = jobRepository.findById(jobId).orElse(null);
        if (job == null)
            throw new IllegalArgumentException("Job id not found");

        List<JobAttempt> jobAttempts = job.getJobAttempts();
        List<JobAttemptResponse> attempts = new ArrayList<>();

        if (jobAttempts != null) {
            for (JobAttempt jobAttempt : jobAttempts)
                attempts.add(new JobAttemptResponse(jobAttempt));
        }

        return attempts;
    }

    @Transactional(readOnly = true)
    public JobStatsResponse getJobStats() {
        try {
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

            int activeWorkers = workerRepository != null ? workerRepository.countActiveWorkers() : 0;

            return JobStatsResponse.builder()
                    .total(total)
                    .pending(pending)
                    .processing(processing)
                    .completed(completed)
                    .failed(failed)
                    .deadLetterCount(deadLetterCount)
                    .successRate(successRate)
                    .avgProcessingTime(avgProcessingTime)
                    .workersActive(activeWorkers)
                    .queueSize(queueSize)
                    .avgRetryCount(calculateAverageRetryCount())
                    .mostCommonError(getMostCommonError())
                    .lastUpdated(System.currentTimeMillis())
                    .systemUptime(0)
                    .emailSuccessRate(emailSuccessRate)
                    .smsSuccessRate(smsSuccessRate)
                    .pushSuccessRate(pushSuccessRate)
                    .databaseStatus("HEALTHY")
                    .redisStatus("HEALTHY")
                    .emailServiceStatus("HEALTHY")
                    .build();
        } catch (Exception e) {
            log.error("Error calculating job statistics", e);
            throw new RuntimeException("Failed to calculate job statistics", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> getJobsByStatus(String statusStr, Pageable pageable) {
        try {
            Page<Job> jobsPage;
            if (statusStr == null || "ALL".equalsIgnoreCase(statusStr.trim())) {
                jobsPage = jobRepository.findAll(pageable);
            } else {
                try {
                    JobStatus status = JobStatus.valueOf(statusStr.trim().toUpperCase());
                    jobsPage = jobRepository.findByStatus(status, pageable);
                } catch (IllegalArgumentException e) {
                    log.warn("Unknown job status '{}', returning all jobs", statusStr);
                    jobsPage = jobRepository.findAll(pageable);
                }
            }
            return jobsPage.map(this::convertToJobResponse);
        } catch (Exception e) {
            log.error("Error fetching jobs by status", e);
            throw new RuntimeException("Failed to fetch jobs", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> getJobsByStatus(JobStatus status, Pageable pageable) {
        return getJobsByStatus(status != null ? status.name() : "ALL", pageable);
    }

    @Transactional(readOnly = true)
    public Page<DeadLetterJobResponse> getDeadLetterQueue(Pageable pageable) {
        log.info("Fetching dead letter queue, page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<Job> dlqJobs = jobRepository.findByStatus(JobStatus.DEAD_LETTER, pageable);
            return dlqJobs.map(this::convertToDeadLetterResponse);
        } catch (Exception e) {
            log.error("Error fetching dead letter queue", e);
            throw new RuntimeException("Failed to fetch dead letter queue", e);
        }
    }

    @Transactional(readOnly = true)
    public List<JobResponse> getRecentJobs(int hours) {
        log.info("Fetching recent jobs from last {} hours", hours);

        try {
            LocalDateTime startTime = LocalDateTime.now().minus(hours, ChronoUnit.HOURS);
            List<Job> jobs = jobRepository.findByCreatedAtAfter(startTime);
            return jobs.stream().map(this::convertToJobResponse).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching recent jobs", e);
            throw new RuntimeException("Failed to fetch recent jobs", e);
        }
    }

    @Transactional(readOnly = true)
    public JobStatsResponse getJobsStatsByDateRange(String startDate, String endDate) {
        log.info("Fetching job stats for date range: {} to {}", startDate, endDate);
        try {
            LocalDateTime start;
            LocalDateTime end;
            if (startDate != null && !startDate.isBlank()) {
                start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE).atStartOfDay();
            } else {
                start = LocalDateTime.now().minusDays(30);
            }

            if (endDate != null && !endDate.isBlank()) {
                end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE).atTime(23, 59, 59);
            } else {
                end = LocalDateTime.now();
            }

            long total = jobRepository.countByCreatedAtBetween(start, end);
            long pending = jobRepository.countByStatusAndCreatedAtBetween(JobStatus.PENDING, start, end);
            long processing = jobRepository.countByStatusAndCreatedAtBetween(JobStatus.PROCESSING, start, end);
            long completed = jobRepository.countByStatusAndCreatedAtBetween(JobStatus.COMPLETED, start, end);
            long failed = jobRepository.countByStatusAndCreatedAtBetween(JobStatus.FAILED, start, end);
            long deadLetterCount = jobRepository.countByStatusAndCreatedAtBetween(JobStatus.DEAD_LETTER, start, end);

            double successRate = total > 0 ? (completed / (double) total) * 100 : 0;
            Double avgProcessing = jobRepository.getAverageProcessingTimeBetween(start, end);

            return JobStatsResponse.builder()
                    .total(total)
                    .pending(pending)
                    .processing(processing)
                    .completed(completed)
                    .failed(failed)
                    .deadLetterCount(deadLetterCount)
                    .successRate(successRate)
                    .avgProcessingTime(avgProcessing != null ? avgProcessing : 0.0)
                    .workersActive(workerRepository != null ? workerRepository.countActiveWorkers() : 0)
                    .queueSize((int) (pending + processing))
                    .avgRetryCount(calculateAverageRetryCount())
                    .mostCommonError(getMostCommonError())
                    .lastUpdated(System.currentTimeMillis())
                    .databaseStatus("HEALTHY")
                    .redisStatus("HEALTHY")
                    .emailServiceStatus("HEALTHY")
                    .build();
        } catch (Exception e) {
            log.error("Error calculating date range job stats, falling back to overall stats", e);
            return getJobStats();
        }
    }

    @Transactional
    public boolean deleteJob(Long jobId) {
        log.info("Deleting job ID: {}", jobId);
        try {
            if (jobRepository.existsById(jobId)) {
                jobRepository.deleteById(jobId);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error deleting job {}", jobId, e);
            throw new RuntimeException("Failed to delete job", e);
        }
    }

    @Transactional(readOnly = true)
    public Object getJobTimeline(int hours) {
        try {
            LocalDateTime startTime = LocalDateTime.now().minus(hours, ChronoUnit.HOURS);
            List<Job> jobs = jobRepository.findByCreatedAtAfter(startTime);

            // Group by hour and status
            Map<String, Map<String, Long>> timeline = new HashMap<>();

            jobs.forEach(job -> {
                String hourKey = job.getCreatedAt() != null ? job.getCreatedAt().toString().substring(0, 13) : "unknown";
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
    public Object getProcessingTimeAnalytics() {
        try {
            List<Object[]> statsList = jobRepository.getProcessingTimeStats();
            Map<String, Object> analytics = new HashMap<>();
            double min = 0.0;
            double max = 0.0;
            double avg = 0.0;

            if (statsList != null && !statsList.isEmpty() && statsList.get(0) != null) {
                Object[] stats = statsList.get(0);
                if (stats[0] != null) min = ((Number) stats[0]).doubleValue();
                if (stats[1] != null) max = ((Number) stats[1]).doubleValue();
                if (stats[2] != null) avg = ((Number) stats[2]).doubleValue();
            }

            analytics.put("min", min);
            analytics.put("max", max);
            analytics.put("avg", avg);

            return analytics;
        } catch (Exception e) {
            log.error("Error fetching processing time analytics", e);
            throw new RuntimeException("Failed to fetch analytics", e);
        }
    }

    private double calculateChannelSuccessRate(String channel) {
        long total = jobRepository.countByJobType(channel);
        if (total == 0) return 0;
        long completed = jobRepository.countByJobTypeAndStatus(channel, JobStatus.COMPLETED);
        return (completed / (double) total) * 100;
    }

    private double calculateAverageRetryCount() {
        Double avg = jobRepository.getAverageRetryCount();
        return avg != null ? avg : 0.0;
    }

    private String getMostCommonError() {
        List<String> commonErrors = jobRepository.findMostCommonErrors(PageRequest.of(0, 1));
        return commonErrors.isEmpty() ? null : commonErrors.get(0);
    }

    // ============ Helper Methods ============

    private JobResponse convertToJobResponse(Job job) {
        String recipient = null;
        String subject = null;
        Long userId = null;
        String channel = null;

        if (job.getNotification() != null) {
            subject = job.getNotification().getSubject();
            if (job.getNotification().getChannel() != null) {
                channel = job.getNotification().getChannel().name();
            }
            if (job.getNotification().getUser() != null) {
                recipient = job.getNotification().getUser().getEmail();
                userId = job.getNotification().getUser().getId();
            }
        }

        return JobResponse.builder()
                .jobId(job.getJobId())
                .notificationId(job.getNotification() != null ? String.valueOf(job.getNotification().getNotificationId()) : null)
                .status(job.getStatus())
                .jobType(job.getJobType())
                .recipient(recipient)
                .subject(subject)
                .userId(userId)
                .channel(channel)
                .attemptCount(job.getAttemptCount())
                .maxAttempts(job.getMaxAttempts() > 0 ? job.getMaxAttempts() : 5)
                .createdAt(job.getCreatedAt())
                .completedAt(job.getCompletedAt())
                .lastError(job.getLast_error())
                .processingTimeSeconds(job.getProcessingTime())
                .priority(String.valueOf(job.getPriority()))
                .inDeadLetterQueue(job.getStatus() == JobStatus.DEAD_LETTER)
                .build();
    }

    private DeadLetterJobResponse convertToDeadLetterResponse(Job job) {
        String recipient = null;
        String subject = null;
        Long userId = null;

        if (job.getNotification() != null) {
            subject = job.getNotification().getSubject();
            if (job.getNotification().getUser() != null) {
                recipient = job.getNotification().getUser().getEmail();
                userId = job.getNotification().getUser().getId();
            }
        }

        DeadLetterJob dlq = job.getDeadLetterJob();
        String reason = (dlq != null && dlq.getReason() != null) ? dlq.getReason() : job.getLast_error();
        LocalDateTime failedAt = (dlq != null && dlq.getFailedAt() != null) ? dlq.getFailedAt() : job.getUpdatedAt();

        return DeadLetterJobResponse.builder()
                .jobId(job.getJobId())
                .notificationId(job.getNotification() != null ? String.valueOf(job.getNotification().getNotificationId()) : null)
                .type(job.getJobType())
                .recipient(recipient)
                .subject(subject)
                .attemptCount(job.getAttemptCount())
                .maxAttempts(job.getMaxAttempts() > 0 ? job.getMaxAttempts() : 5)
                .lastError(job.getLast_error())
                .createdAt(job.getCreatedAt())
                .movedToDlqAt(failedAt)
                .lastAttemptedAt(job.getUpdatedAt())
                .userId(userId)
                .priority(String.valueOf(job.getPriority()))
                .dlqReason(reason)
                .canRetry(true)
                .build();
    }
}
