// src/App.js

import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Route, Routes, Link } from 'react-router-dom';
import ProductForm from './components/ProductForm';
import Home from './components/Home';
import AllProducts from './components/ProductAll';
import EditProduct from './components/EditProduct'; // Импортируем новый компонент
import { getCategories } from './services/categoryService';

const App = () => {
    const [categories, setCategories] = useState([]);

    useEffect(() => {
        const fetchCategories = async () => {
            try {
                const data = await getCategories();
                setCategories(data);
            } catch (error) {
                console.error('Failed to fetch categories:', error);
            }
        };

        fetchCategories();
    }, []);

    return (
        <Router>
            <div className="min-h-screen flex items-center justify-center">
                <nav className="bg-gray-800 w-full p-4">
                    <div className="container mx-auto flex justify-between items-center">
                        <Link to="/" className="text-white text-lg font-bold">Аудит Товаров</Link>
                        <ul className="flex space-x-4">
                            <li>
                                <Link to="/" className="text-white hover:text-gray-300">Главная</Link>
                            </li>
                            <li>
                                <Link to="/add-product" className="text-white hover:text-gray-300">Добавить товар</Link>
                            </li>
                            <li>
                                <Link to="/all-products" className="text-white hover:text-gray-300">Показать все продукты</Link>
                            </li>
                        </ul>
                    </div>
                </nav>
                <main className="container mx-auto px-4 py-8">
                    <Routes>
                        <Route path="/" element={<Home />} />
                        <Route path="/add-product" element={<ProductForm categories={categories} />} />
                        <Route path="/all-products" element={<AllProducts />} />
                        <Route path="/edit-product/:productId" element={<EditProduct categories={categories} />} /> {/* Добавляем новый маршрут */}
                    </Routes>
                </main>
            </div>
        </Router>
    );
};

export default App;