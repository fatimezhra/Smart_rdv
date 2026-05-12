package com.example.demo.controllers;

import com.example.demo.entities.WaitingList;
import com.example.demo.services.WaitingListAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class WaitingListAdminController {

    @Autowired
    private WaitingListAdminService waitingListAdminService;

    @GetMapping("/waiting-list")
    public List<WaitingList> getWaitingList() {
        return waitingListAdminService.getFullWaitingList();
    }

    @PostMapping("/waiting-list/{id}/promote")
    public void promote(@PathVariable Long id) {
        waitingListAdminService.promoteWaitingListEntry(id);
    }

    @DeleteMapping("/waiting-list/{id}")
    public void remove(@PathVariable Long id) {
        waitingListAdminService.removeFromWaitingList(id);
    }
}
