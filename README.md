# E-Commerce Platform

A full-stack e-commerce application with Spring Boot backend and React frontend.

## Project Structure

```
E-Commerce/
├── backend/          # Spring Boot REST API
│   ├── src/
│   ├── pom.xml
│   └── mvnw.cmd
└── frontend/         # React + Vite
    ├── src/
    ├── package.json
    └── vite.config.js
```

## Quick Start

### Backend
```bash
cd backend
./mvnw spring-boot:run
```
Server runs at `http://localhost:8080`

### Frontend
```bash
cd frontend
npm install
npm run dev
```
App runs at `http://localhost:5173`

## Tech Stack
- **Backend**: Java 21, Spring Boot 3, Spring Security, JPA/Hibernate, MySQL
- **Frontend**: React 18, Vite, React Router, Axios, React Toastify

## Features
- User authentication (JWT)
- Product catalog with categories
- Shopping cart
- Order management
- Seller dashboard
- Admin panel
- Wallet system
