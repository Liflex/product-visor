import React, { useState, useEffect } from 'react';
import { API_URLS } from '../config/api-config.js';
import MarketplaceCredentials from './MarketplaceCredentials.jsx';
import { microservicesHttpClient } from '../utils/http-client.js';

export default function OzonCredentials() {
  const [status, setStatus] = useState(null);
  const [credentials, setCredentials] = useState(null);
  const [loading, setLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);

  const checkStatus = async () => {
    setLoading(true);
    try {
      const { data } = await microservicesHttpClient.get(API_URLS.OZON.CREDENTIALS() + '/status');
      setStatus(data);
    } catch (error) {
      console.error('Error checking Ozon credentials status:', error);
      setStatus({ exists: false });
    } finally {
      setLoading(false);
    }
  };

  const loadCredentials = async () => {
    try {
      const { data } = await microservicesHttpClient.get(API_URLS.OZON.CREDENTIALS());
      setCredentials(data);
    } catch (error) {
      console.error('Error loading Ozon credentials:', error);
      setCredentials(null);
    }
  };

  useEffect(() => {
    checkStatus();
    loadCredentials();
  }, []);

  const handleCredentialsSaved = () => {
    // Перепроверяем статус после сохранения
    checkStatus();
    loadCredentials();
    setShowForm(false);
  };

  const handleEdit = () => {
    setShowForm(true);
  };

  const handleCancel = () => {
    setShowForm(false);
  };

  const handleDelete = async () => {
    if (!credentials || !window.confirm('Вы уверены, что хотите удалить учетные данные Ozon?')) {
      return;
    }

    try {
      await microservicesHttpClient.delete(`${API_URLS.OZON.CREDENTIALS()}/${credentials.id}`);
      setCredentials(null);
      checkStatus();
    } catch (error) {
      console.error('Error deleting Ozon credentials:', error);
      alert('Ошибка при удалении учетных данных');
    }
  };

  return (
    <div className="max-w-4xl mx-auto mt-6 p-6">
      {/* Status Section */}
      <div className="mb-6 p-4 bg-gray-800 rounded-lg shadow-lg border border-gray-700">
        <h3 className="text-lg font-semibold text-white mb-4 flex items-center">
          <span className="text-2xl mr-2">🛒</span>
          Статус подключения к Ozon
        </h3>
        
        {loading ? (
          <div className="flex items-center justify-center py-4">
            <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-indigo-500"></div>
            <span className="ml-2 text-gray-300">Проверка статуса...</span>
          </div>
        ) : status ? (
          <>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className={`p-3 rounded-lg border ${
                status.exists ? 'bg-green-900 border-green-700' : 'bg-red-900 border-red-700'
              }`}>
                <div className="text-sm text-gray-300">Настроены</div>
                <div className={`text-lg font-semibold ${status.exists ? 'text-green-200' : 'text-red-200'}`}>
                  {status.exists ? 'Да' : 'Нет'}
                </div>
              </div>
              
              <div className={`p-3 rounded-lg border ${
                status.apiConnectionTest ? 'bg-green-900 border-green-700' : 'bg-red-900 border-red-700'
              }`}>
                <div className="text-sm text-gray-300">API Подключение</div>
                <div className={`text-lg font-semibold ${status.apiConnectionTest ? 'text-green-200' : 'text-red-200'}`}>
                  {status.apiConnectionTest ? 'Работает' : 'Ошибка'}
                </div>
              </div>
            </div>
            
            {(status.syncStatus || status.connectionError) && (
              <div className="mt-4 p-3 bg-gray-700 rounded-lg">
                {status.connectionError && (
                  <div className="mb-3">
                    <div className="text-sm text-gray-300 mb-1">Ошибка подключения к API</div>
                    <div className="text-red-400 font-medium">{status.connectionError}</div>
                  </div>
                )}
                
                {status.syncStatus && (
                  <div className="mb-3">
                    <div className="text-sm text-gray-300 mb-1">Статус синхронизации</div>
                    <div className="text-white font-medium">{status.syncStatus}</div>
                  </div>
                )}
                
                {status.lastSyncAt && (
                  <div className="mb-3">
                    <div className="text-sm text-gray-300 mb-1">Последняя синхронизация</div>
                    <div className="text-gray-300">
                      {new Date(status.lastSyncAt).toLocaleString('ru-RU')}
                    </div>
                  </div>
                )}
                
                {status.errorMessage && (
                  <div>
                    <div className="text-sm text-gray-300 mb-1">Последняя ошибка</div>
                    <div className="text-red-400 text-sm">{status.errorMessage}</div>
                  </div>
                )}
              </div>
            )}
          </>
        ) : (
          <div className="text-gray-400">Не удалось загрузить статус</div>
        )}
        
        <button 
          onClick={checkStatus}
          className="mt-4 bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-md font-medium transition-colors"
          disabled={loading}
        >
          {loading ? 'Обновление...' : 'Обновить статус'}
        </button>
      </div>

      {/* Credentials Section */}
      {credentials ? (
        !showForm ? (
        <div className="mb-6 p-4 bg-gray-800 rounded-lg shadow-lg border border-gray-700">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold text-white flex items-center">
              <span className="text-2xl mr-2">🛒</span>
              Учетные данные Ozon
            </h3>
            <div className="flex space-x-2">
              <button
                onClick={handleEdit}
                className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-1 rounded-md text-sm font-medium transition-colors"
              >
                Редактировать
              </button>
              <button
                onClick={handleDelete}
                className="bg-red-600 hover:bg-red-700 text-white px-3 py-1 rounded-md text-sm font-medium transition-colors"
              >
                Удалить
              </button>
            </div>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-1">Client ID</label>
              <div className="px-3 py-2 bg-gray-900 border border-gray-600 rounded-md text-gray-300">
                {credentials.clientId}
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-1">API Key</label>
              <div className="px-3 py-2 bg-gray-900 border border-gray-600 rounded-md text-gray-300">
                ••••••••••••••••
              </div>
            </div>
          </div>
          
          <div className="border-t border-gray-600 pt-4">
            <h4 className="text-sm font-medium text-gray-300 mb-3">Информация о синхронизации</h4>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-1">Статус синхронизации</label>
                <div className="px-3 py-2 bg-gray-900 border border-gray-600 rounded-md text-gray-300">
                  {credentials.syncStatus || 'Не установлено'}
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-1">Последняя синхронизация</label>
                <div className="px-3 py-2 bg-gray-900 border border-gray-600 rounded-md text-gray-300">
                  {credentials.lastSyncAt ? new Date(credentials.lastSyncAt).toLocaleString('ru-RU') : 'Не установлено'}
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-1">Последняя ошибка</label>
                <div className="px-3 py-2 bg-gray-900 border border-gray-600 rounded-md text-gray-300">
                  {credentials.errorMessage || 'Нет ошибок'}
                </div>
              </div>
            </div>
                     </div>
         </div>
       ) : (
         <div className="mb-6 p-4 bg-gray-800 rounded-lg shadow-lg border border-gray-700">
           <div className="flex items-center justify-between mb-4">
             <h3 className="text-lg font-semibold text-white flex items-center">
               <span className="text-2xl mr-2">🛒</span>
               Учетные данные Ozon
             </h3>
             <button
               onClick={() => setShowForm(true)}
               className="bg-green-600 hover:bg-green-700 text-white px-3 py-1 rounded-md text-sm font-medium transition-colors"
             >
               Добавить
             </button>
           </div>
           <div className="text-gray-400 text-center py-8">
             Учетные данные не настроены. Нажмите "Добавить" для настройки интеграции с Ozon.
           </div>
         </div>
       )
      ) : (
        <MarketplaceCredentials
          marketplace="OZON"
          marketplaceName="Ozon"
          marketplaceIcon="🛒"
          credentialsEndpoint={API_URLS.OZON.CREDENTIALS()}
          fields={[
            { 
              key: 'clientId', 
              label: 'Client ID', 
              placeholder: 'Введите Client ID от Ozon',
              required: true,
              validation: (value) => value && value.trim().length > 0 ? null : 'Client ID обязателен'
            },
            { 
              key: 'apiKey', 
              label: 'API Key', 
              placeholder: 'Введите API Key от Ozon', 
              type: 'password',
              required: true,
              validation: (value) => value && value.trim().length > 0 ? null : 'API Key обязателен'
            }
          ]}
          readOnlyFields={[
            { key: 'syncStatus', label: 'Статус синхронизации' },
            { key: 'lastSyncAt', label: 'Последняя синхронизация' },
            { key: 'errorMessage', label: 'Последняя ошибка' }
          ]}
          onSaved={handleCredentialsSaved}
          onCancel={handleCancel}
        />
      )}
      
      {/* Help Section */}
      <div className="mt-6 p-4 bg-gray-800 rounded-lg shadow-lg border border-gray-700">
        <h3 className="text-lg font-semibold text-white mb-3 flex items-center">
          <span className="text-xl mr-2">❓</span>
          Как получить API ключи Ozon?
        </h3>
        <div className="text-gray-300 space-y-2 text-sm">
          <p>1. Войдите в личный кабинет продавца Ozon</p>
          <p>2. Перейдите в раздел "Настройки" → "API"</p>
          <p>3. Создайте новое приложение или используйте существующее</p>
          <p>4. Скопируйте Client ID и API Key</p>
          <p>5. Вставьте их в форму и сохраните</p>
          <p className="text-yellow-400 mt-3">
            ⚠️ Важно: API ключи должны иметь права на чтение заказов и остатков
          </p>
        </div>
        
        <div className="mt-4 p-3 bg-blue-900 border border-blue-700 rounded-lg">
          <h4 className="text-sm font-medium text-blue-200 mb-2 flex items-center">
            <span className="text-lg mr-2">ℹ️</span>
            Информация о настройке
          </h4>
          <div className="text-blue-100 text-sm space-y-1">
            <p>• Для каждой компании может быть настроен только один набор учетных данных Ozon</p>
            <p>• При редактировании существующих данных они будут автоматически обновлены</p>
            <p>• Учетные данные привязаны к выбранной компании</p>
            <p>• При смене компании настройки будут загружены автоматически</p>
            <p>• Статус "API Подключение" проверяет реальное подключение к Ozon API</p>
          </div>
        </div>
      </div>
    </div>
  );
}




