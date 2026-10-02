package argha.example.notification_processing_system.controller;

import argha.example.notification_processing_system.dto.response.JobStatsResponse;
import argha.example.notification_processing_system.entity.User;
import argha.example.notification_processing_system.entity.type.Role;
import argha.example.notification_processing_system.queue.RedisQueueService;
import argha.example.notification_processing_system.security.UserPrincipal;
import argha.example.notification_processing_system.service.JobService;
import argha.example.notification_processing_system.service.RetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final JobService jobService;
    private final RetryService retryService;
    private final RedisQueueService redisQueueService;

    @GetMapping("/stats")
    public ResponseEntity<?> getAdminStats(@AuthenticationPrincipal UserPrincipal userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        }
        User user = userDetails.getUser();
        if (user == null || user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        JobStatsResponse stats = jobService.getJobStats();
        Map<String, Object> adminStats = new HashMap<>();
        adminStats.put("jobStats", stats);
        adminStats.put("queueSize", redisQueueService.getQueueSize());
        adminStats.put("deadLetterQueueSize", redisQueueService.getDeadLetterQueueSize());
        return ResponseEntity.ok(adminStats);
    }

    @PostMapping("/queue/clear")
    public ResponseEntity<?> clearQueue(@AuthenticationPrincipal UserPrincipal userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        }
        User user = userDetails.getUser();
        if (user == null || user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        redisQueueService.clearQueue();
        return ResponseEntity.ok(Map.of("message", "Job queue cleared successfully"));
    }

    @PostMapping("/queue/dead-letter/purge")
    public ResponseEntity<?> purgeDeadLetterQueue(@AuthenticationPrincipal UserPrincipal userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        }
        User user = userDetails.getUser();
        if (user == null || user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        redisQueueService.clearDeadLetterQueue();
        return ResponseEntity.ok(Map.of("message", "Dead letter queue purged successfully"));
    }

    @PostMapping("/jobs/retry-all")
    public ResponseEntity<?> retryAllFailedJobs(@AuthenticationPrincipal UserPrincipal userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        }
        User user = userDetails.getUser();
        if (user == null || user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        int count = retryService.retryAllFailedJobs();
        return ResponseEntity.ok(Map.of("message", "Retry initiated", "retriedCount", count));
    }
}
