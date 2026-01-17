import { useState, useEffect } from 'react';
import { productsAPI, categoriesAPI } from '../api/api';
import { toast } from 'react-toastify';
import './Admin.css';

export default function SellerProducts() {
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [editingProduct, setEditingProduct] = useState(null);
    const [formData, setFormData] = useState({
        productName: '',
        description: '',
        price: '',
        quantity: '',
        discount: '0',
        categoryId: ''
    });

    useEffect(() => {
        fetchProducts();
        fetchCategories();
    }, []);

    const fetchProducts = async () => {
        try {
            const response = await productsAPI.getSellerProducts(0, 100);
            setProducts(response.data.content || []);
        } catch (err) {
            toast.error('Failed to fetch products');
        } finally {
            setLoading(false);
        }
    };

    const fetchCategories = async () => {
        try {
            const response = await categoriesAPI.getAll(0, 50);
            setCategories(response.data.content || []);
        } catch (err) {
            console.error('Failed to fetch categories');
        }
    };

    const resetForm = () => {
        setFormData({
            productName: '',
            description: '',
            price: '',
            quantity: '',
            discount: '0',
            categoryId: ''
        });
        setEditingProduct(null);
    };

    const openAddModal = () => {
        resetForm();
        setShowModal(true);
    };

    const openEditModal = (product) => {
        setEditingProduct(product);
        setFormData({
            productName: product.productName,
            description: product.description || '',
            price: product.price?.toString() || '',
            quantity: product.quantity?.toString() || '',
            discount: product.discount?.toString() || '0',
            categoryId: product.category?.categoryId?.toString() || ''
        });
        setShowModal(true);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!formData.categoryId) {
            toast.error('Please select a category');
            return;
        }

        const productDTO = {
            productName: formData.productName,
            description: formData.description,
            price: parseFloat(formData.price),
            quantity: parseInt(formData.quantity),
            discount: parseFloat(formData.discount) || 0
        };

        try {
            if (editingProduct) {
                await productsAPI.sellerUpdate(editingProduct.productId, productDTO);
                toast.success('Product updated!');
            } else {
                await productsAPI.create(formData.categoryId, productDTO);
                toast.success('Product added!');
            }
            setShowModal(false);
            fetchProducts();
        } catch (err) {
            toast.error(err.response?.data?.message || 'Operation failed');
        }
    };

    const handleDelete = async (productId, productName) => {
        console.log(`[SellerProducts] Requested delete for product ID: ${productId}, Name: ${productName}`);

        if (!window.confirm(`Are you sure you want to delete "${productName}" (ID: ${productId})?`)) {
            console.log('[SellerProducts] Delete cancelled by user');
            return;
        }

        console.log(`[SellerProducts] Confirmed. Sending DELETE request for ID: ${productId}...`);
        try {
            const response = await productsAPI.sellerDelete(productId);
            console.log('[SellerProducts] DELETE response:', response);
            toast.success('Product deleted successfully!');
            fetchProducts();
        } catch (err) {
            console.error('[SellerProducts] DELETE failed:', err);
            const errorMsg = err.response?.data?.message || err.message || 'Unknown error';
            toast.error(`Delete failed: ${errorMsg}`);
            // Also show an alert just in case toasts are not visible
            alert(`Could not delete product: ${errorMsg}`);
        }
    };

    if (loading) return <div className="seller-content"><p>Loading...</p></div>;

    return (
        <div className="seller-content">
            <div className="admin-header">
                <h1>My Products</h1>
                <button className="btn-add" onClick={openAddModal}>+ Add Product</button>
            </div>

            <p>{products.length} products listed</p>

            <table className="admin-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Price</th>
                        <th>Stock</th>
                        <th>Category</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {products.map((p) => (
                        <tr key={p.productId}>
                            <td>{p.productId}</td>
                            <td>{p.productName}</td>
                            <td>${p.price?.toFixed(2)}</td>
                            <td>{p.quantity}</td>
                            <td>{p.category?.categoryName || '-'}</td>
                            <td className="actions">
                                <button className="btn-edit" onClick={() => openEditModal(p)}>Edit</button>
                                <button className="btn-delete" onClick={() => handleDelete(p.productId, p.productName)}>Delete</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

            {/* Modal */}
            {showModal && (
                <div className="modal-overlay" onClick={() => setShowModal(false)}>
                    <div className="modal-content" onClick={e => e.stopPropagation()}>
                        <h2>{editingProduct ? 'Edit Product' : 'Add New Product'}</h2>
                        <form onSubmit={handleSubmit}>
                            <div className="form-group">
                                <label>Product Name *</label>
                                <input
                                    type="text"
                                    value={formData.productName}
                                    onChange={e => setFormData({ ...formData, productName: e.target.value })}
                                    required
                                />
                            </div>
                            <div className="form-group">
                                <label>Description</label>
                                <textarea
                                    value={formData.description}
                                    onChange={e => setFormData({ ...formData, description: e.target.value })}
                                    rows="3"
                                />
                            </div>
                            <div className="form-row">
                                <div className="form-group">
                                    <label>Price ($) *</label>
                                    <input
                                        type="number"
                                        step="0.01"
                                        value={formData.price}
                                        onChange={e => setFormData({ ...formData, price: e.target.value })}
                                        required
                                    />
                                </div>
                                <div className="form-group">
                                    <label>Quantity *</label>
                                    <input
                                        type="number"
                                        value={formData.quantity}
                                        onChange={e => setFormData({ ...formData, quantity: e.target.value })}
                                        required
                                    />
                                </div>
                            </div>
                            <div className="form-row">
                                <div className="form-group">
                                    <label>Discount (%)</label>
                                    <input
                                        type="number"
                                        step="0.1"
                                        value={formData.discount}
                                        onChange={e => setFormData({ ...formData, discount: e.target.value })}
                                    />
                                </div>
                                <div className="form-group">
                                    <label>Category *</label>
                                    <select
                                        value={formData.categoryId}
                                        onChange={e => setFormData({ ...formData, categoryId: e.target.value })}
                                        required
                                    >
                                        <option value="">Select category</option>
                                        {categories.map(c => (
                                            <option key={c.categoryId} value={c.categoryId}>
                                                {c.categoryName}
                                            </option>
                                        ))}
                                    </select>
                                </div>
                            </div>
                            <div className="modal-actions">
                                <button type="button" className="btn-cancel" onClick={() => setShowModal(false)}>
                                    Cancel
                                </button>
                                <button type="submit" className="btn-save">
                                    {editingProduct ? 'Update' : 'Add Product'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}
