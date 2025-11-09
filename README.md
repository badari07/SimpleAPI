# ProductService

Spring Boot service that manages products, categories, and search for an e-commerce scenario. The project demonstrates:

- RESTful CRUD endpoints (`/products`, `/search`)
- Database access via Spring Data JPA with Flyway migrations
- Consumer-style authentication via an external user-service
- Redis-based caching hook (optional)
- AWS deployment readiness (health check endpoint at `/`)

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.9+
- MySQL 8 (local) or an RDS instance

### Environment Variables
The application relies on these variables (defaults in `application.properties` are for local use):

| Variable      | Description                         |
|---------------|-------------------------------------|
| `DB_URL`      | JDBC URL including database/schema  |
| `DB_USERNAME` | Database username                   |
| `DB_PASSWORD` | Database password                   |

For AWS Elastic Beanstalk, supply a URL such as:
```
jdbc:mysql://<hostname>:3306/ProductService?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
```

### Database
- Flyway migrations live in `src/main/resources/db/migration`.
- The default schema used in the app is `ProductService`.
- Ensure the schema exists before starting the app (e.g., `CREATE DATABASE ProductService;`).

### Running Locally
```bash
./mvnw spring-boot:run
```

Useful endpoints:
- `GET /` – Health check
- `GET /products` – List products
- `POST /products` – Create product (requires `Authorization` header token that passes validation via user-service)
- `GET /search/` – Query products with optional filters/sorting/pagination
- `GET /search/category` – Shortcut for filtering by category

### Tests
Controller tests are in `src/test/java/com/example/productService/contorller/`.

Run all tests:
```bash
./mvnw test
```

### Notes
- Authentication is delegated to `http://userService/auth/validate`; adjust the host or service discovery as needed.
- Redis usage is optional; cache access errors are swallowed to keep environments without Redis functional.


