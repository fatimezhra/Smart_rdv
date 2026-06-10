package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private IJwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    private UserDetails userDetails;
    private String validToken;

    @BeforeEach
    void setUp() {
        userDetails = new User(
                "test@example.com",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_CLIENT"))
        );
        validToken = "valid.jwt.token";
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_ShouldAuthenticate_WhenValidToken() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid(validToken, "test@example.com")).thenReturn(true);

        // When
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername(validToken);
        verify(jwtService).isTokenValid(validToken, "test@example.com");
    }

    @Test
    void doFilterInternal_ShouldContinueChain_WhenNoToken() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(any());
        verify(userDetailsService, never()).loadUserByUsername(any());
    }

    @Test
    void doFilterInternal_ShouldContinueChain_WhenInvalidTokenFormat() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat token");

        // When
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(any());
        verify(userDetailsService, never()).loadUserByUsername(any());
    }

    @Test
    void doFilterInternal_ShouldContinueChain_WhenTokenWithoutBearer() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(validToken);

        // When
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUsername(any());
        verify(userDetailsService, never()).loadUserByUsername(any());
    }

    @Test
    void doFilterInternal_ShouldContinueChain_WhenTokenInvalid() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid(validToken, "test@example.com")).thenReturn(false);

        // When
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername(validToken);
        verify(jwtService).isTokenValid(validToken, "test@example.com");
    }

    @Test
    void doFilterInternal_ShouldContinueChain_WhenTokenExpired() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenThrow(new RuntimeException("Token expired"));

        // When
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername(validToken);
        verify(userDetailsService, never()).loadUserByUsername(any());
    }

    @Test
    void doFilterInternal_ShouldReturnForbidden_WhenUserDisabled() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com"))
                .thenThrow(new DisabledException("User disabled"));

        // When
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Compte désactivé");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldContinueChain_WhenAlreadyAuthenticated() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenReturn("test@example.com");
        org.springframework.security.core.context.SecurityContextHolder.getContext()
                .setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()));

        // When
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(any());
    }

    @Test
    void doFilterInternal_ShouldContinueChain_WhenUsernameIsNull() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.extractUsername(validToken)).thenReturn(null);

        // When
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername(validToken);
        verify(userDetailsService, never()).loadUserByUsername(any());
    }

    @Test
    void shouldNotFilter_ShouldReturnTrue_ForAuthLogin() {
        // Given
        when(request.getRequestURI()).thenReturn("/auth/login");

        // When
        boolean result = jwtAuthFilter.shouldNotFilter(request);

        // Then
        assertTrue(result);
    }

    @Test
    void shouldNotFilter_ShouldReturnTrue_ForAuthRegister() {
        // Given
        when(request.getRequestURI()).thenReturn("/auth/register");

        // When
        boolean result = jwtAuthFilter.shouldNotFilter(request);

        // Then
        assertTrue(result);
    }

    @Test
    void shouldNotFilter_ShouldReturnTrue_ForSlotsAvailable() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/slots/available");

        // When
        boolean result = jwtAuthFilter.shouldNotFilter(request);

        // Then
        assertTrue(result);
    }

    @Test
    void shouldNotFilter_ShouldReturnFalse_ForProtectedEndpoint() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/reservations");

        // When
        boolean result = jwtAuthFilter.shouldNotFilter(request);

        // Then
        assertFalse(result);
    }
}
