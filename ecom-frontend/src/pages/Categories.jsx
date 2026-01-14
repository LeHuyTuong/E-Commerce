import { useState, useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { productsAPI, categoriesAPI } from '../api/api';
import { ProductCard } from '../components/product';
import './Categories.css';

export default function Categories() {
    const { id } = useParams();
    const [categories, setCategories] = useState([]);
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [selectedCategory, setSelectedCategory] = useState(null);

    useEffect(() => {
        fetchCategories();
    }, []);

    useEffect(() => {
        if (id) {
            fetchProductsByCategory(id);
        }
    }, [id]);

    const fetchCategories = async () => {
        try {
            const response = await categoriesAPI.getAll(0, 50);
            setCategories(response.data.content || []);
        } catch (error) {
            console.error('Failed to fetch categories:', error);
        } finally {
            setLoading(false);
        }
    };

    const fetchProductsByCategory = async (categoryId) => {
        setLoading(true);
        try {
            const response = await productsAPI.getByCategory(categoryId, 0, 20);
            setProducts(response.data.content || []);
            const cat = categories.find(c => c.categoryId === parseInt(categoryId));
            setSelectedCategory(cat);
        } catch (error) {
            console.error('Failed to fetch products:', error);
        } finally {
            setLoading(false);
        }
    };

    if (loading && !categories.length) {
        return <div className="container"><p>Loading...</p></div>;
    }

    if (!id) {
        return (
            <div className="container">
                <h1>Categories</h1>
                <ul className="category-list">
                    {categories.map((cat) => (
                        <li key={cat.categoryId}>
                            <Link to={`/category/${cat.categoryId}`}>{cat.categoryName}</Link>
                        </li>
                    ))}
                </ul>
            </div>
        );
    }

    return (
        <div className="container">
            <p><Link to="/categories">&laquo; All Categories</Link></p>
            <h1>{selectedCategory?.categoryName || 'Category'}</h1>
            <p>{products.length} products</p>

            {loading ? (
                <p>Loading...</p>
            ) : products.length > 0 ? (
                <div className="products-grid">
                    {products.map((product) => (
                        <ProductCard key={product.productId} product={product} />
                    ))}
                </div>
            ) : (
                <p>No products in this category.</p>
            )}
        </div>
    );
}
