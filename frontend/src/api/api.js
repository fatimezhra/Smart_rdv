const API_BASE = 'https://smartrdv-production.up.railway.app';


// API_BASE n'est plus nécessaire grâce au proxy
export async function apiFetch(path, options = {}) {
  const token = localStorage.getItem('token');

  const headers = {
    ...(options.body ? { 'Content-Type': 'application/json' } : {}),
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  console.log(`[API] ${options.method || 'GET'} ${path}`);

  const res = await fetch(API_BASE + path, { 
    ...options, 
    headers 
  });

  console.log(`[API Response] ${res.status} ${path}`);

  if (!res.ok) {
    throw new Error(`Erreur ${res.status}`);
  }

  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

// Client - Reservations
export async function fetchUpcomingReservations() {
  return apiFetch('/api/reservations/upcoming');
}

export async function fetchReservationHistory() {
  return apiFetch('/api/reservations/history');
}

export async function cancelReservation(id) {
  return apiFetch('/api/reservations/' + id, { method: 'DELETE' });
}

export async function rescheduleReservation(id, newSlotId) {
  return apiFetch('/api/reservations/' + id + '/reschedule', {
    method: 'PUT',
    body: JSON.stringify({ newSlotId }),
  });
}

export async function downloadAppointmentPdf(id) {
  const token = localStorage.getItem('token');
  const res = await fetch(API_BASE + '/api/reservations/' + id + '/pdf', {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  if (!res.ok) throw new Error(`Erreur ${res.status}`);
  const blob = await res.blob();
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'appointment-' + id + '.pdf';
  a.click();
  window.URL.revokeObjectURL(url);
}

// Client - Slots & Booking
export async function fetchAvailableSlots(date) {
  return apiFetch('/api/slots/available?date=' + date);
}

export async function fetchAllSlots(date) {
  return apiFetch('/api/slots/all?date=' + date);
}

export async function bookSlot(slotId) {
  return apiFetch('/api/reservations/' + slotId, { method: 'POST' });
}

// Client - Waiting List
export async function fetchWaitingList() {
  return apiFetch('/api/reservations/waiting');
}

export async function joinWaitingList(date) {
  return apiFetch('/api/reservations/waiting', { method: 'POST', body: JSON.stringify({ date }) });
}

// Calendar
export async function fetchCalendarAvailability(monthKey) {
  return apiFetch('/api/slots/calendar?month=' + monthKey);
}

// Admin - Dashboard
export async function fetchAdminDashboard() {
  return apiFetch('/api/admin/dashboard');
}

// Admin - Reservations
export async function fetchAdminReservations(params) {
  const query = params ? '?' + new URLSearchParams(params).toString() : '';
  return apiFetch('/api/admin/reservations' + query);
}

// Admin - Calendar Management
export async function fetchBlockedDates(monthKey) {
  return apiFetch('/api/admin/blocked-dates?month=' + monthKey);
}

export async function blockDate(date, reason) {
  return apiFetch('/api/admin/blocked-dates', {
    method: 'POST',
    body: JSON.stringify({ date, reason }),
  });
}

export async function unblockDate(date) {
  return apiFetch('/api/admin/blocked-dates/' + date, {
    method: 'DELETE',
  });
}

export async function generateSlots(date) {
  return apiFetch('/api/admin/slots/generate?date=' + date, {
    method: 'GET',
  });
}

export async function fetchWorkingHours() {
  return apiFetch('/api/admin/config/hours');
}

export async function saveWorkingHours(config) {
  return apiFetch('/api/admin/config/hours', {
    method: 'POST',
    body: JSON.stringify(config),
  });
}

// Admin - Users
export async function fetchAdminUsers(page, size) {
  return apiFetch('/api/admin/users?page=' + page + '&size=' + size);
}

export async function enableUser(id) {
  return apiFetch('/api/admin/users/' + id + '/enable', { method: 'PUT' });
}

export async function disableUser(id) {
  return apiFetch('/api/admin/users/' + id + '/disable', { method: 'PUT' });
}

export async function deleteUser(id) {
  return apiFetch('/api/admin/users/' + id, { method: 'DELETE' });
}

export async function createAdmin(data) {
  return apiFetch('/api/admin/users/create-admin', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

// Admin - Waiting List
export async function fetchAdminWaitingList() {
  return apiFetch('/api/admin/waiting-list');
}

export async function promoteWaitingListEntry(id) {
  return apiFetch('/api/admin/waiting-list/' + id + '/promote', { method: 'POST' });
}

export async function removeWaitingListEntry(id) {
  return apiFetch('/api/admin/waiting-list/' + id, { method: 'DELETE' });
}

export async function loginUser(email, password) {
  return apiFetch('/api/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) });
}

export async function registerUser(data) {
  return apiFetch('/api/auth/register', { method: 'POST', body: JSON.stringify(data) });
}