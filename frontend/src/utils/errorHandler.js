/**
 * Centralized error handling utility for API responses
 */

/**
 * Parse API error response and extract meaningful error information
 * @param {Error} error - Axios error object
 * @returns {Object} - Parsed error with message and field-specific errors
 */
export const parseApiError = (error) => {
    // Network error (no response from server)
    if (!error.response) {
        return {
            message: 'Network error. Please check your internet connection.',
            fields: {},
            isNetworkError: true
        };
    }

    const { status, data } = error.response;

    // Handle different error status codes
    switch (status) {
        case 400: // Bad Request - Validation errors
            return parseValidationError(data);

        case 401: // Unauthorized
            return {
                message: data.message || 'Authentication failed. Please login again.',
                fields: {},
                isAuthError: true
            };

        case 403: // Forbidden
            return {
                message: data.message || 'You do not have permission to perform this action.',
                fields: {},
                isForbiddenError: true
            };

        case 404: // Not Found
            return {
                message: data.message || 'The requested resource was not found.',
                fields: {}
            };

        case 409: // Conflict (e.g., email already exists)
            return {
                message: data.message || data || 'A conflict occurred.',
                fields: {}
            };

        case 500: // Internal Server Error
            return {
                message: 'Server error. Please try again later.',
                fields: {}
            };

        default:
            return {
                message: data.message || data || 'An unexpected error occurred.',
                fields: {}
            };
    }
};

/**
 * Parse Spring Boot validation errors
 * @param {Object} data - Error response data
 * @returns {Object} - Parsed error with field-specific messages
 */
const parseValidationError = (data) => {
    // Spring Boot validation errors come in different formats
    // Format 1: { message: "Validation failed", errors: { field: "error message" } }
    // Format 2: { message: "Email is required" } (single field error)
    // Format 3: String message

    if (typeof data === 'string') {
        return {
            message: data,
            fields: {}
        };
    }

    if (data.errors && typeof data.errors === 'object') {
        // Extract field-specific errors
        const fields = {};
        Object.keys(data.errors).forEach(field => {
            fields[field] = data.errors[field];
        });

        return {
            message: data.message || 'Validation failed. Please check your input.',
            fields
        };
    }

    return {
        message: data.message || 'Invalid input. Please check your data.',
        fields: {}
    };
};

/**
 * Check if error is a network error
 * @param {Error} error - Error object
 * @returns {boolean}
 */
export const isNetworkError = (error) => {
    return !error.response || error.code === 'ECONNABORTED' || error.code === 'ERR_NETWORK';
};

/**
 * Check if error is an authentication error
 * @param {Error} error - Error object
 * @returns {boolean}
 */
export const isAuthError = (error) => {
    return error.response && error.response.status === 401;
};

/**
 * Log error in development mode
 * @param {string} context - Context where error occurred
 * @param {Error} error - Error object
 */
export const logError = (context, error) => {
    if (import.meta.env.DEV) {
        console.error(`[${context}] Error:`, error);
        if (error.response) {
            console.error('Response data:', error.response.data);
            console.error('Response status:', error.response.status);
        }
    }
};

/**
 * Format error message for user display
 * @param {Error} error - Error object
 * @param {string} defaultMessage - Default message if parsing fails
 * @returns {string} - User-friendly error message
 */
export const getErrorMessage = (error, defaultMessage = 'An error occurred') => {
    const parsed = parseApiError(error);
    return parsed.message || defaultMessage;
};
