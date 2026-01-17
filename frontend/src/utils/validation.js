/**
 * Input validation utilities for forms
 * Client-side validation to improve UX and reduce unnecessary API calls
 */

export const validators = {
    // Username: 3-20 chars, alphanumeric and underscore
    username: (value) => {
        if (!value || value.trim().length < 3) {
            return 'Username must be at least 3 characters';
        }
        if (value.length > 20) {
            return 'Username must be less than 20 characters';
        }
        if (!/^[a-zA-Z0-9_]+$/.test(value)) {
            return 'Username can only contain letters, numbers, and underscores';
        }
        return '';
    },

    // Email: basic format check
    email: (value) => {
        if (!value || !value.trim()) {
            return 'Email is required';
        }
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(value)) {
            return 'Please enter a valid email address';
        }
        return '';
    },

    // Password: minimum 6 chars
    password: (value) => {
        if (!value) {
            return 'Password is required';
        }
        if (value.length < 6) {
            return 'Password must be at least 6 characters';
        }
        return '';
    },

    // Confirm password match
    confirmPassword: (value, password) => {
        if (value !== password) {
            return 'Passwords do not match';
        }
        return '';
    },

    // Required field
    required: (value, fieldName = 'This field') => {
        if (!value || (typeof value === 'string' && !value.trim())) {
            return `${fieldName} is required`;
        }
        return '';
    },

    // Pincode: digits only
    pincode: (value) => {
        if (!value || !value.trim()) {
            return 'Pincode is required';
        }
        if (!/^\d+$/.test(value)) {
            return 'Pincode must contain only numbers';
        }
        return '';
    },
};

/**
 * Validate multiple fields at once
 * @param {Object} fields - { fieldName: { value, validator, ...args } }
 * @returns {Object} - { fieldName: errorMessage }
 */
export const validateForm = (fields) => {
    const errors = {};
    for (const [fieldName, config] of Object.entries(fields)) {
        const { value, validator, args = [] } = config;
        const error = validators[validator](value, ...args);
        if (error) {
            errors[fieldName] = error;
        }
    }
    return errors;
};

/**
 * Check if form has any errors
 */
export const hasErrors = (errors) => {
    return Object.keys(errors).length > 0;
};
