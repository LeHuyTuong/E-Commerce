import { useState, useEffect } from 'react';
import { productsAPI, categoriesAPI } from '../api/api';
import { toast } from 'react-toastify';

const PAGE_SIZE = 10;

export default function AdminProducts() {
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);
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
    }, [page]);

    useEffect(() => {
        fetchCategories();
    }, []);

    const fetchProducts = async () => {
        setLoading(true);
        try {
            const response = await productsAPI.getAll(page, PAGE_SIZE);
            setProducts(response.data.content || []);
            setTotalPages(response.data.totalPages || 0);
            setTotalElements(response.data.totalElements || 0);
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
                // Pass categoryId to update the product's category
                await productsAPI.update(editingProduct.productId, productDTO, formData.categoryId);
                toast.success('Product updated successfully!');
            } else {
                await productsAPI.create(formData.categoryId, productDTO);
                toast.success('Product added successfully!');
            }
            setShowModal(false);
            fetchProducts();
        } catch (err) {
            toast.error(err.response?.data?.message || 'Operation failed');
        }
    };

    const handleDelete = async (productId, productName) => {
        if (!window.confirm(`Delete "${productName}"? This cannot be undone.`)) {
            return;
        }

        try {
            await productsAPI.delete(productId);
            toast.success('Product deleted!');
            setProducts(currentProducts => currentProducts.filter(p => p.productId !== productId));
        } catch (err) {
            toast.error(err.response?.data?.message || 'Delete failed');
        }
    };

    if (loading) return <div className="admin-content"><p>Loading...</p></div>;

    return (
        <div className="admin-content">
            <div className="admin-header">
                <h1>Products Management</h1>
                <button className="btn-add" onClick={openAddModal}>+ Add Product</button>
            </div>

            <p>{totalElements} products (Page {page + 1} of {Math.max(1, totalPages)})</p>

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

            {/* Pagination */}
            {totalPages > 1 && (
                <div className="pagination" style={{ marginTop: '20px', display: 'flex', gap: '10px', alignItems: 'center' }}>
                    <button
                        onClick={() => setPage(p => p - 1)}
                        disabled={page === 0}
                        className="btn-edit"
                    >
                        ← Previous
                    </button>
                    <span>Page {page + 1} of {totalPages}</span>
                    <button
                        onClick={() => setPage(p => p + 1)}
                        disabled={page >= totalPages - 1}
                        className="btn-edit"
                    >
                        Next →
                    </button>
                </div>
            )}

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
