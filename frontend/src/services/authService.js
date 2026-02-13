import api from './api';
import { parseApiError } from '../utils/errorHandler';

export const authService = {
    // Register a new user
    register: async (userData) => {
        try {
            const response = await api.post('/auth/register', userData);
            // Backend returns a string message: "User registered successfully"
            return { message: response.data, success: true };
        } catch (error) {
            const parsedError = parseApiError(error);
            throw parsedError;
        }
    },

    // Login user
    login: async (credentials) => {
        try {
            const response = await api.post('/auth/login', credentials);
            // Backend returns: { accessToken, tokenType, expiresIn, user }
            return response.data;
        } catch (error) {
            const parsedError = parseApiError(error);
            throw parsedError;
        }
    },
};
