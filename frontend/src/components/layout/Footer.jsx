import { Link } from 'react-router-dom';
import './Footer.css';

export default function Footer() {
    return (
        <footer className="footer">
            <div className="container">
                <p>&copy; {new Date().getFullYear()} EcomStore. All rights reserved.</p>
            </div>
        </footer>
    );
}
