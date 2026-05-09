# Expense Tracker

A REST API for tracking personal expenses, built with Java and Spring Boot. The project follows a phased learning roadmap — each phase introduces one new Spring Boot concept and applies it directly to the app.

## Tech Stack

- **Java 21** + **Spring Boot 4**
- **PostgreSQL 16** (via Docker)
- **Flyway** — schema migrations
- **Spring Data JPA** / Hibernate
- **JWT** — authentication (Phase 6)
- **Testcontainers** — integration tests
- **Lombok** — boilerplate reduction

## Features

- Full CRUD for categories, expenses, and monthly budget allocations
- Filterable, paginated expense listing (`?month=`, `?categoryId=`, `?page=`, `?size=`)
- Monthly spending reports and budget vs. spent breakdowns
- Global error handling with structured JSON error responses
- Input validation on all request bodies
- CI via GitHub Actions

## Getting Started

### Prerequisites

- Java 21
- Docker + Docker Compose
- Maven (or use the included `./mvnw` wrapper)

### Setup

1. Clone the repository and create a `.env` file in the root:

```env
POSTGRES_USER=your_user
POSTGRES_PASSWORD=your_password
POSTGRES_DB=expensetracker
DATABASE_URL=jdbc:postgresql://localhost:5432/expensetracker
PGADMIN_DEFAULT_EMAIL=admin@admin.com
PGADMIN_DEFAULT_PASSWORD=admin
```

2. Start the database:

```bash
docker compose up -d postgres
```

3. Run the application:

```bash
cd backend
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`. Swagger UI is at `http://localhost:8080/swagger-ui.html`.

### Running Tests

```bash
cd backend
./mvnw verify
```

Tests use Testcontainers and spin up an isolated PostgreSQL instance automatically — no local database required.

## API Overview

| Resource | Base Path |
|---|---|
| Categories | `/api/categories` |
| Expenses | `/api/expenses` |
| Budgets | `/api/budgets` |
| Reports | `/api/reports` |

See [`docs/api-design.md`](docs/api-design.md) for the full endpoint reference, query parameters, and example payloads.

## Project Structure

```
backend/
├── src/main/java/com/tylerlam/expensetracker/
│   ├── category/          # Category entity, controller, service, repository, DTOs
│   ├── expense/           # Expense entity, controller, service, repository, DTOs
│   ├── budget/            # Budget entity, controller, service, repository, DTOs
│   ├── exception/         # Global exception handler and error response
│   └── shared/            # Shared exceptions and converters
├── src/main/resources/
│   ├── db/migration/      # Flyway SQL migration scripts
│   └── application.yaml
└── src/test/              # Unit and integration tests
```

## Roadmap

The app is built across six phases, each targeting a specific Spring Boot concept:

| Phase | Focus |
|---|---|
| 1 | Project setup, first REST endpoint |
| 2 | PostgreSQL, Docker, JPA, Flyway |
| 3 | Service layer, exception handling |
| 4 | Filtering, query params, pagination |
| 5 | Aggregate queries, reports, DTOs |
| 6 | Spring Security, JWT authentication |

See [`roadmap.md`](roadmap.md) for the full breakdown.
