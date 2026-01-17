import { createContext, useContext, useState, useEffect } from 'react';
import { cartAPI } from '../api/api';
import { useAuth } from './AuthContext';
import { logger } from '../utils/logger';

const CartContext = createContext(null);

export function CartProvider({ children }) {
    const [cart, setCart] = useState(null);
    const [loading, setLoading] = useState(false);
    const { isAuthenticated } = useAuth();
    const [fetched, setFetched] = useState(false);

    useEffect(() => {
        if (isAuthenticated && !fetched) {
            setFetched(true);
            fetchCart();
        } else if (!isAuthenticated) {
            clearCart();
        }
    }, [isAuthenticated, fetched]);

    const clearCart = () => {
        setCart(null);
        setFetched(false);
    };

    const fetchCart = async () => {
        setLoading(true);
        try {
            const response = await cartAPI.getCart();
            setCart(response.data);
        } catch (error) {
            logger.error('Failed to fetch cart:', error);
            setCart(null);
        } finally {
            setLoading(false);
        }
    };

    const addToCart = async (productId, quantity = 1) => {
        try {
            const response = await cartAPI.addProduct(productId, quantity);
            setCart(response.data);
            return response.data;
        } catch (error) {
            logger.error('Failed to add to cart:', error);
            throw error;
        }
    };

    const updateQuantity = async (productId, operation) => {
        try {
            const response = await cartAPI.updateQuantity(productId, operation);
            setCart(response.data);
            return response.data;
        } catch (error) {
            logger.error('Failed to update quantity:', error);
            throw error;
        }
    };

    const removeFromCart = async (productId) => {
        if (!cart) return;
        try {
            await cartAPI.removeProduct(cart.cartId, productId);

            setCart(current => {
                if (!current) return null;
                const updatedProducts = current.products.filter(p => p.productId !== productId);
                const removedItem = current.products.find(p => p.productId === productId);
                const priceToRemove = removedItem ? (removedItem.specialPrice || removedItem.price) * removedItem.quantity : 0;
                return {
                    ...current,
                    products: updatedProducts,
                    totalPrice: current.totalPrice - priceToRemove
                };
            });
        } catch (error) {
            logger.error('Failed to remove from cart:', error);
            throw error;
        }
    };

    const refreshCart = async () => {
        if (isAuthenticated) {
            await fetchCart();
        }
    };

    const value = {
        cart,
        loading,
        addToCart,
        updateQuantity,
        removeFromCart,
        refreshCart,
        clearCart,
        itemCount: cart?.products?.length || 0,
    };

    return (
        <CartContext.Provider value={value}>
            {children}
        </CartContext.Provider>
    );
}

export function useCart() {
    const context = useContext(CartContext);
    if (!context) {
        throw new Error('useCart must be used within a CartProvider');
    }
    return context;
}
