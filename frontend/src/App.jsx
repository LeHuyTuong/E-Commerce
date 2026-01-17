import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import { Layout, AdminLayout, SellerLayout } from './components/layout';
import { ProtectedRoute, AdminRoute, SellerRoute } from './components/auth';
import { ErrorBoundary } from './components/ui';
import {
  Home,
  Login,
  Register,
  Cart,
  Checkout,
  ProductDetail,
  Search,
  Categories,
  StripePayment,
  AdminDashboard,
  AdminProducts,
  AdminCategories,
  AdminOrders,
  AdminUsers,
  AdminPayments,
  AdminSettings,
  Unauthorized,
  PaymentSuccess,
  Orders,
  SellerDashboard,
  SellerProducts,
  SellerOrders,
  SellerWallet
} from './pages';
import './index.css';
import './pages/Admin.css';

function App() {
  return (
    <ErrorBoundary>
      <Router>
        <AuthProvider>
          <CartProvider>
            <Routes>
              {/* Public Routes */}
              <Route path="/" element={<Layout />}>
                <Route index element={<Home />} />
                <Route path="login" element={<Login />} />
                <Route path="register" element={<Register />} />
                <Route path="products/:id" element={<ProductDetail />} />
                <Route path="search/:keyword" element={<Search />} />
                <Route path="categories" element={<Categories />} />
                <Route path="category/:id" element={<Categories />} />
                <Route path="unauthorized" element={<Unauthorized />} />

                {/* Protected User Routes */}
                <Route path="cart" element={
                  <ProtectedRoute>
                    <Cart />
                  </ProtectedRoute>
                } />
                <Route path="checkout" element={
                  <ProtectedRoute>
                    <Checkout />
                  </ProtectedRoute>
                } />
                <Route path="payment/stripe" element={
                  <ProtectedRoute>
                    <StripePayment />
                  </ProtectedRoute>
                } />
                <Route path="payment/success" element={
                  <ProtectedRoute>
                    <PaymentSuccess />
                  </ProtectedRoute>
                } />
                <Route path="orders" element={
                  <ProtectedRoute>
                    <Orders />
                  </ProtectedRoute>
                } />
              </Route>

              {/* Admin Routes - Protected by AdminRoute */}
              <Route path="/admin" element={
                <AdminRoute>
                  <AdminLayout />
                </AdminRoute>
              }>
                <Route index element={<AdminDashboard />} />
                <Route path="products" element={<AdminProducts />} />
                <Route path="categories" element={<AdminCategories />} />
                <Route path="orders" element={<AdminOrders />} />
                <Route path="users" element={<AdminUsers />} />
                <Route path="payments" element={<AdminPayments />} />
                <Route path="settings" element={<AdminSettings />} />
              </Route>

              {/* Seller Routes - Protected by SellerRoute */}
              <Route path="/seller" element={
                <SellerRoute>
                  <SellerLayout />
                </SellerRoute>
              }>
                <Route index element={<SellerDashboard />} />
                <Route path="products" element={<SellerProducts />} />
                <Route path="orders" element={<SellerOrders />} />
                <Route path="wallet" element={<SellerWallet />} />
              </Route>
            </Routes>
          </CartProvider>
        </AuthProvider>
      </Router>
    </ErrorBoundary>
  );
}

export default App;
