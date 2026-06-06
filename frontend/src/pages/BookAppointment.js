import React, { useEffect, useState } from 'react';
import { fetchCalendarAvailability, fetchAvailableSlots, fetchAllSlots, bookSlot, joinWaitingList } from '../api/api';
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

export default function BookAppointment() {
  const [currentMonth, setCurrentMonth] = useState(new Date());
  const [selectedDate, setSelectedDate] = useState('');
  const [availability, setAvailability] = useState({});
  const [slots, setSlots] = useState([]);
  const [alternatives, setAlternatives] = useState([]);
  const [waitingPosition, setWaitingPosition] = useState(null);
  const [bookingLoading, setBookingLoading] = useState(null);
  const [bookingSuccess, setBookingSuccess] = useState(null);
  const [bookingError, setBookingError] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [smartSuggestions, setSmartSuggestions] = useState([]);
  const [loadingSuggestions, setLoadingSuggestions] = useState(false);
  const [allSlots, setAllSlots] = useState([]);
  const [takenSlotSuggestions, setTakenSlotSuggestions] = useState([]);
  const [showTakenSlotSuggestions, setShowTakenSlotSuggestions] = useState(false);
  const [loadingTakenSuggestions, setLoadingTakenSuggestions] = useState(false);
  const [blockedDateReasons, setBlockedDateReasons] = useState({});
  const [calendarLoading, setCalendarLoading] = useState(false);
  const { addToast } = useToast();

  const loadCalendar = async () => {
    setCalendarLoading(true);
    try {
      const data = await fetchCalendarAvailability(getMonthYearKey(currentMonth));
      setAvailability(data || {});
      
      // Parse blocked date reasons
      const reasons = {};
      Object.entries(data || {}).forEach(([date, status]) => {
        if (status.startsWith('BLOCKED:')) {
          const reason = status.substring(8); // Remove 'BLOCKED:' prefix
          reasons[date] = reason;
        }
      });
      setBlockedDateReasons(reasons);
    } catch (err) {
      console.error('Error loading calendar:', err);
      addToast('Failed to load calendar availability', 'error');
    } finally {
      setCalendarLoading(false);
    }
  };

  useEffect(() => {
    loadCalendar();
  }, [currentMonth]);

  const selectDate = async (dateStr) => {
    setSelectedDate(dateStr);
    setError('');
    setAlternatives([]);
    setWaitingPosition(null);
    setBookingSuccess(null);
    setBookingError(null);
    setSmartSuggestions([]);
    setTakenSlotSuggestions([]);
    setShowTakenSlotSuggestions(false);
    setLoading(true);
    try {
      // Fetch both available and all slots
      const [availableData, allData] = await Promise.all([
        fetchAvailableSlots(dateStr),
        fetchAllSlots(dateStr)
      ]);
      
      setSlots(Array.isArray(availableData) ? availableData : []);
      setAllSlots(Array.isArray(allData) ? allData : []);
      
      // If no slots available, don't show smart suggestions (removed other days suggestions)
    } catch (err) {
      setError(err.message);
      setSlots([]);
      setAllSlots([]);
    } finally {
      setLoading(false);
    }
  };

  const handleBook = async (slotId) => {
    try {
      setBookingLoading(slotId);
      const result = await bookSlot(slotId);

      if (result.type === 'CONFIRMED' || result.statut === 'CONFIRMED') {
        setBookingSuccess('Your appointment is confirmed!');
        setAlternatives([]);
        setWaitingPosition(null);
        setBookingError(null);
        addToast('Appointment confirmed', 'success');
        if (selectedDate) selectDate(selectedDate); // refresh calendar
      }
      else if (result.type === 'ALTERNATIVES' || result.alternatives) {
        setAlternatives(result.alternatives);
        setBookingSuccess(null);
        setWaitingPosition(null);
        setBookingError('This slot is already taken. Here are the 3 nearest available times:');
      }
      else if (result.type === 'WAITING_LIST' || result.position) {
        setWaitingPosition(result.position);
        setAlternatives([]);
        setBookingSuccess(`The day is fully booked. You are on the waiting list - position ${result.position}.`);
        setBookingError(null);
        addToast('Added to waiting list', 'success');
      }
    } catch (err) {
      setBookingError(err.message);
      setBookingSuccess(null);
      addToast(err.message, 'error');
    } finally {
      setBookingLoading(null);
    }
  };

  const findSmartSuggestions = async (selectedDateStr) => {
    // Function disabled - no longer suggesting other days
    return [];
  };

  const formatDate = (date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  };

  const handleTakenSlotClick = async (takenSlot) => {
    setLoadingTakenSuggestions(true);
    setShowTakenSlotSuggestions(true);
    try {
      // Find 3 nearest available slots to the taken slot's time
      const suggestions = await findNearestTimeSuggestions(takenSlot);
      setTakenSlotSuggestions(suggestions);
    } catch (err) {
      console.error('Error finding time suggestions:', err);
    } finally {
      setLoadingTakenSuggestions(false);
    }
  };

  const findNearestTimeSuggestions = async (takenSlot) => {
    const suggestions = [];
    const takenTime = takenSlot.heure;
    const [takenHour, takenMinute] = takenTime.split(':').map(Number);
    const takenMinutes = takenHour * 60 + takenMinute;
    
    // Search for available slots on the same day first
    const availableSlots = allSlots.filter(slot => slot.disponible);
    
    // Calculate time differences and sort
    const slotsWithDiff = availableSlots.map(slot => {
      const [hour, minute] = slot.heure.split(':').map(Number);
      const slotMinutes = hour * 60 + minute;
      const diff = Math.abs(slotMinutes - takenMinutes);
      return { ...slot, diff };
    }).sort((a, b) => a.diff - b.diff);
    
    // Return top 3 closest slots
    return slotsWithDiff.slice(0, 3);
  };

  const handleJoinWaitingList = async () => {
    if (!selectedDate) return;
    try {
      const result = await joinWaitingList(selectedDate);
      setWaitingPosition(result.position);
      setBookingSuccess(`You are on the waiting list - position ${result.position}.`);
      addToast('Added to waiting list', 'success');
    } catch (err) {
      console.error('Error joining waiting list:', err);
      addToast(err.message, 'error');
    }
  };

  const handleSuggestionClick = (dateStr) => {
    // Update calendar to show the suggested date
    const suggestedDate = new Date(dateStr);
    setCurrentMonth(suggestedDate);
    setSelectedDate(dateStr);
    selectDate(dateStr);
  };

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
    if (status && status.startsWith('BLOCKED')) return 'blocked';
    if (status === 'OPEN') return 'open';
    if (status === 'FULL') return 'full';
    if (status === 'NON_WORKING') return 'non-working';
    return 'no-slots';
  };

  const monthNames = ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'];
  const days = getDaysInMonth(currentMonth);

  return (
    <div className="page fade-in">
      <h1 className="page-title">Book an Appointment</h1>

      {/* Side-by-side grid layout */}
      <div className="booking-grid">
        {/* Calendar Column */}
        <div className="calendar-column">
          <div className="card-section">
            <div className="calendar-nav">
              <button onClick={handlePrev}>←</button>
              <h2>{monthNames[currentMonth.getMonth()]} {currentMonth.getFullYear()}</h2>
              <button onClick={handleNext}>→</button>
            </div>
            {calendarLoading ? (
              <div className="loading">Loading calendar...</div>
            ) : (
              <div className="calendar-grid">
              {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map((d) => (
                <div key={d} className="calendar-day-header">{d}</div>
              ))}
              {days.map((day, i) => {
                const dayClass = getDayClass(day);
                const isBlocked = dayClass === 'blocked';
                const dateKey = dayKey(day);
                const blockedReason = isBlocked ? blockedDateReasons[dateKey] : null;
                
                return (
                  <div
                    key={i}
                    className={`calendar-day ${dayClass} ${selectedDate === dateKey ? 'selected' : ''}`}
                    onClick={() => {
                      if (day && !isBlocked) selectDate(dateKey);
                    }}
                    title={blockedReason || ''}
                  >
                    {day ? (isBlocked ? 'Blocked' : day) : ''}
                    {isBlocked && blockedReason && (
                      <div className="blocked-reason">
                        {blockedReason.length > 10 ? blockedReason.substring(0, 10) + '...' : blockedReason}
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
            )}
          </div>
        </div>

        {/* Slots Panel Column */}
        <div className="slots-column">
          {selectedDate && (
            <div className="card-section">
              <h2>Slots for {selectedDate}</h2>
              {error && <div className="error-message">{error}</div>}
              {bookingSuccess && <div className="success-message">{bookingSuccess}</div>}

              {loading && (
                <div>
                  <div className="skeleton skeleton-card" />
                  <div className="skeleton skeleton-card" />
                </div>
              )}

              {!loading && allSlots.length > 0 && (
                <div className="slots-grid">
                  {allSlots.map((slot) => (
                    <div 
                      key={slot.id} 
                      className={`slot-card ${!slot.disponible ? 'taken-slot' : ''}`}
                    >
                      <span className="slot-time">{slot.heure}</span>
                      {!slot.disponible ? (
                        <button
                          className="btn-taken"
                          onClick={() => handleTakenSlotClick(slot)}
                          disabled={loadingTakenSuggestions}
                        >
                          {loadingTakenSuggestions ? 'Finding...' : 'Taken'}
                        </button>
                      ) : (
                        <button
                          className="btn-primary"
                          onClick={() => handleBook(slot.id)}
                          disabled={bookingLoading === slot.id}
                        >
                          {bookingLoading === slot.id ? 'Booking...' : 'Book'}
                        </button>
                      )}
                    </div>
                  ))}
                </div>
              )}

              {!loading && allSlots.length === 0 && !waitingPosition && !bookingSuccess && !bookingError && (
                <div className="empty-state">
                  <p>No slots available for this date.</p>
                  <button
                    className="btn-primary"
                    onClick={handleJoinWaitingList}
                    disabled={bookingLoading}
                  >
                    Join Waiting List
                  </button>
                </div>
              )}

              {/* Taken Slot Suggestions */}
              {showTakenSlotSuggestions && (
                <div className="suggestion-card">
                  <h3 className="suggestion-title">
                    ⚠️ Taken Slot - Nearest Available Times
                  </h3>
                  {loadingTakenSuggestions ? (
                    <div className="loading">Finding nearby slots...</div>
                  ) : (
                    <div className="suggestion-list">
                      {takenSlotSuggestions.map(slot => (
                        <div key={slot.id} className="suggestion-item">
                          <span className="suggestion-slot-time">
                            {slot.heure?.substring(0, 5)}
                          </span>
                          <button
                            onClick={() => handleBook(slot.id)}
                            disabled={bookingLoading === slot.id}
                            className="btn-primary"
                          >
                            {bookingLoading === slot.id ? 'Booking...' : 'Book'}
                          </button>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              {/* Regular alternatives (when a specific slot is taken) */}
              {alternatives.length > 0 && (
                <div className="suggestion-card">
                  <h3 className="suggestion-title">
                    ⚠️ This slot is taken - Available alternatives:
                  </h3>
                  <div className="suggestion-list">
                    {alternatives.map(slot => (
                      <div key={slot.id} className="suggestion-item">
                        <span className="suggestion-slot-time">
                          {slot.heure?.substring(0, 5)}
                        </span>
                        <button
                          onClick={() => handleBook(slot.id)}
                          disabled={bookingLoading === slot.id}
                          className="btn-primary"
                        >
                          {bookingLoading === slot.id ? 'Booking...' : 'Book'}
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Waiting list message */}
              {waitingPosition && (
                <div className="waiting-list-card">
                  <h3 className="waiting-title">
                    📋 Day Fully Booked - You're on the Waiting List
                  </h3>
                  <div className="waiting-position">
                    Position #{waitingPosition}
                  </div>
                  <p className="waiting-description">
                    You'll be automatically promoted if a cancellation occurs.
                  </p>
                </div>
              )}

              {/* Join Waiting List Button for fully booked days */}
              {!loading && allSlots.length > 0 && allSlots.every(slot => !slot.disponible) && !waitingPosition && (
                <div className="waiting-list-card">
                  <h3 className="waiting-title">
                    📋 All Slots Taken - Join Waiting List
                  </h3>
                  <p className="waiting-description">
                    Join the waiting list to automatically get a slot when someone cancels.
                  </p>
                  <button
                    className="btn-primary"
                    onClick={handleJoinWaitingList}
                    disabled={bookingLoading}
                  >
                    {bookingLoading ? 'Joining...' : 'Join Waiting List'}
                  </button>
                </div>
              )}

              {/* Success/Error messages */}
              {bookingSuccess && (
                <div className="success-message">
                  ✅ {bookingSuccess}
                </div>
              )}

              {bookingError && (
                <div className="error-message">
                  ⚠️ {bookingError}
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
