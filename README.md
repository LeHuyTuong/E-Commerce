# E-Commerce Banking-Grade Platform

A robust, full-stack E-Commerce application architected with Financial Technology (Fintech) standards. Features a complete Wallet System, Transactional Integrity, and Audit Logging similar to real-world banking applications.

<img width="1899" height="860" alt="image" src="https://github.com/user-attachments/assets/754b9be5-e839-4716-9cc6-511ff7dc6d05" />

## Architecture

```mermaid
graph TD
    User[Client Browser] <-->|Rest API / JWT| Frontend[React Frontend]
    Frontend <-->|JSON over HTTP| Backend[Spring Boot Backend]
    Backend <-->|JPA / Hibernate| DB[(PostgreSQL Database)]
    Backend <-->|Integration| Stripe[Stripe Payment Gateway]
```

## Key Features (Banking Mindset)

*   **Transactional Integrity**: Critical flows (Order Placement, Wallet Debit) are wrapped in `@Transactional` to ensure data consistency.
*   **Concurrency Control**: Uses `PESSIMISTIC_WRITE` Locking to prevent Double Spending (race conditions).
*   **Audit Logging**: Every wallet movement needs a corresponding `WalletTransaction` record for reconciliation.
*   **Safety**: Validations (`@DecimalMin`, `@NotNull`) ensuring no negative balances or invalid amounts.

## Quick Start

### Backend (Port 8080)
```bash
cd e-com
mvn spring-boot:run
```

### Frontend (Port 5173)
```bash
cd ecom-frontend
npm install
npm run dev
```

## Key API Endpoints

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/auth/signin` | Login & Get JWT |
| `GET` | `/api/public/products` | Browse Products |
| `GET` | `/api/wallet` | Check Balance |
| `POST` | `/api/order/users/payments/WALLET` | Pay with Wallet (Transactional) |

## Screenshots
<img width="1778" height="917" alt="image" src="https://github.com/user-attachments/assets/39cdec11-5412-49c3-91a2-949f6371e0e8" />
<img width="1777" height="841" alt="image" src="https://github.com/user-attachments/assets/6145d286-e3c0-4c09-adb1-63cf379636fe" />
<img width="1863" height="860" alt="image" src="https://github.com/user-attachments/assets/025893fb-b6db-4b5b-bee3-06e656091a5f" />
<img width="1853" height="782" alt="image" src="https://github.com/user-attachments/assets/a275fafa-5daa-45fc-a2e2-852c8216d75b" />
<img width="1897" height="853" alt="image" src="https://github.com/user-attachments/assets/87081888-a682-4105-9176-a04e01ce6709" />

*   **Username**: `admin1`
*   **Password**: `123456`
