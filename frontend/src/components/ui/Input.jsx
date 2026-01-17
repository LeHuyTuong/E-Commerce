import './Input.css';

export default function Input({
    label,
    error,
    icon,
    type = 'text',
    ...props
}) {
    return (
        <div className="input-group">
            {label && <label className="input-label">{label}</label>}
            <div className="input-wrapper">
                {icon && <span className="input-icon">{icon}</span>}
                <input
                    type={type}
                    className={`input ${error ? 'input-error' : ''} ${icon ? 'input-with-icon' : ''}`}
                    {...props}
                />
            </div>
            {error && <span className="input-error-text">{error}</span>}
        </div>
    );
}
