package argha.example.notification_processing_system.repository;

import argha.example.notification_processing_system.entity.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WorkerRepository extends JpaRepository<Worker, Long> {
    Optional<Worker> findByWorkerId(String workerId);

    List<Worker> findByStatus(String status);

    List<Worker> findByEnabled(boolean enabled);

    List<Worker> findByStatusAndEnabled(String status, boolean enabled);

    List<Worker> findByHealthStatus(String healthStatus);

    List<Worker> findByAcceptingJobs(boolean acceptingJobs);

    @Query("SELECT w FROM Worker w WHERE w.currentlyProcessing > 0")
    List<Worker> findWorkersProcessingJobs();

    @Query("SELECT w FROM Worker w WHERE w.currentlyProcessing = 0 AND w.status = 'ACTIVE'")
    List<Worker> findIdleWorkers();

    @Query("SELECT COUNT(w) FROM Worker w WHERE w.status = 'ACTIVE' AND w.enabled = true")
    int countActiveWorkers();

    @Query("SELECT AVG(w.successRate) FROM Worker w WHERE w.enabled = true")
    Double getAverageSuccessRate();

    @Query("SELECT SUM(w.jobsProcessed) FROM Worker w WHERE w.enabled = true")
    Long getTotalJobsProcessed();

    @Query("SELECT AVG(w.cpuUsagePercent) FROM Worker w WHERE w.enabled = true")
    Double getAverageCpuUsage();

    @Query("SELECT AVG(w.memoryUsageMb) FROM Worker w WHERE w.enabled = true")
    Double getAverageMemoryUsage();

    @Query("SELECT w FROM Worker w WHERE w.enabled = true ORDER BY w.successRate DESC LIMIT 1")
    Optional<Worker> findWorkerWithHighestSuccessRate();

    @Query("SELECT w FROM Worker w WHERE w.enabled = true ORDER BY w.successRate ASC LIMIT 1")
    Optional<Worker> findWorkerWithLowestSuccessRate();

    @Query("SELECT w FROM Worker w WHERE w.healthStatus IN ('DEGRADED', 'UNHEALTHY') AND w.enabled = true")
    List<Worker> findUnhealthyWorkers();

    @Query("SELECT w FROM Worker w WHERE w.lastHeartbeat < :threshold AND w.enabled = true")
    List<Worker> findWorkersWithoutRecentHeartbeat(@Param("threshold") LocalDateTime threshold);

    List<Worker> findByCreatedAtAfter(LocalDateTime createdAt);

    List<Worker> findByUpdatedAtAfter(LocalDateTime updatedAt);

    List<Worker> findByVersion(String version);

    boolean existsByWorkerId(String workerId);

    @Query("SELECT w FROM Worker w WHERE w.enabled = true ORDER BY w.successRate DESC")
    List<Worker> findAllSortedBySuccessRate();

    @Query("SELECT w FROM Worker w WHERE w.enabled = true ORDER BY w.jobsProcessed DESC")
    List<Worker> findAllSortedByJobsProcessed();

    @Query("SELECT w FROM Worker w WHERE w.enabled = true ORDER BY w.responseTimeMs ASC")
    List<Worker> findAllSortedByResponseTime();

    @Query("SELECT w FROM Worker w WHERE w.errorRate > :threshold AND w.enabled = true")
    List<Worker> findWorkersWithHighErrorRate(@Param("threshold") double threshold);

    void deleteByWorkerId(String workerId);

    long countByStatus(String status);

    @Query("SELECT w FROM Worker w WHERE w.status = 'ACTIVE' AND w.acceptingJobs = true")
    List<Worker> findAvailableWorkers();

    @Query("SELECT SUM(w.errorCount) FROM Worker w WHERE w.enabled = true")
    Long getTotalErrorCount();

    @Query("SELECT w FROM Worker w WHERE w.throughput < :threshold AND w.enabled = true")
    List<Worker> findWorkerWithLowThroughput(@Param("threshold") double threshold);

    @Query("SELECT w FROM Worker w WHERE w.enabled = true ORDER BY w.throughput DESC LIMIT 1")
    Optional<Worker> findWorkerWithHighestThroughput();
}
