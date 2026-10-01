package argha.example.notification_processing_system.repository;

import argha.example.notification_processing_system.entity.Job;
import argha.example.notification_processing_system.entity.type.JobStatus;
import org.hibernate.query.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {
    long countByStatus(JobStatus status);

    long countByJobType(String jobType);

    long countByJobTypeAndStatus(String jobType, JobStatus status);

    List<Job> findByStatus(JobStatus status);

    List<Job> findByCreatedAtAfter(LocalDateTime createdAt);

    @Query("SELECT AVG(j.processingTime) FROM Job j WHERE j.status = argha.example.notification_processing_system.entity.type.JobStatus.COMPLETED")
    Double getAverageProcessingTime();

    @Query("SELECT j FROM Job j WHERE j.notification.notificationId = :notificationId")
    Optional<Job> findByNotificationId(@Param("notificationId") String notificationId);

    @Query("SELECT j FROM Job j WHERE j.status = argha.example.notification_processing_system.entity.type.JobStatus.FAILED ORDER BY j.updatedAt DESC")
    List<Job> findRecentlyFailedJobs(Pageable pageable);
}
