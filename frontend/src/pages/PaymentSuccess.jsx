import { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { stripeAPI } from '../api/api';
import { useCart } from '../context/CartContext';
import './StripePayment.css'; // Reusing styles for card layout

const PaymentSuccess = () => {
    const [searchParams] = useSearchParams();
    const sessionId = searchParams.get('session_id');
    const navigate = useNavigate();
    const { refreshCart } = useCart();

    const [status, setStatus] = useState('verifying'); // verifying, success, failed
    const [message, setMessage] = useState('Verifying your payment...');

    useEffect(() => {
        if (!sessionId) {
            setStatus('failed');
            setMessage('No payment session found.');
            return;
        }

        // Check if this session was already processed (prevents duplicate validation on refresh)
        const processedSessions = JSON.parse(localStorage.getItem('processed_stripe_sessions') || '[]');
        if (processedSessions.includes(sessionId)) {
            setStatus('success');
            setMessage('Payment Successful! Your order has been placed.');
            return;
        }

        const validatePayment = async () => {
            try {
                // Call backend to validate session and create order
                await stripeAPI.validateSession(sessionId);
                setStatus('success');
                setMessage('Payment Successful! Your order has been placed.');

                // Mark session as processed
                const updated = [...processedSessions, sessionId];
                localStorage.setItem('processed_stripe_sessions', JSON.stringify(updated.slice(-10))); // Keep last 10

                // Refresh cart to reflect empty state
                await refreshCart();
            } catch (error) {
                console.error('Payment Validation Failed:', error);
                // If cart is empty, likely order was already created
                if (error.response?.data?.message?.includes('Cart is empty')) {
                    setStatus('success');
                    setMessage('Payment Successful! Your order has been placed.');
                    // Mark as processed
                    const updated = [...processedSessions, sessionId];
                    localStorage.setItem('processed_stripe_sessions', JSON.stringify(updated.slice(-10)));
                } else {
                    setStatus('failed');
                    setMessage(error.response?.data?.message || 'Payment verification failed. Please contact support if you were charged.');
                }
            }
        };

        validatePayment();
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [sessionId]);

    return (
        <div className="stripe-container">
            <div className={`stripe-card ${status === 'success' ? 'success-card' : ''}`}>
                {status === 'verifying' && (
                    <div className="status-content">
                        <div className="spinner"></div>
                        <h2>Verifying Payment</h2>
                        <p>{message}</p>
                    </div>
                )}

                {status === 'success' && (
                    <div className="status-content">
                        <div className="success-icon">✅</div>
                        <h2>Payment Successful!</h2>
                        <p>{message}</p>
                        <button className="btn-primary" onClick={() => navigate('/')}>
                            Go to Home
                        </button>
                    </div>
                )}

                {status === 'failed' && (
                    <div className="status-content">
                        <div className="error-icon">❌</div>
                        <h2>Payment Failed</h2>
                        <p className="error-msg">{message}</p>
                        <button className="btn-stripe" onClick={() => navigate('/checkout')}>
                            Try Again
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
};

export default PaymentSuccess;
