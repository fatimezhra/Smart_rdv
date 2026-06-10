package com.example.demo.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entities.Role;
import com.example.demo.entities.User;
import com.example.demo.repositories.UserRepository;
import com.example.demo.security.IJwtService;
import com.example.demo.services.UserService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private IJwtService jwtService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    // INSCRIPTION — always creates a CLIENT
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(Role.CLIENT);
        user.setEnabled(true); // ← AJOUTÉ
        return ResponseEntity.ok(userService.register(user));
    }

    // ADMIN CREATION — protected, only ADMIN can call
    @PostMapping("/admin/create")
    public ResponseEntity<?> createAdmin(@RequestBody User user) {
        user.setRole(Role.ADMIN);
        user.setEnabled(true); // ← AJOUTÉ
        return ResponseEntity.ok(userService.register(user));
    }

    // LOGIN
    @PostMapping("/login")
    public AuthResponse login(@RequestBody User user) {
        return userService.login(user.getEmail(), user.getPassword());
    }

    // CURRENT USER
    @GetMapping("/me")
    public User me() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}