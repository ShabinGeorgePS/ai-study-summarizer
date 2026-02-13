import { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/authService';
import { tokenStorage } from '../utils/tokenStorage';

const AuthContext = createContext(null);

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    // Check if user is already authenticated on mount
    useEffect(() => {
        const initAuth = () => {
            const token = tokenStorage.getToken();
            const userInfo = tokenStorage.getUserInfo();

            if (token && userInfo && !tokenStorage.isTokenExpired()) {
                // Restore user from localStorage
                setUser({
                    authenticated: true,
                    ...userInfo
                });
            } else {
                // Clear invalid/expired auth data
                tokenStorage.clearAll();
            }
            setLoading(false);
        };

        initAuth();
    }, []);

    const login = async (credentials) => {
        try {
            const response = await authService.login(credentials);

            // Store the JWT token with expiration (backend returns accessToken, not token)
            if (response.accessToken) {
                tokenStorage.setToken(response.accessToken, response.expiresIn);

                // Store user info
                const userInfo = {
                    id: response.user.id,
                    email: response.user.email
                };
                tokenStorage.setUserInfo(userInfo);

                setUser({
                    authenticated: true,
                    ...userInfo
                });
            }

            return response;
        } catch (error) {
            throw error;
        }
    };

    const register = async (userData) => {
        try {
            const response = await authService.register(userData);

            // Backend register returns a success message, not a token
            // User needs to login separately after registration
            return response;
        } catch (error) {
            throw error;
        }
    };

    const logout = () => {
        tokenStorage.clearAll();
        setUser(null);
    };

    const value = {
        user,
        login,
        register,
        logout,
        loading,
        isAuthenticated: !!user,
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
