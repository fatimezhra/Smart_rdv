package com.example.demo.services;

import com.example.demo.entities.BlockedDate;
import com.example.demo.entities.RendezVous;
import com.example.demo.entities.Statut;
import com.example.demo.repositories.BlockedDateRepository;
import com.example.demo.repositories.RendezVousRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.repositories.WaitingListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private RendezVousRepository rendezVousRepository;

    @Mock
    private WaitingListRepository waitingListRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BlockedDateRepository blockedDateRepository;

    @InjectMocks
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getDashboardData_ShouldReturnDashboardStatistics() {
        // Given
        when(userRepository.count()).thenReturn(100L);
        when(rendezVousRepository.count()).thenReturn(500L);
        when(rendezVousRepository.countByStatutAndDate(eq(Statut.CONFIRMED), any(LocalDate.class))).thenReturn(10L);
        when(rendezVousRepository.countByStatutAndDate(eq(Statut.CANCELLED), any(LocalDate.class))).thenReturn(5L);
        when(waitingListRepository.count()).thenReturn(20L);
        when(rendezVousRepository.findByStatutAndDate(eq(Statut.CONFIRMED), any(LocalDate.class))).thenReturn(List.of());
        when(rendezVousRepository.findTop10ByOrderByUpdatedAtDesc()).thenReturn(List.of());
        when(blockedDateRepository.findByDateBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of());

        // When
        Map<String, Object> result = adminService.getDashboardData();

        // Then
        assertNotNull(result);
        assertEquals(100L, result.get("totalUsers"));
        assertEquals(500L, result.get("totalReservations"));
        assertEquals(10L, result.get("confirmedToday"));
        assertEquals(5L, result.get("cancelledToday"));
        assertEquals(20L, result.get("waitingListCount"));
        verify(userRepository).count();
        verify(rendezVousRepository).count();
        verify(rendezVousRepository).countByStatutAndDate(eq(Statut.CONFIRMED), any(LocalDate.class));
        verify(rendezVousRepository).countByStatutAndDate(eq(Statut.CANCELLED), any(LocalDate.class));
        verify(waitingListRepository).count();
    }

    @Test
    void getDashboardData_ShouldIncludeUpcomingAppointments() {
        // Given
        RendezVous upcomingRdv = new RendezVous();
        upcomingRdv.setId(1L);
        upcomingRdv.setStatut(Statut.CONFIRMED);

        when(userRepository.count()).thenReturn(100L);
        when(rendezVousRepository.count()).thenReturn(500L);
        when(rendezVousRepository.countByStatutAndDate(any(), any())).thenReturn(10L);
        when(waitingListRepository.count()).thenReturn(20L);
        when(rendezVousRepository.findByStatutAndDate(eq(Statut.CONFIRMED), any(LocalDate.class))).thenReturn(List.of(upcomingRdv));
        when(rendezVousRepository.findTop10ByOrderByUpdatedAtDesc()).thenReturn(List.of());
        when(blockedDateRepository.findByDateBetween(any(), any())).thenReturn(List.of());

        // When
        Map<String, Object> result = adminService.getDashboardData();

        // Then
        assertNotNull(result);
        List<?> upcoming = (List<?>) result.get("upcomingAppointments");
        assertNotNull(upcoming);
        verify(rendezVousRepository).findByStatutAndDate(eq(Statut.CONFIRMED), any(LocalDate.class));
    }

    @Test
    void getDashboardData_ShouldIncludeRecentCancellations() {
        // Given
        RendezVous cancelledRdv = new RendezVous();
        cancelledRdv.setId(1L);
        cancelledRdv.setStatut(Statut.CANCELLED);

        when(userRepository.count()).thenReturn(100L);
        when(rendezVousRepository.count()).thenReturn(500L);
        when(rendezVousRepository.countByStatutAndDate(any(), any())).thenReturn(10L);
        when(waitingListRepository.count()).thenReturn(20L);
        when(rendezVousRepository.findByStatutAndDate(any(), any())).thenReturn(List.of());
        when(rendezVousRepository.findTop10ByOrderByUpdatedAtDesc()).thenReturn(List.of(cancelledRdv));
        when(blockedDateRepository.findByDateBetween(any(), any())).thenReturn(List.of());

        // When
        Map<String, Object> result = adminService.getDashboardData();

        // Then
        assertNotNull(result);
        List<?> recentCancellations = (List<?>) result.get("recentCancellations");
        assertNotNull(recentCancellations);
        verify(rendezVousRepository).findTop10ByOrderByUpdatedAtDesc();
    }

    @Test
    void getDashboardData_ShouldIncludeBlockedDatesForCurrentMonth() {
        // Given
        BlockedDate blockedDate = new BlockedDate();
        blockedDate.setId(1L);
        blockedDate.setDate(LocalDate.now().plusDays(5));

        when(userRepository.count()).thenReturn(100L);
        when(rendezVousRepository.count()).thenReturn(500L);
        when(rendezVousRepository.countByStatutAndDate(any(), any())).thenReturn(10L);
        when(waitingListRepository.count()).thenReturn(20L);
        when(rendezVousRepository.findByStatutAndDate(any(), any())).thenReturn(List.of());
        when(rendezVousRepository.findTop10ByOrderByUpdatedAtDesc()).thenReturn(List.of());
        when(blockedDateRepository.findByDateBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(List.of(blockedDate));

        // When
        Map<String, Object> result = adminService.getDashboardData();

        // Then
        assertNotNull(result);
        List<?> blockedDates = (List<?>) result.get("blockedDatesThisMonth");
        assertNotNull(blockedDates);
        verify(blockedDateRepository).findByDateBetween(any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void getDashboardData_ShouldHandleEmptyData() {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(rendezVousRepository.count()).thenReturn(0L);
        when(rendezVousRepository.countByStatutAndDate(any(), any())).thenReturn(0L);
        when(waitingListRepository.count()).thenReturn(0L);
        when(rendezVousRepository.findByStatutAndDate(any(), any())).thenReturn(List.of());
        when(rendezVousRepository.findTop10ByOrderByUpdatedAtDesc()).thenReturn(List.of());
        when(blockedDateRepository.findByDateBetween(any(), any())).thenReturn(List.of());

        // When
        Map<String, Object> result = adminService.getDashboardData();

        // Then
        assertNotNull(result);
        assertEquals(0L, result.get("totalUsers"));
        assertEquals(0L, result.get("totalReservations"));
        assertEquals(0L, result.get("waitingListCount"));
    }
}
