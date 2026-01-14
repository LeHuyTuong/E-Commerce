import { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import { stripeAPI } from '../api/api';
import { toast } from 'react-toastify';
import './StripePayment.css';

const StripePayment = () => {
    const { cart } = useCart();
    const { user } = useAuth();
    const [isLoading, setIsLoading] = useState(false);
    const navigate = useNavigate();
    const location = useLocation();

    // Get addressId from navigation state (passed from Checkout)
    const { addressId } = location.state || {};

    useEffect(() => {
        if (!cart || cart.totalPrice === 0) {
            toast.info('Your cart is empty');
            navigate('/');
        }
        if (!addressId) {
            toast.warn('No delivery address found. Please verify your address.');
            navigate('/checkout');
        }
    }, [cart, addressId, navigate]);

    const handlePayment = async () => {
        if (!addressId) return;

        setIsLoading(true);

        try {
            const paymentData = {
                amount: Math.round(cart.totalPrice * 100), // Stripe expects cents
                currency: 'usd',
                email: user.username + '@example.com', // Use username as fallback
                name: user.username,
                address: { addressId: addressId }, // Backend only needs addressId
                description: `Order from ${user.username}`,
            };

            const response = await stripeAPI.createCheckoutSession(paymentData);

            if (response.data && response.data.url) {
                // Redirect to Stripe Checkout
                window.location.href = response.data.url;
            } else {
                toast.error('Failed to create payment session');
            }
        } catch (error) {
            console.error('Stripe Session Error:', error);
            const msg = error.response?.data?.message || error.message || 'Something went wrong';
            toast.error(msg);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="stripe-payment-container">
            <h2>Secure Payment</h2>

            <div className="payment-info-card">
                <p className="payment-description">
                    You will be redirected to <strong>Stripe</strong> to complete your secure payment.
                </p>

                {cart && (
                    <div className="order-summary-box">
                        <h3>Total Amount</h3>
                        <div className="total-price">${(cart.totalPrice || 0).toFixed(2)}</div>
                    </div>
                )}

                <button
                    onClick={handlePayment}
                    disabled={isLoading}
                    className="stripe-pay-button"
                >
                    {isLoading ? 'Redirecting to Stripe...' : 'Proceed to Payment'}
                </button>

                <div className="secure-badge">
                    🔒 256-bit SSL Encrypted Payment
                </div>
            </div>
        </div>
    );
};

export default StripePayment;
