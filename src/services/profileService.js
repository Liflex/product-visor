import { API_URLS } from '../config/api-config.js';
import httpClient from '../utils/http-client.js';

/**
 * Сервис для работы с профилем пользователя
 */
export const profileService = {
  /**
   * Получить данные текущего пользователя
   */
  async getProfile() {
    try {
      const response = await httpClient.get(API_URLS.PROFILE.ME());
      return response.data;
    } catch (error) {
      console.error('Error fetching profile:', error);
      throw error;
    }
  },

  /**
   * Обновить данные профиля
   */
  async updateProfile(profileData) {
    try {
      const response = await httpClient.put(API_URLS.PROFILE.UPDATE(), profileData);
      return response.data;
    } catch (error) {
      console.error('Error updating profile:', error);
      throw error;
    }
  },

  /**
   * Загрузить аватар пользователя
   */
  async uploadAvatar(file) {
    try {
      const formData = new FormData();
      formData.append('file', file);

      const response = await httpClient.post(API_URLS.PROFILE.AVATAR(), formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      return response.data;
    } catch (error) {
      console.error('Error uploading avatar:', error);
      throw error;
    }
  },

  /**
   * Создать URL для отображения аватара из base64
   */
  createAvatarUrl(avatarBase64) {
    if (!avatarBase64) return null;
    return `data:image/png;base64,${avatarBase64}`;
  }
};
