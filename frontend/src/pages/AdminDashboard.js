import React, { useEffect, useState } from 'react';
import { fetchAdminDashboard } from '../api/api';

function StatCard({ value, label, variant }) {
  return (
    <div className="stat-card">
      <span className={`stat-value ${variant || ''}`}>{value}</span>
      <span className="stat-label">{label}</span>
    </div>
  );
}

function StatusBadge({ status }) {
  const classes = {
    CONFIRMED: 'badge-confirmed',
    CANCELLED: 'badge-cancelled',
    WAITING: 'badge-waiting',
  };
  return <span className={`status-badge ${classes[status] || ''}`}>{status}</span>;
}

export default function AdminDashboard() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const res = await fetchAdminDashboard();
        setData(res);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const stats = data || {};

  return (
    <div className="page fade-in">
      <h1 className="page-title">Admin Dashboard</h1>

      {loading && (
        <div className="stats-grid">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="stat-card skeleton" style={{ height: '100px' }} />
          ))}
        </div>
      )}
      {error && <div className="error-message">{error}</div>}

      {data && (
        <>
          <div className="stats-grid">
            <StatCard value={stats.totalUsers || 0} label="Total Users" />
            <StatCard value={stats.totalReservations || 0} label="Total Reservations" />
            <StatCard value={stats.confirmedToday || 0} label="Confirmed Today" variant="confirmed" />
            <StatCard value={stats.cancelledToday || 0} label="Cancelled Today" variant="cancelled" />
            <StatCard value={stats.waitingListCount || 0} label="Waiting List" variant="waiting" />
            <StatCard value={(stats.blockedDatesThisMonth || []).length} label="Blocked Dates This Month" />
          </div>

          <div className="card-section">
            <h2>Upcoming Appointments</h2>
            {(stats.upcomingAppointments || []).length === 0 ? (
              <p className="empty-hint">No upcoming appointments.</p>
            ) : (
              <table className="admin-table">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Time</th>
                    <th>User</th>
                  </tr>
                </thead>
                <tbody>
                  {(stats.upcomingAppointments || []).slice(0, 10).map((r) => (
                    <tr key={r.id}>
                      <td>{r.date}</td>
                      <td>{r.heure}</td>
                      <td>{r.user?.name || r.user?.email || '-'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>

          <div className="card-section">
            <h2>Recent Cancellations</h2>
            {(stats.recentCancellations || []).length === 0 ? (
              <p className="empty-hint">No recent cancellations.</p>
            ) : (
              <table className="admin-table">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Time</th>
                    <th>Status</th>
                    <th>User</th>
                  </tr>
                </thead>
                <tbody>
                  {(stats.recentCancellations || []).slice(0, 10).map((r) => (
                    <tr key={r.id}>
                      <td>{r.date}</td>
                      <td>{r.heure}</td>
                      <td><StatusBadge status={r.statut} /></td>
                      <td>{r.user?.name || r.user?.email || '-'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </>
      )}
    </div>
  );
}
