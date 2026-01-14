import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useCart } from '../../context/CartContext';
import { useState } from 'react';
import './Header.css';

export default function Header() {
    const { user, isAuthenticated, logout, isAdmin, isSeller } = useAuth();
    const { itemCount } = useCart();
    const navigate = useNavigate();
    const [searchQuery, setSearchQuery] = useState('');

    const handleSearch = (e) => {
        e.preventDefault();
        if (searchQuery.trim()) {
            navigate(`/search/${encodeURIComponent(searchQuery.trim())}`);
            setSearchQuery('');
        }
    };

    const handleLogout = async () => {
        await logout();
        navigate('/');
    };

    return (
        <header className="header">
            <div className="container header-inner">
                <Link to="/" className="logo">🛒 EcomStore</Link>

                <form className="search-form" onSubmit={handleSearch}>
                    <input
                        type="text"
                        placeholder="Search products..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                    />
                    <button type="submit">Search</button>
                </form>

                <nav className="nav">
                    <Link to="/">Home</Link>
                    <Link to="/categories">Categories</Link>

                    {isAuthenticated ? (
                        <>
                            <Link to="/cart">Cart ({itemCount})</Link>
                            <Link to="/orders">My Orders</Link>
                            {isAdmin() && (
                                <Link to="/admin" className="admin-link">🛠️ Admin</Link>
                            )}
                            {isSeller() && !isAdmin() && (
                                <Link to="/seller" className="seller-link">📦 Seller</Link>
                            )}
                            <span>Hi, {user?.username}</span>
                            <button onClick={handleLogout}>Logout</button>
                        </>
                    ) : (
                        <>
                            <Link to="/login">Login</Link>
                            <Link to="/register">Register</Link>
                        </>
                    )}
                </nav>
            </div>
        </header>
    );
}
