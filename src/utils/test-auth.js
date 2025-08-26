/**
 * Test utility for authentication
 */

export const testAuth = async () => {
  const credentials = {
    username: 'admin',
    password: 'password'
  };

  try {
    console.log('=== TEST AUTH START ===');
    console.log('Testing auth with credentials:', {
      username: credentials.username,
      password: '[HIDDEN]'
    });
    
    // Преобразуем параметры в правильный формат для сервера
    const formData = new URLSearchParams();
    formData.append('username', credentials.username);
    formData.append('password', credentials.password);
    
    console.log('Request URL:', 'http://192.168.1.59:9099/api/auth/login');
    console.log('Request method:', 'POST');
    console.log('Request headers:', {
      'Content-Type': 'application/x-www-form-urlencoded'
    });
    console.log('Request body (formData):', formData.toString());
    
    const response = await fetch('http://192.168.1.59:9099/api/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: formData,
    });

    console.log('=== RESPONSE RECEIVED ===');
    console.log('Response status:', response.status);
    console.log('Response status text:', response.statusText);
    console.log('Response headers:', Object.fromEntries(response.headers.entries()));

    if (!response.ok) {
      const errorData = await response.text();
      console.error('=== ERROR RESPONSE ===');
      console.error('Status:', response.status);
      console.error('Error data:', errorData);
      return { success: false, error: errorData, status: response.status };
    }

    const data = await response.json();
    console.log('=== SUCCESS RESPONSE ===');
    console.log('Response data:', data);
    console.log('Token type:', data.token_type);
    console.log('Token expires in:', data.expires_in, 'seconds');
    console.log('User:', data.user);
    console.log('=== TEST AUTH COMPLETE ===');
    return { success: true, data };
  } catch (error) {
    console.error('=== REQUEST ERROR ===');
    console.error('Error type:', error.name);
    console.error('Error message:', error.message);
    console.error('Error stack:', error.stack);
    return { success: false, error: error.message };
  }
};

// Test function that can be called from browser console
window.testAuth = testAuth;
