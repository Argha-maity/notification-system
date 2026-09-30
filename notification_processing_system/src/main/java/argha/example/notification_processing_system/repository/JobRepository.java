package argha.example.notification_processing_system.repository;

import argha.example.notification_processing_system.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {
}
