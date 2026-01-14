import { useState, useEffect } from 'react';
import { usersAPI } from '../api/api';
import './Admin.css';

export default function AdminUsers() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchUsers();
    }, []);

    const fetchUsers = async () => {
        try {
            const response = await usersAPI.getAll();
            setUsers(response.data || []);
        } catch (err) {
            console.error('Failed to fetch users', err);
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <div className="admin-content"><p>Loading users...</p></div>;

    return (
        <div className="admin-content">
            <h1>👥 User Management</h1>
            <p className="subtitle">{users.length} registered users</p>

            {users.length === 0 ? (
                <div className="empty-state">
                    <p>No users found.</p>
                </div>
            ) : (
                <div className="table-responsive">
                    <table className="admin-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Username</th>
                                <th>Email</th>
                                <th>Roles</th>
                            </tr>
                        </thead>
                        <tbody>
                            {users.map((user) => (
                                <tr key={user.userId}>
                                    <td>{user.userId}</td>
                                    <td><strong>{user.username}</strong></td>
                                    <td>{user.email}</td>
                                    <td>
                                        {user.roles?.map((role) => (
                                            <span key={role} className={`role-badge ${role.toLowerCase()}`}>
                                                {role.replace('ROLE_', '')}
                                            </span>
                                        ))}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}
