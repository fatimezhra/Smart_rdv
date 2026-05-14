// On force l'URL sur le port 8081 pour éviter les erreurs 404 sur le port 3001
const API_BASE = 'http://localhost:8081';
console.error("DEBUG: L'URL utilisée est bien :", API_BASE);
alert("Le fichier API est chargé avec le port : " + API_BASE);
function getToken() {
  return localStorage.getItem('token');
}

/**
 * Fonction générique pour appeler l'API
 */
export async function apiFetch(path, options = {}) {
  const token = getToken();

  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  // Log pour vérifier dans la console du navigateur
  console.log(`[API CALL] ${options.method || 'GET'} ${API_BASE}${path}`);

  const res = await fetch(`${API_BASE}${path}`, { ...options, headers });

  // Gestion des erreurs d'authentification (401 ou 403)
  if (res.status === 401 || res.status === 403) {
    console.warn("Erreur d'authentification détectée");
    window.dispatchEvent(new CustomEvent('auth:logout'));
    throw new Error(`Session expirée ou accès refusé (${res.status})`);
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

// ========== AUTHENTIFICATION ==========

export async function loginUser(email, password) {
  const res = await fetch(`${API_BASE}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  });

  if (!res.ok) throw new Error('Identifiants invalides');

  const data = await res.json();
  const jwt = data.token ?? data.jwt ?? data.accessToken;
  
  if (!jwt) throw new Error('Token non reçu du serveur');

  localStorage.setItem('token', jwt);

  // Extraction du payload du JWT pour le rôle
  const payload = JSON.parse(atob(jwt.split('.')[1]));
  const user = { 
    email: payload.sub, 
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

// ========== RÉSERVATIONS CLIENT ==========

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

// ========== SLOTS ET DISPONIBILITÉS ==========

export async function fetchAvailableSlots(date) {
  return apiFetch(`/api/slots/available?date=${date}`);
}

export async function fetchAllSlots(date) {
  return apiFetch(`/api/slots/all?date=${date}`);
}

export async function fetchCalendarAvailability(month) {
  return apiFetch(`/api/slots/calendar?month=${month}`);
}

// ========== ADMINISTRATION ==========

export async function fetchAdminReservations(params = {}) {
  const query = new URLSearchParams(params).toString();
  return apiFetch(`/api/admin/reservations?${query}`);
}

export async function fetchAdminUsers(page = 0, size = 20) {
  return apiFetch(`/api/admin/users?page=${page}&size=${size}`);
}

export async function generateSlots(date) {
  return apiFetch(`/api/admin/slots/generate?date=${date}`);
}

// ========== TÉLÉCHARGEMENT PDF ==========

export async function downloadAppointmentPdf(id) {
  const token = getToken();
  
  const res = await fetch(`${API_BASE}/reservations/${id}/pdf`, {
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  });

  if (!res.ok) throw new Error('Impossible de générer le PDF');

  const blob = await res.blob();
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `rendez_vous_${id}.pdf`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
}
