package argha.example.notification_processing_system.controller;

import argha.example.notification_processing_system.dto.request.LoginRequest;
import argha.example.notification_processing_system.dto.request.SignupRequest;
import argha.example.notification_processing_system.dto.response.LoginResponse;
import argha.example.notification_processing_system.dto.response.SignupResponse;
import argha.example.notification_processing_system.entity.User;
import argha.example.notification_processing_system.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?>  signup(@RequestBody SignupRequest user){
        if(user == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("user cannot be null");

        SignupResponse createdUser=authService.signupUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully\n"+createdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request){
        if(request==null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("request cannot be null");

        LoginResponse response=authService.loginUser(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
