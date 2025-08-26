import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext.jsx';
import { companyService } from '../services/companyService.js';

export default function CompanySelectionRequired() {
  const { user, setCompany, completeCompanySelection } = useAuth();
  const [companies, setCompanies] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    note: '',
    link: ''
  });
  const [message, setMessage] = useState('');
  const [selectedAvatar, setSelectedAvatar] = useState(null);
  const [avatarPreview, setAvatarPreview] = useState(null);

  useEffect(() => {
    // Загружаем компании только если пользователь авторизован
    if (user) {
      loadCompanies();
    }
  }, [user]);

  const loadCompanies = async () => {
    // Не загружаем компании, если пользователь не авторизован
    if (!user) {
      return;
    }
    
    setIsLoading(true);
    try {
      const companies = await companyService.getAllCompanies();
      setCompanies(companies);
    } catch (error) {
      setMessage('Ошибка при загрузке компаний: ' + error.message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleAvatarChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setSelectedAvatar(file);
      // Создаем превью
      const reader = new FileReader();
      reader.onload = (e) => {
        setAvatarPreview(e.target.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleCreateCompany = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setMessage('');
    
    try {
      const newCompany = await companyService.createCompany(formData);
      
      // Если выбран аватар, загружаем его
      if (selectedAvatar) {
        try {
          await companyService.uploadAvatar(newCompany.id, selectedAvatar);
          setMessage('Компания и аватар успешно созданы!');
          // Перезагружаем список компаний, чтобы получить обновленную компанию с аватаром
          await loadCompanies();
          // Находим обновленную компанию с аватаром
          const updatedCompanies = await companyService.getAllCompanies();
          const updatedCompany = updatedCompanies.find(c => c.id === newCompany.id);
          if (updatedCompany) {
            await handleSelectCompany(updatedCompany);
          } else {
            await handleSelectCompany(newCompany);
          }
        } catch (avatarError) {
          setMessage('Компания создана, но не удалось загрузить аватар: ' + avatarError.message);
          await handleSelectCompany(newCompany);
        }
      } else {
        setMessage('Компания успешно создана!');
        // Автоматически выбираем созданную компанию
        await handleSelectCompany(newCompany);
      }
      
      setFormData({
        name: '',
        note: '',
        link: ''
      });
      setSelectedAvatar(null);
      setAvatarPreview(null);
      setShowCreateForm(false);
    } catch (error) {
      setMessage('Ошибка при создании компании: ' + error.message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSelectCompany = async (company) => {
    try {
      await companyService.switchToCompany(company.id);
      await setCompany(company.id);
      completeCompanySelection();
      setMessage(`Выбрана компания: ${company.name}`);
    } catch (error) {
      setMessage('Ошибка при выборе компании: ' + error.message);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-900 flex items-center justify-center">
        <div className="text-white text-lg">Загрузка компаний...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-900 flex items-center justify-center p-4">
      <div className="bg-gray-800 rounded-lg shadow-lg border border-gray-700 p-8 max-w-2xl w-full">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-white mb-4">Добро пожаловать!</h1>
          <p className="text-gray-300 text-lg">
            Для продолжения работы необходимо выбрать или создать компанию
          </p>
        </div>

        {message && (
          <div className={`mb-6 p-4 rounded ${
            message.includes('Ошибка') 
              ? 'bg-red-900 border border-red-700 text-red-200' 
              : 'bg-green-900 border border-green-700 text-green-200'
          }`}>
            {message}
          </div>
        )}

        {/* Existing Companies */}
        {companies.length > 0 && (
          <div className="mb-8">
            <h2 className="text-xl font-semibold text-white mb-4">Выберите компанию</h2>
            <div className="grid gap-4">
              {companies.map(company => (
                <div 
                  key={company.id} 
                  className="p-4 bg-gray-700 border border-gray-600 rounded-lg hover:bg-gray-600 transition-colors cursor-pointer"
                  onClick={() => handleSelectCompany(company)}
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-3">
                      {company.avatar && (
                        <img 
                          src={companyService.createAvatarUrl(company.avatar)} 
                          alt={`Аватар ${company.name}`}
                          className="w-12 h-12 rounded-full object-cover"
                        />
                      )}
                      <div>
                        <h3 className="text-lg font-medium text-white">{company.name}</h3>
                        {company.note && <p className="text-gray-300 mt-1">{company.note}</p>}
                        {company.link && (
                          <div className="text-sm text-gray-400 mt-2">
                            <p>🔗 {company.link}</p>
                          </div>
                        )}
                      </div>
                    </div>
                    <div className="text-gray-400">
                      <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                      </svg>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Create New Company */}
        <div className="text-center">
          <div className="mb-4">
            <span className="text-gray-400">или</span>
          </div>
          <button
            onClick={() => setShowCreateForm(true)}
            className="px-6 py-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded-md font-medium transition-colors"
          >
            Создать новую компанию
          </button>
        </div>

        {/* Create Company Modal */}
        {showCreateForm && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-gray-800 rounded-lg p-6 w-full max-w-md mx-4">
              <h2 className="text-xl font-bold text-white mb-4">Создать новую компанию</h2>
              
              <form onSubmit={handleCreateCompany} className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-300 mb-1">
                    Название компании *
                  </label>
                  <input
                    type="text"
                    name="name"
                    value={formData.name}
                    onChange={handleInputChange}
                    required
                    className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-white placeholder-gray-400"
                    placeholder="Введите название компании"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-300 mb-1">
                    Заметка
                  </label>
                  <textarea
                    name="note"
                    value={formData.note}
                    onChange={handleInputChange}
                    rows="3"
                    className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-white placeholder-gray-400"
                    placeholder="Дополнительная информация о компании"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-300 mb-1">
                    Ссылка
                  </label>
                  <input
                    type="url"
                    name="link"
                    value={formData.link}
                    onChange={handleInputChange}
                    className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-white placeholder-gray-400"
                    placeholder="https://example.com"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-300 mb-1">
                    Аватар компании
                  </label>
                  <input
                    type="file"
                    accept="image/*"
                    onChange={handleAvatarChange}
                    className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-white placeholder-gray-400"
                  />
                  {(selectedAvatar || avatarPreview) && (
                    <div className="mt-2">
                      {selectedAvatar && (
                        <p className="text-sm text-gray-400 mb-2">
                          Выбран файл: {selectedAvatar.name}
                        </p>
                      )}
                      <div className="flex items-center space-x-4">
                        <img 
                          src={avatarPreview} 
                          alt="Превью аватара"
                          className="w-16 h-16 rounded-full object-cover border border-gray-600"
                        />
                        <button
                          type="button"
                          onClick={() => {
                            setSelectedAvatar(null);
                            setAvatarPreview(null);
                          }}
                          className="text-sm text-red-400 hover:text-red-300"
                        >
                          Удалить
                        </button>
                      </div>
                    </div>
                  )}
                </div>

                <div className="flex justify-end space-x-3 pt-4">
                  <button
                    type="button"
                    onClick={() => {
                      setShowCreateForm(false);
                      setSelectedAvatar(null);
                      setAvatarPreview(null);
                    }}
                    className="px-4 py-2 bg-gray-600 hover:bg-gray-700 text-white rounded-md font-medium transition-colors"
                  >
                    Отмена
                  </button>
                  <button
                    type="submit"
                    disabled={isLoading}
                    className={`px-4 py-2 rounded-md font-medium transition-colors ${
                      isLoading
                        ? 'bg-gray-600 cursor-not-allowed'
                        : 'bg-green-600 hover:bg-green-700'
                    } text-white`}
                  >
                    {isLoading ? 'Создание...' : 'Создать'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
