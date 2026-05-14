import React, { useEffect, useState } from 'react';
import {
  fetchCalendarAvailability,
  fetchBlockedDates,
  blockDate,
  unblockDate,
  generateSlots,
  fetchWorkingHours,
  saveWorkingHours,
} from '../api/api';
import { useToast } from '../context/ToastContext';

function getMonthYearKey(date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
}

function getDaysInMonth(date) {
  const year = date.getFullYear();
  const month = date.getMonth();
  const firstDay = new Date(year, month, 1);
  const lastDay = new Date(year, month + 1, 0);
  const days = [];
  for (let i = 0; i < firstDay.getDay(); i++) days.push(null);
  for (let i = 1; i <= lastDay.getDate(); i++) days.push(i);
  return days;
}

export default function AdminCalendar() {
  const [currentMonth, setCurrentMonth] = useState(new Date());
  const [availability, setAvailability] = useState({});
  const [blockedDates, setBlockedDates] = useState([]);
  const [workingHours, setWorkingHours] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [selectedDate, setSelectedDate] = useState('');
  const [blockReason, setBlockReason] = useState('');
  const [showWorkingModal, setShowWorkingModal] = useState(false);
  const [actionLoading, setActionLoading] = useState('');
  const { addToast } = useToast();

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const monthKey = getMonthYearKey(currentMonth);
      // Only fetch calendar data, cache working hours since they rarely change
      const [avail, blocked] = await Promise.all([
        fetchCalendarAvailability(monthKey),
        fetchBlockedDates(monthKey),
      ]);
      setAvailability(avail || {});
      setBlockedDates(blocked || []);
      
      // Only fetch working hours if not already loaded
      if (workingHours.length === 0) {
        const hours = await fetchWorkingHours();
        setWorkingHours(hours || []);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, [currentMonth]);

  const handlePrev = () => {
    const d = new Date(currentMonth);
    d.setMonth(d.getMonth() - 1);
    setCurrentMonth(d);
  };

  const handleNext = () => {
    const d = new Date(currentMonth);
    d.setMonth(d.getMonth() + 1);
    setCurrentMonth(d);
  };

  const dayKey = (day) => {
    const y = currentMonth.getFullYear();
    const m = String(currentMonth.getMonth() + 1).padStart(2, '0');
    const d = String(day).padStart(2, '0');
    return `${y}-${m}-${d}`;
  };

  const getDayClass = (day) => {
    if (!day) return 'empty';
    const key = dayKey(day);
    const status = availability[key];
    if (blockedDates.some((b) => b.date === key)) return 'blocked';
    if (status === 'OPEN') return 'open';
    if (status === 'FULL') return 'full';
    return 'no-slots';
  };

  const handleBlock = async () => {
    if (!selectedDate || !blockReason) return;
    setActionLoading('block');
    try {
      await blockDate(selectedDate, blockReason);
      addToast('Date blocked', 'success');
      setSelectedDate('');
      setBlockReason('');
      // Only refresh calendar data, not working hours
      const monthKey = getMonthYearKey(currentMonth);
      const [avail, blocked] = await Promise.all([
        fetchCalendarAvailability(monthKey),
        fetchBlockedDates(monthKey),
      ]);
      setAvailability(avail || {});
      setBlockedDates(blocked || []);
    } catch (err) {
      addToast(err.message, 'error');
    } finally {
      setActionLoading('');
    }
  };

  const handleUnblock = async (date) => {
    setActionLoading('unblock-' + date);
    try {
      await unblockDate(date);
      addToast('Date unblocked', 'success');
      // Only refresh calendar data, not working hours
      const monthKey = getMonthYearKey(currentMonth);
      const [avail, blocked] = await Promise.all([
        fetchCalendarAvailability(monthKey),
        fetchBlockedDates(monthKey),
      ]);
      setAvailability(avail || {});
      setBlockedDates(blocked || []);
    } catch (err) {
      addToast(err.message, 'error');
    } finally {
      setActionLoading('');
    }
  };

  const handleGenerateSlots = async () => {
    if (!selectedDate) return;
    setActionLoading('generate');
    try {
      await generateSlots(selectedDate);
      addToast('Slots generated', 'success');
      // Only refresh calendar availability, not blocked dates or working hours
      const monthKey = getMonthYearKey(currentMonth);
      const avail = await fetchCalendarAvailability(monthKey);
      setAvailability(avail || {});
    } catch (err) {
      addToast(err.message, 'error');
    } finally {
      setActionLoading('');
    }
  };

  const handleSaveHours = async (config) => {
    try {
      await saveWorkingHours(config);
      addToast('Working hours saved', 'success');
      setShowWorkingModal(false);
      // Only refresh working hours, not calendar data
      const hours = await fetchWorkingHours();
      setWorkingHours(hours || []);
    } catch (err) {
      addToast(err.message, 'error');
    }
  };

  const days = getDaysInMonth(currentMonth);
  const monthNames = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];

  return (
    <div className="page fade-in">
      <h1 className="page-title">Calendar Management</h1>

      <div className="filter-bar">
        <button className="btn-primary" onClick={() => setShowWorkingModal(true)}>
          Working Hours
        </button>
        <button className="btn-primary" onClick={handleGenerateSlots} disabled={actionLoading === 'generate'}>
          {actionLoading === 'generate' ? 'Generating...' : 'Generate Slots'}
        </button>
        <input
          type="date"
          value={selectedDate}
          onChange={(e) => setSelectedDate(e.target.value)}
          placeholder="Select date"
        />
        <input
          type="text"
          value={blockReason}
          onChange={(e) => setBlockReason(e.target.value)}
          placeholder="Block reason"
        />
        <button className="btn-danger" onClick={handleBlock} disabled={!selectedDate || !blockReason || actionLoading === 'block'}>
          {actionLoading === 'block' ? 'Blocking...' : 'Block Date'}
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="calendar-nav">
        <button onClick={handlePrev}>←</button>
        <h2>{monthNames[currentMonth.getMonth()]} {currentMonth.getFullYear()}</h2>
        <button onClick={handleNext}>→</button>
      </div>

      {loading && <div className="skeleton" style={{ height: '300px' }} />}

      {!loading && (
        <div className="calendar-grid">
          {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map((d) => (
            <div key={d} className="calendar-day-header">{d}</div>
          ))}
          {days.map((day, i) => (
            <div
              key={i}
              className={`calendar-day ${getDayClass(day)} ${selectedDate === dayKey(day) ? 'selected' : ''}`}
              onClick={() => {
                if (day) setSelectedDate(dayKey(day));
              }}
            >
              {day || ''}
            </div>
          ))}
        </div>
      )}

      {!loading && blockedDates.length > 0 && (
        <div className="card-section" style={{ marginTop: '1.5rem' }}>
          <h3>Blocked Dates</h3>
          <div className="appointments-grid">
            {blockedDates.map((b) => (
              <div key={b.id} className="appointment-card muted">
                <div className="appointment-header">
                  <span>{b.date} — {b.reason}</span>
                </div>
                <button className="btn-primary" onClick={() => handleUnblock(b.date)} disabled={actionLoading === 'unblock-' + b.date}>
                  {actionLoading === 'unblock-' + b.date ? 'Unblocking...' : 'Unblock'}
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {showWorkingModal && (
        <div className="modal-overlay" onClick={(e) => { if (e.target === e.currentTarget) setShowWorkingModal(false); }}>
          <div className="modal-card" style={{ maxWidth: '600px' }}>
            <div className="modal-header">
              <h3>Working Hours</h3>
              <button className="modal-close" onClick={() => setShowWorkingModal(false)}>×</button>
            </div>
            <WorkingHoursEditor hours={workingHours} onSave={handleSaveHours} />
          </div>
        </div>
      )}
    </div>
  );
}

function WorkingHoursEditor({ hours, onSave }) {
  const days = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
  const [configs, setConfigs] = useState(
    days.map((day) => {
      const existing = hours.find((h) => h.dayOfWeek === day);
      return existing || { dayOfWeek: day, startTime: '09:00', endTime: '17:00', slotDurationMinutes: 30 };
    })
  );

  const update = (index, field, value) => {
    const next = [...configs];
    next[index] = { ...next[index], [field]: value };
    setConfigs(next);
  };

  const saveAll = () => {
    configs.forEach((c) => onSave(c));
  };

  return (
    <div>
      {configs.map((c, i) => (
        <div key={c.dayOfWeek} className="form-row" style={{ gap: '0.5rem', marginBottom: '0.5rem' }}>
          <span style={{ minWidth: '90px', fontWeight: 600 }}>{c.dayOfWeek}</span>
          <input type="time" value={c.startTime} onChange={(e) => update(i, 'startTime', e.target.value)} />
          <input type="time" value={c.endTime} onChange={(e) => update(i, 'endTime', e.target.value)} />
          <input
            type="number"
            value={c.slotDurationMinutes}
            onChange={(e) => update(i, 'slotDurationMinutes', parseInt(e.target.value) || 30)}
            style={{ width: '80px' }}
            placeholder="Min"
          />
        </div>
      ))}
      <button className="btn-primary" onClick={saveAll} style={{ marginTop: '1rem' }}>
        Save All
      </button>
    </div>
  );
}
