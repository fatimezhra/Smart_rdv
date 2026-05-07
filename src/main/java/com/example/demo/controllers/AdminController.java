package com.example.demo.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.repositories.RendezVousRepository;
import com.example.demo.repositories.WaitingListRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    private WaitingListRepository waitingListRepository;

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {

        Map<String, Object> data = new HashMap<>();

        data.put("reservations", rendezVousRepository.findAll());
        data.put("waitingList", waitingListRepository.findAll());

        return data;
    }
}