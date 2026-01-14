import './Button.css';

export default function Button({
    children,
    variant = 'primary',
    size = 'md',
    fullWidth = false,
    loading = false,
    disabled = false,
    icon,
    ...props
}) {
    return (
        <button
            className={`btn btn-${variant} btn-${size} ${fullWidth ? 'btn-full' : ''}`}
            disabled={disabled || loading}
            {...props}
        >
            {loading && <span className="btn-spinner" />}
            {icon && !loading && <span className="btn-icon">{icon}</span>}
            {children}
        </button>
    );
}
