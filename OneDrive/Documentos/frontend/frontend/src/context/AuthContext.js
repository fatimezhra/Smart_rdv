import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext(null);

function decodeJwtPayload(token) {
  try {
    const base64Payload = token.split('.')[1];
    const payload = atob(base64Payload.replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(payload);
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => {
    const saved = localStorage.getItem('token');
    if (saved && !decodeJwtPayload(saved)) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      return null;
    }
    return saved;
  });
  const [user, setUser] = useState(() => {
    try {
      const saved = localStorage.getItem('user');
      return saved ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  });

  useEffect(() => {
    if (token) {
      const payload = decodeJwtPayload(token);
      if (!payload) {
        logout();
        return;
      }
      setUser(payload);
    } else {
      setUser(null);
    }
  }, [token]);

  useEffect(() => {
    const handler = () => {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      setToken(null);
      setUser(null);
    };
    window.addEventListener('auth:logout', handler);
    return () => window.removeEventListener('auth:logout', handler);
  }, []);

  const login = (newToken) => {
    localStorage.setItem('token', newToken);
    setToken(newToken);
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
  };

  const isAuthenticated = !!token;
  const role = user?.role || null;
  const isAdmin = role === 'ADMIN';

  return (
    <AuthContext.Provider value={{ token, user, role, isAdmin, isAuthenticated, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
