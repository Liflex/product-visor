/**
 * Home Page Component
 * Provides welcome screen and overview of the Product Visor application
 */

import React from 'react';
import { Link } from 'react-router-dom';
import { useProducts } from '../hooks/use-products.js';
import { useCategories } from '../hooks/use-categories.js';
import useBarcodeScanner from '../hooks/use-barcode-scanner.js';
import Notification from './ui/notification.jsx';

/**
 * Statistics card component
 */
const StatCard = ({ title, value, icon, description, linkTo, linkText }) => (
  <div className="bg-gray-800 p-6 rounded-lg shadow-lg">
    <div className="flex items-center">
      <div className="text-3xl mr-4">{icon}</div>
      <div className="flex-1">
        <h3 className="text-lg font-semibold text-white">{title}</h3>
        <p className="text-3xl font-bold text-indigo-400">{value}</p>
        <p className="text-sm text-gray-400">{description}</p>
        {linkTo && linkText && (
          <Link 
            to={linkTo} 
            className="text-sm text-indigo-400 hover:text-indigo-300 transition-colors"
          >
            {linkText} →
          </Link>
        )}
      </div>
    </div>
  </div>
);

/**
 * Feature card component
 */
const FeatureCard = ({ title, description, icon }) => (
  <div className="bg-gray-800 p-6 rounded-lg shadow-lg">
    <div className="text-center">
      <div className="text-4xl mb-4">{icon}</div>
      <h3 className="text-lg font-semibold text-white mb-2">{title}</h3>
      <p className="text-gray-400">{description}</p>
    </div>
  </div>
);

/**
 * Main Home component
 */
const Home = () => {
  const { totalProducts, filteredCount } = useProducts();
  const { totalCategories } = useCategories();
  
  // Global barcode scanner
  const { notification, hideNotification } = useBarcodeScanner({
    enabled: true
  });

  const features = [
    {
      title: 'Smart Inventory',
      description: 'Track contact lens inventory with dynamic attributes like power, color, and expiry dates.',
      icon: '📦'
    },
    {
      title: 'Advanced Search',
      description: 'Find products quickly with powerful search and filtering capabilities.',
      icon: '🔍'
    },
    {
      title: 'Easy Management',
      description: 'Add, edit, and delete products with an intuitive interface and real-time validation.',
      icon: '⚡'
    },
    {
      title: 'Visual Preview',
      description: 'Upload and preview product images with automatic optimization.',
      icon: '🖼️'
    }
  ];

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="space-y-8">
        {/* Hero Section */}
        <div className="text-center py-12">
        <h1 className="text-4xl font-bold text-white mb-4">
          Welcome to Product Visor
        </h1>
        <p className="text-xl text-gray-400 mb-8 max-w-2xl mx-auto">
          Your comprehensive solution for contact lens inventory management. 
          Track, search, and manage your products with ease and precision.
        </p>
        <div className="flex justify-center space-x-4">
          <Link
            to="/add-product"
            className="bg-indigo-600 hover:bg-indigo-700 text-white px-6 py-3 rounded-md transition-colors font-medium"
          >
            Add First Product
          </Link>
          <Link
            to="/all-products"
            className="border border-gray-600 text-gray-300 hover:bg-gray-700 px-6 py-3 rounded-md transition-colors font-medium"
          >
            View Catalog
          </Link>
        </div>
      </div>

      {/* Statistics */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <StatCard
          title="Total Products"
          value={totalProducts || 0}
          icon="📦"
          description="Products in your inventory"
          linkTo="/all-products"
          linkText="View all products"
        />
        <StatCard
          title="Categories"
          value={totalCategories || 0}
          icon="🏷️"
          description="Product categories available"
        />
        <StatCard
          title="Quick Actions"
          value="🚀"
          icon="⚡"
          description="Start managing your inventory"
          linkTo="/add-product"
          linkText="Add new product"
        />
      </div>

      {/* Features */}
      <div>
        <h2 className="text-2xl font-bold text-white mb-6 text-center">Key Features</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {features.map((feature, index) => (
            <FeatureCard
              key={index}
              title={feature.title}
              description={feature.description}
              icon={feature.icon}
            />
          ))}
        </div>
      </div>

      {/* Getting Started */}
      <div className="bg-gray-800 rounded-lg p-8">
        <h2 className="text-2xl font-bold text-white mb-4">Getting Started</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="text-center">
            <div className="text-3xl mb-2">1️⃣</div>
            <h3 className="font-semibold text-white mb-2">Add Products</h3>
            <p className="text-gray-400 text-sm">
              Start by adding your contact lens products with detailed attributes.
            </p>
          </div>
          <div className="text-center">
            <div className="text-3xl mb-2">2️⃣</div>
            <h3 className="font-semibold text-white mb-2">Organize & Search</h3>
            <p className="text-gray-400 text-sm">
              Use categories and search features to find products quickly.
            </p>
          </div>
          <div className="text-center">
            <div className="text-3xl mb-2">3️⃣</div>
            <h3 className="font-semibold text-white mb-2">Manage Inventory</h3>
            <p className="text-gray-400 text-sm">
              Track expiry dates, stock levels, and product specifications.
            </p>
          </div>
        </div>
      </div>
      </div>
      
      {/* Global Notifications */}
      {notification && (
        <Notification
          type={notification.type}
          message={notification.message}
          duration={notification.duration}
          onClose={hideNotification}
        />
      )}
    </div>
  );
};

export default Home;