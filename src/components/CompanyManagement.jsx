import React, { useState, useEffect } from 'react';
import { useAuth } from '../contexts/AuthContext.jsx';
import { companyService } from '../services/companyService.js';

export default function CompanyManagement() {
  const { user, setCompany } = useAuth();
  const [companies, setCompanies] = useState([]);
  const [currentCompany, setCurrentCompany] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [editingCompany, setEditingCompany] = useState(null);
  const [formData, setFormData] = useState({
    name: '',
    note: '',
    link: ''
  });
  const [message, setMessage] = useState('');
  const [selectedAvatar, setSelectedAvatar] = useState(null);
  const [avatarPreview, setAvatarPreview] = useState(null);

  useEffect(() => {
    loadCompanies();
  }, []);

  const loadCompanies = async () => {
    setIsLoading(true);
    try {
      // Получаем все компании пользователя
      const companies = await companyService.getAllCompanies();
      
      // Получаем текущую активную компанию
      const currentCompanyResponse = await companyService.getCurrentCompany();
      const currentCompanyId = currentCompanyResponse?.companyId;
      
      // Обновляем состояние компаний
      setCompanies(companies);
      
      // Устанавливаем активную компанию
      if (currentCompanyId) {
        const activeCompany = companies.find(c => c.id === currentCompanyId);
        if (activeCompany) {
          setCurrentCompany(activeCompany);
          setCompany(currentCompanyId);
        }
      }
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
      // Создаем новую компанию через API
      const newCompany = await companyService.createCompany(formData);
      
      // Если выбран аватар, загружаем его
      if (selectedAvatar) {
        try {
          await companyService.uploadAvatar(newCompany.id, selectedAvatar);
          setMessage('Компания и аватар успешно созданы!');
          // Перезагружаем список компаний, чтобы получить обновленную компанию с аватаром
          await loadCompanies();
        } catch (avatarError) {
          setMessage('Компания создана, но не удалось загрузить аватар: ' + avatarError.message);
          // Все равно перезагружаем список
          await loadCompanies();
        }
      } else {
        setMessage('Компания успешно создана!');
        // Обновляем список компаний
        setCompanies(prev => [...prev, newCompany]);
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

  const handleEditCompany = (company) => {
    setEditingCompany(company);
    setFormData({
      name: company.name || '',
      note: company.note || '',
      link: company.link || ''
    });
    // Устанавливаем превью аватара если есть
    if (company.avatar) {
      setAvatarPreview(companyService.createAvatarUrl(company.avatar));
    } else {
      setAvatarPreview(null);
    }
  };

  const handleUpdateCompany = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setMessage('');
    
    try {
      // Обновляем компанию через API
      const updatedCompany = await companyService.updateCompany(editingCompany.id, formData);
      
      // Обновляем список компаний
      setCompanies(prev => prev.map(c => c.id === editingCompany.id ? updatedCompany : c));
      
      // Если редактируемая компания была активной, обновляем её
      if (currentCompany?.id === editingCompany.id) {
        setCurrentCompany(updatedCompany);
      }
      
      setFormData({
        name: '',
        description: '',
        address: '',
        phone: '',
        email: ''
      });
      setEditingCompany(null);
      setMessage('Компания успешно обновлена!');
    } catch (error) {
      setMessage('Ошибка при обновлении компании: ' + error.message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleCancelEdit = () => {
    setEditingCompany(null);
    setFormData({
      name: '',
      note: '',
      link: ''
    });
    setSelectedAvatar(null);
    setAvatarPreview(null);
  };

  const handleSwitchCompany = async (company) => {
    try {
      // Переключаемся на компанию
      await companyService.switchToCompany(company.id);
      
      // Обновляем текущую компанию
      setCurrentCompany(company);
      await setCompany(company.id);
      setMessage(`Переключились на компанию: ${company.name}`);
    } catch (error) {
      setMessage('Ошибка при переключении компании: ' + error.message);
    }
  };

  const handleDeleteCompany = async (companyId) => {
    if (!window.confirm('Вы уверены, что хотите удалить эту компанию?')) {
      return;
    }
    
    try {
      // Удаляем компанию через API
      await companyService.deleteCompany(companyId);
      
      setCompanies(prev => prev.filter(c => c.id !== companyId));
      
      // Если удаляемая компания была активной, переключаемся на первую доступную
      if (currentCompany?.id === companyId) {
        const remainingCompanies = companies.filter(c => c.id !== companyId);
        if (remainingCompanies.length > 0) {
          handleSwitchCompany(remainingCompanies[0]);
        } else {
          setCurrentCompany(null);
          await setCompany(null);
        }
      }
      
      setMessage('Компания успешно удалена');
    } catch (error) {
      setMessage('Ошибка при удалении компании: ' + error.message);
    }
  };

  if (isLoading && companies.length === 0) {
    return (
      <div className="min-h-screen bg-gray-900 flex items-center justify-center">
        <div className="text-white">Загрузка компаний...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-900 py-8">
      <div className="max-w-4xl mx-auto px-4">
        <div className="bg-gray-800 rounded-lg shadow-lg border border-gray-700 p-6">
          <div className="flex items-center justify-between mb-6">
            <h1 className="text-2xl font-bold text-white">Управление компаниями</h1>
            <button
              onClick={() => setShowCreateForm(true)}
              className="px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-md font-medium transition-colors"
            >
              Создать компанию
            </button>
          </div>

          {message && (
            <div className={`mb-4 p-3 rounded ${
              message.includes('Ошибка') 
                ? 'bg-red-900 border border-red-700 text-red-200' 
                : 'bg-green-900 border border-green-700 text-green-200'
            }`}>
              {message}
            </div>
          )}

          {/* Current Company */}
          {currentCompany && (
            <div className="mb-6 p-4 bg-indigo-900 border border-indigo-700 rounded-lg">
              <h2 className="text-lg font-semibold text-white mb-2">Текущая компания</h2>
              <div className="flex items-center space-x-3 text-indigo-200">
                {currentCompany.avatar && (
                  <img 
                    src={companyService.createAvatarUrl(currentCompany.avatar)} 
                    alt={`Аватар ${currentCompany.name}`}
                    className="w-12 h-12 rounded-full object-cover"
                  />
                )}
                <div>
                  <p className="font-medium">{currentCompany.name}</p>
                  {currentCompany.note && <p className="text-sm">{currentCompany.note}</p>}
                  {currentCompany.link && <p className="text-sm">🔗 {currentCompany.link}</p>}
                </div>
              </div>
            </div>
          )}

          {/* Companies List */}
          <div className="space-y-4">
            <h2 className="text-xl font-semibold text-white mb-4">Мои компании</h2>
            
            {companies.length === 0 ? (
              <div className="text-center py-8">
                <div className="text-gray-400 text-lg mb-4">У вас пока нет компаний</div>
                <button
                  onClick={() => setShowCreateForm(true)}
                  className="px-6 py-3 bg-indigo-600 hover:bg-indigo-700 text-white rounded-md font-medium transition-colors"
                >
                  Создать первую компанию
                </button>
              </div>
            ) : (
              <div className="grid gap-4">
                {companies.map(company => (
                  <div 
                    key={company.id} 
                    className={`p-4 rounded-lg border cursor-pointer hover:bg-opacity-80 transition-colors ${
                      currentCompany?.id === company.id
                        ? 'bg-indigo-900 border-indigo-700' 
                        : 'bg-gray-700 border-gray-600'
                    }`}
                    onClick={() => handleSwitchCompany(company)}
                  >
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3">
                          {company.avatar && (
                            <img 
                              src={companyService.createAvatarUrl(company.avatar)} 
                              alt={`Аватар ${company.name}`}
                              className="w-10 h-10 rounded-full object-cover"
                            />
                          )}
                          <div>
                            <h3 className="text-lg font-medium text-white">{company.name}</h3>
                            {currentCompany?.id === company.id && (
                              <span className="px-2 py-1 bg-green-600 text-white text-xs rounded-full">
                                Активна
                              </span>
                            )}
                          </div>
                        </div>
                        {company.note && <p className="text-gray-300 mt-1">{company.note}</p>}
                        {company.link && (
                          <div className="text-sm text-gray-400 mt-2">
                            <p>🔗 {company.link}</p>
                          </div>
                        )}
                      </div>
                      <div className="flex space-x-2" onClick={(e) => e.stopPropagation()}>
                        <button
                          onClick={() => handleEditCompany(company)}
                          className="px-3 py-1 bg-blue-600 hover:bg-blue-700 text-white text-sm rounded transition-colors"
                        >
                          Редактировать
                        </button>
                        <button
                          onClick={() => handleDeleteCompany(company.id)}
                          className="px-3 py-1 bg-red-600 hover:bg-red-700 text-white text-sm rounded transition-colors"
                        >
                          Удалить
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Create/Edit Company Modal */}
          {(showCreateForm || editingCompany) && (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
              <div className="bg-gray-800 rounded-lg p-6 w-full max-w-md mx-4">
                <h2 className="text-xl font-bold text-white mb-4">
                  {editingCompany ? 'Редактировать компанию' : 'Создать новую компанию'}
                </h2>
                
                <form onSubmit={editingCompany ? handleUpdateCompany : handleCreateCompany} className="space-y-4">
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
                      onClick={editingCompany ? handleCancelEdit : () => setShowCreateForm(false)}
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
                      {isLoading 
                        ? (editingCompany ? 'Обновление...' : 'Создание...') 
                        : (editingCompany ? 'Обновить' : 'Создать')
                      }
                    </button>
                  </div>
                </form>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
