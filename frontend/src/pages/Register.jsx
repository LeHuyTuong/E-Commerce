import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { validators } from '../utils/validation';
import './Auth.css';

export default function Register() {
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        confirmPassword: '',
        role: 'user'
    });
    const [errors, setErrors] = useState({});
    const [serverError, setServerError] = useState('');
    const [loading, setLoading] = useState(false);

    const { register } = useAuth();
    const navigate = useNavigate();

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
        // Clear error when user types
        if (errors[name]) {
            setErrors({ ...errors, [name]: '' });
        }
    };

    const validateForm = () => {
        const newErrors = {};

        const usernameError = validators.username(formData.username);
        if (usernameError) newErrors.username = usernameError;

        const emailError = validators.email(formData.email);
        if (emailError) newErrors.email = emailError;

        const passwordError = validators.password(formData.password);
        if (passwordError) newErrors.password = passwordError;

        const confirmError = validators.confirmPassword(formData.confirmPassword, formData.password);
        if (confirmError) newErrors.confirmPassword = confirmError;

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setServerError('');

        if (!validateForm()) return;

        setLoading(true);
        try {
            const role = formData.role === 'user' ? null : formData.role;
            await register(formData.username, formData.email, formData.password, role);
            alert('Registration successful! Please login.');
            navigate('/login');
        } catch (err) {
            const errorData = err.response?.data;
            const message = typeof errorData === 'string'
                ? errorData
                : (errorData?.message || 'Registration failed');
            setServerError(message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container">
            <div className="auth-box">
                <h1>Register</h1>

                {serverError && <p className="error">{serverError}</p>}

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Username:</label>
                        <input
                            type="text"
                            name="username"
                            value={formData.username}
                            onChange={handleChange}
                            className={errors.username ? 'input-error' : ''}
                        />
                        {errors.username && <span className="field-error">{errors.username}</span>}
                    </div>

                    <div className="form-group">
                        <label>Email:</label>
                        <input
                            type="email"
                            name="email"
                            value={formData.email}
                            onChange={handleChange}
                            className={errors.email ? 'input-error' : ''}
                        />
                        {errors.email && <span className="field-error">{errors.email}</span>}
                    </div>

                    <div className="form-group">
                        <label>Password:</label>
                        <input
                            type="password"
                            name="password"
                            value={formData.password}
                            onChange={handleChange}
                            className={errors.password ? 'input-error' : ''}
                        />
                        {errors.password && <span className="field-error">{errors.password}</span>}
                    </div>

                    <div className="form-group">
                        <label>Confirm Password:</label>
                        <input
                            type="password"
                            name="confirmPassword"
                            value={formData.confirmPassword}
                            onChange={handleChange}
                            className={errors.confirmPassword ? 'input-error' : ''}
                        />
                        {errors.confirmPassword && <span className="field-error">{errors.confirmPassword}</span>}
                    </div>

                    <div className="form-group">
                        <label>Register as:</label>
                        <select
                            name="role"
                            value={formData.role}
                            onChange={handleChange}
                            className="role-select"
                        >
                            <option value="user">Customer (User)</option>
                            <option value="seller">Seller</option>
                        </select>
                    </div>

                    <button type="submit" disabled={loading}>
                        {loading ? 'Registering...' : 'Register'}
                    </button>
                </form>

                <p className="auth-link">
                    Already have an account? <Link to="/login">Login</Link>
                </p>
            </div>
        </div>
    );
}
