package com.example.demo.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.repositories.RendezVousRepository;
import com.example.demo.repositories.WaitingListRepository;

@Service
public class AdminService {

    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    private WaitingListRepository waitingListRepository;

    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();
        data.put("reservations", rendezVousRepository.findAll());
        data.put("waitingList", waitingListRepository.findAll());
        return data;
    }
}