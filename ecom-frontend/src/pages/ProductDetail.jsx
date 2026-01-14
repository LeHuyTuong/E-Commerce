import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import { productsAPI } from '../api/api';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import './ProductDetail.css';

export default function ProductDetail() {
    const { id } = useParams();
    const [product, setProduct] = useState(null);
    const [loading, setLoading] = useState(true);
    const [quantity, setQuantity] = useState(1);

    const { addToCart } = useCart();
    const { isAuthenticated } = useAuth();

    useEffect(() => {
        fetchProduct();
    }, [id]);

    const fetchProduct = async () => {
        setLoading(true);
        try {
            const response = await productsAPI.getById(id);
            setProduct(response.data);
        } catch (error) {
            console.error('Failed to fetch product:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleAddToCart = async () => {
        if (!isAuthenticated) {
            window.location.href = '/login';
            return;
        }
        try {
            await addToCart(product.productId, quantity);
            toast.success('Added to cart!');
        } catch (error) {
            toast.error('Failed to add to cart');
        }
    };

    if (loading) return <div className="container"><p>Loading...</p></div>;

    if (!product) {
        return (
            <div className="container">
                <h1>Product not found</h1>
                <Link to="/">Back to Home</Link>
            </div>
        );
    }

    const imageUrl = product.image
        ? `http://localhost:8080/images/${product.image}`
        : 'https://via.placeholder.com/300x300?text=No+Image';

    return (
        <div className="container">
            <p><Link to="/">&laquo; Back to Products</Link></p>

            <div className="product-detail">
                <div className="product-image">
                    <img src={imageUrl} alt={product.productName} />
                </div>

                <div className="product-info">
                    <h1>{product.productName}</h1>

                    <p className="price">
                        Price: <strong>${product.specialPrice > 0 ? product.specialPrice.toFixed(2) : product.price?.toFixed(2)}</strong>
                        {product.discount > 0 && (
                            <span className="original"> (was ${product.price?.toFixed(2)}, save {product.discount}%)</span>
                        )}
                    </p>

                    <p>Stock: {product.quantity > 0 ? `${product.quantity} available` : 'Out of Stock'}</p>

                    <p>{product.description || 'No description available.'}</p>

                    <div className="add-to-cart">
                        <label>Quantity: </label>
                        <input
                            type="number"
                            min="1"
                            max={product.quantity}
                            value={quantity}
                            onChange={(e) => setQuantity(parseInt(e.target.value) || 1)}
                        />
                        <button onClick={handleAddToCart} disabled={product.quantity === 0}>
                            Add to Cart
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}
