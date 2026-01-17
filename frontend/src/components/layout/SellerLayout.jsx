import { Outlet, NavLink } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import './SellerLayout.css';

export default function SellerLayout() {
    const { user, logout } = useAuth();

    const handleLogout = async () => {
        await logout();
        window.location.href = '/';
    };

    return (
        <div className="seller-layout">
            <aside className="seller-sidebar">
                <div className="seller-logo">
                    <h2>📦 Seller Portal</h2>
                </div>
                <nav className="seller-nav">
                    <NavLink to="/seller" end className={({ isActive }) => isActive ? 'active' : ''}>
                        🏠 Dashboard
                    </NavLink>
                    <NavLink to="/seller/products" className={({ isActive }) => isActive ? 'active' : ''}>
                        📋 My Products
                    </NavLink>
                    <NavLink to="/seller/orders" className={({ isActive }) => isActive ? 'active' : ''}>
                        🛒 Orders
                    </NavLink>
                </nav>
                <div className="seller-user">
                    <span>👤 {user?.username}</span>
                    <button onClick={handleLogout}>Logout</button>
                    <NavLink to="/" className="back-link">← Back to Store</NavLink>
                </div>
            </aside>
            <main className="seller-main">
                <Outlet />
            </main>
        </div>
    );
}
