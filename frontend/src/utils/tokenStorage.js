// Token storage utilities for JWT management

const TOKEN_KEY = 'auth_token';
const TOKEN_EXPIRY_KEY = 'auth_token_expiry';
const USER_INFO_KEY = 'user_info';

export const tokenStorage = {
  // Store JWT token with expiration time
  setToken: (token, expiresIn = null) => {
    if (token) {
      localStorage.setItem(TOKEN_KEY, token);

      // Store expiration timestamp if provided
      if (expiresIn) {
        const expiryTime = Date.now() + (expiresIn * 1000); // Convert seconds to milliseconds
        localStorage.setItem(TOKEN_EXPIRY_KEY, expiryTime.toString());
      }
    }
  },

  // Retrieve JWT token from localStorage
  getToken: () => {
    // Check if token is expired before returning
    if (tokenStorage.isTokenExpired()) {
      tokenStorage.clearAll();
      return null;
    }
    return localStorage.getItem(TOKEN_KEY);
  },

  // Remove JWT token from localStorage
  removeToken: () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(TOKEN_EXPIRY_KEY);
  },

  // Check if token is expired
  isTokenExpired: () => {
    const expiryTime = localStorage.getItem(TOKEN_EXPIRY_KEY);
    if (!expiryTime) {
      return false; // No expiry set, assume valid
    }
    return Date.now() > parseInt(expiryTime, 10);
  },

  // Store user information
  setUserInfo: (userInfo) => {
    if (userInfo) {
      localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo));
    }
  },

  // Retrieve user information
  getUserInfo: () => {
    const userInfo = localStorage.getItem(USER_INFO_KEY);
    return userInfo ? JSON.parse(userInfo) : null;
  },

  // Remove user information
  removeUserInfo: () => {
    localStorage.removeItem(USER_INFO_KEY);
  },

  // Clear all authentication data
  clearAll: () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(TOKEN_EXPIRY_KEY);
    localStorage.removeItem(USER_INFO_KEY);
  },

  // Check if user is authenticated
  isAuthenticated: () => {
    const token = localStorage.getItem(TOKEN_KEY);
    return !!token && !tokenStorage.isTokenExpired();
  }
};
