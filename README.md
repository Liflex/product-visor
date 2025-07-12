# Product Visor - Contact Lens Inventory Management System

A modern React-based web application for managing contact lens inventory with dynamic attributes, advanced search capabilities, and comprehensive CRUD operations.

## 🚀 Features

### Core Functionality
- **Product Management**: Complete CRUD operations for contact lens products
- **Dynamic Attributes**: Flexible attribute system with support for multiple data types
- **Advanced Search & Filtering**: Powerful search with category and attribute-based filters
- **Image Management**: Product image upload with preview and optimization
- **Real-time Validation**: Comprehensive form validation with immediate feedback

### Technical Features
- **Modern React Architecture**: Functional components with custom hooks
- **Responsive Design**: Mobile-first approach with Tailwind CSS
- **Error Handling**: Comprehensive error boundaries and user feedback
- **Performance Optimized**: Debounced search, lazy loading, and efficient state management
- **Type Safety**: JSDoc documentation and prop validation

## 🛠️ Technology Stack

### Frontend
- **React 19.1.0** - Modern UI framework
- **Vite** - Fast build tool and development server
- **React Router** - Client-side routing
- **Tailwind CSS** - Utility-first CSS framework
- **Axios** - HTTP client with interceptors

### Backend Integration
- **Spring Boot API** - RESTful backend services
- **PostgreSQL** - Database for product and category storage
- **File Upload** - Image storage and retrieval system

## 📁 Project Structure

```
src/
├── components/           # React components
│   ├── ui/              # Reusable UI components
│   ├── search/          # Search and filter components
│   ├── Home.jsx         # Landing page
│   ├── ProductAll.jsx   # Product catalog
│   ├── ProductFormNew.jsx # Add product form
│   └── EditProductNew.jsx # Edit product form
├── hooks/               # Custom React hooks
│   ├── use-products.js  # Product management hook
│   ├── use-categories.js # Category management hook
│   └── use-form-validation.js # Form validation hook
├── services/            # API service layer
│   ├── productService.js
│   └── categoryService.js
├── utils/               # Utility functions
│   ├── validation.js    # Validation helpers
│   └── http-client.js   # HTTP client configuration
├── config/              # Configuration files
│   └── api-config.js    # API endpoints and settings
└── App.jsx             # Main application component
```

## 🚀 Getting Started

### Prerequisites
- Node.js 16+ and npm
- Running Spring Boot backend
- PostgreSQL database

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd product-visor
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Configure API endpoints**
   Update `src/config/api-config.js` with your backend URL:
   ```javascript
   export const API_CONFIG = {
     BASE_URL: 'http://localhost:8085',
     // ... other settings
   };
   ```

4. **Start development server**
   ```bash
   npm run dev
   ```

5. **Build for production**
   ```bash
   npm run build
   ```

## 💡 Usage Guide

### Managing Products

#### Adding Products
1. Navigate to "Add Product" from the main menu
2. Fill in the product name (required)
3. Select a product category (required)
4. Upload a product image (required for new products)
5. Fill in dynamic attributes based on selected category
6. Submit the form to create the product

#### Viewing Products
- Access the product catalog from "Products" in the main menu
- Use the search bar for quick text-based searches
- Apply advanced filters by clicking "Show Filters"
- View product details including images and attributes

#### Editing Products
1. Click "Edit" on any product card in the catalog
2. Modify product information as needed
3. Upload a new image if desired (optional for edits)
4. Save changes to update the product

#### Deleting Products
- Click "Delete" on any product card
- Confirm the deletion in the popup dialog
- Product will be permanently removed from inventory

### Search and Filtering

#### Basic Search
- Use the main search bar to find products by name, category, or attributes
- Search is debounced for optimal performance

#### Advanced Filtering
- Click "Show Filters" to access advanced options
- Filter by category, brand, power, color, expiry status
- Sort results by various criteria
- Clear individual filters or all filters at once

### Product Attributes

The system supports dynamic attributes with different types:

- **String**: Text values (brand, color, etc.)
- **Double**: Numeric values with decimals (power, diameter)
- **Integer**: Whole number values (quantity, count, etc.)
- **Date**: Date values (expiry date, manufacturing date)
- **Multiple Values**: Attributes that can have multiple entries

## 🔧 Configuration

### API Configuration
File: `src/config/api-config.js`

```javascript
export const API_CONFIG = {
  BASE_URL: 'http://localhost:8085',  // Backend URL
  API_VERSION: 'v1',                  // API version
  TIMEOUT: 10000,                     // Request timeout
  IMAGE: {
    MAX_SIZE: 5 * 1024 * 1024,       // 5MB max file size
    ALLOWED_TYPES: ['image/jpeg', 'image/png', 'image/webp']
  }
};
```

### Validation Rules
File: `src/utils/validation.js`

- Product names: 1-255 characters
- Images: Max 5MB, JPEG/PNG/WebP only
- Numeric values: Must be valid numbers
- Dates: Must be valid date format
- Required fields: Based on attribute configuration

## 🎨 Customization

### Styling
The application uses Tailwind CSS for styling. Key customization points:

- **Color Scheme**: Modify the gray/indigo theme in component classes
- **Layout**: Adjust container sizes and grid layouts
- **Typography**: Update font sizes and weights in Tailwind classes

### Components
All components are modular and can be easily customized:

- **UI Components**: Located in `src/components/ui/`
- **Form Components**: Reusable form fields with validation
- **Layout Components**: Navigation, layout, and error boundaries

## 🐛 Troubleshooting

### Common Issues

**Products not loading**
- Check API configuration in `api-config.js`
- Verify backend is running and accessible
- Check browser console for network errors

**Images not displaying**
- Ensure image service is running
- Check file permissions on image storage directory
- Verify image URLs in API responses

**Search not working**
- Check if backend search endpoint is implemented
- Fallback to client-side filtering is automatic
- Verify search parameters in network tab

**Form validation errors**
- Check required field configuration
- Verify attribute types match expected formats
- Ensure all custom validation rules are met

## 📚 API Integration

The frontend expects the following backend endpoints:

### Products
- `GET /api/v1/product` - Get all products
- `GET /api/v1/product/{id}` - Get product by ID
- `POST /api/v1/product` - Create new product
- `PUT /api/v1/product/{id}` - Update product
- `DELETE /api/v1/product/{id}` - Delete product

### Categories
- `GET /api/v1/category` - Get all categories with attributes

### Images
- `GET /api/v1/image/{filename}` - Get product image

## 🤝 Contributing

1. Follow the established code style and patterns
2. Use descriptive function and variable names
3. Add JSDoc documentation for public functions
4. Write components with single responsibility
5. Test your changes thoroughly

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
