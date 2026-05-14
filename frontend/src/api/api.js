// ===================== CONFIGURATION =====================
const API_BASE = 'http://localhost:8081';

export async function apiFetch(path, options = {}) {
  const token = localStorage.getItem('token');

  const headers = {
    ...(options.body ? { 'Content-Type': 'application/json' } : {}),
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  console.log(`[API] ${options.method || 'GET'} ${API_BASE}${path}`);

  const res = await fetch(`${API_BASE}${path}`, { 
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

// Fonction critique pour Upcoming
export async function fetchUpcomingReservations() {
  return apiFetch('/reservations/upcoming');
}
