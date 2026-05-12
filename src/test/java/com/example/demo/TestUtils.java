package com.example.demo;

import com.example.demo.entities.User;
import com.example.demo.entities.Role;
import com.example.demo.security.JwtService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

public class TestUtils {

    public static void setAuthentication(User user) {
        List<SimpleGrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
        
        UsernamePasswordAuthenticationToken authentication = 
            new UsernamePasswordAuthenticationToken(
                user.getEmail(), 
                null, 
                authorities
            );
        
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    public static String generateTestToken(User user) {
        JwtService jwtService = new JwtService();
        // Use reflection or create static wrapper - for now, return dummy token
        return "dummy-token-for-testing";
    }

    public static String generateTestToken(String email, String role) {
        JwtService jwtService = new JwtService();
        // Use reflection or create static wrapper - for now, return dummy token
        return "dummy-token-for-testing";
    }
}
