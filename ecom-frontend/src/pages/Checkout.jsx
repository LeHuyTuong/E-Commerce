import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import { addressAPI, orderAPI, walletAPI } from '../api/api';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import './Checkout.css';

export default function Checkout() {
    const [addresses, setAddresses] = useState([]);
    const [selectedAddress, setSelectedAddress] = useState(null);
    const [showAddForm, setShowAddForm] = useState(false);
    const [loading, setLoading] = useState(true);
    const [placing, setPlacing] = useState(false);
    const [paymentMethod, setPaymentMethod] = useState('COD');
    const [errorMessage, setErrorMessage] = useState('');
    const [newAddress, setNewAddress] = useState({
        street: '', buildingName: '', city: '', state: '', country: '', pincode: ''
    });
    const [walletBalance, setWalletBalance] = useState(0);

    const { cart, refreshCart } = useCart();
    const { isAuthenticated } = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        if (!isAuthenticated) {
            navigate('/login');
            return;
        }
        fetchAddresses();
        fetchWallet();
    }, [isAuthenticated]);

    const fetchWallet = async () => {
        try {
            const response = await walletAPI.getMyWallet();
            setWalletBalance(response.data?.balance || 0);
        } catch (error) {
            console.error('Failed to fetch wallet:', error);
        }
    };

    const fetchAddresses = async () => {
        try {
            const response = await addressAPI.getAll();
            setAddresses(response.data || []);
            if (response.data?.length > 0) {
                setSelectedAddress(response.data[0].addressId);
            }
        } catch (error) {
            console.error('Failed to fetch addresses:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleAddAddress = async (e) => {
        e.preventDefault();
        setErrorMessage('');
        try {
            const response = await addressAPI.create(newAddress);
            setAddresses([...addresses, response.data]);
            setSelectedAddress(response.data.addressId);
            setShowAddForm(false);
            setNewAddress({ street: '', buildingName: '', city: '', state: '', country: '', pincode: '' });
        } catch (error) {
            if (error.response && error.response.data) {
                // If backend returns a map of errors or message
                const data = error.response.data;
                if (typeof data === 'object') {
                    // Combine all values into a string
                    const msg = Object.values(data).join(', ');
                    setErrorMessage(msg || 'Failed to add address');
                } else {
                    setErrorMessage(data || 'Failed to add address');
                }
            } else {
                setErrorMessage('Failed to add address');
            }
        }
    };

    const handlePlaceOrder = async () => {
        if (!selectedAddress) {
            toast.error('Please select an address');
            return;
        }

        if (paymentMethod === 'STRIPE') {
            navigate('/payment/stripe', { state: { addressId: selectedAddress } });
            return;
        }

        setPlacing(true);
        try {
            await orderAPI.placeOrder(paymentMethod, { // COD or WALLET
                addressId: selectedAddress,
                pgPaymentId: '',
                pgStatus: 'pending',
                pgResponseMessage: '',
                pgName: paymentMethod
            });
            await refreshCart();
            toast.success('Order placed successfully!');
            navigate('/');
        } catch (error) {
            toast.error(error.response?.data?.message || 'Failed to place order');
        } finally {
            setPlacing(false);
        }
    };

    if (loading) return <div className="container"><p>Loading...</p></div>;

    const products = cart?.products || [];

    if (products.length === 0) {
        navigate('/cart');
        return null;
    }

    return (
        <div className="container">
            <h1>Checkout</h1>

            <div className="checkout-layout">
                <div className="checkout-main">
                    <h2>Delivery Address</h2>

                    {addresses.length > 0 && (
                        <div className="address-list">
                            {addresses.map((addr) => (
                                <label key={addr.addressId} className="address-item">
                                    <input
                                        type="radio"
                                        name="address"
                                        checked={selectedAddress === addr.addressId}
                                        onChange={() => setSelectedAddress(addr.addressId)}
                                    />
                                    <span>
                                        {addr.buildingName}, {addr.street}, {addr.city}, {addr.state} {addr.pincode}, {addr.country}
                                    </span>
                                </label>
                            ))}
                        </div>
                    )}

                    {!showAddForm ? (
                        <button onClick={() => setShowAddForm(true)}>+ Add New Address</button>
                    ) : (
                        <form onSubmit={handleAddAddress} className="address-form">
                            {errorMessage && <div className="error-message" style={{ color: 'red', marginBottom: '10px' }}>{errorMessage}</div>}
                            <input placeholder="Street" value={newAddress.street} onChange={e => setNewAddress({ ...newAddress, street: e.target.value })} required />
                            <input placeholder="Building Name" value={newAddress.buildingName} onChange={e => setNewAddress({ ...newAddress, buildingName: e.target.value })} required />
                            <input placeholder="City" value={newAddress.city} onChange={e => setNewAddress({ ...newAddress, city: e.target.value })} required />
                            <input placeholder="State" value={newAddress.state} onChange={e => setNewAddress({ ...newAddress, state: e.target.value })} required />
                            <input placeholder="Country" value={newAddress.country} onChange={e => setNewAddress({ ...newAddress, country: e.target.value })} required />
                            <input placeholder="Pincode" value={newAddress.pincode} onChange={e => setNewAddress({ ...newAddress, pincode: e.target.value })} required />
                            <button type="submit">Save</button>
                            <button type="button" onClick={() => { setShowAddForm(false); setErrorMessage(''); }}>Cancel</button>
                        </form>
                    )}

                    <h2 style={{ marginTop: '20px' }}>Payment Method</h2>
                    <div className="payment-methods">
                        <label className="payment-option">
                            <input
                                type="radio"
                                name="payment"
                                value="COD"
                                checked={paymentMethod === 'COD'}
                                onChange={() => setPaymentMethod('COD')}
                            />
                            <span>Cash on Delivery (COD)</span>
                        </label>
                        <label className="payment-option">
                            <input
                                type="radio"
                                name="payment"
                                value="STRIPE"
                                checked={paymentMethod === 'STRIPE'}
                                onChange={() => setPaymentMethod('STRIPE')}
                            />
                            <span>Pay with Credit Card (Stripe)</span>
                        </label>
                        <label className={`payment-option ${cart?.totalPrice > walletBalance ? 'disabled' : ''}`}>
                            <input
                                type="radio"
                                name="payment"
                                value="WALLET"
                                checked={paymentMethod === 'WALLET'}
                                onChange={() => setPaymentMethod('WALLET')}
                                disabled={cart?.totalPrice > walletBalance}
                            />
                            <span>Wallet (Balance: ${walletBalance.toFixed(2)})</span>
                        </label>
                        {cart?.totalPrice > walletBalance && (
                            <small style={{ color: 'red', marginLeft: '30px', display: 'block' }}>Insufficient balance</small>
                        )}
                    </div>
                </div>

                <div className="checkout-summary">
                    <h2>Order Summary</h2>
                    <ul>
                        {products.map((p) => (
                            <li key={p.productId}>
                                {p.productName} x{p.quantity} = ${((p.specialPrice || p.price) * p.quantity).toFixed(2)}
                            </li>
                        ))}
                    </ul>
                    <hr />
                    <p><strong>Total: ${cart?.totalPrice?.toFixed(2)}</strong></p>
                    <button onClick={handlePlaceOrder} disabled={placing || !selectedAddress}>
                        {placing ? 'Placing Order...' : paymentMethod === 'STRIPE' ? 'Proceed to Payment' : 'Place Order'}
                    </button>
                </div>
            </div>
        </div>
    );
}
