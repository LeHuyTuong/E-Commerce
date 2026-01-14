import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

/**
 * ProtectedRoute - Protects routes that require authentication
 * Redirects to /login if user is not authenticated
 */
export function ProtectedRoute({ children }) {
    const { isAuthenticated, loading } = useAuth();
    const location = useLocation();

    if (loading) {
        return (
            <div style={{ padding: '50px', textAlign: 'center' }}>
                <p>Loading...</p>
            </div>
        );
    }

    if (!isAuthenticated) {
        // Redirect to login with return URL
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    return children;
}

/**
 * AdminRoute - Protects admin routes
 * Requires ROLE_ADMIN or ROLE_SELLER
 * Redirects to /403 or home if not authorized
 */
export function AdminRoute({ children, requireAdmin = false }) {
    const { isAuthenticated, loading, isAdmin, isSeller } = useAuth();
    const location = useLocation();

    if (loading) {
        return (
            <div style={{ padding: '50px', textAlign: 'center' }}>
                <p>Loading...</p>
            </div>
        );
    }

    if (!isAuthenticated) {
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    // Check role permissions
    const hasAccess = requireAdmin ? isAdmin() : (isAdmin() || isSeller());

    if (!hasAccess) {
        return <Navigate to="/unauthorized" replace />;
    }

    return children;
}

export default ProtectedRoute;
