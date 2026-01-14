import { Navigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function SellerRoute({ children }) {
    const { isAuthenticated, loading, isSeller, isAdmin } = useAuth();

    if (loading) {
        return <div className="loading">Loading...</div>;
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    // Allow both sellers and admins to access seller routes
    if (!isSeller() && !isAdmin()) {
        return <Navigate to="/unauthorized" replace />;
    }

    return children;
}
