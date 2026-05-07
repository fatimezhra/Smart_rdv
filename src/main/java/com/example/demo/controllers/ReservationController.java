package com.example.demo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // Vérifie cet import
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entities.User;

import com.example.demo.services.ReservationService;

import java.time.LocalDateTime; // Import indispensable pour getDateTime()

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;
    
    @GetMapping
    public String test() {
        return "API OK";
    }
     @PostMapping("/{slotId}")
public Object reserver(@PathVariable Long slotId, @RequestHeader Long userId) {
    User user = new User();
    user.setId(userId);

    return reservationService.reserver(slotId, user);
}

    @DeleteMapping("/{id}")
    public void annuler(@PathVariable Long id) {
        reservationService.annuler(id);
    }
}