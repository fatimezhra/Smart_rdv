const API_BASE = 'http://localhost:8081';
console.log(">>> API_BASE FORCEE SUR :", API_BASE); // AJOUTE CETTE LIGNEfunction getToken() {
  return localStorage.getItem('token');
}

export async function apiFetch(path, options = {}) {
  const token = localStorage.getItem('token');

  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  console.log(`[API] ${options.method || 'GET'} ${path} | Token present: ${!!token}`);

  const res = await fetch(`${API_BASE}${path}`, { ...options, headers });

  if (res.status === 401 || res.status === 403) {
    window.dispatchEvent(new CustomEvent('auth:logout'));
    throw new Error(`Auth error ${res.status} on ${path}`);
  }

  if (!res.ok) {
    let errorMessage = `Erreur ${res.status}`;
    try {
      const errorData = await res.json();
      errorMessage = errorData.message || errorData.error || errorMessage;
    } catch {
      errorMessage = await res.text() || errorMessage;
    }
    throw new Error(errorMessage);
  }

  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

export async function loginUser(email, password) {
  const res = await fetch(`${API_BASE}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  });

  if (!res.ok) throw new Error('Login failed');

  const data = await res.json();
  console.log('Login response:', data);

  const jwt = data.token ?? data.jwt ?? data.accessToken;
  if (!jwt) throw new Error('No token in login response');

  localStorage.setItem('token', jwt);

  const payload = JSON.parse(atob(jwt.split('.')[1]));
  const user = { email: payload.sub, role: payload.role ?? payload.roles?.[0] };
  localStorage.setItem('user', JSON.stringify(user));

  return data;
}

export async function registerUser(user) {
  return apiFetch('/auth/register', {
    method: 'POST',
    body: JSON.stringify(user),
  });
}

export async function createAdmin(user) {
  return apiFetch('/auth/admin/create', {
    method: 'POST',
    body: JSON.stringify(user),
  });
}

export async function fetchReservations() {
  return apiFetch('/reservations');
}

export async function cancelReservation(id) {
  return apiFetch(`/reservations/${id}`, {
    method: 'DELETE',
  });
}

export async function bookSlot(slotId) {
  return apiFetch(`/reservations/${slotId}`, {
    method: 'POST',
  });
}

export async function fetchTimeSlots(date) {
  const params = date ? `?date=${date}` : '';
  return apiFetch(`/timeslots${params}`);
}

export async function fetchAdminDashboard() {
  return apiFetch('/api/admin/dashboard');
}

export async function fetchCurrentUser() {
  return apiFetch('/auth/me');
}

// ========== CLIENT RESERVATIONS ==========
export async function fetchUpcomingReservations() {
  return apiFetch('/reservations/upcoming');
}

export async function fetchReservationHistory() {
  return apiFetch('/reservations/history');
}

export async function fetchWaitingList() {
  return apiFetch('/reservations/waiting');
}

export async function rescheduleReservation(id, newSlotId) {
  return apiFetch(`/reservations/${id}/reschedule`, {
    method: 'PUT',
    body: JSON.stringify({ newSlotId }),
  });
}

// ========== SLOTS / CALENDAR ==========
export async function fetchAvailableSlots(date) {
  return apiFetch(`/api/slots/available?date=${date}`);
}

export async function fetchAllSlots(date) {
  return apiFetch(`/api/slots/all?date=${date}`);
}

export async function fetchCalendarAvailability(month) {
  return apiFetch(`/api/slots/calendar?month=${month}`);
}

// ========== ADMIN DASHBOARD ==========
export async function fetchAdminReservations(params = {}) {
  const query = new URLSearchParams(params).toString();
  return apiFetch(`/api/admin/reservations?${query}`);
}

// ========== ADMIN WAITING LIST ==========
export async function fetchAdminWaitingList() {
  return apiFetch('/api/admin/waiting-list');
}

export async function promoteWaitingListEntry(id) {
  return apiFetch(`/api/admin/waiting-list/${id}/promote`, {
    method: 'POST',
  });
}

export async function removeWaitingListEntry(id) {
  return apiFetch(`/api/admin/waiting-list/${id}`, {
    method: 'DELETE',
  });
}

// ========== ADMIN USERS ==========
export async function fetchAdminUsers(page = 0, size = 20) {
  return apiFetch(`/api/admin/users?page=${page}&size=${size}`);
}

export async function disableUser(id) {
  return apiFetch(`/api/admin/users/${id}/disable`, {
    method: 'PUT',
  });
}

export async function enableUser(id) {
  return apiFetch(`/api/admin/users/${id}/enable`, {
    method: 'PUT',
  });
}

export async function deleteUser(id) {
  return apiFetch(`/api/admin/users/${id}`, {
    method: 'DELETE',
  });
}

// ========== ADMIN CALENDAR / SLOTS ==========
export async function generateSlots(date) {
  return apiFetch(`/api/admin/slots/generate?date=${date}`);
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

export async function blockDate(date, reason) {
  return apiFetch('/api/admin/blocked-dates', {
    method: 'POST',
    body: JSON.stringify({ date, reason }),
  });
}

export async function unblockDate(date) {
  return apiFetch(`/api/admin/blocked-dates/${date}`, {
    method: 'DELETE',
  });
}

export async function fetchBlockedDates(month) {
  const query = month ? `?month=${month}` : '';
  return apiFetch(`/api/admin/blocked-dates${query}`);
}

// ========== PDF DOWNLOAD ==========
export async function downloadAppointmentPdf(id) {
  const token = getToken();
  
  const res = await fetch(`${API_BASE}/reservations/${id}/pdf`, {
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  });

  if (res.status === 401 || res.status === 403) {
    window.dispatchEvent(new CustomEvent('auth:logout'));
    throw new Error(`Auth error ${res.status} on /reservations/${id}/pdf`);
  }

  if (!res.ok) {
    let errorMessage = `Error ${res.status}`;
    try {
      const errorData = await res.json();
      errorMessage = errorData.message || errorData.error || errorMessage;
    } catch {
      errorMessage = await res.text() || errorMessage;
    }
    throw new Error(errorMessage);
  }

  // Get filename from headers or create default
  const disposition = res.headers.get('Content-Disposition');
  let filename = `appointment_${id}.pdf`;
  if (disposition && disposition.includes('filename=')) {
    const filenameMatch = disposition.match(/filename="?([^"]+)"?/);
    if (filenameMatch) {
      filename = filenameMatch[1];
    }
  }

  // Convert response to blob and create download link
  const blob = await res.blob();
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
  
  return { success: true, filename };
}
