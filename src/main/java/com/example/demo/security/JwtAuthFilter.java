package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/auth/login")
            || path.startsWith("/auth/register")
            || path.startsWith("/api/slots/available")
            || path.startsWith("/api/slots/calendar")
            || path.startsWith("/timeslots")
            || path.startsWith("/v3/api-docs")
            || path.startsWith("/swagger-ui");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        System.out.println("[JWT] " + path + " | Auth header: " + (authHeader != null ? "present" : "missing"));

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("[JWT] " + path + " | No Bearer token, skipping auth");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        System.out.println("[JWT] " + path + " | Token: " + token.substring(0, Math.min(30, token.length())) + "...");

        try {
            String email = jwtService.extractUsername(token);
            System.out.println("[JWT] " + path + " | Extracted email: " + email);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                System.out.println("[JWT] " + path + " | Loaded user: " + userDetails.getUsername() +
                                   " | enabled=" + userDetails.isEnabled() +
                                   " | authorities=" + userDetails.getAuthorities() +
                                   " | has ROLE_ADMIN=" + userDetails.getAuthorities().stream()
                                       .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));

                boolean valid = jwtService.isTokenValid(token, userDetails.getUsername());
                System.out.println("[JWT] " + path + " | Token valid: " + valid);

                if (valid) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("[JWT] " + path + " | Authentication set successfully");
                } else {
                    System.out.println("[JWT] " + path + " | Token validation failed");
                }
            }
        } catch (DisabledException e) {
            System.err.println("[JWT] Compte désactivé : " + e.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Compte désactivé");
            return;
        } catch (Exception e) {
            System.err.println("[JWT] Erreur token : " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}