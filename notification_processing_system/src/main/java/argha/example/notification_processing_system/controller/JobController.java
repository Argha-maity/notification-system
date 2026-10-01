package argha.example.notification_processing_system.controller;

import argha.example.notification_processing_system.dto.response.JobAttemptResponse;
import argha.example.notification_processing_system.dto.response.JobResponse;
import argha.example.notification_processing_system.dto.response.JobStatsResponse;
import argha.example.notification_processing_system.security.UserPrincipal;
import argha.example.notification_processing_system.service.JobProcessingService;
import argha.example.notification_processing_system.service.JobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobs")
@Slf4j
public class JobController {

    @Autowired
    private JobService jobService;

    @Autowired
    private JobProcessingService jobProcessingService;

    @GetMapping("/{jobId}")
    public ResponseEntity<?> getJob(@AuthenticationPrincipal UserPrincipal userDetails, @PathVariable("jobId") Long jobId){
        if(userDetails == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        if(jobId == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Job id is not provided");

        JobResponse job=jobService.getJobById(jobId);
        if(job == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Job not found");
        return ResponseEntity.status(HttpStatus.OK).body(job);
    }

    @GetMapping("/{jobId}/attempts")
    public ResponseEntity<?> getAttemptsOfJob(@AuthenticationPrincipal UserPrincipal userDetails,@PathVariable("jobId") Long jobId){
        if(userDetails == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        if(jobId == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Job id is not provided");

        List<JobAttemptResponse> attempts=jobService.getAttemptsOfJob(jobId);
        if(attempts == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No Attempts taken");
        return ResponseEntity.status(HttpStatus.OK).body(attempts);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getJobStats(){
        try {
            JobStatsResponse stats = jobService.getJobStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching job stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

//    @GetMapping()
//    public ResponseEntity<?> getAllJobs(){
//        try {
//
//        }catch (Exception e){
//            log.error("Error fetching jobs", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<?> retryJob(@PathVariable(name = "id") Long jobId) {
//        log.info("Retrying job ID: {}", jobId);
        try {
            boolean success=jobProcessingService.retryJob(jobId);
            if(success)
                return ResponseEntity.ok("Job retry initiated successfully");
            else
                return ResponseEntity.badRequest().body("Failed to retry job");

        } catch (Exception e) {
            log.error("Error retrying job {}", jobId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable(name = "id") Long jobId){
//        log.info("Deleting job ID: {}", jobId);
        try {
            boolean success = jobService.deleteJob(jobId);
            if (success) {
                return ResponseEntity.ok("Job deleted successfully");
            } else {
                return ResponseEntity.badRequest().body("Failed to delete job");
            }
        } catch (Exception e) {
            log.error("Error deleting job {}", jobId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/analytics/timeline")
    public ResponseEntity<?> getJobTimeline(@RequestParam(defaultValue = "24") int hours){
//        log.info("Fetching job timeline for last {} hours", hours);
        try {
            Object timeline = jobService.getJobTimeline(hours);
            return ResponseEntity.ok(timeline);
        } catch (Exception e) {
            log.error("Error fetching job timeline", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/analytics/processing-time")
    public ResponseEntity<?> getProcessingTimeAnalytics() {
        log.info("Fetching processing time analytics");
        try {
            Object analytics = jobService.getProcessingTimeAnalytics();
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            log.error("Error fetching processing time analytics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
