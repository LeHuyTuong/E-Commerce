import { Outlet, Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import './AdminLayout.css';

export default function AdminLayout() {
    const { user, logout, isAuthenticated } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    // Redirect to login if not authenticated
    if (!isAuthenticated) {
        navigate('/login');
        return null;
    }

    const handleLogout = async () => {
        await logout();
        navigate('/');
    };

    const isActive = (path) => location.pathname === path;

    const menuItems = [
        { path: '/admin', icon: '📊', label: 'Dashboard' },
        { path: '/admin/products', icon: '📦', label: 'Products' },
        { path: '/admin/categories', icon: '📁', label: 'Categories' },
        { path: '/admin/orders', icon: '🛒', label: 'Orders' },
        { path: '/admin/users', icon: '👥', label: 'Users' },
        { path: '/admin/payments', icon: '💳', label: 'Payments' },
        { path: '/admin/settings', icon: '⚙️', label: 'Settings' },
    ];

    return (
        <div className="admin-layout">
            <aside className="admin-sidebar">
                <div className="admin-brand">
                    <h2>🛠️ Admin Panel</h2>
                    <span className="brand-subtitle">E-Commerce Store</span>
                </div>

                <nav className="admin-nav">
                    {menuItems.map((item) => (
                        <Link
                            key={item.path}
                            to={item.path}
                            className={isActive(item.path) ? 'active' : ''}
                        >
                            <span className="nav-icon">{item.icon}</span>
                            <span className="nav-label">{item.label}</span>
                        </Link>
                    ))}
                </nav>

                <div className="admin-footer">
                    <div className="user-info">
                        <span className="user-avatar">👤</span>
                        <span className="user-name">{user?.username || 'Admin'}</span>
                    </div>
                    <Link to="/" className="back-to-store">← Back to Store</Link>
                    <button onClick={handleLogout}>Logout</button>
                </div>
            </aside>

            <main className="admin-main">
                <Outlet />
            </main>
        </div>
    );
}
