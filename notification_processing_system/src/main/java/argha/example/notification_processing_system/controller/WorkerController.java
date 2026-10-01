package argha.example.notification_processing_system.controller;

import argha.example.notification_processing_system.dto.response.WorkerStatsResponse;
import argha.example.notification_processing_system.service.WorkerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/workers")
@RequiredArgsConstructor
@Slf4j
public class WorkerController {

    @Autowired
    private WorkerService workerService;

    @GetMapping
    public ResponseEntity<List<WorkerStatsResponse>> getWorkerStats(){
        log.info("Fetching worker statistics");
        try {
            List<WorkerStatsResponse> workerStats = workerService.getAllWorkerStats();
            return ResponseEntity.ok(workerStats);
        } catch (Exception e) {
            log.error("Error fetching worker stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{workerId}")
    public ResponseEntity<WorkerStatsResponse> getWorkerById(@PathVariable String workerId){
//        log.info("Fetching worker stats for worker: {}", workerId);
        try {
            WorkerStatsResponse workerStats = workerService.getWorkerStats(workerId);
            if (workerStats == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(workerStats);
        } catch (Exception e) {
            log.error("Error fetching worker stats for {}", workerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getWorkerPoolStats(){
//        log.info("Fetching worker pool statistics");
        try {
            Object poolStats = workerService.getWorkerPoolStats();
            return ResponseEntity.ok(poolStats);
        } catch (Exception e) {
            log.error("Error fetching worker pool stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> getWorkersHealth(){
//        log.info("Fetching worker health status");
        try {
            Object health = workerService.getWorkerHealth();
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("Error fetching worker health", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{workerId}/current-job")
    public ResponseEntity<?> getCurrentJob(@PathVariable String workerId) {
//        log.info("Fetching current job for worker: {}", workerId);
        try {
            Object currentJob = workerService.getCurrentJob(workerId);
            return ResponseEntity.ok(currentJob);
        } catch (Exception e) {
            log.error("Error fetching current job for worker {}", workerId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}