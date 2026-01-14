import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { analyticsAPI, walletAPI } from '../api/api';
import './AdminDashboard.css';

export default function AdminDashboard() {
    const [analytics, setAnalytics] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        fetchAnalytics();
    }, []);

    const fetchAnalytics = async () => {
        try {
            const [analyticsRes, walletRes] = await Promise.all([
                analyticsAPI.getAnalytics(),
                walletAPI.getPlatformWallet().catch(() => ({ data: { balance: 0 } }))
            ]);

            setAnalytics({
                ...analyticsRes.data,
                platformEarnings: walletRes.data?.totalEarnings || 0
            });
        } catch (err) {
            setError('Unable to load analytics.');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="admin-content">
                <div className="loading-spinner">Loading...</div>
            </div>
        );
    }

    return (
        <div className="admin-content">
            <div className="dashboard-header">
                <h1>🛒 E-Commerce Admin Center</h1>
                <p>Welcome! Select a management function below.</p>
            </div>

            {/* Stats Cards */}
            <div className="stats-row">
                <div className="stat-card revenue">
                    <div className="stat-icon">💰</div>
                    <div className="stat-info">
                        <h4>Platform Earnings</h4>
                        <p className="stat-value">${parseFloat(analytics?.platformEarnings || 0).toFixed(2)}</p>
                    </div>
                </div>
                <div className="stat-card revenue">
                    <div className="stat-icon">💵</div>
                    <div className="stat-info">
                        <h4>Total Revenue</h4>
                        <p className="stat-value">${parseFloat(analytics?.totalRevenue || 0).toFixed(2)}</p>
                    </div>
                </div>
                <div className="stat-card orders">
                    <div className="stat-icon">📦</div>
                    <div className="stat-info">
                        <h4>Total Orders</h4>
                        <p className="stat-value">{analytics?.totalOrders || 0}</p>
                    </div>
                </div>
                <div className="stat-card products">
                    <div className="stat-icon">🏷️</div>
                    <div className="stat-info">
                        <h4>Total Products</h4>
                        <p className="stat-value">{analytics?.productCount || 0}</p>
                    </div>
                </div>
            </div>

            {error && <p className="error-msg">{error}</p>}

            {/* Management Cards */}
            <h2 className="section-title">Management Functions</h2>
            <div className="admin-cards-grid">
                <div className="admin-card products-card" onClick={() => navigate('/admin/products')}>
                    <div className="card-icon">📦</div>
                    <h3>Product Management</h3>
                    <p>Add, edit, and manage your product catalog</p>
                    <button>Access →</button>
                </div>

                <div className="admin-card categories-card" onClick={() => navigate('/admin/categories')}>
                    <div className="card-icon">📁</div>
                    <h3>Category Management</h3>
                    <p>Organize products into categories</p>
                    <button>Access →</button>
                </div>

                <div className="admin-card orders-card" onClick={() => navigate('/admin/orders')}>
                    <div className="card-icon">🛒</div>
                    <h3>Order Management</h3>
                    <p>View and process customer orders</p>
                    <button>Access →</button>
                </div>

                <div className="admin-card users-card" onClick={() => navigate('/admin/users')}>
                    <div className="card-icon">👥</div>
                    <h3>User Management</h3>
                    <p>Manage customer accounts and roles</p>
                    <button>Access →</button>
                </div>

                <div className="admin-card payments-card" onClick={() => navigate('/admin/payments')}>
                    <div className="card-icon">💳</div>
                    <h3>Payment Settings</h3>
                    <p>Configure payment methods and Stripe</p>
                    <button>Access →</button>
                </div>

                <div className="admin-card settings-card" onClick={() => navigate('/admin/settings')}>
                    <div className="card-icon">⚙️</div>
                    <h3>Store Settings</h3>
                    <p>Configure store preferences</p>
                    <button>Access →</button>
                </div>
            </div>
        </div>
    );
}
