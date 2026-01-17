export default function AdminPayments() {
    return (
        <div className="admin-content">
            <h1>💳 Payment Settings</h1>
            <p>Configure payment methods and gateways.</p>

            <div style={{ marginTop: '30px' }}>
                <div style={{ padding: '25px', background: 'white', borderRadius: '12px', boxShadow: '0 2px 10px rgba(0,0,0,0.08)', marginBottom: '20px' }}>
                    <h3>Stripe Configuration</h3>
                    <p style={{ color: '#666' }}>Connected to Stripe payment gateway.</p>
                    <div style={{ marginTop: '15px', padding: '15px', background: '#e8f5e9', borderRadius: '6px' }}>
                        <strong>Status:</strong> ✅ Connected
                    </div>
                </div>

                <div style={{ padding: '25px', background: 'white', borderRadius: '12px', boxShadow: '0 2px 10px rgba(0,0,0,0.08)' }}>
                    <h3>Payment Methods</h3>
                    <ul style={{ marginTop: '15px' }}>
                        <li style={{ padding: '10px 0' }}>💵 Cash on Delivery (COD) - <span style={{ color: '#28a745' }}>Enabled</span></li>
                        <li style={{ padding: '10px 0' }}>💳 Credit/Debit Card (Stripe) - <span style={{ color: '#28a745' }}>Enabled</span></li>
                    </ul>
                </div>
            </div>
        </div>
    );
}
