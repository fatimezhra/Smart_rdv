package com.example.demo.controllers;

import com.example.demo.entities.RendezVous;
import com.example.demo.entities.Statut;
import com.example.demo.entities.User;
import com.example.demo.entities.WaitingList;
import com.example.demo.repositories.RendezVousRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.repositories.WaitingListRepository;
import com.example.demo.services.IReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IReservationService reservationService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RendezVousRepository rendezVousRepository;

    @MockBean
    private WaitingListRepository waitingListRepository;

    private User testUser;
    private RendezVous testRendezVous;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("Test User");

        testRendezVous = new RendezVous();
        testRendezVous.setId(1L);
        testRendezVous.setUser(testUser);
        testRendezVous.setDate(LocalDate.of(2025, java.time.Month.JANUARY, 15));
        testRendezVous.setStatut(Statut.CONFIRMED);

        when(userRepository.findByEmail("test@example.com")).thenReturn(java.util.Optional.of(testUser));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getMyReservations_ShouldReturnUserReservations() throws Exception {
        List<RendezVous> reservations = Arrays.asList(testRendezVous);
        when(rendezVousRepository.findByUser(testUser)).thenReturn(reservations);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getUpcoming_ShouldReturnConfirmedReservations() throws Exception {
        List<RendezVous> upcoming = Arrays.asList(testRendezVous);
        when(rendezVousRepository.findByUserAndStatut(testUser, Statut.CONFIRMED)).thenReturn(upcoming);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/reservations/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].statut").value("CONFIRMED"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getHistory_ShouldReturnCancelledReservations() throws Exception {
        testRendezVous.setStatut(Statut.CANCELLED);
        List<RendezVous> history = Arrays.asList(testRendezVous);
        when(rendezVousRepository.findByUserAndStatutInOrderByDateDesc(
                eq(testUser), anyList())).thenReturn(history);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/reservations/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].statut").value("CANCELLED"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void reserver_ShouldCreateReservation() throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("id", 1L);
        org.springframework.http.ResponseEntity<Map<String, Object>> responseEntity =
                org.springframework.http.ResponseEntity.ok(response);
        when(reservationService.reserver(anyLong(), eq(testUser))).thenReturn(responseEntity);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/reservations/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void annuler_ShouldCancelReservation() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/reservations/1")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void reschedule_ShouldRescheduleReservation() throws Exception {
        Map<String, Long> body = new HashMap<>();
        body.put("newSlotId", 2L);
        when(reservationService.reschedule(anyLong(), anyLong(), eq(testUser))).thenReturn(testRendezVous);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/reservations/1/reschedule")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void addNotes_ShouldAddNotesToReservation() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("notes", "Test notes");
        when(reservationService.addNotes(anyLong(), anyString(), eq(testUser))).thenReturn(testRendezVous);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/reservations/1/notes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getWaitingList_ShouldReturnUserWaitingList() throws Exception {
        WaitingList waitingList = new WaitingList();
        waitingList.setId(1L);
        waitingList.setUser(testUser);
        waitingList.setPosition(1);
        List<WaitingList> waitingLists = Arrays.asList(waitingList);
        when(waitingListRepository.findByUserOrderByPositionAsc(testUser)).thenReturn(waitingLists);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/reservations/waiting"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].position").value(1));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void joinWaitingList_ShouldAddUserToWaitingList() throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("date", "2025-01-15");
        Map<String, Object> response = new HashMap<>();
        response.put("position", 1);
        when(reservationService.joinWaitingList(any(LocalDate.class), eq(testUser))).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/reservations/waiting")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value(1));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getAppointmentPdf_ShouldReturnPdf() throws Exception {
        when(rendezVousRepository.findById(1L)).thenReturn(java.util.Optional.of(testRendezVous));
        when(rendezVousRepository.findByUser(testUser)).thenReturn(Arrays.asList(testRendezVous));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/reservations/1/pdf"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getAppointmentPdf_ShouldThrowException_WhenRendezVousNotFound() throws Exception {
        when(rendezVousRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/reservations/1/pdf"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void getAppointmentPdf_ShouldThrowException_WhenAccessDenied() throws Exception {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("other@example.com");
        testRendezVous.setUser(otherUser);
        when(rendezVousRepository.findById(1L)).thenReturn(java.util.Optional.of(testRendezVous));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/reservations/1/pdf"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "unknown@example.com")
    void getMyReservations_ShouldThrowException_WhenUserNotFound() throws Exception {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(java.util.Optional.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/reservations"))
                .andExpect(status().isBadRequest());
    }
}
