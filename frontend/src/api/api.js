// API_BASE n'est plus nécessaire grâce au proxy
export async function apiFetch(path, options = {}) {
  const token = localStorage.getItem('token');

  const headers = {
    ...(options.body ? { 'Content-Type': 'application/json' } : {}),
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  console.log(`[API] ${options.method || 'GET'} ${path}`);

  const res = await fetch(path, { 
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

export async function fetchUpcomingReservations() {
  return apiFetch('/reservations/upcoming');
}

export async function fetchAdminReservations() {
  return apiFetch('/api/admin/reservations');
}

export async function cancelReservation(id) {
  return apiFetch(/api/reservations/ + id + /cancel, { method: 'PUT' });
}
