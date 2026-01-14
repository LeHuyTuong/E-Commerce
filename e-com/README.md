# E-Commerce Platform

A robust, full-stack E-Commerce application built with Spring Boot and React.

## Features

- **User Authentication**: Secure Login/Register with JWT, Role-based access control (Admin, User).
- **Product Management**: CRUD operations for products and categories (Admin), Search and Filter (User).
- **Shopping Cart**: Real-time cart updates, optimistic UI for smooth experience.
- **Checkout**: Support for Cash on Delivery (COD) and Credit Card (Stripe integration), plus Wallet payments.
- **Soft Delete**: Data safety with soft delete implementation for critical entities.
- **Responsive Design**: Modern, responsive UI built with React.

## Technology Stack

### Backend
- **Java 17+**
- **Spring Boot 3.x**: Web, Data JPA, Security.
- **Database**: H2 (Dev) / MySQL (Prod).
- **Security**: Spring Security, JWT (Stateless).

### Frontend
- **React 18**: Functional components, Hooks.
- **Vite**: Fast build tool.
- **Axios**: API integration.
- **React Toastify**: Notifications.

## Setup Instructions

### Backend
1. Navigate to `e-com` directory.
2. Run `mvn spring-boot:run`.
3. Server runs on `http://localhost:8080`.

### Frontend
1. Navigate to `ecom-frontend` directory.
2. Run `npm install`.
3. Run `npm run dev`.
4. App runs on `http://localhost:5173`.

## Admin Credentials
- **Username**: `admin1`
- **Password**: `123456`
