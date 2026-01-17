import { useState, useEffect } from 'react';
import { ordersAPI } from '../api/api';
import './Admin.css';

export default function AdminOrders() {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [updating, setUpdating] = useState(null);

    useEffect(() => {
        fetchOrders();
    }, []);

    const fetchOrders = async () => {
        try {
            const response = await ordersAPI.getAllOrders();
            setOrders(response.data || []);
        } catch (err) {
            console.error('Failed to fetch orders', err);
        } finally {
            setLoading(false);
        }
    };

    const handleStatusUpdate = async (orderId, newStatus) => {
        if (!window.confirm(`Update order #${orderId} to ${newStatus}?`)) return;

        setUpdating(orderId);
        try {
            await ordersAPI.updateStatus(orderId, newStatus);
            fetchOrders();
        } catch (err) {
            console.error('Failed to update status', err);
            alert('Failed to update status: ' + (err.response?.data?.message || err.message));
        } finally {
            setUpdating(null);
        }
    };

    if (loading) return <div className="admin-content"><p>Loading orders...</p></div>;

    return (
        <div className="admin-content">
            <h1>📦 Orders Management</h1>
            <p className="subtitle">Manage all customer orders</p>

            {orders.length === 0 ? (
                <div className="empty-state">
                    <p>No orders found.</p>
                </div>
            ) : (
                <div className="table-responsive">
                    <table className="admin-table">
                        <thead>
                            <tr>
                                <th>Order ID</th>
                                <th>Customer</th>
                                <th>Date</th>
                                <th>Total</th>
                                <th>Payment</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {orders.map((order) => (
                                <tr key={order.orderId}>
                                    <td>#{order.orderId}</td>
                                    <td>{order.email}</td>
                                    <td>{new Date(order.orderDate).toLocaleDateString()}</td>
                                    <td>${order.totalAmount?.toFixed(2)}</td>
                                    <td>{order.payment?.paymentMethod || 'N/A'}</td>
                                    <td>
                                        <span className={`status-badge ${order.orderStatus?.toLowerCase().replace(/\s/g, '-')}`}>
                                            {order.orderStatus}
                                        </span>
                                    </td>
                                    <td>
                                        <div className="action-buttons-small">
                                            {order.orderStatus === 'Order Accepted !' && (
                                                <button
                                                    className="btn-success-sm"
                                                    disabled={updating === order.orderId}
                                                    onClick={() => handleStatusUpdate(order.orderId, 'Shipped')}
                                                >
                                                    {updating === order.orderId ? '...' : 'Mark Shipped'}
                                                </button>
                                            )}
                                            {order.orderStatus === 'Shipped' && (
                                                <button
                                                    className="btn-success-sm"
                                                    disabled={updating === order.orderId}
                                                    onClick={() => handleStatusUpdate(order.orderId, 'Delivered')}
                                                >
                                                    {updating === order.orderId ? '...' : 'Mark Delivered'}
                                                </button>
                                            )}
                                            {order.orderStatus === 'Delivered' && (
                                                <span className="text-success">✓ Completed</span>
                                            )}
                                        </div>
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
