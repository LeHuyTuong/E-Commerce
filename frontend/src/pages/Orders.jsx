import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { ordersAPI } from '../api/api';
import { useAuth } from '../context/AuthContext';
import './Orders.css';

const Orders = () => {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const { isAuthenticated } = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        if (!isAuthenticated) {
            navigate('/login');
            return;
        }
        fetchOrders();
    }, [isAuthenticated, navigate]);

    const fetchOrders = async () => {
        try {
            const response = await ordersAPI.getUserOrders();
            setOrders(response.data || []);
        } catch (error) {
            console.error('Failed to fetch orders:', error);
            setOrders([]);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return <div className="container"><p>Loading orders...</p></div>;
    }

    return (
        <div className="container orders-page">
            <h1>My Orders</h1>

            {orders.length === 0 ? (
                <div className="empty-orders">
                    <p>You haven't placed any orders yet.</p>
                    <button onClick={() => navigate('/')}>Shop Now</button>
                </div>
            ) : (
                <div className="orders-list">
                    {orders.map((order) => (
                        <div key={order.orderId} className="order-card">
                            <div className="order-header">
                                <div>
                                    <h3>Order #{order.orderId}</h3>
                                    <p className="order-date">{new Date(order.orderDate).toLocaleDateString()}</p>
                                </div>
                                <div className="order-status">
                                    <span className={`status-badge ${order.orderStatus?.toLowerCase()}`}>
                                        {order.orderStatus || 'PENDING'}
                                    </span>
                                </div>
                            </div>

                            <div className="order-details">
                                <p><strong>Total:</strong> ${order.totalAmount?.toFixed(2)}</p>
                                <p><strong>Payment:</strong> {order.payment?.paymentMethod || 'N/A'}</p>
                            </div>

                            {order.orderItems && order.orderItems.length > 0 && (
                                <div className="order-items">
                                    <h4>Items:</h4>
                                    <ul>
                                        {order.orderItems.map((item, idx) => (
                                            <li key={idx}>
                                                {item.product?.productName || item.productName}
                                                <span className="item-quantity"> x{item.quantity}</span>
                                                <span className="item-price"> ${item.orderedProductPrice?.toFixed(2)}</span>
                                            </li>
                                        ))}
                                    </ul>
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default Orders;
