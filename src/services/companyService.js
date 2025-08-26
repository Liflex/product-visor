import { API_URLS } from '../config/api-config.js';
import { getAuthToken, setCompanyId } from '../utils/http-client.js';

/**
 * Сервис для работы с компаниями
 */
export const companyService = {
  /**
   * Получить все компании пользователя
   */
  async getAllCompanies() {
    try {
      const token = getAuthToken();
      if (!token) {
        throw new Error('Токен авторизации не найден');
      }

      const response = await fetch(API_URLS.COMPANIES.BASE(), {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'Ошибка при получении списка компаний');
      }

      return await response.json();
    } catch (error) {
      console.error('Error fetching companies:', error);
      throw error;
    }
  },

  /**
   * Получить текущую активную компанию
   */
  async getCurrentCompany() {
    try {
      const token = getAuthToken();
      if (!token) {
        throw new Error('Токен авторизации не найден');
      }

      const response = await fetch(API_URLS.COMPANIES.CURRENT(), {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'Ошибка при получении текущей компании');
      }

      return await response.json();
    } catch (error) {
      console.error('Error fetching current company:', error);
      throw error;
    }
  },

  /**
   * Получить компанию по ID
   */
  async getCompanyById(id) {
    try {
      const token = getAuthToken();
      if (!token) {
        throw new Error('Токен авторизации не найден');
      }

      const response = await fetch(API_URLS.COMPANIES.BY_ID(id), {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'Ошибка при получении компании');
      }

      return await response.json();
    } catch (error) {
      console.error('Error fetching company by ID:', error);
      throw error;
    }
  },

  /**
   * Создать новую компанию
   */
  async createCompany(companyData) {
    try {
      const token = getAuthToken();
      if (!token) {
        throw new Error('Токен авторизации не найден');
      }

      const response = await fetch(API_URLS.COMPANIES.BASE(), {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(companyData),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'Ошибка при создании компании');
      }

      return await response.json();
    } catch (error) {
      console.error('Error creating company:', error);
      throw error;
    }
  },

  /**
   * Обновить компанию
   */
  async updateCompany(id, companyData) {
    try {
      const token = getAuthToken();
      if (!token) {
        throw new Error('Токен авторизации не найден');
      }

      const response = await fetch(API_URLS.COMPANIES.BY_ID(id), {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(companyData),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'Ошибка при обновлении компании');
      }

      return await response.json();
    } catch (error) {
      console.error('Error updating company:', error);
      throw error;
    }
  },

  /**
   * Удалить компанию
   */
  async deleteCompany(id) {
    try {
      const token = getAuthToken();
      if (!token) {
        throw new Error('Токен авторизации не найден');
      }

      const response = await fetch(API_URLS.COMPANIES.BY_ID(id), {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'Ошибка при удалении компании');
      }

      return await response.json();
    } catch (error) {
      console.error('Error deleting company:', error);
      throw error;
    }
  },

  /**
   * Переключиться на компанию (установить как активную)
   * Компания выбирается через заголовок X-Company-Id в последующих запросах
   */
  async switchToCompany(id) {
    try {
      const token = getAuthToken();
      if (!token) {
        throw new Error('Токен авторизации не найден');
      }

      // Устанавливаем компанию в localStorage и http-client
      localStorage.setItem('companyId', id);
      setCompanyId(id);
      
      // Возвращаем объект с id для совместимости
      return { id };
    } catch (error) {
      console.error('Error switching company:', error);
      throw error;
    }
  },

  /**
   * Создать URL для отображения аватара компании из base64
   */
  createAvatarUrl(avatarBase64) {
    if (!avatarBase64) return null;
    return `data:image/png;base64,${avatarBase64}`;
  },

  /**
   * Загрузить аватар компании
   */
  async uploadAvatar(companyId, file) {
    try {
      const token = getAuthToken();
      if (!token) {
        throw new Error('Токен авторизации не найден');
      }

      const formData = new FormData();
      formData.append('file', file);

      const response = await fetch(API_URLS.COMPANIES.AVATAR(companyId), {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
        },
        body: formData,
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.error || 'Ошибка при загрузке аватара');
      }

      return await response.json();
    } catch (error) {
      console.error('Error uploading company avatar:', error);
      throw error;
    }
  },


};




