package argha.example.notification_processing_system.repository;

import argha.example.notification_processing_system.entity.Job;
import argha.example.notification_processing_system.entity.type.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {
    long countByStatus(JobStatus status);

    long countByJobType(String jobType);

    long countByJobTypeAndStatus(String jobType, JobStatus status);

    List<Job> findByStatus(JobStatus status);

    Page<Job> findByStatus(JobStatus status, Pageable pageable);

    List<Job> findByCreatedAtAfter(LocalDateTime createdAt);

    @Query("SELECT AVG(j.processingTime) FROM Job j WHERE j.status = argha.example.notification_processing_system.entity.type.JobStatus.COMPLETED")
    Double getAverageProcessingTime();

    @Query("SELECT j FROM Job j WHERE j.notification.notificationId = :notificationId")
    Optional<Job> findByNotificationId(@Param("notificationId") String notificationId);

    @Query("SELECT j FROM Job j WHERE j.status = argha.example.notification_processing_system.entity.type.JobStatus.FAILED ORDER BY j.updatedAt DESC")
    List<Job> findRecentlyFailedJobs(Pageable pageable);

    @Query("SELECT COALESCE(AVG(j.attemptCount), 0.0) FROM Job j")
    Double getAverageRetryCount();

    @Query("SELECT j.last_error FROM Job j WHERE j.status = argha.example.notification_processing_system.entity.type.JobStatus.FAILED AND j.last_error IS NOT NULL AND j.last_error != '' GROUP BY j.last_error ORDER BY COUNT(j) DESC")
    List<String> findMostCommonErrors(Pageable pageable);

    @Query("SELECT MIN(j.processingTime), MAX(j.processingTime), AVG(j.processingTime) FROM Job j WHERE j.status = argha.example.notification_processing_system.entity.type.JobStatus.COMPLETED AND j.processingTime IS NOT NULL")
    List<Object[]> getProcessingTimeStats();

    @Query("SELECT COUNT(j) FROM Job j WHERE j.createdAt BETWEEN :start AND :end")
    long countByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(j) FROM Job j WHERE j.status = :status AND j.createdAt BETWEEN :start AND :end")
    long countByStatusAndCreatedAtBetween(@Param("status") JobStatus status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT AVG(j.processingTime) FROM Job j WHERE j.status = argha.example.notification_processing_system.entity.type.JobStatus.COMPLETED AND j.createdAt BETWEEN :start AND :end")
    Double getAverageProcessingTimeBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
