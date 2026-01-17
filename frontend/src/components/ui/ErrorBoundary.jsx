import { Component } from 'react';

/**
 * Error Boundary - Catches JavaScript errors in child components
 * Prevents entire app from crashing on render errors
 */
class ErrorBoundary extends Component {
    constructor(props) {
        super(props);
        this.state = { hasError: false, error: null };
    }

    static getDerivedStateFromError(error) {
        return { hasError: true, error };
    }

    componentDidCatch(error, errorInfo) {
        // Only log in development
        if (import.meta.env.DEV) {
            console.error('ErrorBoundary caught:', error, errorInfo);
        }
    }

    handleReload = () => {
        window.location.href = '/';
    };

    render() {
        if (this.state.hasError) {
            return (
                <div style={styles.container}>
                    <div style={styles.card}>
                        <h1 style={styles.title}>Oops! Something went wrong</h1>
                        <p style={styles.message}>
                            We're sorry, but something unexpected happened.
                        </p>
                        {import.meta.env.DEV && this.state.error && (
                            <pre style={styles.error}>
                                {this.state.error.toString()}
                            </pre>
                        )}
                        <button style={styles.button} onClick={this.handleReload}>
                            Go to Homepage
                        </button>
                    </div>
                </div>
            );
        }

        return this.props.children;
    }
}

const styles = {
    container: {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh',
        backgroundColor: '#f5f5f5',
        padding: '20px',
    },
    card: {
        backgroundColor: '#fff',
        padding: '40px',
        borderRadius: '8px',
        boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
        textAlign: 'center',
        maxWidth: '500px',
    },
    title: {
        color: '#e53935',
        marginBottom: '16px',
    },
    message: {
        color: '#666',
        marginBottom: '24px',
    },
    error: {
        backgroundColor: '#ffebee',
        color: '#c62828',
        padding: '12px',
        borderRadius: '4px',
        textAlign: 'left',
        fontSize: '12px',
        overflow: 'auto',
        marginBottom: '24px',
    },
    button: {
        backgroundColor: '#1976d2',
        color: '#fff',
        border: 'none',
        padding: '12px 24px',
        borderRadius: '4px',
        cursor: 'pointer',
        fontSize: '16px',
    },
};

export default ErrorBoundary;
