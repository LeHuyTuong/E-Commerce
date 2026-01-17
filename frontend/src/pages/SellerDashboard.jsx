import { useState, useEffect } from 'react';
import { productsAPI, ordersAPI, walletAPI } from '../api/api';
import { useAuth } from '../context/AuthContext';
import './Seller.css';

export default function SellerDashboard() {
    const { user } = useAuth();
    const [stats, setStats] = useState({
        totalProducts: 0,
        totalOrders: 0,
        revenue: 0
    });
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchStats();
    }, []);

    const fetchStats = async () => {
        try {
            // Fetch products count
            const productsRes = await productsAPI.getAll(0, 1);

            // Fetch wallet stats
            let walletData = { balance: 0, totalEarnings: 0 };
            try {
                const walletRes = await walletAPI.getMyWallet();
                walletData = walletRes.data;
            } catch (e) {
                // Wallet might not exist yet
            }

            // Fetch seller orders count
            let ordersCount = 0;
            try {
                const ordersRes = await ordersAPI.getSellerOrders();
                ordersCount = ordersRes.data?.length || 0;
            } catch (e) {
                // Ignore
            }

            setStats({
                totalProducts: productsRes.data?.totalElements || 0,
                totalOrders: ordersCount,
                revenue: walletData.totalEarnings || 0,
                balance: walletData.balance || 0
            });
        } catch (err) {
            console.error('Failed to fetch stats', err);
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return <div className="seller-content"><p>Loading dashboard...</p></div>;
    }

    return (
        <div className="seller-content">
            <h1>Welcome back, {user?.username}! 👋</h1>
            <p className="subtitle">Here's your seller overview</p>

            <div className="stats-grid">
                <div className="stat-card">
                    <div className="stat-icon">📦</div>
                    <div className="stat-info">
                        <h3>{stats.totalProducts}</h3>
                        <p>Total Products</p>
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon">🛒</div>
                    <div className="stat-info">
                        <h3>{stats.totalOrders}</h3>
                        <p>Orders</p>
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon">💰</div>
                    <div className="stat-info">
                        <h3>${stats.revenue.toFixed(2)}</h3>
                        <p>Total Revenue</p>
                    </div>
                </div>
                <div className="stat-card">
                    <div className="stat-icon">💳</div>
                    <div className="stat-info">
                        <h3>${stats.balance?.toFixed(2) || '0.00'}</h3>
                        <p>Wallet Balance</p>
                    </div>
                </div>
            </div>

            <div className="quick-actions">
                <h2>Quick Actions</h2>
                <div className="action-buttons">
                    <a href="/seller/products" className="action-btn">
                        ➕ Add New Product
                    </a>
                    <a href="/seller/orders" className="action-btn">
                        📋 View Orders
                    </a>
                    <a href="/seller/wallet" className="action-btn">
                        💳 My Wallet
                    </a>
                </div>
            </div>
        </div>
    );
}
