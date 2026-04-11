# Expense Tracker — API Design

## Data Model

### Expense
| Field | Type | Notes |
|---|---|---|
| `id` | Long | Auto-generated |
| `amount` | BigDecimal | Use BigDecimal, not double |
| `date` | LocalDate | ISO-8601 format (yyyy-MM-dd) |
| `description` | String | Optional notes |
| `categoryId` | Long | FK to Category |
| `createdAt` | LocalDateTime | Auto-set on creation |
| `updatedAt` | LocalDateTime | Auto-set on update |

### Category
| Field | Type | Notes |
|---|---|---|
| `id` | Long | Auto-generated |
| `name` | String | e.g. Food, Rent, Transport |
| `budgetLimit` | BigDecimal | Nullable — not all categories need a limit |
| `createdAt` | LocalDateTime | Auto-set on creation |

---

## Endpoints

### Categories — `/api/categories`

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/categories` | List all categories |
| `POST` | `/api/categories` | Create a category |
| `GET` | `/api/categories/{id}` | Get a single category |
| `PUT` | `/api/categories/{id}` | Update name or budget limit |
| `DELETE` | `/api/categories/{id}` | Delete a category |

> **Note:** `DELETE /api/categories/{id}` returns `409 Conflict` if any expenses reference that category.

### Expenses — `/api/expenses`

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/expenses` | List expenses (filterable, paginated) |
| `POST` | `/api/expenses` | Create an expense |
| `GET` | `/api/expenses/{id}` | Get a single expense |
| `PUT` | `/api/expenses/{id}` | Update an expense |
| `DELETE` | `/api/expenses/{id}` | Delete an expense |

#### Query Parameters for `GET /api/expenses`

| Param | Example | Description |
|---|---|---|
| `month` | `?month=2026-04` | Filter by month |
| `categoryId` | `?categoryId=3` | Filter by category |
| `page` | `?page=0` | Page number (zero-indexed) |
| `size` | `?size=20` | Page size |

Parameters can be combined, e.g. `?month=2026-04&categoryId=3&page=0&size=20`.

#### Paginated Response Envelope

```json
{
  "content": [ "...expenses..." ],
  "page": 0,
  "size": 20,
  "totalElements": 84,
  "totalPages": 5
}
```

### Reports — `/api/reports`

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/reports/monthly` | Total spent for a month, broken down by category |
| `GET` | `/api/reports/budget` | Spent vs. budget limit per category for a month |

Both endpoints accept `?month=2026-04` as a required query parameter.

---

## Example Payloads

### `POST /api/categories`
```json
{
  "name": "Food",
  "budgetLimit": 400.00
}
```

### `POST /api/expenses`
```json
{
  "amount": 42.50,
  "date": "2026-04-09",
  "description": "Grocery run",
  "categoryId": 2
}
```

### `GET /api/reports/monthly?month=2026-04`
```json
{
  "month": "2026-04",
  "totalSpent": 1340.00,
  "byCategory": [
    { "category": "Food", "spent": 320.00 },
    { "category": "Rent", "spent": 1000.00 },
    { "category": "Transport", "spent": 20.00 }
  ]
}
```

### `GET /api/reports/budget?month=2026-04`
```json
{
  "month": "2026-04",
  "budgets": [
    { "category": "Food", "budgetLimit": 400.00, "spent": 320.00, "remaining": 80.00 },
    { "category": "Rent", "budgetLimit": 1000.00, "spent": 1000.00, "remaining": 0.00 },
    { "category": "Transport", "budgetLimit": null, "spent": 20.00, "remaining": null }
  ]
}
```

---

## Design Decisions

- **`amount` type:** `BigDecimal` in Java — never `double` for money values.
- **Date format:** `LocalDate` / ISO-8601 (`yyyy-MM-dd`) for expense dates. No time component needed.
- **Category deletion:** Blocked with `409 Conflict` if expenses reference the category.
- **Missing `categoryId`:** Returns `404 Not Found` with a descriptive message if the category doesn't exist when creating an expense.
- **Reports:** Read-only (`GET` only). Fully derived from expense data — no separate storage.
- **Categories:** Fully user-managed. No pre-seeded defaults.
- **Auth:** JWT authentication to be added after core functionality is complete, securing all `/api/**` endpoints.