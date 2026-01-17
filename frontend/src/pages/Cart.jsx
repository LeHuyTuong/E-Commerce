import { Link } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import './Cart.css';

export default function Cart() {
    const { cart, loading, updateQuantity, removeFromCart } = useCart();
    const { isAuthenticated } = useAuth();

    if (!isAuthenticated) {
        return (
            <div className="container">
                <h1>Shopping Cart</h1>
                <p>Please <Link to="/login">login</Link> to view your cart.</p>
            </div>
        );
    }

    if (loading) {
        return <div className="container"><p>Loading cart...</p></div>;
    }

    const products = cart?.products || [];

    if (products.length === 0) {
        return (
            <div className="container">
                <h1>Shopping Cart</h1>
                <p>Your cart is empty. <Link to="/">Continue shopping</Link></p>
            </div>
        );
    }

    return (
        <div className="container">
            <h1>Shopping Cart</h1>

            <table className="cart-table">
                <thead>
                    <tr>
                        <th>Product</th>
                        <th>Price</th>
                        <th>Quantity</th>
                        <th>Total</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    {products.map((product) => (
                        <tr key={product.productId}>
                            <td>
                                <Link to={`/products/${product.productId}`}>{product.productName}</Link>
                            </td>
                            <td>${(product.specialPrice || product.price)?.toFixed(2)}</td>
                            <td>
                                <button onClick={() => updateQuantity(product.productId, 'delete')}>-</button>
                                <span style={{ margin: '0 10px' }}>{product.quantity}</span>
                                <button onClick={() => updateQuantity(product.productId, 'add')}>+</button>
                            </td>
                            <td>${((product.specialPrice || product.price) * product.quantity).toFixed(2)}</td>
                            <td>
                                <button className="remove-btn" onClick={() => removeFromCart(product.productId)}>
                                    Remove
                                </button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

            <div className="cart-total">
                <strong>Total: ${(cart?.totalPrice > 0
                    ? cart.totalPrice
                    : products.reduce((sum, p) => sum + (p.specialPrice || p.price) * p.quantity, 0)
                ).toFixed(2)}</strong>
            </div>

            <div style={{ marginTop: '20px' }}>
                <Link to="/checkout"><button className="checkout-btn">Proceed to Checkout</button></Link>
            </div>
        </div>
    );
}
