/**
 * Main Application Component
 * Provides routing and layout for the Product Visor application
 */

import React from 'react';
import { BrowserRouter as Router, Route, Routes, Link, useLocation } from 'react-router-dom';
import Home from './components/Home';
import ProductAll from './components/ProductAll';
import ProductFormNew from './components/ProductFormNew';
import EditProductNew from './components/EditProductNew';
import ProductDetail from './components/ProductDetail';
import MarketList from './components/MarketList';
import ErrorMessage from './components/ui/error-message.jsx';

/**
 * Navigation component
 * @returns {JSX.Element} Navigation bar
 */
const Navigation = () => {
  const location = useLocation();
  
  const navigationItems = [
    { path: '/', label: 'Home', icon: '🏠' },
    { path: '/all-products', label: 'Products', icon: '📦' },
    { path: '/add-product', label: 'Add Product', icon: '➕' },
    { path: '/markets', label: 'Markets', icon: '🏪' }
  ];

  /**
   * Check if current path is active
   */
  const isActivePath = (path) => {
    return location.pathname === path;
  };

  return (
    <nav className="bg-gray-800 shadow-lg">
      <div className="container mx-auto px-4">
        <div className="flex justify-between items-center h-16">
          {/* Logo */}
          <Link 
            to="/" 
            className="flex items-center space-x-2 text-white text-lg font-bold hover:text-gray-300 transition-colors"
          >
            <span className="text-2xl">👁️</span>
            <span>Product Visor</span>
          </Link>

          {/* Navigation Links */}
          <ul className="flex space-x-1">
            {navigationItems.map(item => (
              <li key={item.path}>
                <Link
                  to={item.path}
                  className={`
                    flex items-center space-x-2 px-4 py-2 rounded-md text-sm font-medium transition-colors
                    ${isActivePath(item.path)
                      ? 'bg-gray-700 text-white'
                      : 'text-gray-300 hover:text-white hover:bg-gray-700'
                    }
                  `}
                >
                  <span>{item.icon}</span>
                  <span>{item.label}</span>
                </Link>
              </li>
            ))}
          </ul>
        </div>
      </div>
    </nav>
  );
};

/**
 * Layout component
 * @param {Object} props - Component props
 * @param {React.ReactNode} props.children - Child components
 * @returns {JSX.Element} Layout wrapper
 */
const Layout = ({ children }) => {
  return (
    <div className="min-h-screen w-full">
      <Navigation />
      <main className="w-full">
        {children}
      </main>
      
      {/* Footer */}
      <footer className="bg-gray-800 border-t border-gray-700 mt-8">
        <div className="container mx-auto px-4 py-6">
          <div className="text-center text-gray-400 text-sm">
            <p>&copy; 2024 Product Visor. Contact lens inventory management system.</p>
          </div>
        </div>
      </footer>
    </div>
  );
};

/**
 * Error boundary component
 */
class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('Application error:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <Layout>
          <div className="max-w-2xl mx-auto p-4">
            <ErrorMessage
              message="Something went wrong. Please refresh the page and try again."
              onRetry={() => window.location.reload()}
              type="error"
            />
          </div>
        </Layout>
      );
    }

    return this.props.children;
  }
}

/**
 * Main App component
 */
const App = () => {
  return (
    <ErrorBoundary>
      <Router>
        <Layout>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/all-products" element={<ProductAll />} />
            <Route path="/add-product" element={<ProductFormNew />} />
            <Route path="/edit-product/:productId" element={<EditProductNew />} />
            <Route path="/product/:productId" element={<ProductDetail />} />
            <Route path="/markets" element={<MarketList />} />
            
            {/* 404 Route */}
            <Route 
              path="*" 
              element={
                <div className="text-center py-12 px-4">
                  <h2 className="text-2xl font-bold text-white mb-4">Page Not Found</h2>
                  <p className="text-gray-400 mb-6">The page you're looking for doesn't exist.</p>
                  <Link 
                    to="/" 
                    className="text-indigo-400 hover:text-indigo-300 transition-colors"
                  >
                    Go back to home
                  </Link>
                </div>
              } 
            />
          </Routes>
        </Layout>
      </Router>
    </ErrorBoundary>
  );
};

export default App;