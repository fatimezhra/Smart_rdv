import React, { useEffect, useState } from 'react';
import { fetchAdminReservations, cancelReservation } from '../api/api';
import { useToast } from '../context/ToastContext';

export default function AdminAppointments() {
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filters, setFilters] = useState({ date: '', status: '', page: 0 });
  const [totalPages, setTotalPages] = useState(0);
  const { addToast } = useToast();

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const params = {};
      if (filters.date) params.date = filters.date;
      if (filters.status) params.status = filters.status;
      params.page = filters.page;
      params.size = 20;
      const data = await fetchAdminReservations(params);
      setReservations(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, [filters]);

  const handleCancel = async (id) => {
    if (!window.confirm('Cancel this appointment?')) return;
    try {
      await cancelReservation(id);
      addToast('Appointment cancelled', 'success');
      load();
    } catch (err) {
      addToast(err.message, 'error');
    }
  };

  const statusBadge = (status) => {
    const map = {
      CONFIRMED: 'badge-confirmed',
      CANCELLED: 'badge-cancelled',
      WAITING: 'badge-waiting',
    };
    return <span className={`status-badge ${map[status] || ''}`}>{status}</span>;
  };

  return (
    <div className="page fade-in">
      <h1 className="page-title">Appointments</h1>

      <div className="filter-bar">
        <input
          type="date"
          value={filters.date}
          onChange={(e) => setFilters({ ...filters, date: e.target.value, page: 0 })}
        />
        <select
          value={filters.status}
          onChange={(e) => setFilters({ ...filters, status: e.target.value, page: 0 })}
        >
          <option value="">All Statuses</option>
          <option value="CONFIRMED">Confirmed</option>
          <option value="CANCELLED">Cancelled</option>
          <option value="WAITING">Waiting</option>
        </select>
      </div>

      {error && <div className="error-message">{error}</div>}

      {loading && (
        <div>
          <div className="skeleton skeleton-table-row" />
          <div className="skeleton skeleton-table-row" />
          <div className="skeleton skeleton-table-row" />
        </div>
      )}

      {!loading && reservations.length === 0 && (
        <div className="empty-state">No appointments found.</div>
      )}

      {!loading && reservations.length > 0 && (
        <table className="admin-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Date</th>
              <th>Time</th>
              <th>Status</th>
              <th>User</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {reservations.map((r) => (
              <tr key={r.id}>
                <td>{r.id}</td>
                <td>{r.date}</td>
                <td>{r.heure}</td>
                <td>{statusBadge(r.statut)}</td>
                <td>{r.user ? r.user.name : '-'}</td>
                <td>
                  {r.statut === 'CONFIRMED' && (
                    <button className="btn-danger" onClick={() => handleCancel(r.id)}>
                      Cancel
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {totalPages > 1 && (
        <div className="pagination">
          {Array.from({ length: totalPages }).map((_, i) => (
            <button
              key={i}
              className={filters.page === i ? 'active' : ''}
              onClick={() => setFilters({ ...filters, page: i })}
            >
              {i + 1}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
