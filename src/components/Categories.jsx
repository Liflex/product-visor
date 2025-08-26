import React, { useState, useEffect } from 'react';
import { getCategories } from '../services/categoryService.js';

/**
 * Categories Management Component
 * For managing product categories
 */
const Categories = () => {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAddForm, setShowAddForm] = useState(false);
  const [newCategory, setNewCategory] = useState({ name: '', description: '' });

  useEffect(() => {
    fetchCategories();
  }, []);

  const fetchCategories = async () => {
    try {
      setLoading(true);
      const categoriesData = await getCategories();
      setCategories(categoriesData);
    } catch (error) {
      console.error('Error fetching categories:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleAddCategory = async (e) => {
    e.preventDefault();
    // TODO: Implement add category functionality
    console.log('Adding category:', newCategory);
    setShowAddForm(false);
    setNewCategory({ name: '', description: '' });
  };

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="flex justify-center items-center h-64">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-indigo-500"></div>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="bg-gray-800 rounded-lg shadow-lg p-6">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-3xl font-bold text-white">🏷️ Categories</h1>
          <button
            onClick={() => setShowAddForm(true)}
            className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded transition-colors"
          >
            ➕ Add Category
          </button>
        </div>

        {/* Add Category Form */}
        {showAddForm && (
          <div className="mb-8 bg-gray-700 rounded-lg p-6">
            <h3 className="text-xl font-semibold text-white mb-4">Add New Category</h3>
            <form onSubmit={handleAddCategory} className="space-y-4">
              <div>
                <label className="block text-gray-300 mb-2">Name</label>
                <input
                  type="text"
                  value={newCategory.name}
                  onChange={(e) => setNewCategory({ ...newCategory, name: e.target.value })}
                  className="w-full bg-gray-600 text-white px-3 py-2 rounded border border-gray-500 focus:border-blue-500 focus:outline-none"
                  placeholder="Enter category name"
                  required
                />
              </div>
              <div>
                <label className="block text-gray-300 mb-2">Description</label>
                <textarea
                  value={newCategory.description}
                  onChange={(e) => setNewCategory({ ...newCategory, description: e.target.value })}
                  className="w-full bg-gray-600 text-white px-3 py-2 rounded border border-gray-500 focus:border-blue-500 focus:outline-none"
                  placeholder="Enter category description"
                  rows="3"
                />
              </div>
              <div className="flex space-x-3">
                <button
                  type="submit"
                  className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded transition-colors"
                >
                  Save Category
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setShowAddForm(false);
                    setNewCategory({ name: '', description: '' });
                  }}
                  className="bg-gray-600 hover:bg-gray-700 text-white px-4 py-2 rounded transition-colors"
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        )}

        {/* Categories Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {categories.map((category) => (
            <div key={category.id} className="bg-gray-700 rounded-lg p-6">
              <div className="flex justify-between items-start mb-4">
                <h3 className="text-xl font-semibold text-white">{category.name}</h3>
                <div className="flex space-x-2">
                  <button className="text-blue-400 hover:text-blue-300 transition-colors">
                    ✏️
                  </button>
                  <button className="text-red-400 hover:text-red-300 transition-colors">
                    🗑️
                  </button>
                </div>
              </div>
              {category.description && (
                <p className="text-gray-300 mb-4">{category.description}</p>
              )}
              <div className="flex justify-between items-center text-sm text-gray-400">
                <span>Products: {category.productCount || 0}</span>
                <span>ID: {category.id}</span>
              </div>
            </div>
          ))}
        </div>

        {categories.length === 0 && (
          <div className="text-center py-12">
            <p className="text-gray-400 text-lg mb-4">No categories found</p>
            <button
              onClick={() => setShowAddForm(true)}
              className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded transition-colors"
            >
              Create Your First Category
            </button>
          </div>
        )}

        <div className="mt-8">
          <p className="text-gray-400 text-center">
            Category management features are coming soon. This page will include:
          </p>
          <ul className="text-gray-400 text-center mt-4 space-y-1">
            <li>• Category hierarchy and subcategories</li>
            <li>• Bulk category operations</li>
            <li>• Category-based product filtering</li>
            <li>• Category analytics and reporting</li>
            <li>• Category import/export functionality</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default Categories;

