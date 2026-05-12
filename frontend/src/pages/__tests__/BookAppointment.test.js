import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import BookAppointment from '../BookAppointment';
import * as api from '../../api/api';
import { ToastProvider } from '../../context/ToastContext';

// Mock the API module
jest.mock('../../api/api');

describe('BookAppointment', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    api.fetchCalendarAvailability.mockResolvedValue({});
    api.fetchAvailableSlots.mockResolvedValue([]);
    api.fetchAllSlots.mockResolvedValue([]);
    api.bookSlot.mockResolvedValue({ type: 'CONFIRMED' });
  });

  const renderWithToast = (component) => {
    return render(
      <ToastProvider>
        {component}
      </ToastProvider>
    );
  };

  test('renders page title', () => {
    renderWithToast(<BookAppointment />);
    expect(screen.getByText('Book an Appointment')).toBeInTheDocument();
  });

  test('renders calendar navigation', () => {
    renderWithToast(<BookAppointment />);
    expect(screen.getByText('←')).toBeInTheDocument();
    expect(screen.getByText('→')).toBeInTheDocument();
  });

  test('renders calendar grid', () => {
    renderWithToast(<BookAppointment />);
    expect(screen.getByText('Sun')).toBeInTheDocument();
    expect(screen.getByText('Mon')).toBeInTheDocument();
    expect(screen.getByText('Tue')).toBeInTheDocument();
    expect(screen.getByText('Wed')).toBeInTheDocument();
    expect(screen.getByText('Thu')).toBeInTheDocument();
    expect(screen.getByText('Fri')).toBeInTheDocument();
    expect(screen.getByText('Sat')).toBeInTheDocument();
  });

  test('loads calendar availability on mount', () => {
    renderWithToast(<BookAppointment />);
    expect(api.fetchCalendarAvailability).toHaveBeenCalled();
  });

  test('loads slots when date is selected', async () => {
    const mockSlots = [
      { id: 1, heure: '09:00', disponible: true },
      { id: 2, heure: '10:00', disponible: false }
    ];
    
    api.fetchAvailableSlots.mockResolvedValue(mockSlots);
    api.fetchAllSlots.mockResolvedValue(mockSlots);
    
    renderWithToast(<BookAppointment />);
    
    // Find and click a date
    const dateElement = screen.getByText('15');
    fireEvent.click(dateElement);
    
    waitFor(() => {
      expect(api.fetchAvailableSlots).toHaveBeenCalled();
      expect(api.fetchAllSlots).toHaveBeenCalled();
    });
  });

  test('displays available slots', async () => {
    const mockSlots = [
      { id: 1, heure: '09:00', disponible: true },
      { id: 2, heure: '10:00', disponible: true }
    ];
    
    api.fetchAvailableSlots.mockResolvedValue(mockSlots);
    api.fetchAllSlots.mockResolvedValue(mockSlots);
    
    renderWithToast(<BookAppointment />);
    
    // Click a date to load slots
    const dateElement = screen.getByText('15');
    fireEvent.click(dateElement);
    
    await waitFor(() => {
      expect(screen.getByText('09:00')).toBeInTheDocument();
      expect(screen.getByText('10:00')).toBeInTheDocument();
      expect(screen.getByText('Book')).toBeInTheDocument();
    });
  });

  test('displays taken slots with correct button', async () => {
    const mockSlots = [
      { id: 1, heure: '09:00', disponible: true },
      { id: 2, heure: '10:00', disponible: false }
    ];
    
    api.fetchAvailableSlots.mockResolvedValue([mockSlots[0]]);
    api.fetchAllSlots.mockResolvedValue(mockSlots);
    
    renderWithToast(<BookAppointment />);
    
    // Click a date to load slots
    const dateElement = screen.getByText('15');
    fireEvent.click(dateElement);
    
    await waitFor(() => {
      expect(screen.getByText('Taken')).toBeInTheDocument();
    });
  });

  test('calls bookSlot when book button is clicked', async () => {
    const mockSlots = [
      { id: 1, heure: '09:00', disponible: true }
    ];
    
    api.fetchAvailableSlots.mockResolvedValue(mockSlots);
    api.fetchAllSlots.mockResolvedValue(mockSlots);
    api.bookSlot.mockResolvedValue({ type: 'CONFIRMED', statut: 'CONFIRMED' });
    
    renderWithToast(<BookAppointment />);
    
    // Click a date to load slots
    const dateElement = screen.getByText('15');
    fireEvent.click(dateElement);
    
    await waitFor(() => {
      expect(screen.getByText('Book')).toBeInTheDocument();
    });
    
    fireEvent.click(screen.getByText('Book'));
    
    expect(api.bookSlot).toHaveBeenCalledWith(1);
  });

  test('shows success message when booking is confirmed', async () => {
    const mockSlots = [
      { id: 1, heure: '09:00', disponible: true }
    ];
    
    api.fetchAvailableSlots.mockResolvedValue(mockSlots);
    api.fetchAllSlots.mockResolvedValue(mockSlots);
    api.bookSlot.mockResolvedValue({ 
      type: 'CONFIRMED', 
      statut: 'CONFIRMED' 
    });
    
    renderWithToast(<BookAppointment />);
    
    // Click a date to load slots
    const dateElement = screen.getByText('15');
    fireEvent.click(dateElement);
    
    await waitFor(() => {
      fireEvent.click(screen.getByText('Book'));
    });
    
    await waitFor(() => {
      expect(screen.getByText('Your appointment is confirmed!')).toBeInTheDocument();
    });
  });

  test('shows error message when no slots available', async () => {
    api.fetchAvailableSlots.mockResolvedValue([]);
    api.fetchAllSlots.mockResolvedValue([]);
    
    renderWithToast(<BookAppointment />);
    
    // Click a date to load slots
    const dateElement = screen.getByText('15');
    fireEvent.click(dateElement);
    
    await waitFor(() => {
      expect(screen.getByText('No slots available for this date.')).toBeInTheDocument();
      expect(screen.getByText('Join Waiting List')).toBeInTheDocument();
    });
  });

  test('navigates to previous month', () => {
    renderWithToast(<BookAppointment />);
    
    const prevButton = screen.getByText('←');
    fireEvent.click(prevButton);
    
    // Should fetch calendar for previous month
    expect(api.fetchCalendarAvailability).toHaveBeenCalled();
  });

  test('navigates to next month', () => {
    renderWithToast(<BookAppointment />);
    
    const nextButton = screen.getByText('→');
    fireEvent.click(nextButton);
    
    // Should fetch calendar for next month
    expect(api.fetchCalendarAvailability).toHaveBeenCalled();
  });

  test('shows loading state', () => {
    // Don't resolve mocks to keep loading state
    api.fetchCalendarAvailability.mockImplementation(() => new Promise(() => {}));
    
    renderWithToast(<BookAppointment />);
    
    expect(screen.getByTestId('skeleton')).toBeInTheDocument();
  });
});
