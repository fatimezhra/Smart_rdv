package com.example.demo.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AuthResponse;
import com.example.demo.entities.User;
import com.example.demo.security.JwtService;
import com.example.demo.services.UserService;

@RestController
@RequestMapping("/auth")
public class AuthController {
	@Autowired
	private JwtService jwtService;
    @Autowired
    private UserService userService;

    // INSCRIPTION
    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userService.register(user);
    }

    // LOGIN
    @PostMapping("/login")
    public AuthResponse login(@RequestBody User user) {
        return userService.login(user.getEmail(), user.getPassword());
    }
}
