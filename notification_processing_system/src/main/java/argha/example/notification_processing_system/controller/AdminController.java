package argha.example.notification_processing_system.controller;

import argha.example.notification_processing_system.entity.User;
import argha.example.notification_processing_system.security.UserPrincipal;
import argha.example.notification_processing_system.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private JobService jobService;

//    @GetMapping("/jobs/pending")
//    public ResponseEntity<?> getAllPendingJobs(@AuthenticationPrincipal UserPrincipal userDetails){
//        if(userDetails == null)
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not logged in");
//
//        User user=userDetails.getUser();
//        if()
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You are not allowed to perform this action");
//    }
}
