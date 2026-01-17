# E-Commerce Backend (Banking Standard)

Spring Boot application designed with financial system principles.

## Database Schema (Key Entities)

*   **User**: Stores credentials and roles.
*   **Wallet**: 1-to-1 with User. Stores `balance`.
*   **WalletTransaction**: 1-to-Many with Wallet. Immutable history of all money movements.
    *   *Fields*: `amount`, `type` (CREDIT/DEBIT), `status`, `relatedOrder`.

## Security & Compliance

1.  **JWT Authentication**: Stateless authentication.
2.  **BCrypt**: Password hashing.
3.  **CORS**: Configured to allow specific frontend origins only.
4.  **Transactional**: 
    *   `OrderServiceImpl.placeOrder` is `@Transactional`.
    *   If any step fails (Inventory update, Wallet debit, Payment record), the entire transaction rolls back.

## Testing

Run Unit Tests to verify financial logic:

```bash
mvn test
```

*Includes strictly tested `WalletServiceTest` ensuring money calculations are accurate.*

## Transaction Flow (Concurrency)

To prevent **Double Spending**, we utilize **Pessimistic Locking**:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<Wallet> findByUser(User user);
```

This ensures that when a transaction is processing a specific wallet, other transactions must wait, preventing race conditions.

## API Documentation

(Assuming application is running)
*   **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
*   **H2 Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
