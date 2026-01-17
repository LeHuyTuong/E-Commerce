import { useState, useEffect } from 'react';
import { ordersAPI } from '../api/api';
import './Admin.css';

export default function SellerOrders() {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [updating, setUpdating] = useState(null);

    useEffect(() => {
        fetchOrders();
    }, []);

    const fetchOrders = async () => {
        try {
            const response = await ordersAPI.getSellerOrders();
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
            // Refresh orders
            fetchOrders();
        } catch (err) {
            console.error('Failed to update status', err);
            alert('Failed to update status: ' + (err.response?.data?.message || err.message));
        } finally {
            setUpdating(null);
        }
    };

    if (loading) return <div className="seller-content"><p>Loading orders...</p></div>;

    return (
        <div className="seller-content">
            <h1>Your Orders</h1>
            <p className="subtitle">Manage orders for your products</p>

            {orders.length === 0 ? (
                <div className="empty-state">
                    <p>No orders received yet.</p>
                </div>
            ) : (
                <div className="table-responsive">
                    <table className="admin-table">
                        <thead>
                            <tr>
                                <th>Order ID</th>
                                <th>Date</th>
                                <th>Address</th>
                                <th>Total</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            {orders.map((order) => (
                                <tr key={order.orderId}>
                                    <td>#{order.orderId}</td>
                                    <td>{new Date(order.orderDate).toLocaleDateString()}</td>
                                    <td>
                                        {order.address ? (
                                            <div className="small-text">
                                                {order.address.street},<br />
                                                {order.address.city}, {order.address.state}<br />
                                                {order.address.pincode}
                                            </div>
                                        ) : (
                                            <span className="text-muted">N/A</span>
                                        )}
                                    </td>
                                    <td>${order.totalAmount?.toFixed(2)}</td>
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
                                                <span className="text-success">Completed</span>
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
