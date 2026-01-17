import { useState, useEffect } from 'react';
import { walletAPI } from '../api/api';
import './Admin.css';

export default function SellerWallet() {
    const [wallet, setWallet] = useState(null);
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchWalletData();
    }, []);

    const fetchWalletData = async () => {
        try {
            const [walletRes, txnRes] = await Promise.all([
                walletAPI.getMyWallet(),
                walletAPI.getTransactions()
            ]);
            setWallet(walletRes.data);
            setTransactions(txnRes.data || []);
        } catch (err) {
            console.error('Failed to fetch wallet info', err);
        } finally {
            setLoading(false);
        }
    };

    if (loading) return <div className="seller-content"><p>Loading wallet...</p></div>;

    return (
        <div className="seller-content">
            <h1>My Wallet</h1>

            {/* Wallet Balance Card */}
            <div className="wallet-card-container">
                <div className="wallet-card">
                    <h2>Available Balance</h2>
                    <div className="balance-amount">${wallet?.balance?.toFixed(2) || '0.00'}</div>
                    <div className="wallet-stats">
                        <div>
                            <span>Total Earnings</span>
                            <span className="val">${wallet?.totalEarnings?.toFixed(2) || '0.00'}</span>
                        </div>
                        <div>
                            <span>Pending</span>
                            <span className="val">${wallet?.pendingBalance?.toFixed(2) || '0.00'}</span>
                        </div>
                    </div>
                </div>
            </div>

            {/* Transaction History */}
            <div className="transactions-section">
                <h2>Transaction History</h2>
                {transactions.length === 0 ? (
                    <p className="no-data">No transactions yet.</p>
                ) : (
                    <table className="admin-table">
                        <thead>
                            <tr>
                                <th>Date</th>
                                <th>Type</th>
                                <th>Amount</th>
                                <th>Description</th>
                                <th>Order ID</th>
                            </tr>
                        </thead>
                        <tbody>
                            {transactions.map((txn) => (
                                <tr key={txn.transactionId}>
                                    <td>{new Date(txn.createdAt).toLocaleDateString()} {new Date(txn.createdAt).toLocaleTimeString()}</td>
                                    <td>
                                        <span className={`txn-type ${txn.type}`}>
                                            {txn.type}
                                        </span>
                                    </td>
                                    <td className={txn.type === 'DEBIT' ? 'text-danger' : 'text-success'}>
                                        {txn.type === 'DEBIT' ? '-' : '+'}${parseFloat(txn.amount).toFixed(2)}
                                    </td>
                                    <td>{txn.description}</td>
                                    <td>{txn.orderId ? `#${txn.orderId}` : '-'}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                )}
            </div>

            <style>{`
                .wallet-card-container {
                    margin-bottom: 2rem;
                }
                .wallet-card {
                    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
                    color: white;
                    padding: 2rem;
                    border-radius: 15px;
                    max-width: 400px;
                    box-shadow: 0 4px 15px rgba(0,0,0,0.1);
                }
                .balance-amount {
                    font-size: 2.5rem;
                    font-weight: bold;
                    margin: 1rem 0;
                }
                .wallet-stats {
                    display: flex;
                    justify-content: space-between;
                    border-top: 1px solid rgba(255,255,255,0.3);
                    padding-top: 1rem;
                }
                .wallet-stats div {
                    display: flex;
                    flex-direction: column;
                }
                .wallet-stats span {
                    font-size: 0.9rem;
                    opacity: 0.9;
                }
                .wallet-stats .val {
                    font-weight: bold;
                    font-size: 1.1rem;
                }
                .txn-type.CREDIT { color: green; font-weight: bold; }
                .txn-type.DEBIT { color: red; font-weight: bold; }
                .txn-type.COMMISSION { color: blue; }
                .text-success { color: #28a745; }
                .text-danger { color: #dc3545; }
            `}</style>
        </div>
    );
}
