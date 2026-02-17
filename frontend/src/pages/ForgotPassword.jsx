import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import Input from '../components/common/Input';
import Button from '../components/common/Button';
import Card from '../components/common/Card';

const ForgotPassword = () => {
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [errors, setErrors] = useState({});
    const [loading, setLoading] = useState(false);
    const [apiError, setApiError] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    const [resetToken, setResetToken] = useState('');

    const handleChange = (e) => {
        const { value } = e.target;
        setEmail(value);
        // Clear error when user starts typing
        if (errors.email) {
            setErrors(prev => ({ ...prev, email: '' }));
        }
        setApiError('');
    };

    const validate = () => {
        const newErrors = {};

        if (!email.trim()) {
            newErrors.email = 'Email is required';
        } else if (!/\S+@\S+\.\S+/.test(email)) {
            newErrors.email = 'Email is invalid';
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
            const response = await authService.forgotPassword(email);

            setSuccessMessage('Reset link sent! Check your email for instructions.');
            setResetToken(response.resetToken);

            // Auto-redirect after 5 seconds
            setTimeout(() => {
                navigate('/login');
            }, 5000);
        } catch (error) {
            setApiError(error.message || 'Failed to process password reset. Please try again.');
        } finally {
            setLoading(false);
        }
    };

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
                        <h1 className="text-3xl font-bold text-white mb-2">Reset Password</h1>
                        <p className="text-gray-400">Enter your email to receive a reset link</p>
                    </div>

                    {/* Form */}
                    <form onSubmit={handleSubmit} className="space-y-6 relative z-10">
                        {successMessage && (
                            <div className="bg-green-500/10 border border-green-500/20 text-green-200 px-4 py-3 rounded-lg text-sm">
                                {successMessage}
                                {resetToken && (
                                    <div className="mt-4 p-4 bg-white/5 rounded border border-white/10">
                                        <p className="text-xs text-gray-400 mb-2">Reset token (for demo):</p>
                                        <p className="text-xs font-mono text-gray-300 break-all">{resetToken}</p>
                                        <p className="text-xs text-gray-500 mt-2">In production, this would be sent via email.</p>
                                    </div>
                                )}
                            </div>
                        )}

                        {apiError && (
                            <div className="bg-red-500/10 border border-red-500/20 text-red-200 px-4 py-3 rounded-lg text-sm">
                                {apiError}
                            </div>
                        )}

                        {!successMessage && (
                            <>
                                <Input
                                    label="Email Address"
                                    type="email"
                                    name="email"
                                    value={email}
                                    onChange={handleChange}
                                    error={errors.email}
                                    placeholder="you@example.com"
                                    autoComplete="email"
                                />

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
                                            Sending reset link...
                                        </span>
                                    ) : (
                                        'Send Reset Link'
                                    )}
                                </Button>
                            </>
                        )}
                    </form>

                    <div className="mt-8 text-center relative z-10">
                        <p className="text-gray-400">
                            Remember your password?{' '}
                            <Link to="/login" className="text-neon-purple hover:text-neon-pink font-medium transition-colors duration-300">
                                Sign in
                            </Link>
                        </p>
                    </div>
                </Card>

                {/* Footer */}
                <p className="text-center text-gray-600 text-sm mt-8">
                    Secure password recovery
                </p>
            </div>
        </div>
    );
};

export default ForgotPassword;
