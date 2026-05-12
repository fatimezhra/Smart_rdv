import React, { useEffect, useState } from 'react';
import {
  fetchUpcomingReservations,
  fetchReservationHistory,
  fetchWaitingList,
  cancelReservation,
  rescheduleReservation,
  fetchAvailableSlots,
} from '../api/api';
import { useToast } from '../context/ToastContext';

function StatusBadge({ status }) {
  const classes = {
    CONFIRMED: 'badge-confirmed',
    CANCELLED: 'badge-cancelled',
    WAITING: 'badge-waiting',
  };
  return <span className={`status-badge ${classes[status] || ''}`}>{status}</span>;
}

export default function ClientDashboard() {
  const [tab, setTab] = useState('upcoming');
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [rescheduleAppt, setRescheduleAppt] = useState(null);
  const [rescheduleDate, setRescheduleDate] = useState('');
  const [rescheduleSlots, setRescheduleSlots] = useState([]);
  const [rescheduleLoading, setRescheduleLoading] = useState(false);
  const { addToast } = useToast();

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const data =
        tab === 'upcoming'
          ? await fetchUpcomingReservations()
          : tab === 'waiting'
          ? await fetchWaitingList()
          : await fetchReservationHistory();
      setAppointments(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, [tab]);

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

  const handleLeaveWaitingList = async (id) => {
    if (!window.confirm('Êtes-vous sûr de vouloir quitter la liste d\'attente ?')) return;
    try {
      await cancelReservation(id); // Using same endpoint to remove from waiting list
      addToast('Vous avez quitté la liste d\'attente', 'success');
      load();
    } catch (err) {
      addToast(err.message, 'error');
    }
  };

  const openReschedule = (appt) => {
    setRescheduleAppt(appt);
    setRescheduleDate('');
    setRescheduleSlots([]);
  };

  const searchRescheduleSlots = async () => {
    if (!rescheduleDate) return;
    setRescheduleLoading(true);
    try {
      const data = await fetchAvailableSlots(rescheduleDate);
      setRescheduleSlots(Array.isArray(data) ? data : []);
    } catch (err) {
      addToast(err.message, 'error');
    } finally {
      setRescheduleLoading(false);
    }
  };

  const confirmReschedule = async (slotId) => {
    try {
      await rescheduleReservation(rescheduleAppt.id, slotId);
      addToast('Appointment rescheduled', 'success');
      setRescheduleAppt(null);
      load();
    } catch (err) {
      addToast(err.message, 'error');
    }
  };

  return (
    <div className="page fade-in">
      <h1 className="page-title">My Appointments</h1>

      <div className="tab-bar">
        <button className={`tab-btn ${tab === 'upcoming' ? 'active' : ''}`} onClick={() => setTab('upcoming')}>
          Upcoming
        </button>
        <button className={`tab-btn ${tab === 'waiting' ? 'active' : ''}`} onClick={() => setTab('waiting')}>
          Waiting List
        </button>
        <button className={`tab-btn ${tab === 'history' ? 'active' : ''}`} onClick={() => setTab('history')}>
          History
        </button>
      </div>

      {loading && (
        <div>
          <div className="skeleton skeleton-card" />
          <div className="skeleton skeleton-card" />
        </div>
      )}
      {error && <div className="error-message">{error}</div>}

      {!loading && appointments.length === 0 && (
        <div className="empty-state">
          <p>
            {tab === 'waiting' ? 'Aucune entrée dans la liste d\'attente.' :
             tab === 'upcoming' ? 'No appointments found.' :
             'No appointments in history.'}
          </p>
          {tab === 'upcoming' && <a href="/book" className="btn-primary">Book an appointment</a>}
        </div>
      )}

      {!loading && appointments.length > 0 && (
        <div className="appointments-grid">
          {appointments.map((a) => (
            <div key={a.id} className={`appointment-card ${a.statut === 'CANCELLED' ? 'muted' : ''}`}>
              <div className="appointment-header">
                <span className="appointment-date">
                  {tab === 'waiting' ? a.date : `${a.date} &bull; ${a.heure}`}
                </span>
                <StatusBadge status={a.statut} />
              </div>
              {tab === 'waiting' && (
                <div style={{ textAlign: 'center', marginTop: '0.75rem' }}>
                  <p style={{ fontSize: '18px', fontWeight: 'bold', color: '#1976D2', margin: '0 0 0.5rem 0' }}>
                    Position #{a.position}
                  </p>
                  <p style={{ fontSize: '14px', color: '#666', margin: '0 0 1rem 0' }}>
                    Vous serez automatiquement promu si une annulation survient.
                  </p>
                  <button className="btn-danger" onClick={() => handleLeaveWaitingList(a.id)}>
                    Quitter la liste d'attente
                  </button>
                </div>
              )}
              {tab === 'upcoming' && a.statut === 'CONFIRMED' && (
                <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.75rem' }}>
                  <button className="btn-primary" onClick={() => openReschedule(a)}>
                    Reschedule
                  </button>
                  <button className="btn-danger" onClick={() => handleCancel(a.id)}>
                    Cancel
                  </button>
                </div>
              )}
              {tab === 'upcoming' && a.statut === 'WAITING' && (
                <button className="btn-danger" onClick={() => handleCancel(a.id)}>
                  Leave Waitlist
                </button>
              )}
            </div>
          ))}
        </div>
      )}

      {rescheduleAppt && (
        <div className="modal-overlay" onClick={(e) => { if (e.target === e.currentTarget) setRescheduleAppt(null); }}>
          <div className="modal-card" style={{ maxWidth: '520px' }}>
            <div className="modal-header">
              <h3>Reschedule Appointment</h3>
              <button className="modal-close" onClick={() => setRescheduleAppt(null)}>×</button>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>New Date</label>
                <input
                  type="date"
                  value={rescheduleDate}
                  onChange={(e) => setRescheduleDate(e.target.value)}
                />
              </div>
              <button className="btn-primary" onClick={searchRescheduleSlots} disabled={!rescheduleDate || rescheduleLoading}>
                {rescheduleLoading ? 'Loading...' : 'Find Slots'}
              </button>
            </div>
            {rescheduleSlots.length > 0 && (
              <div className="slots-grid" style={{ marginTop: '1rem' }}>
                {rescheduleSlots.map((slot) => (
                  <div key={slot.id} className="slot-card">
                    <span className="slot-time">{slot.heure}</span>
                    <button className="btn-primary" onClick={() => confirmReschedule(slot.id)}>
                      Select
                    </button>
                  </div>
                ))}
              </div>
            )}
            {!rescheduleLoading && rescheduleDate && rescheduleSlots.length === 0 && (
              <p className="hint">No available slots for this date.</p>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
