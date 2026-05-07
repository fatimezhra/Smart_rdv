package com.example.demo.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.repositories.RendezVousRepository;
import com.example.demo.repositories.WaitingListRepository;
import com.example.demo.services.AdminService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;  // ✅ plus de repositories ici

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return adminService.getDashboardData();
    }
}