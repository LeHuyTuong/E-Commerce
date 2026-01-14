import './Loading.css';

export function Spinner() {
    return <span className="spinner">Loading...</span>;
}

export function LoadingPage() {
    return (
        <div className="loading-page">
            <Spinner />
        </div>
    );
}
