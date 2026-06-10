package com.example.demo.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleRuntime_ShouldReturnBadRequest_WhenRuntimeExceptionThrown() {
        // Given
        RuntimeException ex = new RuntimeException("Test runtime error");

        // When
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleRuntime(ex);

        // Then
        assertEquals(400, response.getStatusCode().value());
        assertEquals("Test runtime error", response.getBody().get("message"));
        assertEquals("BAD_REQUEST", response.getBody().get("error"));
    }

    @Test
    void handleRuntime_ShouldReturnBadRequest_WhenNullPointerExceptionThrown() {
        // Given
        RuntimeException ex = new NullPointerException("Null pointer");

        // When
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleRuntime(ex);

        // Then
        assertEquals(400, response.getStatusCode().value());
        assertEquals("Null pointer", response.getBody().get("message"));
        assertEquals("BAD_REQUEST", response.getBody().get("error"));
    }

    @Test
    void handleValidation_ShouldReturnBadRequest_WhenValidationFails() {
        // Given
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(new FieldError("user", "email", "Email is required"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // When
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleValidation(ex);

        // Then
        assertEquals(400, response.getStatusCode().value());
        assertEquals("email: Email is required", response.getBody().get("message"));
        assertEquals("VALIDATION_ERROR", response.getBody().get("error"));
    }

    @Test
    void handleValidation_ShouldReturnBadRequest_WhenMultipleValidationErrors() {
        // Given
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(new FieldError("user", "email", "Email is required"));
        bindingResult.addError(new FieldError("user", "name", "Name is required"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // When
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleValidation(ex);

        // Then
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().get("message").contains("email"));
        assertTrue(response.getBody().get("message").contains("name"));
        assertEquals("VALIDATION_ERROR", response.getBody().get("error"));
    }

    @ParameterizedTest
    @MethodSource("provideNotFoundMessages")
    void handleNotFound_ShouldReturnNotFound(String message) {
        // Given
        UsernameNotFoundException ex = new UsernameNotFoundException(message);

        // When
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleNotFound(ex);

        // Then
        assertEquals(404, response.getStatusCode().value());
        assertEquals(message, response.getBody().get("message"));
        assertEquals("NOT_FOUND", response.getBody().get("error"));
    }

    private static Stream<String> provideNotFoundMessages() {
        return Stream.of("User not found", "Resource not found", "Custom not found message");
    }

    @ParameterizedTest
    @MethodSource("provideAccessDeniedMessages")
    void handleAccessDenied_ShouldReturnForbidden(String message) {
        // Given
        AccessDeniedException ex = new AccessDeniedException(message);

        // When
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleAccessDenied(ex);

        // Then
        assertEquals(403, response.getStatusCode().value());
        assertEquals("Access denied", response.getBody().get("message"));
        assertEquals("FORBIDDEN", response.getBody().get("error"));
    }

    private static Stream<String> provideAccessDeniedMessages() {
        return Stream.of("Access denied", "Insufficient permissions", "Custom access denied message");
    }

    @ParameterizedTest
    @MethodSource("provideAuthenticationMessages")
    void handleAuthentication_ShouldReturnUnauthorized(String message) {
        // Given
        AuthenticationException ex = new AuthenticationException(message) {};

        // When
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleAuthentication(ex);

        // Then
        assertEquals(401, response.getStatusCode().value());
        assertEquals("Authentication failed", response.getBody().get("message"));
        assertEquals("UNAUTHORIZED", response.getBody().get("error"));
    }

    private static Stream<String> provideAuthenticationMessages() {
        return Stream.of("Authentication failed", "Invalid credentials", "Custom auth message");
    }

    @Test
    void handleRuntime_ShouldPreserveOriginalMessage() {
        // Given
        String customMessage = "Custom error message for testing";
        RuntimeException ex = new RuntimeException(customMessage);

        // When
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleRuntime(ex);

        // Then
        assertEquals(customMessage, response.getBody().get("message"));
    }

    @Test
    void handleValidation_ShouldHandleEmptyFieldErrors() {
        // Given
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "object");
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // When
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleValidation(ex);

        // Then
        assertEquals(400, response.getStatusCode().value());
        assertEquals("", response.getBody().get("message"));
        assertEquals("VALIDATION_ERROR", response.getBody().get("error"));
    }

    @Test
    void handleRuntime_ShouldReturnBadRequest_WhenIllegalArgumentException() {
        // Given
        RuntimeException ex = new IllegalArgumentException("Invalid argument");

        // When
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleRuntime(ex);

        // Then
        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid argument", response.getBody().get("message"));
        assertEquals("BAD_REQUEST", response.getBody().get("error"));
    }

    @Test
    void handleRuntime_ShouldReturnBadRequest_WhenIllegalStateException() {
        // Given
        RuntimeException ex = new IllegalStateException("Illegal state");

        // When
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleRuntime(ex);

        // Then
        assertEquals(400, response.getStatusCode().value());
        assertEquals("Illegal state", response.getBody().get("message"));
        assertEquals("BAD_REQUEST", response.getBody().get("error"));
    }

    @Test
    void handleValidation_ShouldHandleSingleFieldError() {
        // Given
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(new FieldError("user", "password", "Password is required"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // When
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleValidation(ex);

        // Then
        assertEquals(400, response.getStatusCode().value());
        assertEquals("password: Password is required", response.getBody().get("message"));
        assertEquals("VALIDATION_ERROR", response.getBody().get("error"));
    }

    @Test
    void handleRuntime_ShouldReturnBadRequest_WhenEmptyMessage() {
        // Given
        RuntimeException ex = new RuntimeException("");

        // When
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleRuntime(ex);

        // Then
        assertEquals(400, response.getStatusCode().value());
        assertEquals("", response.getBody().get("message"));
        assertEquals("BAD_REQUEST", response.getBody().get("error"));
    }

    @Test
    void handleValidation_ShouldHandleThreeFieldErrors() {
        // Given
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(new FieldError("user", "email", "Email is required"));
        bindingResult.addError(new FieldError("user", "name", "Name is required"));
        bindingResult.addError(new FieldError("user", "password", "Password is required"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // When
        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleValidation(ex);

        // Then
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().get("message").contains("email"));
        assertTrue(response.getBody().get("message").contains("name"));
        assertTrue(response.getBody().get("message").contains("password"));
        assertEquals("VALIDATION_ERROR", response.getBody().get("error"));
    }
}
