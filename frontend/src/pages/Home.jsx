import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { productsAPI, categoriesAPI } from '../api/api';
import { ProductCard } from '../components/product';
import './Home.css';

export default function Home() {
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    useEffect(() => {
        fetchData();
    }, [page]);

    const fetchData = async () => {
        setLoading(true);
        try {
            const [productsRes, categoriesRes] = await Promise.all([
                productsAPI.getAll(page, 8),
                categoriesAPI.getAll(0, 20)
            ]);
            setProducts(productsRes.data.content || []);
            setTotalPages(productsRes.data.totalPages || 0);
            setCategories(categoriesRes.data.content || []);
        } catch (error) {
            console.error('Failed to fetch data:', error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="container">
            <h1>Welcome to EcomStore</h1>
            <p>Your one-stop shop for quality products.</p>

            <hr />

            {/* Categories */}
            {categories.length > 0 && (
                <div className="section">
                    <h2>Categories</h2>
                    <ul className="category-list">
                        {categories.map((cat) => (
                            <li key={cat.categoryId}>
                                <Link to={`/category/${cat.categoryId}`}>{cat.categoryName}</Link>
                            </li>
                        ))}
                    </ul>
                </div>
            )}

            <hr />

            {/* Products */}
            <div className="section">
                <h2>Products</h2>

                {loading ? (
                    <p>Loading...</p>
                ) : products.length > 0 ? (
                    <>
                        <div className="products-grid">
                            {products.map((product) => (
                                <ProductCard key={product.productId} product={product} />
                            ))}
                        </div>

                        {totalPages > 1 && (
                            <div className="pagination">
                                <button onClick={() => setPage(p => p - 1)} disabled={page === 0}>
                                    Previous
                                </button>
                                <span>Page {page + 1} of {totalPages}</span>
                                <button onClick={() => setPage(p => p + 1)} disabled={page >= totalPages - 1}>
                                    Next
                                </button>
                            </div>
                        )}
                    </>
                ) : (
                    <p>No products available.</p>
                )}
            </div>
        </div>
    );
}
