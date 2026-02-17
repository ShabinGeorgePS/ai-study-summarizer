import { useState, useEffect } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import { authService } from '../services/authService';
import Button from '../components/common/Button';
import Card from '../components/common/Card';

const ResetPassword = () => {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const token = searchParams.get('token');

    const [formData, setFormData] = useState({
        password: '',
        confirmPassword: '',
    });

    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [errors, setErrors] = useState({});
    const [loading, setLoading] = useState(false);
    const [verifying, setVerifying] = useState(true);
    const [apiError, setApiError] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    const [tokenValid, setTokenValid] = useState(false);

    // Verify token on mount
    useEffect(() => {
        const verifyToken = async () => {
            if (!token) {
                setApiError('No reset token provided');
                setVerifying(false);
                return;
            }

            try {
                await authService.verifyResetToken(token);
                setTokenValid(true);
            } catch (error) {
                setApiError(error.message || 'Invalid or expired reset token');
            } finally {
                setVerifying(false);
            }
        };

        verifyToken();
    }, [token]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
        // Clear error when user starts typing
        if (errors[name]) {
            setErrors(prev => ({ ...prev, [name]: '' }));
        }
        setApiError('');
    };

    const validate = () => {
        const newErrors = {};

        if (!formData.password) {
            newErrors.password = 'Password is required';
        } else if (formData.password.length < 8) {
            newErrors.password = 'Password must be at least 8 characters';
        }

        if (!formData.confirmPassword) {
            newErrors.confirmPassword = 'Please confirm your password';
        } else if (formData.password !== formData.confirmPassword) {
            newErrors.confirmPassword = 'Passwords do not match';
        }

        return newErrors;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        const newErrors = validate();
        if (Object.keys(newErrors).length > 0) {
            setErrors(newErrors);
            return;
        }

        setLoading(true);
        setApiError('');
        setSuccessMessage('');

        try {
            await authService.resetPassword(token, formData.password);
            setSuccessMessage('Password reset successfully! Redirecting to login...');

            // Redirect after 2 seconds
            setTimeout(() => {
                navigate('/login');
            }, 2000);
        } catch (error) {
            setApiError(error.message || 'Failed to reset password. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    if (verifying) {
        return (
            <div className="min-h-screen flex items-center justify-center p-4">
                <div className="text-center">
                    <div className="relative inline-block">
                        <div className="absolute inset-0 bg-neon-purple/50 rounded-full blur-xl animate-pulse"></div>
                        <div className="relative inline-block animate-spin rounded-full h-16 w-16 border-t-2 border-b-2 border-neon-purple"></div>
                    </div>
                    <p className="mt-6 text-gray-400 animate-pulse">Verifying reset token...</p>
                </div>
            </div>
        );
    }

    if (!tokenValid) {
        return (
            <div className="min-h-screen flex items-center justify-center p-4">
                <div className="w-full max-w-md animate-[float_6s_ease-in-out_infinite]">
                    <Card className="p-8 backdrop-blur-2xl bg-black/30 border border-white/10 shadow-2xl">
                        <div className="text-center">
                            <div className="inline-flex items-center justify-center w-16 h-16 bg-red-500/20 rounded-2xl mb-4 shadow-lg shadow-red-500/30">
                                <svg className="w-8 h-8 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4v2m0-6a4 4 0 110 8 4 4 0 010-8z" />
                                </svg>
                            </div>
                            <h2 className="text-2xl font-bold text-white mb-2">Invalid Reset Link</h2>
                            <p className="text-gray-400 mb-6">{apiError}</p>
                            <Link to="/forgot-password">
                                <Button className="w-full">Request New Reset Link</Button>
                            </Link>
                        </div>
                    </Card>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen flex items-center justify-center p-4">
            <div className="w-full max-w-md animate-[float_6s_ease-in-out_infinite]">
                <Card className="p-8 backdrop-blur-2xl bg-black/30 border border-white/10 shadow-2xl relative overflow-hidden group">
                    {/* Decorative glow background */}
                    <div className="absolute -top-24 -left-24 w-48 h-48 bg-neon-purple/20 rounded-full blur-[80px] pointer-events-none group-hover:bg-neon-purple/30 transition-all duration-700"></div>
                    <div className="absolute -bottom-24 -right-24 w-48 h-48 bg-neon-pink/20 rounded-full blur-[80px] pointer-events-none group-hover:bg-neon-pink/30 transition-all duration-700"></div>

                    {/* Header */}
                    <div className="text-center mb-8 relative z-10">
                        <div className="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-neon-purple to-neon-pink rounded-2xl mb-4 shadow-lg shadow-purple-500/30 transform transition-transform group-hover:scale-110 duration-300">
                            <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" />
                            </svg>
                        </div>
                        <h1 className="text-3xl font-bold text-white mb-2">Create New Password</h1>
                        <p className="text-gray-400">Enter your new password below</p>
                    </div>

                    {/* Form */}
                    <form onSubmit={handleSubmit} className="space-y-5 relative z-10">
                        {successMessage && (
                            <div className="bg-green-500/10 border border-green-500/20 text-green-200 px-4 py-3 rounded-lg text-sm">
                                {successMessage}
                            </div>
                        )}

                        {apiError && !successMessage && (
                            <div className="bg-red-500/10 border border-red-500/20 text-red-200 px-4 py-3 rounded-lg text-sm">
                                {apiError}
                            </div>
                        )}

                        <div>
                            <label className="block text-sm font-medium text-gray-300 mb-2">New Password</label>
                            <div className="relative">
                                <input
                                    type={showPassword ? 'text' : 'password'}
                                    name="password"
                                    value={formData.password}
                                    onChange={handleChange}
                                    placeholder="••••••••"
                                    autoComplete="new-password"
                                    disabled={loading}
                                    className={`w-full px-4 py-3 rounded-lg bg-white/5 border transition-all duration-300 placeholder-gray-500 text-white focus:outline-none focus:ring-2 focus:ring-neon-purple/50 disabled:opacity-50 ${
                                        errors.password
                                            ? 'border-red-500/50 focus:border-red-500'
                                            : 'border-white/10 hover:border-white/20'
                                    }`}
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPassword(!showPassword)}
                                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-300 transition-colors"
                                >
                                    {showPassword ? (
                                        <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                                            <path d="M10 12a2 2 0 100-4 2 2 0 000 4z" />
                                            <path fillRule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clipRule="evenodd" />
                                        </svg>
                                    ) : (
                                        <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                                            <path fillRule="evenodd" d="M3.707 2.293a1 1 0 00-1.414 1.414l14 14a1 1 0 001.414-1.414l-1.473-1.473A10.014 10.014 0 0019.542 10C18.268 5.943 14.478 3 10 3a9.958 9.958 0 00-4.512 1.074l-1.78-1.781zm4.261 4.26l1.514 1.515a2.003 2.003 0 012.45 2.45l1.514 1.514a4 4 0 00-5.478-5.478z" clipRule="evenodd" />
                                            <path d="M15.171 13.576l1.414 1.414A10.015 10.015 0 0120.458 10C19.185 5.943 15.395 3 11 3a9.958 9.958 0 00-1.457.11l2.1 2.1a4 4 0 015.528 5.466z" />
                                        </svg>
                                    )}
                                </button>
                            </div>
                            {errors.password && (
                                <p className="mt-1 text-sm text-red-400">{errors.password}</p>
                            )}
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-300 mb-2">Confirm Password</label>
                            <div className="relative">
                                <input
                                    type={showConfirmPassword ? 'text' : 'password'}
                                    name="confirmPassword"
                                    value={formData.confirmPassword}
                                    onChange={handleChange}
                                    placeholder="••••••••"
                                    autoComplete="new-password"
                                    disabled={loading}
                                    className={`w-full px-4 py-3 rounded-lg bg-white/5 border transition-all duration-300 placeholder-gray-500 text-white focus:outline-none focus:ring-2 focus:ring-neon-purple/50 disabled:opacity-50 ${
                                        errors.confirmPassword
                                            ? 'border-red-500/50 focus:border-red-500'
                                            : 'border-white/10 hover:border-white/20'
                                    }`}
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                                    className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-300 transition-colors"
                                >
                                    {showConfirmPassword ? (
                                        <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                                            <path d="M10 12a2 2 0 100-4 2 2 0 000 4z" />
                                            <path fillRule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clipRule="evenodd" />
                                        </svg>
                                    ) : (
                                        <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                                            <path fillRule="evenodd" d="M3.707 2.293a1 1 0 00-1.414 1.414l14 14a1 1 0 001.414-1.414l-1.473-1.473A10.014 10.014 0 0019.542 10C18.268 5.943 14.478 3 10 3a9.958 9.958 0 00-4.512 1.074l-1.78-1.781zm4.261 4.26l1.514 1.515a2.003 2.003 0 012.45 2.45l1.514 1.514a4 4 0 00-5.478-5.478z" clipRule="evenodd" />
                                            <path d="M15.171 13.576l1.414 1.414A10.015 10.015 0 0120.458 10C19.185 5.943 15.395 3 11 3a9.958 9.958 0 00-1.457.11l2.1 2.1a4 4 0 015.528 5.466z" />
                                        </svg>
                                    )}
                                </button>
                            </div>
                            {errors.confirmPassword && (
                                <p className="mt-1 text-sm text-red-400">{errors.confirmPassword}</p>
                            )}
                        </div>

                        <Button
                            type="submit"
                            disabled={loading}
                            className="w-full"
                        >
                            {loading ? (
                                <span className="flex items-center justify-center">
                                    <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
                                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                    </svg>
                                    Resetting Password...
                                </span>
                            ) : (
                                'Reset Password'
                            )}
                        </Button>
                    </form>

                    <div className="mt-8 text-center relative z-10">
                        <p className="text-gray-400">
                            <Link to="/login" className="text-neon-purple hover:text-neon-pink font-medium transition-colors duration-300">
                                Back to Sign In
                            </Link>
                        </p>
                    </div>
                </Card>

                {/* Footer */}
                <p className="text-center text-gray-600 text-sm mt-8">
                    Secure password reset
                </p>
            </div>
        </div>
    );
};

export default ResetPassword;
