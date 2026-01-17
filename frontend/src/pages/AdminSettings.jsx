export default function AdminSettings() {
    return (
        <div className="admin-content">
            <h1>⚙️ Store Settings</h1>
            <p>Configure your store preferences.</p>

            <div style={{ marginTop: '30px' }}>
                <div style={{ padding: '25px', background: 'white', borderRadius: '12px', boxShadow: '0 2px 10px rgba(0,0,0,0.08)', marginBottom: '20px' }}>
                    <h3>Store Information</h3>
                    <div style={{ display: 'grid', gap: '15px', marginTop: '15px' }}>
                        <div>
                            <label style={{ fontWeight: '600', display: 'block', marginBottom: '5px' }}>Store Name</label>
                            <input type="text" value="EcomStore" readOnly style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '6px' }} />
                        </div>
                        <div>
                            <label style={{ fontWeight: '600', display: 'block', marginBottom: '5px' }}>Store Email</label>
                            <input type="email" value="contact@ecomstore.com" readOnly style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: '6px' }} />
                        </div>
                    </div>
                </div>

                <div style={{ padding: '25px', background: 'white', borderRadius: '12px', boxShadow: '0 2px 10px rgba(0,0,0,0.08)' }}>
                    <h3>Currency Settings</h3>
                    <p style={{ marginTop: '10px', color: '#666' }}>Default Currency: <strong>USD ($)</strong></p>
                </div>
            </div>
        </div>
    );
}
