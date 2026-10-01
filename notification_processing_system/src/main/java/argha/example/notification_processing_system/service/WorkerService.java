package argha.example.notification_processing_system.service;

import argha.example.notification_processing_system.dto.response.WorkerStatsResponse;
import argha.example.notification_processing_system.entity.Worker;
import argha.example.notification_processing_system.repository.JobRepository;
import argha.example.notification_processing_system.repository.WorkerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WorkerService {
    @Autowired
    private WorkerRepository workerRepository;

    @Autowired
    private JobRepository jobRepository;

    @Transactional(readOnly = true)
    public List<WorkerStatsResponse> getAllWorkerStats(){
        try {
            List<Worker> workers = workerRepository.findByEnabled(true);

            return workers.stream()
                    .map(this::convertToWorkerStatsResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching all worker stats", e);
            throw new RuntimeException("Failed to fetch worker statistics", e);
        }
    }

    @Transactional(readOnly = true)
    public WorkerStatsResponse getWorkerStats(String workerId) {
//        log.info("Fetching stats for worker: {}", workerId);

        try {
            return workerRepository.findByWorkerId(workerId)
                    .map(this::convertToWorkerStatsResponse)
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error fetching worker stats for {}", workerId, e);
            throw new RuntimeException("Failed to fetch worker stats", e);
        }
    }

    @Transactional(readOnly = true)
    public Object getWorkerPoolStats() {
//        log.info("Fetching worker pool statistics");

        try {
            List<Worker> allWorkers = workerRepository.findByEnabled(true);

            Map<String, Object> poolStats = new HashMap<>();

            // Basic counts
            poolStats.put("totalWorkers", allWorkers.size());
            poolStats.put("activeWorkers",
                    (int) allWorkers.stream()
                            .filter(w -> "ACTIVE".equals(w.getStatus()))
                            .count());
            poolStats.put("idleWorkers",
                    (int) allWorkers.stream()
                            .filter(w -> "IDLE".equals(w.getStatus()))
                            .count());
            poolStats.put("pausedWorkers",
                    (int) allWorkers.stream()
                            .filter(w -> "PAUSED".equals(w.getStatus()))
                            .count());
            poolStats.put("unhealthyWorkers",
                    (int) allWorkers.stream()
                            .filter(w -> "DEGRADED".equals(w.getHealthStatus()) ||
                                    "UNHEALTHY".equals(w.getHealthStatus()))
                            .count());

            // Performance metrics
            poolStats.put("avgCpuUsage",
                    allWorkers.stream()
                            .mapToDouble(Worker::getCpuUsagePercent)
                            .average()
                            .orElse(0));

            poolStats.put("avgMemoryUsage",
                    allWorkers.stream()
                            .mapToDouble(Worker::getMemoryUsageMb)
                            .average()
                            .orElse(0));

            poolStats.put("totalJobsProcessed",
                    allWorkers.stream()
                            .mapToLong(Worker::getJobsProcessed)
                            .sum());

            poolStats.put("avgSuccessRate",
                    allWorkers.stream()
                            .mapToDouble(Worker::getSuccessRate)
                            .average()
                            .orElse(0));

            poolStats.put("avgResponseTime",
                    allWorkers.stream()
                            .mapToLong(Worker::getResponseTimeMs)
                            .average()
                            .orElse(0));

            poolStats.put("avgThroughput",
                    allWorkers.stream()
                            .mapToDouble(Worker::getThroughput)
                            .average()
                            .orElse(0));

            // Today's metrics
            poolStats.put("totalJobsCompletedToday",
                    allWorkers.stream()
                            .mapToLong(Worker::getJobsCompletedToday)
                            .sum());

            poolStats.put("totalJobsFailedToday",
                    allWorkers.stream()
                            .mapToLong(Worker::getJobsFailedToday)
                            .sum());

            // Database metrics
            Long totalErrors = workerRepository.getTotalErrorCount();
            poolStats.put("totalErrorCount", totalErrors != null ? totalErrors : 0);

            // Most efficient worker
            workerRepository.findWorkerWithHighestSuccessRate()
                    .ifPresent(w -> {
                        Map<String, Object> topWorker = new HashMap<>();
                        topWorker.put("workerId", w.getWorkerId());
                        topWorker.put("successRate", w.getSuccessRate());
                        topWorker.put("jobsProcessed", w.getJobsProcessed());
                        poolStats.put("topPerformer", topWorker);
                    });

            // Least efficient worker
            workerRepository.findWorkerWithLowestSuccessRate()
                    .ifPresent(w -> {
                        Map<String, Object> bottomWorker = new HashMap<>();
                        bottomWorker.put("workerId", w.getWorkerId());
                        bottomWorker.put("successRate", w.getSuccessRate());
                        bottomWorker.put("healthStatus", w.getHealthStatus());
                        poolStats.put("needsAttention", bottomWorker);
                    });

            return poolStats;
        } catch (Exception e) {
            log.error("Error fetching worker pool stats", e);
            throw new RuntimeException("Failed to fetch pool statistics", e);
        }
    }

    @Transactional(readOnly = true)
    public Object getWorkerHealth() {
//        log.info("Checking worker health status");
        try {
            List<Worker> allWorkers = workerRepository.findByEnabled(true);

            Map<String, Object> health = new HashMap<>();
            List<Map<String, Object>> workerHealth = new ArrayList<>();

            allWorkers.forEach(worker -> {
                Map<String, Object> wHealth = new HashMap<>();
                wHealth.put("workerId", worker.getWorkerId());
                wHealth.put("healthStatus", worker.getHealthStatus());
                wHealth.put("responseTime", worker.getResponseTimeMs());
                wHealth.put("lastHeartbeat", worker.getLastHeartbeat());
                wHealth.put("errorRate", worker.getErrorRate());
                wHealth.put("uptime", worker.getUptimeSeconds());
                wHealth.put("status", worker.getStatus());
                wHealth.put("acceptingJobs", worker.isAcceptingJobs());
                workerHealth.add(wHealth);
            });

            health.put("workers", workerHealth);
            health.put("overallHealth", calculateOverallHealth(allWorkers));
            health.put("lastChecked", LocalDateTime.now());
            health.put("unhealthyWorkers",
                    workerHealth.stream()
                            .filter(w -> !("HEALTHY".equals(w.get("healthStatus"))))
                            .count());

            return health;
        } catch (Exception e) {
            log.error("Error checking worker health", e);
            throw new RuntimeException("Failed to check health", e);
        }
    }

    @Transactional(readOnly = true)
    public Object getCurrentJob(String workerId) {
//        log.info("Fetching current job for worker: {}", workerId);

        try {
            return workerRepository.findByWorkerId(workerId)
                    .map(worker -> {
                        if (worker.getCurrentJobId() != null) {
                            Map<String, Object> currentJob = new HashMap<>();
                            currentJob.put("jobId", worker.getCurrentJobId());
                            currentJob.put("workerId", workerId);
                            currentJob.put("status", "PROCESSING");
                            currentJob.put("currentlyProcessing", worker.getCurrentlyProcessing());
                            return (Object) currentJob;
                        }
                        return null;
                    })
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error fetching current job for worker {}", workerId, e);
            throw new RuntimeException("Failed to fetch current job", e);
        }
    }

    @Transactional
    public boolean registerWorker(String workerId, String workerName) {
//        log.info("Registering worker: {} - {}", workerId, workerName);

        try {
            // Check if worker already exists
            if (workerRepository.existsByWorkerId(workerId)) {
                log.warn("Worker {} already exists", workerId);
                return false;
            }
            Worker worker = Worker.builder()
                    .workerId(workerId)
                    .workerName(workerName)
                    .status("ACTIVE")
                    .healthStatus("HEALTHY")
                    .enabled(true)
                    .acceptingJobs(true)
                    .startedAt(LocalDateTime.now())
                    .lastHeartbeat(LocalDateTime.now())
                    .jobsProcessed(0)
                    .successRate(0)
                    .failedJobs(0)
                    .build();

            workerRepository.save(worker);
            log.info("Worker {} registered successfully", workerId);
            return true;
        } catch (Exception e) {
            log.error("Error registering worker", e);
            throw new RuntimeException("Failed to register worker", e);
        }
    }

    @Transactional
    public boolean unregisterWorker(String workerId) {
//        log.info("Unregistering worker: {}", workerId);

        try {
            Optional<Worker> worker = workerRepository.findByWorkerId(workerId);
            if (worker.isPresent()) {
                Worker w = worker.get();
                w.setEnabled(false);
                w.setStatus("STOPPED");
                w.setHealthStatus("DOWN");
                w.setAcceptingJobs(false);
                workerRepository.save(w);
                log.info("Worker {} unregistered successfully", workerId);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error unregistering worker", e);
            throw new RuntimeException("Failed to unregister worker", e);
        }
    }

    @Transactional
    public void updateWorkerStats(String workerId, WorkerStatsResponse stats) {
//        log.info("Updating stats for worker: {}", workerId);

        try {
            Optional<Worker> optionalWorker = workerRepository.findByWorkerId(workerId);

            if (optionalWorker.isPresent()) {
                Worker worker = optionalWorker.get();

                // Update metrics
                worker.setCpuUsagePercent(stats.getCpuUsagePercent());
                worker.setMemoryUsageMb(stats.getMemoryUsageMb());
                worker.setAvgProcessingTimeSeconds(stats.getAvgProcessingTimeSeconds());
                worker.setSuccessRate(stats.getSuccessRate());
                worker.setErrorRate(stats.getErrorRate());
                worker.setResponseTimeMs(stats.getResponseTimeMs());
                worker.setThroughput(stats.getThroughput());
                worker.setJobsProcessed(stats.getJobsProcessed());
                worker.setFailedJobs(stats.getFailedJobs());
                worker.setCurrentlyProcessing(stats.getCurrentlyProcessing());
                worker.setCurrentJobId(stats.getCurrentJobId());
                worker.setLastHeartbeat(LocalDateTime.now());

                // Let @PreUpdate handle health status calculation
                workerRepository.save(worker);
                log.info("Worker {} stats updated successfully", workerId);
            } else {
                log.warn("Worker {} not found for update", workerId);
            }
        } catch (Exception e) {
            log.error("Error updating worker stats", e);
            throw new RuntimeException("Failed to update worker stats", e);
        }
    }

    @Transactional(readOnly = true)
    public List<WorkerStatsResponse> getWorkersByJobType(String jobType) {
//        log.info("Fetching workers processing job type: {}", jobType);
        try {
            // Get all active workers (can add job type filtering if Job entity tracks it)
            List<Worker> workers = workerRepository.findByStatusAndEnabled("ACTIVE", true);

            return workers.stream()
                    .map(this::convertToWorkerStatsResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching workers by job type", e);
            throw new RuntimeException("Failed to fetch workers", e);
        }
    }

    @Transactional(readOnly = true)
    public boolean isWorkerHealthy(String workerId) {
//        log.info("Checking health of worker: {}", workerId);

        try {
            return workerRepository.findByWorkerId(workerId)
                    .map(w -> ("HEALTHY".equals(w.getHealthStatus()) || "DEGRADED".equals(w.getHealthStatus()))
                            && "ACTIVE".equals(w.getStatus())
                            && w.isEnabled())
                    .orElse(false);
        } catch (Exception e) {
            log.error("Error checking worker health", e);
            return false;
        }
    }

//    @Transactional(readOnly = true)
//    public List<?> getWorkerProcessingHistory(String workerId, int limit) {
////        log.info("Fetching processing history for worker: {}, limit: {}", workerId, limit);
//
//        try {
//            // Get jobs processed by this worker
//            List<?> history = jobRepository.findAll()
//                    .stream()
//                    .filter(job -> workerId.equals(job.getWorkerId()))
//                    .limit(limit)
//                    .collect(Collectors.toList());
//
//            return history;
//        } catch (Exception e) {
//            log.error("Error fetching worker history", e);
//            throw new RuntimeException("Failed to fetch history", e);
//        }
//    }


    // ============ Helper Methods ============

    private WorkerStatsResponse convertToWorkerStatsResponse(Worker worker) {
        return WorkerStatsResponse.builder()
                .workerId(worker.getWorkerId())
                .status(worker.getStatus())
                .jobsProcessed(worker.getJobsProcessed())
                .currentlyProcessing(worker.getCurrentlyProcessing())
                .currentJobId(worker.getCurrentJobId())
                .cpuUsagePercent(worker.getCpuUsagePercent())
                .memoryUsageMb(worker.getMemoryUsageMb())
                .totalMemoryMb(worker.getTotalMemoryMb())
                .avgProcessingTimeSeconds(worker.getAvgProcessingTimeSeconds())
                .successRate(worker.getSuccessRate())
                .failedJobs(worker.getFailedJobs())
                .uptimeSeconds(worker.getUptimeSeconds())
                .lastHeartbeat(worker.getLastHeartbeat())
                .version(worker.getVersion())
                .threadCount(worker.getThreadCount())
                .throughput(worker.getThroughput())
                .healthStatus(worker.getHealthStatus())
                .responseTimeMs(worker.getResponseTimeMs())
                .errorRate(worker.getErrorRate())
                .avgRetryCount(worker.getAvgRetryCount())
                .startedAt(worker.getStartedAt())
                .networkLatencyMs(worker.getNetworkLatencyMs())
                .dbResponseTimeMs(worker.getDbResponseTimeMs())
                .redisResponseTimeMs(worker.getRedisResponseTimeMs())
                .errorCount(worker.getErrorCount())
                .lastError(worker.getLastError())
                .instanceName(worker.getInstanceName())
                .availableThreads(worker.getAvailableThreads())
                .jobsCompletedToday(worker.getJobsCompletedToday())
                .jobsFailedToday(worker.getJobsFailedToday())
                .build();
    }

    private String calculateOverallHealth(List<Worker> workers) {
        if (workers.isEmpty()) return "DOWN";

        long healthyCount = workers.stream()
                .filter(w -> "HEALTHY".equals(w.getHealthStatus()) && w.isEnabled())
                .count();

        double healthyPercent = (healthyCount / (double) workers.size()) * 100;

        if (healthyPercent >= 80) return "HEALTHY";
        if (healthyPercent >= 50) return "DEGRADED";
        return "UNHEALTHY";
    }

    @Transactional
    public void markStaleWorkers(int minutesThreshold) {
//        log.info("Marking stale workers (no heartbeat for {} minutes)", minutesThreshold);

        try {
            LocalDateTime threshold = LocalDateTime.now().minus(minutesThreshold, ChronoUnit.MINUTES);
            List<Worker> staleWorkers = workerRepository.findWorkersWithoutRecentHeartbeat(threshold);

            staleWorkers.forEach(worker -> {
                worker.setStatus("STOPPED");
                worker.setHealthStatus("DOWN");
                worker.setAcceptingJobs(false);
                worker.setLastError("No heartbeat received");
                log.warn("Marked worker {} as stale", worker.getWorkerId());
            });

            if (!staleWorkers.isEmpty()) {
                workerRepository.saveAll(staleWorkers);
            }
        } catch (Exception e) {
            log.error("Error marking stale workers", e);
        }
    }
}
