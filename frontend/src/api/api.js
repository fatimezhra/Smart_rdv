// ===================== CONFIGURATION =====================
const API_BASE = 'http://localhost:8081';   // Assure-toi que c'est bien ça

function getToken() {
  return localStorage.getItem('token');
}

// ===================== API FETCH =====================
export async function apiFetch(path, options = {}) {
  const token = getToken();

  const headers = {
    ...(options.body ? { 'Content-Type': 'application/json' } : {}),
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  console.log(`[API] ${options.method || 'GET'} ${API_BASE}${path}`);

  const res = await fetch(`${API_BASE}${path}`, { 
    ...options, 
    headers,
    credentials: 'omit'
  });

  if (res.status === 401 || res.status === 403) {
    window.dispatchEvent(new CustomEvent('auth:logout'));
    throw new Error(`Auth error ${res.status}`);
  }

  if (!res.ok) {
    let errorMsg = `Erreur ${res.status}`;
    try {
      const err = await res.json();
      errorMsg = err.message || errorMsg;
    } catch {}
    throw new Error(errorMsg);
  }

  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

// ===================== CLIENT =====================
export async function fetchUpcomingReservations() {
  return apiFetch('/reservations/upcoming');
}

// ... (tu peux garder le reste de tes fonctions)
