const Input = ({
    label,
    error,
    type = 'text',
    className = '',
    ...props
}) => {
    return (
        <div className="w-full">
            {label && (
                <label className="block text-sm font-medium text-gray-300 mb-2">
                    {label}
                </label>
            )}
            <input
                type={type}
                className={`w-full px-4 py-3 rounded-xl glass-input ${error
                    ? 'border-red-500/50 focus:border-red-500'
                    : 'border-white/10 focus:border-neon-purple'
                    } focus:outline-none transition-colors placeholder-gray-500 ${className}`}
                {...props}
            />
            {error && (
                <p className="mt-1 text-sm text-red-400">{error}</p>
            )}
        </div>
    );
};

export default Input;
