import {
  fetchUpcomingReservations,
  fetchReservationHistory,
  fetchWaitingList,
  cancelReservation,
  bookSlot,
  downloadAppointmentPdf,
  loginUser,
  registerUser
} from '../api';

// Mock fetch globally
global.fetch = jest.fn();

describe('API Functions', () => {
  beforeEach(() => {
    fetch.mockClear();
    localStorage.clear();
  });

  describe('Authentication', () => {
    test('loginUser should post to auth endpoint', async () => {
      const mockResponse = { token: 'mock-token', user: { email: 'test@example.com' } };
      fetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockResponse)
      });

      const result = await loginUser('test@example.com', 'password');

      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('/auth/login'),
        expect.objectContaining({
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ email: 'test@example.com', password: 'password' })
        })
      );
      expect(localStorage.getItem('token')).toBe('mock-token');
    });

    test('loginUser should handle login failure', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        status: 401
      });

      await expect(loginUser('test@example.com', 'wrongpassword'))
        .rejects.toThrow('Login failed');
    });

    test('registerUser should post to register endpoint', async () => {
      fetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve({ id: 1, email: 'new@example.com' })
      });

      await registerUser({ 
        name: 'New User', 
        email: 'new@example.com', 
        password: 'password123' 
      });

      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('/auth/register'),
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify({ 
            name: 'New User', 
            email: 'new@example.com', 
            password: 'password123' 
          })
        })
      );
    });
  });

  describe('Reservations', () => {
    test('fetchUpcomingReservations should get upcoming reservations', async () => {
      const mockReservations = [{ id: 1, date: '2026-05-15', statut: 'CONFIRMED' }];
      fetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockReservations)
      });

      const result = await fetchUpcomingReservations();

      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('/reservations/upcoming'),
        expect.objectContaining({
          headers: expect.objectContaining({
            'Authorization': 'Bearer null'
          })
        })
      );
      expect(result).toEqual(mockReservations);
    });

    test('fetchReservationHistory should get reservation history', async () => {
      const mockHistory = [{ id: 1, date: '2026-05-10', statut: 'CANCELLED' }];
      fetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockHistory)
      });

      const result = await fetchReservationHistory();

      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('/reservations/history'),
        expect.any(Object)
      );
      expect(result).toEqual(mockHistory);
    });

    test('fetchWaitingList should get waiting list entries', async () => {
      const mockWaitingList = [{ id: 1, position: 1 }];
      fetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockWaitingList)
      });

      const result = await fetchWaitingList();

      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('/reservations/waiting'),
        expect.any(Object)
      );
      expect(result).toEqual(mockWaitingList);
    });

    test('cancelReservation should delete reservation', async () => {
      fetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve({})
      });

      await cancelReservation(1);

      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('/reservations/1'),
        expect.objectContaining({
          method: 'DELETE',
          headers: expect.objectContaining({
            'Authorization': 'Bearer null'
          })
        })
      );
    });

    test('bookSlot should create reservation', async () => {
      const mockBooking = { id: 1, statut: 'CONFIRMED' };
      fetch.mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockBooking)
      });

      const result = await bookSlot(1);

      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('/reservations/1'),
        expect.objectContaining({
          method: 'POST',
          headers: expect.objectContaining({
            'Authorization': 'Bearer null'
          })
        })
      );
      expect(result).toEqual(mockBooking);
    });
  });

  describe('PDF Download', () => {
    test('downloadAppointmentPdf should download PDF file', async () => {
      const mockBlob = new Blob(['pdf content'], { type: 'application/pdf' });
      const mockUrl = 'blob:mock-url';
      
      fetch.mockResolvedValueOnce({
        ok: true,
        headers: {
          get: (name) => name === 'Content-Disposition' ? 
            'attachment; filename="appointment_1_2026-05-15.pdf"' : null
        },
        blob: () => Promise.resolve(mockBlob)
      });

      // Mock URL.createObjectURL and link creation
      global.URL.createObjectURL = jest.fn(() => mockUrl);
      const mockLink = {
        href: '',
        download: '',
        click: jest.fn()
      };
      global.document.createElement = jest.fn(() => mockLink);
      global.document.body = { appendChild: jest.fn(), removeChild: jest.fn() };
      global.URL.revokeObjectURL = jest.fn();

      await downloadAppointmentPdf(1);

      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('/reservations/1/pdf'),
        expect.objectContaining({
          headers: expect.objectContaining({
            'Authorization': 'Bearer null'
          })
        })
      );
      expect(global.URL.createObjectURL).toHaveBeenCalledWith(mockBlob);
      expect(mockLink.download).toBe('appointment_1_2026-05-15.pdf');
      expect(mockLink.click).toHaveBeenCalled();
      expect(global.document.body.removeChild).toHaveBeenCalledWith(mockLink);
      expect(global.URL.revokeObjectURL).toHaveBeenCalledWith(mockUrl);
    });

    test('downloadAppointmentPdf should handle auth error', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        status: 401
      });

      // Mock the auth:logout event
      const mockEvent = new CustomEvent('auth:logout');
      global.CustomEvent = jest.fn(() => mockEvent);
      global.window = { dispatchEvent: jest.fn() };

      await expect(downloadAppointmentPdf(1))
        .rejects.toThrow('Auth error 401 on /reservations/1/pdf');

      expect(global.window.dispatchEvent).toHaveBeenCalledWith(mockEvent);
    });
  });

  describe('Error Handling', () => {
    test('should handle JSON error responses', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        status: 400,
        json: () => Promise.resolve({ message: 'Bad request' })
      });

      await expect(fetchUpcomingReservations())
        .rejects.toThrow('Bad request');
    });

    test('should handle text error responses', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        status: 500,
        json: () => Promise.reject(new Error('No JSON'))
      });

      fetch.mockResolvedValueOnce({
        ok: false,
        status: 500,
        text: () => Promise.resolve('Internal Server Error')
      });

      await expect(fetchUpcomingReservations())
        .rejects.toThrow('Internal Server Error');
    });
  });
});
