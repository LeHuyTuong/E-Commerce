import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { productsAPI } from '../api/api';
import { ProductCard } from '../components/product';
import './Search.css';

export default function Search() {
    const { keyword } = useParams();
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchProducts();
    }, [keyword]);

    const fetchProducts = async () => {
        setLoading(true);
        try {
            const response = await productsAPI.search(keyword, 0, 20);
            setProducts(response.data.content || []);
        } catch (error) {
            console.error('Failed to search products:', error);
            setProducts([]);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container">
            <h1>Search Results for "{keyword}"</h1>

            {loading ? (
                <p>Loading...</p>
            ) : products.length > 0 ? (
                <>
                    <p>{products.length} products found</p>
                    <div className="products-grid">
                        {products.map((product) => (
                            <ProductCard key={product.productId} product={product} />
                        ))}
                    </div>
                </>
            ) : (
                <p>No products found.</p>
            )}
        </div>
    );
}
