import axios from 'axios';
import { tokenStorage } from '../utils/tokenStorage';
import { logError } from '../utils/errorHandler';

// API configuration
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
const API_TIMEOUT = import.meta.env.VITE_API_TIMEOUT || 120000; // 30 seconds
const MAX_RETRIES = 3;
const RETRY_DELAY = 1000; // 1 second

// Create axios instance with base configuration
const api = axios.create({
    baseURL: API_BASE_URL,
    timeout: API_TIMEOUT,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Request interceptor - attach JWT token to every request (except auth endpoints)
api.interceptors.request.use(
    (config) => {
        const isAuthEndpoint = config.url?.includes('/auth/login') || config.url?.includes('/auth/register');
        if (!isAuthEndpoint) {
            const token = tokenStorage.getToken();
            if (token) {
                config.headers.Authorization = `Bearer ${token}`;
            }
        }

        // Log request in development mode
        if (import.meta.env.DEV) {
            console.log(`[API Request] ${config.method.toUpperCase()} ${config.url}`, config.data);
        }

        return config;
    },
    (error) => {
        logError('Request Interceptor', error);
        return Promise.reject(error);
    }
);

// Response interceptor - handle errors globally
api.interceptors.response.use(
    (response) => {
        // Log response in development mode
        if (import.meta.env.DEV) {
            console.log(`[API Response] ${response.config.method.toUpperCase()} ${response.config.url}`, response.data);
        }
        return response;
    },
    async (error) => {
        const originalRequest = error.config;

        // Log error in development mode
        logError('Response Interceptor', error);

        // Handle 401 Unauthorized - token expired or invalid
        if (error.response && error.response.status === 401) {
            // Clear authentication data
            tokenStorage.clearAll();

            // Redirect to login page only if not already there
            if (!window.location.pathname.includes('/login')) {
                window.location.href = '/login';
            }

            return Promise.reject(error);
        }

        // Retry logic for network errors
        if (!error.response && !originalRequest._retry) {
            originalRequest._retryCount = originalRequest._retryCount || 0;

            if (originalRequest._retryCount < MAX_RETRIES) {
                originalRequest._retryCount++;
                originalRequest._retry = true;

                // Wait before retrying
                await new Promise(resolve => setTimeout(resolve, RETRY_DELAY * originalRequest._retryCount));

                if (import.meta.env.DEV) {
                    console.log(`[API Retry] Attempt ${originalRequest._retryCount} for ${originalRequest.url}`);
                }

                return api(originalRequest);
            }
        }

        return Promise.reject(error);
    }
);

export default api;
