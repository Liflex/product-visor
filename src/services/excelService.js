import httpClient from '../utils/http-client.js';
import { API_URLS } from '../config/api-config.js';

export const exportLensesExcel = async (productIds) => {
  const url = `${API_URLS.PRODUCTS.BASE.replace('/product','')}/excel/lenses/export`;
  const response = await httpClient.post(url, { productIds }, {
    responseType: 'blob'
  });
  return response.data;
};

export default { exportLensesExcel }; 