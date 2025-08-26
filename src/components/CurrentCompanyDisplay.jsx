import React, { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext.jsx';
import { companyService } from '../services/companyService.js';
import { getCompanyId } from '../utils/http-client.js';

export default function CurrentCompanyDisplay() {
  const { user } = useAuth();
  const [currentCompany, setCurrentCompany] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isOpen, setIsOpen] = useState(false);
  const menuRef = useRef(null);

  useEffect(() => {
    loadCurrentCompany();
  }, []);

  // Обновляем при изменении компании в localStorage
  useEffect(() => {
    const handleStorageChange = () => {
      loadCurrentCompany();
    };

    window.addEventListener('storage', handleStorageChange);
    return () => window.removeEventListener('storage', handleStorageChange);
  }, []);

  // Закрываем меню при клике вне его
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const loadCurrentCompany = async () => {
    const companyId = getCompanyId();
    if (!companyId) {
      setCurrentCompany(null);
      return;
    }

    // Если у нас есть информация о компании в контексте, используем её
    if (user?.currentCompany && user.currentCompany.id === companyId) {
      setCurrentCompany(user.currentCompany);
      return;
    }

    setIsLoading(true);
    try {
      const company = await companyService.getCompanyById(companyId);
      setCurrentCompany(company);
    } catch (error) {
      console.error('Error loading current company:', error);
      setCurrentCompany(null);
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center space-x-2 text-gray-300">
        <div className="w-4 h-4 border-2 border-gray-300 border-t-transparent rounded-full animate-spin"></div>
        <span className="text-sm">Загрузка...</span>
      </div>
    );
  }

  if (!currentCompany) {
    return (
      <div className="flex items-center space-x-2 text-gray-400">
        <span className="text-sm">Компания не выбрана</span>
      </div>
    );
  }

  return (
    <div className="relative" ref={menuRef}>
      {/* Company Avatar Button */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center space-x-2 text-gray-300 hover:text-white transition-colors focus:outline-none"
      >
        <div className="relative">
          <div className="w-8 h-8 rounded-full overflow-hidden bg-gray-700 border border-gray-600 flex items-center justify-center">
            {currentCompany.avatar ? (
              <img 
                src={companyService.createAvatarUrl(currentCompany.avatar)} 
                alt={`Аватар ${currentCompany.name}`}
                className="w-full h-full object-cover"
              />
            ) : (
              <span className="text-sm font-medium text-gray-300">
                {currentCompany.name.charAt(0).toUpperCase()}
              </span>
            )}
          </div>
          {/* Active indicator */}
          <div className="absolute bottom-0 right-0 w-3 h-3 bg-green-500 border-2 border-gray-800 rounded-full"></div>
        </div>
        <span className="text-sm font-medium hidden md:block">{currentCompany.name}</span>
        <svg 
          className={`w-4 h-4 transition-transform ${isOpen ? 'rotate-180' : ''}`} 
          fill="none" 
          stroke="currentColor" 
          viewBox="0 0 24 24"
        >
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
        </svg>
      </button>

      {/* Dropdown Menu */}
      {isOpen && (
        <div className="absolute right-0 mt-2 w-56 bg-gray-800 rounded-md shadow-lg border border-gray-700 z-50">
          <div className="py-1">
            {/* Company Info */}
            <div className="px-4 py-3 border-b border-gray-700">
              <div className="flex items-center space-x-3">
                <div className="w-10 h-10 rounded-full overflow-hidden bg-gray-700 border border-gray-600 flex items-center justify-center">
                  {currentCompany.avatar ? (
                    <img 
                      src={companyService.createAvatarUrl(currentCompany.avatar)} 
                      alt={`Аватар ${currentCompany.name}`}
                      className="w-full h-full object-cover"
                    />
                  ) : (
                    <span className="text-sm font-medium text-gray-300">
                      {currentCompany.name.charAt(0).toUpperCase()}
                    </span>
                  )}
                </div>
                <div>
                  <p className="text-sm font-medium text-white">{currentCompany.name}</p>
                  <p className="text-xs text-gray-400">Активная компания</p>
                </div>
              </div>
            </div>

            {/* Menu Items */}
            <Link
              to="/company-management"
              onClick={() => setIsOpen(false)}
              className="flex items-center px-4 py-2 text-sm text-gray-300 hover:bg-gray-700 hover:text-white transition-colors"
            >
              <svg className="w-4 h-4 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
              </svg>
              Управление компаниями
            </Link>

            <Link
              to="/company-selector"
              onClick={() => setIsOpen(false)}
              className="flex items-center px-4 py-2 text-sm text-gray-300 hover:bg-gray-700 hover:text-white transition-colors"
            >
              <svg className="w-4 h-4 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.367 2.684 3 3 0 00-5.367-2.684z" />
              </svg>
              Сменить компанию
            </Link>
          </div>
        </div>
      )}
    </div>
  );
}
