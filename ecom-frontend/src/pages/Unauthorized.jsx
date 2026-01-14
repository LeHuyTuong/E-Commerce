import { Link } from 'react-router-dom';

export default function Unauthorized() {
    return (
        <div className="container" style={{ textAlign: 'center', padding: '60px 20px' }}>
            <h1 style={{ fontSize: '80px', margin: '0', color: '#dc3545' }}>403</h1>
            <h2>Access Denied</h2>
            <p style={{ color: '#666', marginBottom: '30px' }}>
                You don't have permission to access this page.
            </p>
            <Link
                to="/"
                style={{
                    padding: '12px 30px',
                    background: '#0066cc',
                    color: 'white',
                    textDecoration: 'none',
                    borderRadius: '4px'
                }}
            >
                Go to Home
            </Link>
        </div>
    );
}
