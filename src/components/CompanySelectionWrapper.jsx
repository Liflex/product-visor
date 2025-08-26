import React from 'react';
import { useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext.jsx';
import CompanySelectionRequired from './CompanySelectionRequired.jsx';

export default function CompanySelectionWrapper({ children }) {
  const { needsCompanySelection, isLoading, isAuthenticated } = useAuth();
  const location = useLocation();

  // Если мы на странице логина, показываем содержимое без проверки компании
  if (location.pathname === '/login') {
    return children;
  }

  // Показываем загрузку только для авторизованных пользователей
  if (isLoading && isAuthenticated) {
    return (
      <div className="min-h-screen bg-gray-900 flex items-center justify-center">
        <div className="text-white text-lg">Загрузка...</div>
      </div>
    );
  }

  // Если нужен выбор компании и пользователь авторизован, показываем компонент выбора
  if (needsCompanySelection && isAuthenticated) {
    return <CompanySelectionRequired />;
  }

  // Иначе показываем основное содержимое
  return children;
}

