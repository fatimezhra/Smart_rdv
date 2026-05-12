package com.example.demo.controllers;

import com.example.demo.entities.User;
import com.example.demo.services.UserAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    @Autowired
    private UserAdminService userAdminService;

    @GetMapping("/users")
    public Map<String, Object> getUsers(Pageable pageable) {
        Page<User> page = userAdminService.getAllUsers(pageable);
        List<Map<String, Object>> content = page.getContent().stream()
                .map(userAdminService::getUserWithStats)
                .collect(Collectors.toList());

        return Map.of(
                "content", content,
                "totalElements", page.getTotalElements(),
                "totalPages", page.getTotalPages(),
                "number", page.getNumber(),
                "size", page.getSize()
        );
    }

    @PutMapping("/users/{id}/disable")
    public User disableUser(@PathVariable Long id) {
        return userAdminService.disableUser(id);
    }

    @PutMapping("/users/{id}/enable")
    public User enableUser(@PathVariable Long id) {
        return userAdminService.enableUser(id);
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        userAdminService.deleteUser(id);
    }
}
