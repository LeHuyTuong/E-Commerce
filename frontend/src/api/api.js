import axios from 'axios';
import { logger } from '../utils/logger';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
const JWT_TOKEN_KEY = 'ecom_jwt_token';

// Token management helpers
export const setToken = (token) => {
  if (token) {
    localStorage.setItem(JWT_TOKEN_KEY, token);
  } else {
    localStorage.removeItem(JWT_TOKEN_KEY);
  }
};

export const getToken = () => localStorage.getItem(JWT_TOKEN_KEY);
export const clearToken = () => localStorage.removeItem(JWT_TOKEN_KEY);

const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true, // Keep for cookie fallback
  headers: {
    'Content-Type': 'application/json',
  },
});

// Global state for logout callback (will be set by AuthContext)
let logoutCallback = null;

export const setLogoutCallback = (callback) => {
  logoutCallback = callback;
};

// REQUEST INTERCEPTOR - Add Authorization header from localStorage
api.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// RESPONSE INTERCEPTOR - Handle 401 globally
let isRedirecting = false;

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const isAuthEndpoint = error.config?.url?.includes('/auth/');
    const isAlreadyOnLogin = window.location.pathname === '/login';
    const status = error.response?.status;

    if (status === 401 && !isAuthEndpoint && !isAlreadyOnLogin && !isRedirecting) {
      logger.warn('401 Unauthorized - clearing token and redirecting...');
      isRedirecting = true;

      // Clear token on 401
      clearToken();

      if (logoutCallback) {
        logoutCallback();
      }

      setTimeout(() => {
        window.location.href = '/login';
        isRedirecting = false;
      }, 100);
    }
    return Promise.reject(error);
  }
);

// Auth API
export const authAPI = {
  login: (username, password) =>
    api.post('/auth/signin', { username, password }),
  register: (username, email, password, role = null) =>
    api.post('/auth/signup', { username, email, password, role: role ? [role] : null }),
  logout: () => {
    clearToken(); // Clear token on logout
    return api.post('/auth/signout');
  },
  getUser: () => api.get('/auth/user'),
  getUsername: () => api.get('/auth/username'),
};

// Products API
export const productsAPI = {
  getAll: (pageNumber = 0, pageSize = 10, sortBy = 'productId', sortOrder = 'asc') =>
    api.get('/public/products', { params: { pageNumber, pageSize, sortBy, sortOrder } }),
  getById: (id) => api.get(`/public/products/${id}`),
  getByCategory: (categoryId, pageNumber = 0, pageSize = 10) =>
    api.get(`/public/categories/${categoryId}/products`, { params: { pageNumber, pageSize } }),
  search: (keyword, pageNumber = 0, pageSize = 10) =>
    api.get(`/public/products/keyword/${keyword}`, { params: { pageNumber, pageSize } }),
  create: (categoryId, productDTO) =>
    api.post(`/admin/categories/${categoryId}/product`, productDTO),
  update: (productId, productDTO, categoryId = null) =>
    api.put(`/admin/products/${productId}${categoryId ? `?categoryId=${categoryId}` : ''}`, productDTO),
  delete: (productId) =>
    api.delete(`/admin/products/${productId}`),
  // Seller-specific endpoints
  getSellerProducts: (pageNumber = 0, pageSize = 10, sortBy = 'productId', sortOrder = 'asc') =>
    api.get('/seller/products', { params: { pageNumber, pageSize, sortBy, sortOrder } }),
  sellerUpdate: (productId, productDTO, categoryId = null) =>
    api.put(`/seller/products/${productId}${categoryId ? `?categoryId=${categoryId}` : ''}`, productDTO),
  sellerDelete: (productId) =>
    api.delete(`/seller/products/${productId}`),
  uploadImage: (productId, formData) =>
    api.put(`/products/${productId}/image`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    }),
};

// Categories API
export const categoriesAPI = {
  getAll: (pageNumber = 0, pageSize = 50) =>
    api.get('/public/categories', { params: { pageNumber, pageSize } }),
  create: (categoryDTO) => api.post('/public/categories', categoryDTO),
  update: (categoryId, categoryDTO) => api.put(`/public/categories/${categoryId}`, categoryDTO),
  delete: (categoryId) => api.delete(`/admin/categories/${categoryId}`),
};

// Cart API - Note: paths must match cookie path /api
export const cartAPI = {
  getCart: () => api.get('/carts/users/cart'),
  addProduct: (productId, quantity) =>
    api.post(`/carts/products/${productId}/quantity/${quantity}`),
  updateQuantity: (productId, operation) =>
    api.put(`/carts/products/${productId}/quantity/${operation}`),
  removeProduct: (cartId, productId) =>
    api.delete(`/carts/${cartId}/product/${productId}`),
};

// Address API
export const addressAPI = {
  getAll: () => api.get('/address'),
  getById: (addressId) => api.get(`/address/${addressId}`),
  create: (addressDTO) => api.post('/address', addressDTO),
  update: (addressId, addressDTO) => api.put(`/address/${addressId}`, addressDTO),
  delete: (addressId) => api.delete(`/address/${addressId}`),
};

// Order API
export const orderAPI = {
  placeOrder: (paymentMethod, orderRequest) =>
    api.post(`/order/users/payments/${paymentMethod}`, orderRequest),
};

// Stripe Payment API
// Stripe Payment API
export const stripeAPI = {
  createPaymentIntent: (paymentData) =>
    api.post('/order/stripe-client-secret', paymentData),
  createCheckoutSession: (paymentData) =>
    api.post('/order/stripe/create-session', paymentData),
  validateSession: (sessionId) =>
    api.get(`/order/stripe/validate?sessionId=${sessionId}`),
};

// Analytics API (Admin)
export const analyticsAPI = {
  getAnalytics: () => api.get('/admin/analytics'),
};

// Orders API
export const ordersAPI = {
  getUserOrders: () => api.get('/order/users/orders'),
  getSellerOrders: () => api.get('/order/seller/orders'),
  getAllOrders: () => api.get('/order/admin/all'), // Admin: Get all orders
  updateStatus: (orderId, status) => api.put(`/order/${orderId}/status/${status}`),
};

// Wallet API
export const walletAPI = {
  getMyWallet: () => api.get('/wallet'),
  getTransactions: () => api.get('/wallet/transactions'),
  getPlatformWallet: () => api.get('/wallet/platform'),
};

// Users API (Admin)
export const usersAPI = {
  getAll: () => api.get('/admin/users'),
};

export default api;
