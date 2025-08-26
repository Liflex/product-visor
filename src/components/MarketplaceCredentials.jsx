import React, { useEffect, useState } from 'react';
import { microservicesHttpClient } from '../utils/http-client.js';

export default function MarketplaceCredentials({ 
  marketplace, 
  marketplaceName, 
  marketplaceIcon,
  credentialsEndpoint,
  fields = [
    { key: 'clientId', label: 'Client ID', placeholder: 'Client ID' },
    { key: 'apiKey', label: 'API Key', placeholder: 'API Key', type: 'password' }
  ],
  readOnlyFields = [],
  onSaved = null,
  onCancel = null
}) {
  const [form, setForm] = useState({});
  const [readOnlyData, setReadOnlyData] = useState({});
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});

  // Инициализируем форму с пустыми значениями для всех полей
  useEffect(() => {
    const initialForm = {};
    fields.forEach(field => {
      initialForm[field.key] = '';
    });
    setForm(initialForm);
  }, [fields]);

  const validateForm = () => {
    const newErrors = {};
    let isValid = true;

    fields.forEach(field => {
      const value = form[field.key];
      
      // Проверяем обязательные поля
      if (field.required && (!value || value.trim().length === 0)) {
        newErrors[field.key] = `${field.label} обязателен`;
        isValid = false;
      }
      
      // Проверяем кастомную валидацию
      if (field.validation && value) {
        const validationError = field.validation(value);
        if (validationError) {
          newErrors[field.key] = validationError;
          isValid = false;
        }
      }
    });

    setErrors(newErrors);
    return isValid;
  };

  const load = async () => {
    setLoading(true);
    try {
      const { data } = await microservicesHttpClient.get(credentialsEndpoint);
      if (data) {
        const updatedForm = {};
        const updatedReadOnly = {};
        
        fields.forEach(field => {
          if (field.key === 'apiKey' && data[field.key]) {
            updatedForm[field.key] = '********';
          } else {
            updatedForm[field.key] = data[field.key] || '';
          }
        });
        
        readOnlyFields.forEach(field => {
          updatedReadOnly[field.key] = data[field.key] || '';
        });
        
        setForm(updatedForm);
        setReadOnlyData(updatedReadOnly);
      }
    } catch (error) {
      console.error(`Error loading ${marketplaceName} credentials:`, error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { 
    load(); 
  }, [credentialsEndpoint]);

  const handleSave = async () => {
    setMessage('');
    setErrors({});
    
    if (!validateForm()) {
      setMessage('Пожалуйста, исправьте ошибки в форме');
      return;
    }
    
    setLoading(true);
    try {
      const payload = {};
      fields.forEach(field => {
        // Не отправляем замаскированные API ключи
        if (field.key === 'apiKey' && form[field.key] === '********') {
          return;
        }
        payload[field.key] = form[field.key];
      });
      
      await microservicesHttpClient.post(credentialsEndpoint, payload);
      setMessage('Сохранено');
      
      // Вызываем callback если предоставлен
      if (onSaved) {
        onSaved();
      }
      
      // Перезагружаем данные после сохранения
      await load();
    } catch (error) {
      setMessage(error.message || 'Ошибка сохранения');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (key, value) => {
    setForm(prev => ({ ...prev, [key]: value }));
    
    // Очищаем ошибку для этого поля при изменении
    if (errors[key]) {
      setErrors(prev => ({ ...prev, [key]: null }));
    }
  };

  return (
    <div className="max-w-xl mx-auto p-6 bg-gray-800 rounded-lg shadow-lg border border-gray-700">
      <div className="flex items-center space-x-3 mb-4">
        <span className="text-2xl">{marketplaceIcon}</span>
        <h2 className="text-lg font-semibold text-white">
          {marketplaceName} API ключи (для выбранной компании)
        </h2>
      </div>
      
      {message && (
        <div className={`mb-4 p-3 rounded ${
          message === 'Сохранено' 
            ? 'bg-green-900 border border-green-700 text-green-200' 
            : 'bg-red-900 border border-red-700 text-red-200'
        }`}>
          {message}
        </div>
      )}
      
      <div className="space-y-4">
        {fields.map(field => (
          <div key={field.key}>
            <label className="block text-sm font-medium text-gray-300 mb-1">
              {field.label}
              {field.required && <span className="text-red-400 ml-1">*</span>}
            </label>
            <input 
              type={field.type || 'text'}
              className={`w-full px-3 py-2 bg-gray-700 border rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-white placeholder-gray-400 ${
                errors[field.key] ? 'border-red-500' : 'border-gray-600'
              }`}
              placeholder={field.placeholder}
              value={form[field.key] || ''} 
              onChange={e => handleInputChange(field.key, e.target.value)}
            />
            {errors[field.key] && (
              <div className="text-red-400 text-sm mt-1">{errors[field.key]}</div>
            )}
          </div>
        ))}
        
        {readOnlyFields.length > 0 && (
          <div className="border-t border-gray-600 pt-4 mt-4">
            <h3 className="text-sm font-medium text-gray-300 mb-3">Информация о синхронизации</h3>
            {readOnlyFields.map(field => (
              <div key={field.key} className="mb-3">
                <label className="block text-sm font-medium text-gray-400 mb-1">
                  {field.label}
                </label>
                <div className="px-3 py-2 bg-gray-900 border border-gray-600 rounded-md text-gray-300">
                  {readOnlyData[field.key] || 'Не установлено'}
                </div>
              </div>
            ))}
          </div>
        )}
        
        <div className="flex space-x-2">
          <button 
            className="flex-1 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-md font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            onClick={handleSave}
            disabled={loading}
          >
            {loading ? 'Сохранение...' : 'Сохранить'}
          </button>
          {onCancel && (
            <button 
              className="px-4 py-2 bg-gray-600 hover:bg-gray-700 text-white rounded-md font-medium transition-colors"
              onClick={onCancel}
              disabled={loading}
            >
              Отмена
            </button>
          )}
        </div>
      </div>
    </div>
  );
}

