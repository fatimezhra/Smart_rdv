import React, { useEffect, useState } from 'react';
import { fetchAdminUsers, enableUser, disableUser, deleteUser } from '../api/api';
import { useToast } from '../context/ToastContext';
import { createAdmin } from '../api/api';

export default function AdminUsers() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [showModal, setShowModal] = useState(false);
  const [newAdmin, setNewAdmin] = useState({ name: '', email: '', password: '' });
  const { addToast } = useToast();

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await fetchAdminUsers(page, 20);
      setUsers(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, [page]);

  const handleToggle = async (id, enabled) => {
    try {
      if (enabled) {
        await disableUser(id);
        addToast('User disabled', 'success');
      } else {
        await enableUser(id);
        addToast('User enabled', 'success');
      }
      load();
    } catch (err) {
      addToast(err.message, 'error');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this user permanently?')) return;
    try {
      await deleteUser(id);
      addToast('User deleted', 'success');
      load();
    } catch (err) {
      addToast(err.message, 'error');
    }
  };

  const handleCreateAdmin = async () => {
    try {
      await createAdmin(newAdmin);
      addToast('Admin created', 'success');
      setShowModal(false);
      setNewAdmin({ name: '', email: '', password: '' });
      load();
    } catch (err) {
      addToast(err.message, 'error');
    }
  };

  return (
    <div className="page fade-in">
      <h1 className="page-title">Users</h1>

      <div className="filter-bar">
        <button className="btn-primary" onClick={() => setShowModal(true)}>
          + Add Admin
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      {loading && (
        <div>
          <div className="skeleton skeleton-table-row" />
          <div className="skeleton skeleton-table-row" />
          <div className="skeleton skeleton-table-row" />
        </div>
      )}

      {!loading && users.length === 0 && (
        <div className="empty-state">No users found.</div>
      )}

      {!loading && users.length > 0 && (
        <table className="admin-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Role</th>
              <th>Reservations</th>
              <th>Enabled</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id}>
                <td>{u.name}</td>
                <td>{u.email}</td>
                <td>{u.role}</td>
                <td>{u.reservationCount}</td>
                <td>{u.enabled ? 'Yes' : 'No'}</td>
                <td>
                  <button
                    className={u.enabled ? 'btn-danger' : 'btn-primary'}
                    onClick={() => handleToggle(u.id, u.enabled)}
                  >
                    {u.enabled ? 'Disable' : 'Enable'}
                  </button>
                  <button className="btn-danger" onClick={() => handleDelete(u.id)}>
                    Delete
                  </button>
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
              className={page === i ? 'active' : ''}
              onClick={() => setPage(i)}
            >
              {i + 1}
            </button>
          ))}
        </div>
      )}

      {showModal && (
        <div className="modal-overlay" onClick={(e) => { if (e.target === e.currentTarget) setShowModal(false); }}>
          <div className="modal-card">
            <div className="modal-header">
              <h3>Create Admin</h3>
              <button className="modal-close" onClick={() => setShowModal(false)}>×</button>
            </div>
            <div className="form-group">
              <label>Name</label>
              <input value={newAdmin.name} onChange={(e) => setNewAdmin({ ...newAdmin, name: e.target.value })} />
            </div>
            <div className="form-group">
              <label>Email</label>
              <input type="email" value={newAdmin.email} onChange={(e) => setNewAdmin({ ...newAdmin, email: e.target.value })} />
            </div>
            <div className="form-group">
              <label>Password</label>
              <input type="password" value={newAdmin.password} onChange={(e) => setNewAdmin({ ...newAdmin, password: e.target.value })} />
            </div>
            <button className="btn-primary" onClick={handleCreateAdmin}>Create</button>
          </div>
        </div>
      )}
    </div>
  );
}
