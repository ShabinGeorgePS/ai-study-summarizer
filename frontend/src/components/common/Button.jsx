const Button = ({
    children,
    variant = 'primary',
    type = 'button',
    disabled = false,
    onClick,
    className = '',
    ...props
}) => {
    const baseStyles = 'px-6 py-3 rounded-xl font-medium transition-all duration-300 transform active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100';

    const variants = {
        primary: 'bg-gradient-to-r from-neon-purple to-neon-pink text-white shadow-lg shadow-purple-900/50 hover:shadow-purple-700/50 hover:-translate-y-0.5 border border-white/10',
        secondary: 'glass-panel text-white hover:bg-white/10 border-white/10 hover:border-white/20',
        outline: 'border border-neon-purple text-neon-purple hover:bg-neon-purple/10',
        danger: 'bg-red-500/20 text-red-400 border border-red-500/50 hover:bg-red-500/30',
    };

    return (
        <button
            type={type}
            disabled={disabled}
            onClick={onClick}
            className={`${baseStyles} ${variants[variant]} ${className}`}
            {...props}
        >
            {children}
        </button>
    );
};

export default Button;
