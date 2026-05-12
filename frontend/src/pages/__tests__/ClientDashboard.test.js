import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import ClientDashboard from '../ClientDashboard';
import * as api from '../../api/api';
import { ToastProvider } from '../../context/ToastContext';

// Mock the API module
jest.mock('../../api/api');

const mockAppointments = [
  {
    id: 1,
    date: '2026-05-15',
    heure: '10:00',
    statut: 'CONFIRMED',
    notes: 'Regular checkup'
  },
  {
    id: 2,
    date: '2026-05-16',
    heure: '14:00',
    statut: 'CANCELLED',
    notes: 'Cancelled appointment'
  }
];

describe('ClientDashboard', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    api.fetchUpcomingReservations.mockResolvedValue([]);
    api.fetchReservationHistory.mockResolvedValue([]);
    api.fetchWaitingList.mockResolvedValue([]);
    api.cancelReservation.mockResolvedValue({});
    api.downloadAppointmentPdf.mockResolvedValue({ success: true });
  });

  const renderWithToast = (component) => {
    return render(
      <ToastProvider>
        {component}
      </ToastProvider>
    );
  };

  test('renders page title', () => {
    renderWithToast(<ClientDashboard />);
    expect(screen.getByText('My Appointments')).toBeInTheDocument();
  });

  test('renders tab buttons', () => {
    renderWithToast(<ClientDashboard />);
    expect(screen.getByText('Upcoming')).toBeInTheDocument();
    expect(screen.getByText('Waiting List')).toBeInTheDocument();
    expect(screen.getByText('History')).toBeInTheDocument();
  });

  test('loads upcoming reservations on mount', async () => {
    api.fetchUpcomingReservations.mockResolvedValue([mockAppointments[0]]);
    
    renderWithToast(<ClientDashboard />);
    
    await waitFor(() => {
      expect(api.fetchUpcomingReservations).toHaveBeenCalledTimes(1);
    });
  });

  test('displays upcoming appointments', async () => {
    api.fetchUpcomingReservations.mockResolvedValue([mockAppointments[0]]);
    
    renderWithToast(<ClientDashboard />);
    
    await waitFor(() => {
      expect(screen.getByText('2026-05-15 • 10:00')).toBeInTheDocument();
      expect(screen.getByText('CONFIRMED')).toBeInTheDocument();
    });
  });

  test('displays cancelled appointments in history', async () => {
    api.fetchReservationHistory.mockResolvedValue([mockAppointments[1]]);
    
    renderWithToast(<ClientDashboard />);
    
    // Switch to history tab
    fireEvent.click(screen.getByText('History'));
    
    await waitFor(() => {
      expect(screen.getByText('2026-05-16 • 14:00')).toBeInTheDocument();
      expect(screen.getByText('CANCELLED')).toBeInTheDocument();
    });
  });

  test('shows reschedule and cancel buttons for confirmed appointments', async () => {
    api.fetchUpcomingReservations.mockResolvedValue([mockAppointments[0]]);
    
    renderWithToast(<ClientDashboard />);
    
    await waitFor(() => {
      expect(screen.getByText('Reschedule')).toBeInTheDocument();
      expect(screen.getByText('Cancel')).toBeInTheDocument();
      expect(screen.getByText('📄 Download PDF')).toBeInTheDocument();
    });
  });

  test('shows download PDF button for cancelled appointments in history', async () => {
    api.fetchReservationHistory.mockResolvedValue([mockAppointments[1]]);
    
    renderWithToast(<ClientDashboard />);
    
    // Switch to history tab
    fireEvent.click(screen.getByText('History'));
    
    await waitFor(() => {
      expect(screen.getByText('📄 Download PDF')).toBeInTheDocument();
    });
  });

  test('calls cancelReservation when cancel button is clicked', async () => {
    api.fetchUpcomingReservations.mockResolvedValue([mockAppointments[0]]);
    api.cancelReservation.mockResolvedValue({});
    
    renderWithToast(<ClientDashboard />);
    
    await waitFor(() => {
      expect(screen.getByText('Cancel')).toBeInTheDocument();
    });
    
    // Mock window.confirm
    window.confirm = jest.fn(() => true);
    
    fireEvent.click(screen.getByText('Cancel'));
    
    expect(window.confirm).toHaveBeenCalledWith('Cancel this appointment?');
    expect(api.cancelReservation).toHaveBeenCalledWith(1);
  });

  test('calls downloadAppointmentPdf when PDF download button is clicked', async () => {
    api.fetchUpcomingReservations.mockResolvedValue([mockAppointments[0]]);
    api.downloadAppointmentPdf.mockResolvedValue({ success: true });
    
    renderWithToast(<ClientDashboard />);
    
    await waitFor(() => {
      expect(screen.getByText('📄 Download PDF')).toBeInTheDocument();
    });
    
    fireEvent.click(screen.getByText('📄 Download PDF'));
    
    expect(api.downloadAppointmentPdf).toHaveBeenCalledWith(1);
  });

  test('shows empty state when no appointments', async () => {
    api.fetchUpcomingReservations.mockResolvedValue([]);
    
    renderWithToast(<ClientDashboard />);
    
    await waitFor(() => {
      expect(screen.getByText('No appointments found.')).toBeInTheDocument();
      expect(screen.getByText('Book an appointment')).toBeInTheDocument();
    });
  });

  test('switches tabs correctly', async () => {
    renderWithToast(<ClientDashboard />);
    
    // Initially on upcoming tab
    expect(screen.getByText('Upcoming')).toHaveClass('active');
    
    // Click waiting list tab
    fireEvent.click(screen.getByText('Waiting List'));
    expect(api.fetchWaitingList).toHaveBeenCalledTimes(1);
    
    // Click history tab
    fireEvent.click(screen.getByText('History'));
    expect(api.fetchReservationHistory).toHaveBeenCalledTimes(1);
  });

  test('shows loading skeleton while loading', () => {
    // Don't resolve the mock to keep loading state
    api.fetchUpcomingReservations.mockImplementation(() => new Promise(() => {}));
    
    renderWithToast(<ClientDashboard />);
    
    expect(screen.getByTestId('skeleton')).toBeInTheDocument();
  });
});
