import axios from 'axios';
import { API_URLS } from '../config/api-config.js';
import { setAuthToken } from '../utils/http-client.js';

export const login = async ({ username, password, clientId, clientSecret }) => {
  const params = new URLSearchParams();
  params.append('username', username);
  params.append('password', password);
  params.append('client_id', clientId);
  params.append('client_secret', clientSecret);

  const { data } = await axios.post(API_URLS.AUTH.LOGIN(), params);
  if (data?.access_token) setAuthToken(data.access_token);
  return data;
};




