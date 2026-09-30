package argha.example.notification_processing_system.controller;

import argha.example.notification_processing_system.dto.response.JobAttemptResponse;
import argha.example.notification_processing_system.dto.response.JobResponse;
import argha.example.notification_processing_system.security.UserPrincipal;
import argha.example.notification_processing_system.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

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
}
