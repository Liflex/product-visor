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
import Orders from './components/Orders';
import OzonOrders from './components/OzonOrders';
import YandexOrders from './components/YandexOrders.jsx';
import OrderAnalysis from './components/OrderAnalysis';
import Warehouse from './components/Warehouse.jsx';
import WarehouseManagement from './components/WarehouseManagement.jsx';
import Inventory from './components/Inventory.jsx';
import StockSync from './components/StockSync.jsx';
import Analytics from './components/Analytics.jsx';
import Categories from './components/Categories.jsx';
import ErrorMessage from './components/ui/error-message.jsx';
import { API_URLS } from './config/api-config.js';
import { getCategories } from './services/categoryService.js';
import Login from './components/Login.jsx';
import CompanySelector from './components/CompanySelector.jsx';
import OzonCredentials from './components/OzonCredentials.jsx';
import YandexCredentials from './components/YandexCredentials.jsx';
import UserProfile from './components/UserProfile.jsx';
import CompanyManagement from './components/CompanyManagement.jsx';
import UserMenu from './components/UserMenu.jsx';
import Settings from './components/Settings.jsx';
import CurrentCompanyDisplay from './components/CurrentCompanyDisplay.jsx';
import CompanySelectionRequired from './components/CompanySelectionRequired.jsx';
import CompanySelectionWrapper from './components/CompanySelectionWrapper.jsx';
import { AuthProvider, useAuth } from './contexts/AuthContext.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import DropdownMenu from './components/DropdownMenu.jsx';

/**
 * Navigation component with auth context
 */
const NavigationWithAuth = () => {
  const { user } = useAuth();
  const location = useLocation();
  
  // Dashboard/Overview
  const dashboardItems = [
    { path: '/', label: 'Dashboard', icon: '📊' },
    { path: '/analytics', label: 'Analytics', icon: '📈' },
  ];

  // Product management
  const productItems = [
    { path: '/all-products', label: 'All Products', icon: '📦' },
    { path: '/add-product', label: 'Add Product', icon: '➕' },
    { path: '/categories', label: 'Categories', icon: '🏷️' },
  ];

  // Inventory & Warehouse management
  const inventoryItems = [
    { path: '/inventory', label: 'Inventory', icon: '📋' },
    { path: '/warehouse', label: 'Warehouse', icon: '🏭' },
    { path: '/warehouse-management', label: 'Warehouse Management', icon: '🏗️' },
    { path: '/stock-sync', label: 'Stock Sync', icon: '🔄' },
    { path: '/markets', label: 'Marketplaces', icon: '🏪' },
  ];

  // Orders management
  const ordersItems = [
    { path: '/orders', label: 'All Orders', icon: '📋' },
    { path: '/ozon/orders', label: 'Ozon Orders', icon: '🛒' },
    { path: '/yandex/orders', label: 'Yandex Orders', icon: '🟡' },
    { path: '/order-analysis', label: 'Order Analysis', icon: '📊' },
  ];

  // Settings & Configuration
  const settingsItems = [
    { path: '/profile', label: 'Profile', icon: '👤' },
    { path: '/company-management', label: 'Company', icon: '🏢' },
    { path: '/ozon/credentials', label: 'Ozon Settings', icon: '⚙️' },
    { path: '/yandex/credentials', label: 'Yandex Settings', icon: '⚙️' },
    { path: '/settings', label: 'System Settings', icon: '🔧' },
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
            {/* Dashboard dropdown */}
            <li>
              <DropdownMenu 
                items={dashboardItems}
                label="Dashboard"
                icon="📊"
              />
            </li>

            {/* Products dropdown */}
            <li>
              <DropdownMenu 
                items={productItems}
                label="Products"
                icon="📦"
              />
            </li>

            {/* Inventory dropdown */}
            <li>
              <DropdownMenu 
                items={inventoryItems}
                label="Inventory"
                icon="📋"
              />
            </li>

            {/* Orders dropdown */}
            <li>
              <DropdownMenu 
                items={ordersItems}
                label="Orders"
                icon="📋"
              />
            </li>

            {/* Settings dropdown */}
            <li>
              <DropdownMenu 
                items={settingsItems}
                label="Settings"
                icon="⚙️"
              />
            </li>
          </ul>

          {/* User menu */}
          <div className="flex items-center space-x-4">
            <CurrentCompanyDisplay />
            <UserMenu />
          </div>
        </div>
      </div>
    </nav>
  );
};

/**
 * AddProductPage component with categories loading
 */
const AddProductPage = () => {
  const [categories, setCategories] = React.useState([]);
  const [loading, setLoading] = React.useState(true);
  const location = useLocation();

  React.useEffect(() => {
    const fetchCategories = async () => {
      try {
        const categoriesData = await getCategories();
        setCategories(categoriesData);
      } catch (error) {
        console.error('Error fetching categories:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchCategories();
  }, []);

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-indigo-500"></div>
      </div>
    );
  }

  // Pass location state to ProductFormNew
  return <ProductFormNew categories={categories} locationState={location.state} />;
};



/**
 * Layout component
 * @param {Object} props - Component props
 * @param {React.ReactNode} props.children - Child components
 * @returns {JSX.Element} Layout wrapper
 */
const Layout = ({ children }) => {
  const location = useLocation();
  const isLoginPage = location.pathname === '/login';

  return (
    <CompanySelectionWrapper>
      <div className="min-h-screen w-full">
        {!isLoginPage && <NavigationWithAuth />}
        <main className="w-full">
          {children}
        </main>
        
        {/* Footer */}
        {!isLoginPage && (
          <footer className="bg-gray-800 border-t border-gray-700 mt-8">
            <div className="container mx-auto px-4 py-6">
              <div className="text-center text-gray-400 text-sm">
                <p>&copy; 2024 Product Visor. Contact lens inventory management system.</p>
              </div>
            </div>
          </footer>
        )}
      </div>
    </CompanySelectionWrapper>
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
        <div className="min-h-screen w-full bg-gray-900">
          <div className="max-w-2xl mx-auto p-4 pt-20">
            <ErrorMessage
              message="Something went wrong. Please refresh the page and try again."
              onRetry={() => window.location.reload()}
              type="error"
            />
          </div>
        </div>
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
        <AuthProvider>
          <Layout>
            <Routes>
              {/* Public routes */}
              <Route path="/login" element={<Login />} />
              
              {/* Protected routes */}
              <Route path="/" element={
                <ProtectedRoute>
                  <Home />
                </ProtectedRoute>
              } />
              <Route path="/all-products" element={
                <ProtectedRoute>
                  <ProductAll />
                </ProtectedRoute>
              } />
              <Route path="/add-product" element={
                <ProtectedRoute>
                  <AddProductPage />
                </ProtectedRoute>
              } />
              <Route path="/edit-product/:productId" element={
                <ProtectedRoute>
                  <EditProductNew />
                </ProtectedRoute>
              } />
              <Route path="/product/:productId" element={
                <ProtectedRoute>
                  <ProductDetail />
                </ProtectedRoute>
              } />
              <Route path="/markets" element={
                <ProtectedRoute>
                  <MarketList />
                </ProtectedRoute>
              } />
              <Route path="/orders" element={
                <ProtectedRoute>
                  <Orders />
                </ProtectedRoute>
              } />
              <Route path="/ozon/orders" element={
                <ProtectedRoute>
                  <OzonOrders />
                </ProtectedRoute>
              } />
              <Route path="/yandex/orders" element={
                <ProtectedRoute>
                  <YandexOrders />
                </ProtectedRoute>
              } />
              <Route path="/order-analysis" element={
                <ProtectedRoute>
                  <OrderAnalysis />
                </ProtectedRoute>
              } />
              <Route path="/warehouse" element={
                <ProtectedRoute>
                  <Warehouse />
                </ProtectedRoute>
              } />
              <Route path="/warehouse-management" element={
                <ProtectedRoute>
                  <WarehouseManagement />
                </ProtectedRoute>
              } />
              <Route path="/inventory" element={
                <ProtectedRoute>
                  <Inventory />
                </ProtectedRoute>
              } />
              <Route path="/stock-sync" element={
                <ProtectedRoute>
                  <StockSync />
                </ProtectedRoute>
              } />
              <Route path="/analytics" element={
                <ProtectedRoute>
                  <Analytics />
                </ProtectedRoute>
              } />
              <Route path="/categories" element={
                <ProtectedRoute>
                  <Categories />
                </ProtectedRoute>
              } />
              <Route path="/companies" element={
                <ProtectedRoute>
                  <CompanySelector />
                </ProtectedRoute>
              } />
              <Route path="/company-management" element={
                <ProtectedRoute>
                  <CompanyManagement />
                </ProtectedRoute>
              } />
              <Route path="/profile" element={
                <ProtectedRoute>
                  <UserProfile />
                </ProtectedRoute>
              } />
              <Route path="/settings" element={
                <ProtectedRoute>
                  <Settings />
                </ProtectedRoute>
              } />
              <Route path="/ozon/credentials" element={
                <ProtectedRoute>
                  <OzonCredentials />
                </ProtectedRoute>
              } />
              <Route path="/yandex/credentials" element={
                <ProtectedRoute>
                  <YandexCredentials />
                </ProtectedRoute>
              } />
              
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
        </AuthProvider>
      </Router>
    </ErrorBoundary>
  );
};

export default App;