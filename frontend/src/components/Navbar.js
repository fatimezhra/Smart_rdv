import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Navbar() {
  const { isAuthenticated, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <Link to="/">SmartRDV</Link>
      </div>
      <div className="navbar-links">
        {isAuthenticated ? (
          <>
            <Link to="/dashboard">My Appointments</Link>
            <Link to="/book">Book</Link>
            {isAdmin && (
              <>
                <Link to="/admin">Dashboard</Link>
                <Link to="/admin/appointments">Appointments</Link>
                <Link to="/admin/waiting-list">Waiting List</Link>
                <Link to="/admin/users">Users</Link>
                <Link to="/admin/calendar">Calendar</Link>
              </>
            )}
            <button className="btn-link" onClick={handleLogout}>Logout</button>
          </>
        ) : (
          <Link to="/login">Login</Link>
        )}
      </div>
    </nav>
  );
}
