import { Link } from 'react-router-dom';
import { useCart } from '../../context/CartContext';
import { useAuth } from '../../context/AuthContext';
import { useState } from 'react';
import './ProductCard.css';

export default function ProductCard({ product }) {
    const { addToCart } = useCart();
    const { isAuthenticated } = useAuth();
    const [adding, setAdding] = useState(false);
    const [message, setMessage] = useState('');

    const handleAddToCart = async () => {
        if (!isAuthenticated) {
            window.location.href = '/login';
            return;
        }

        setAdding(true);
        setMessage('');

        try {
            await addToCart(product.productId, 1);
            setMessage('✓ Added!');
            setTimeout(() => setMessage(''), 2000);
        } catch (error) {
            const errorMsg = error.response?.data?.message || 'Failed to add';
            setMessage('✗ ' + errorMsg);
            setTimeout(() => setMessage(''), 3000);
        } finally {
            setAdding(false);
        }
    };

    const imageUrl = product.image
        ? `http://localhost:8080/images/${product.image}`
        : 'https://via.placeholder.com/150x150?text=No+Image';

    return (
        <div className="product-card">
            <Link to={`/products/${product.productId}`}>
                <img src={imageUrl} alt={product.productName} />
            </Link>
            <div className="product-info">
                <h3>
                    <Link to={`/products/${product.productId}`}>{product.productName}</Link>
                </h3>
                <p className="price">
                    ${product.specialPrice > 0 ? product.specialPrice.toFixed(2) : product.price?.toFixed(2)}
                    {product.discount > 0 && (
                        <span className="original-price"> (was ${product.price?.toFixed(2)})</span>
                    )}
                </p>
                <p className="stock">
                    {product.quantity > 0 ? 'In Stock' : 'Out of Stock'}
                </p>
                <button
                    onClick={handleAddToCart}
                    disabled={product.quantity === 0 || adding}
                >
                    {adding ? 'Adding...' : 'Add to Cart'}
                </button>
                {message && (
                    <p className={`cart-message ${message.startsWith('✓') ? 'success' : 'error'}`}>
                        {message}
                    </p>
                )}
            </div>
        </div>
    );
}
