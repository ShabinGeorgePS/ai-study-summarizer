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

    // Request password reset
    forgotPassword: async (email) => {
        try {
            const response = await api.post('/auth/forgot-password', { email });
            return response.data;
        } catch (error) {
            const parsedError = parseApiError(error);
            throw parsedError;
        }
    },

    // Verify reset token
    verifyResetToken: async (token) => {
        try {
            const response = await api.get('/auth/verify-reset-token', { params: { token } });
            return response.data;
        } catch (error) {
            const parsedError = parseApiError(error);
            throw parsedError;
        }
    },

    // Reset password
    resetPassword: async (token, newPassword) => {
        try {
            const response = await api.post('/auth/reset-password', {
                token,
                newPassword
            });
            return response.data;
        } catch (error) {
            const parsedError = parseApiError(error);
            throw parsedError;
        }
    },
};
