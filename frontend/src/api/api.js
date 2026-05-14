// ===================== CONFIGURATION API =====================
const API_BASE = 'http://localhost:8081';

console.log("🚀 API_BASE configuré sur :", API_BASE);

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
    headers,
  });

  console.log(`[API Response] ${res.status} ${path}`);

  if (!res.ok) {
    console.error(`❌ Erreur ${res.status} sur ${path}`);
    throw new Error(`Erreur ${res.status}`);
  }

  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

// ===================== FONCTIONS IMPORTANTES =====================
export async function fetchUpcomingReservations() {
  return apiFetch('/reservations/upcoming');
}

export async function fetchReservations() {
  return apiFetch('/reservations');
}
