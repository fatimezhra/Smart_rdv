package com.example.demo.security;

public interface IJwtService {
    String generateToken(String email, String role);
    String extractUsername(String token);
    String extractRole(String token);
    boolean isTokenValid(String token, String email);
}
