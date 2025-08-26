import React, { createContext, useContext, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { setAuthToken, setCompanyId, getAuthToken } from '../utils/http-client.js';
import { API_URLS } from '../config/api-config.js';
import { profileService } from '../services/profileService.js';
import { companyService } from '../services/companyService.js';

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [user, setUser] = useState(null);
  const [needsCompanySelection, setNeedsCompanySelection] = useState(false);
  const [selectedCompanyId, setSelectedCompanyId] = useState(null);
  const navigate = useNavigate();

  // Проверяем токен при загрузке приложения
  useEffect(() => {
    const initializeAuth = async () => {
      const token = localStorage.getItem('authToken');
      const companyId = localStorage.getItem('companyId');
      
      console.log('Initializing auth with token:', token ? 'present' : 'absent');
      
      if (token) {
        setAuthToken(token);
        setIsAuthenticated(true);
        
        // Загружаем профиль пользователя
        try {
          const profile = await profileService.getProfile();
          setUser(profile);
          
          // Проверяем наличие выбранной компании
          if (companyId) {
            setCompanyId(companyId);
            setSelectedCompanyId(companyId);
            // Проверяем, что компания все еще существует и загружаем её данные
            try {
              const currentCompany = await companyService.getCompanyById(companyId);
              // Обновляем профиль с информацией о текущей компании
              setUser(prev => ({ ...prev, currentCompany }));
            } catch (error) {
              console.warn('Saved company not found, clearing company selection');
              localStorage.removeItem('companyId');
              setCompanyId(null);
              setSelectedCompanyId(null);
              setNeedsCompanySelection(true);
            }
          } else {
            setNeedsCompanySelection(true);
          }
        } catch (error) {
          console.error('Error loading profile:', error);
          // Если получили 401, значит токен недействителен - очищаем данные и перенаправляем на логин
          if (error.message && (error.message.includes('Unauthorized') || error.message.includes('401'))) {
            console.log('Token is invalid, redirecting to login...');
            localStorage.removeItem('authToken');
            localStorage.removeItem('companyId');
            setAuthToken(null);
            setCompanyId(null);
            setIsAuthenticated(false);
            setUser(null);
            // Используем window.location.href для принудительного перенаправления
            window.location.href = '/login';
            return; // Прерываем выполнение
          } else {
            // Для других ошибок используем базовые данные
            setUser({ username: 'user' });
            setNeedsCompanySelection(true);
          }
        }
      }
      
      setIsLoading(false);
    };

    initializeAuth();
  }, [navigate]);

  const login = async (credentials) => {
    try {
      // Преобразуем параметры в правильный формат для сервера
      const formData = new URLSearchParams();
      formData.append('username', credentials.username);
      formData.append('password', credentials.password);
      
      console.log('Sending auth request with params:', {
        username: credentials.username,
        password: '***'
      });

      const response = await fetch(API_URLS.AUTH.LOGIN(), {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: formData,
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'Ошибка авторизации');
      }

      const data = await response.json();
      
             if (data.access_token) {
         localStorage.setItem('authToken', data.access_token);
         setAuthToken(data.access_token);
         setIsAuthenticated(true);
         
         // Загружаем полный профиль пользователя
         try {
           const profile = await profileService.getProfile();
           setUser(profile);
         } catch (error) {
           console.error('Error loading profile after login:', error);
           // Если не удалось загрузить профиль, используем базовые данные
           const userData = {
             username: credentials.username,
             email: data.user?.email || '',
             firstName: data.user?.firstName || '',
             lastName: data.user?.lastName || '',
             avatar: data.user?.avatar || null,
             ...data.user
           };
           setUser(userData);
         }
         
         // Перенаправляем на главную страницу после успешного входа
         navigate('/');
         return data;
       } else {
         throw new Error('Токен не получен');
       }
    } catch (error) {
      console.error('Login error:', error);
      throw error;
    }
  };

  const logout = () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('companyId');
    setAuthToken(null);
    setCompanyId(null);
    setIsAuthenticated(false);
    setUser(null);
    navigate('/login');
  };

  const setCompany = async (companyId) => {
    localStorage.setItem('companyId', companyId);
    setCompanyId(companyId);
    setSelectedCompanyId(companyId);
    
    // Загружаем информацию о компании и обновляем контекст
    try {
      const currentCompany = await companyService.getCompanyById(companyId);
      setUser(prev => ({ ...prev, currentCompany }));
    } catch (error) {
      console.error('Error loading company info:', error);
    }
  };

  const completeCompanySelection = () => {
    setNeedsCompanySelection(false);
  };

  const value = {
    isAuthenticated,
    isLoading,
    user,
    companyId: selectedCompanyId,
    login,
    logout,
    setCompany,
    needsCompanySelection,
    completeCompanySelection,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
