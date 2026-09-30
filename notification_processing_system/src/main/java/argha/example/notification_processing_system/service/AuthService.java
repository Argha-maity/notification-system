package argha.example.notification_processing_system.service;

import argha.example.notification_processing_system.dto.request.LoginRequest;
import argha.example.notification_processing_system.dto.request.SignupRequest;
import argha.example.notification_processing_system.dto.response.LoginResponse;
import argha.example.notification_processing_system.dto.response.SignupResponse;
import argha.example.notification_processing_system.entity.User;
import argha.example.notification_processing_system.entity.type.Role;
import argha.example.notification_processing_system.repository.UserRepository;
import argha.example.notification_processing_system.security.JwtAuthFilter;
import argha.example.notification_processing_system.security.JwtUtil;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public SignupResponse signupUser(SignupRequest request){
        if(userRepository.existsByEmail(request.getEmail()))
            throw new RuntimeException("User with email already exists");

        User user= User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
//                .role(Role.valueOf(request.getRole()))
                .build();

        try {
            Role role= Role.valueOf(request.getRole().toUpperCase());
            user.setRole(role);
        } catch(IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + request.getRole());
        }

        userRepository.save(user);
        return SignupResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    public LoginResponse loginUser(LoginRequest request){
        User user=userRepository.findByEmail(request.getEmail());
        if(user==null)
            throw new RuntimeException("User not found");
        if(!passwordEncoder.matches(request.getPassword(),user.getPassword()))
            throw new RuntimeException("Wrong credentials");

        String token = jwtUtil.generateToken(user.getEmail());

        return LoginResponse.builder()
                .email(user.getEmail())
                .token(token)
                .build();
    }
}
