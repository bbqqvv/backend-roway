import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import { ApiResponse } from './types.ts';

const BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';

const apiClient: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Add JWT Token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('roway_auth_token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Handle Errors
apiClient.interceptors.response.use(
  (response: AxiosResponse<ApiResponse<any>>) => {
    // If our Backend returns a code different from 1000, we could throw it as error
    if (response.data && response.data.code && response.data.code !== 1000) {
      return Promise.reject(response.data);
    }
    return response;
  },
  (error) => {
    if (error.response && error.response.status === 401) {
      // Handle Unauthorized (e.g. redirect to login)
      localStorage.removeItem('roway_auth_token');
    }
    return Promise.reject(error);
  }
);

export default apiClient;
