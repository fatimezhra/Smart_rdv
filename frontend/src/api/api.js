const API_BASE = 'http://localhost:8081';   // ←←← C'est la correction principale

function getToken() {
  return localStorage.getItem('token');
}

export async function apiFetch(path, options = {}) {
  const token = getToken();

  const headers = {
    ...(options.body ? { 'Content-Type': 'application/json' } : {}),
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  console.log(`[API] ${options.method || 'GET'} ${API_BASE}${path} | Token present: ${!!token}`);

  const res = await fetch(`${API_BASE}${path}`, { 
    ...options, 
    headers,
    credentials: 'omit'   // Important avec CORS
  });

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

// ===================== AUTH =====================
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
  const user = { 
    email: payload.sub || payload.email, 
    role: payload.role ?? payload.roles?.[0] 
  };
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

// ===================== CLIENT =====================
export async function fetchReservations() {
  return apiFetch('/reservations');
}

export async function fetchUpcomingReservations() {
  return apiFetch('/reservations/upcoming');
}

export async function fetchReservationHistory() {
  return apiFetch('/reservations/history');
}

export async function fetchWaitingList() {
  return apiFetch('/reservations/waiting');
}

export async function bookSlot(slotId) {
  return apiFetch(`/reservations/${slotId}`, {
    method: 'POST',
  });
}

export async function cancelReservation(id) {
  return apiFetch(`/reservations/${id}`, {
    method: 'DELETE',
  });
}

export async function rescheduleReservation(id, newSlotId) {
  return apiFetch(`/reservations/${id}/reschedule`, {
    method: 'PUT',
    body: JSON.stringify({ newSlotId }),
  });
}

// ===================== SLOTS =====================
export async function fetchTimeSlots(date) {
  const params = date ? `?date=${date}` : '';
  return apiFetch(`/timeslots${params}`);
}

export async function fetchAvailableSlots(date) {
  return apiFetch(`/api/slots/available?date=${date}`);
}

export async function fetchCalendarAvailability(month) {
  return apiFetch(`/api/slots/calendar?month=${month}`);
}

// ===================== ADMIN =====================
export async function fetchAdminDashboard() {
  return apiFetch('/api/admin/dashboard');
}

export async function fetchAdminReservations(params = {}) {
  const query = new URLSearchParams(params).toString();
  return apiFetch(`/api/admin/reservations?${query}`);
}

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

// ===================== PDF =====================
export async function downloadAppointmentPdf(id) {
  const token = getToken();
  
  const res = await fetch(`${API_BASE}/reservations/${id}/pdf`, {
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  });

  if (res.status === 401 || res.status === 403) {
    window.dispatchEvent(new CustomEvent('auth:logout'));
    throw new Error(`Auth error ${res.status} on PDF`);
  }

  if (!res.ok) {
    const errorText = await res.text();
    throw new Error(errorText || `Error ${res.status}`);
  }

  const disposition = res.headers.get('Content-Disposition');
  let filename = `appointment_${id}.pdf`;
  
  if (disposition && disposition.includes('filename=')) {
    const match = disposition.match(/filename="?([^"]+)"?/);
    if (match) filename = match[1];
  }

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
