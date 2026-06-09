package com.example.demo;

import com.example.demo.entities.User;
import com.example.demo.entities.Role;
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
        // Return dummy token for testing purposes
        return "dummy-token-for-testing";
    }

    public static String generateTestToken(String email, String role) {
        // Return dummy token for testing purposes
        return "dummy-token-for-testing";
    }
}
