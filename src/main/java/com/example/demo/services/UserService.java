package com.example.demo.services;

import com.example.demo.dto.AuthResponse;
import com.example.demo.entities.User;
import com.example.demo.exceptions.BadRequestException;
import com.example.demo.exceptions.ResourceNotFoundException;
import com.example.demo.repositories.UserRepository;
import com.example.demo.security.IJwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private IJwtService jwtService;

 public AuthResponse login(String email, String password) {

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (!passwordEncoder.matches(password, user.getPassword())) {
        throw new BadRequestException("Password incorrect");
    }

    String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

    return new AuthResponse(token, user.getRole().name(), user.getEmail(), user.getName());
}
    public User register(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

  

    
}