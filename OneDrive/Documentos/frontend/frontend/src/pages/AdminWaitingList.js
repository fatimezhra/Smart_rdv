import React, { useEffect, useState } from 'react';
import { fetchAdminWaitingList, promoteWaitingListEntry, removeWaitingListEntry } from '../api/api';
import { useToast } from '../context/ToastContext';

export default function AdminWaitingList() {
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { addToast } = useToast();

  const load = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await fetchAdminWaitingList();
      setList(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const handlePromote = async (id) => {
    if (!window.confirm('Promote this entry to a confirmed appointment?')) return;
    try {
      await promoteWaitingListEntry(id);
      addToast('Entry promoted successfully', 'success');
      load();
    } catch (err) {
      addToast(err.message, 'error');
    }
  };

  const handleRemove = async (id) => {
    if (!window.confirm('Remove this entry from the waiting list?')) return;
    try {
      await removeWaitingListEntry(id);
      addToast('Entry removed', 'success');
      load();
    } catch (err) {
      addToast(err.message, 'error');
    }
  };

  return (
    <div className="page fade-in">
      <h1 className="page-title">Waiting List</h1>

      {error && <div className="error-message">{error}</div>}

      {loading && (
        <div>
          <div className="skeleton skeleton-table-row" />
          <div className="skeleton skeleton-table-row" />
          <div className="skeleton skeleton-table-row" />
        </div>
      )}

      {!loading && list.length === 0 && (
        <div className="empty-state">Waiting list is empty.</div>
      )}

      {!loading && list.length > 0 && (
        <table className="admin-table">
          <thead>
            <tr>
              <th>Date</th>
              <th>User</th>
              <th>Position</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {list.map((entry) => (
              <tr key={entry.id}>
                <td>{entry.date}</td>
                <td>{entry.user ? entry.user.name : '-'}</td>
                <td>{entry.position}</td>
                <td>
                  <button className="btn-primary" onClick={() => handlePromote(entry.id)}>
                    Promote
                  </button>
                  <button className="btn-danger" onClick={() => handleRemove(entry.id)}>
                    Remove
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
