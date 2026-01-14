import { useState, useEffect } from 'react';
import { categoriesAPI } from '../api/api';
import { toast } from 'react-toastify';
import './Admin.css';

export default function AdminCategories() {
    const [categories, setCategories] = useState([]);
    const [loading, setLoading] = useState(true);
    const [newCategoryName, setNewCategoryName] = useState('');
    const [saving, setSaving] = useState(false);
    const [editingId, setEditingId] = useState(null);
    const [editingName, setEditingName] = useState('');

    useEffect(() => {
        fetchCategories();
    }, []);

    const fetchCategories = async () => {
        try {
            const response = await categoriesAPI.getAll(0, 50);
            setCategories(response.data.content || []);
        } catch (err) {
            console.error('Failed to fetch categories');
        } finally {
            setLoading(false);
        }
    };

    const handleAddCategory = async (e) => {
        e.preventDefault();
        if (!newCategoryName.trim()) {
            toast.error('Category name is required');
            return;
        }
        if (newCategoryName.trim().length < 5) {
            toast.error('Category name must be at least 5 characters');
            return;
        }

        setSaving(true);
        try {
            await categoriesAPI.create({ categoryName: newCategoryName.trim() });
            toast.success('Category added successfully!');
            setNewCategoryName('');
            fetchCategories();
        } catch (err) {
            toast.error(err.response?.data?.message || 'Failed to add category');
        } finally {
            setSaving(false);
        }
    };

    const handleEditStart = (category) => {
        setEditingId(category.categoryId);
        setEditingName(category.categoryName);
    };

    const handleEditCancel = () => {
        setEditingId(null);
        setEditingName('');
    };

    const handleEditSave = async (categoryId) => {
        if (!editingName.trim()) {
            toast.error('Category name is required');
            return;
        }
        if (editingName.trim().length < 5) {
            toast.error('Category name must be at least 5 characters');
            return;
        }

        try {
            await categoriesAPI.update(categoryId, { categoryName: editingName.trim() });
            toast.success('Category updated!');
            setEditingId(null);
            setEditingName('');
            fetchCategories();
        } catch (err) {
            toast.error(err.response?.data?.message || 'Failed to update category');
        }
    };

    const handleDelete = async (categoryId, categoryName) => {
        if (!window.confirm(`Delete "${categoryName}"? Products in this category may be affected.`)) {
            return;
        }

        try {
            await categoriesAPI.delete(categoryId);
            toast.success('Category deleted!');
            setCategories(current => current.filter(c => c.categoryId !== categoryId));
        } catch (err) {
            toast.error(err.response?.data?.message || 'Failed to delete category');
        }
    };

    if (loading) return <div className="admin-content"><p>Loading...</p></div>;

    return (
        <div className="admin-content">
            <h1>Categories Management</h1>
            <p className="subtitle">{categories.length} categories</p>

            <div className="add-form-card">
                <h3>Add New Category</h3>
                <form onSubmit={handleAddCategory} className="add-form">
                    <input
                        type="text"
                        placeholder="Category Name (min 5 characters)"
                        value={newCategoryName}
                        onChange={(e) => setNewCategoryName(e.target.value)}
                        disabled={saving}
                    />
                    <button type="submit" className="btn-primary" disabled={saving}>
                        {saving ? 'Adding...' : 'Add Category'}
                    </button>
                </form>
            </div>

            {/* Categories List */}
            <table className="admin-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {categories.map((c) => (
                        <tr key={c.categoryId}>
                            <td>{c.categoryId}</td>
                            <td>
                                {editingId === c.categoryId ? (
                                    <input
                                        type="text"
                                        value={editingName}
                                        onChange={(e) => setEditingName(e.target.value)}
                                        className="edit-input"
                                        autoFocus
                                    />
                                ) : (
                                    <strong>{c.categoryName}</strong>
                                )}
                            </td>
                            <td className="actions">
                                {editingId === c.categoryId ? (
                                    <>
                                        <button className="btn-save-sm" onClick={() => handleEditSave(c.categoryId)}>Save</button>
                                        <button className="btn-cancel-sm" onClick={handleEditCancel}>Cancel</button>
                                    </>
                                ) : (
                                    <>
                                        <button className="btn-edit" onClick={() => handleEditStart(c)}>Edit</button>
                                        <button className="btn-delete" onClick={() => handleDelete(c.categoryId, c.categoryName)}>Delete</button>
                                    </>
                                )}
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}
