import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { validators } from '../utils/validation';
import './Auth.css';

export default function Login() {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [errors, setErrors] = useState({});
    const [serverError, setServerError] = useState('');
    const [loading, setLoading] = useState(false);

    const { login } = useAuth();
    const navigate = useNavigate();

    const validateForm = () => {
        const newErrors = {};

        const usernameError = validators.required(username, 'Username');
        if (usernameError) newErrors.username = usernameError;

        const passwordError = validators.required(password, 'Password');
        if (passwordError) newErrors.password = passwordError;

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setServerError('');

        if (!validateForm()) return;

        setLoading(true);
        try {
            await login(username, password);
            navigate('/');
        } catch (err) {
            setServerError(err.response?.data?.message || 'Login failed');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container">
            <div className="auth-box">
                <h1>Login</h1>

                {serverError && <p className="error">{serverError}</p>}

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Username:</label>
                        <input
                            type="text"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            className={errors.username ? 'input-error' : ''}
                        />
                        {errors.username && <span className="field-error">{errors.username}</span>}
                    </div>

                    <div className="form-group">
                        <label>Password:</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            className={errors.password ? 'input-error' : ''}
                        />
                        {errors.password && <span className="field-error">{errors.password}</span>}
                    </div>

                    <button type="submit" disabled={loading}>
                        {loading ? 'Logging in...' : 'Login'}
                    </button>
                </form>

                <p className="auth-link">
                    Don't have an account? <Link to="/register">Register</Link>
                </p>
            </div>
        </div>
    );
}
