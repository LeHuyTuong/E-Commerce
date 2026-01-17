import { createContext, useContext, useState, useEffect } from 'react';
import { authAPI, setLogoutCallback, setToken, clearToken, getToken } from '../api/api';
import { logger } from '../utils/logger';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);
    const [checked, setChecked] = useState(false);

    useEffect(() => {
        if (!checked) {
            setChecked(true);
            checkAuth();
        }
    }, [checked]);

    // Register logout callback for 401 handling
    useEffect(() => {
        setLogoutCallback(() => {
            setUser(null);
            clearToken();
        });
    }, []);

    const checkAuth = async () => {
        // Check if we have a token first
        const token = getToken();
        if (!token) {
            setLoading(false);
            return;
        }

        try {
            const response = await authAPI.getUser();
            setUser(response.data);
        } catch (error) {
            setUser(null);
            clearToken(); // Clear invalid token
        } finally {
            setLoading(false);
        }
    };

    const login = async (username, password) => {
        const response = await authAPI.login(username, password);
        const userData = response.data;

        // Save JWT token to localStorage
        if (userData.jwtToken) {
            setToken(userData.jwtToken);
            logger.log('JWT token saved to localStorage');
        }

        setUser(userData);
        return userData;
    };

    const register = async (username, email, password, role = null) => {
        const response = await authAPI.register(username, email, password, role);
        return response.data;
    };

    const logout = async () => {
        try {
            await authAPI.logout();
        } catch (e) { }
        clearToken();
        setUser(null);
    };

    // Role helpers
    const hasRole = (roleName) => {
        if (!user?.roles) return false;
        return user.roles.some(role =>
            role.roleName === roleName || role === roleName
        );
    };

    const isAdmin = () => hasRole('ROLE_ADMIN');
    const isSeller = () => hasRole('ROLE_SELLER');
    const isUser = () => hasRole('ROLE_USER');

    const value = {
        user,
        loading,
        login,
        register,
        logout,
        isAuthenticated: !!user,
        hasRole,
        isAdmin,
        isSeller,
        isUser,
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
}
