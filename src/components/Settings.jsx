import React from 'react';
import { useAuth } from '../contexts/AuthContext.jsx';

export default function Settings() {
  const { user } = useAuth();

  return (
    <div className="min-h-screen bg-gray-900 py-8">
      <div className="max-w-2xl mx-auto px-4">
        <div className="bg-gray-800 rounded-lg shadow-lg border border-gray-700 p-6">
          <h1 className="text-2xl font-bold text-white mb-6">Настройки</h1>
          
          <div className="text-center py-12">
            <div className="text-gray-400 text-lg mb-4">
              🚧 Страница настроек находится в разработке
            </div>
            <p className="text-gray-500">
              Здесь будут доступны настройки приложения, уведомлений и другие параметры.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

