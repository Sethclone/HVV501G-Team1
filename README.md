# HBV501G Team 1: Inventory Store API

REST API for a small store's inventory, built with Spring Boot, Spring Security (JWT) and JPA (H2).
Use cases are described in [docs/Verkefni1.md](docs/Verkefni1.md). Sprint plans and API decisions are in
[docs/Sprint1Contracts.md](docs/Sprint1Contracts.md) and [docs/Sprint2Contracts.md](docs/Sprint2Contracts.md).

## Running it

```bash
./mvnw spring-boot:run      # Windows: mvnw.cmd spring-boot:run
./mvnw test                 # run the tests
```

- Runs on `http://localhost:8090`. The database is H2 in memory, so data resets on restart.
- On startup an admin is seeded: `root@example.com` / `password`.
- Log in with `POST /api/auth/login`, then send `Authorization: Bearer <token>` on other requests.
- Example requests: [REST_CLIENT/](REST_CLIENT/)

## Sprint 1 (done)

| UC | Feature | Endpoint | Status |
|---|---|---|---|
| UC2 | Add product (with image upload) | `POST /api/products` | Done |
| UC4 | Register staff account (admin only) | `POST /api/auth/register` | Done |
| UC5 | Sign in, returns JWT | `POST /api/auth/login` | Done |
| UC8 | View product by id | `GET /api/products/{id}` | Done |
| UC16 | Create admin account (admin only) | `POST /api/admin/users` | Done |

## Sprint 2

| UC | Feature | Endpoint | Status |
|---|---|---|---|
| UC1 | Record stock movement (add/remove) | `POST /api/products/{id}/stock-movements` | Done |
| UC6 | Manage own account (name, password) | `PUT /api/users/me` | Done |
| UC9 | Flag product for reorder (stretch goal) | `PATCH /api/products/{id}/reorder-flag` | Done |
| UC3 | Search and filter products, paginated | `GET /api/products?category=&minPrice=&maxPrice=&page=&size=` | Not started |
| UC10 | Update or remove product | `PATCH` / `DELETE /api/products/{id}` | Not started |
| UC20 | Upload profile picture (stretch goal) | `POST /api/users/me/profile-picture` | Not started |

UC1, UC6 and UC9 went through a review and fix round and have integration tests
(`ProductControllerTests`, `UserControllerTests`).

## Exceptions and known issues

| Item | Notes |
|---|---|
| UC3, UC10 | Not started this sprint, might have to be carried over. |
| UC6 email | Users can't change their own email. It's a work email set by an admin (decision #5 in the Sprint 2 contracts). |
| UC2 image limits | Allowed image formats and max size are still undecided (Sprint 1 decision #2). |
| Error status codes | Some Spring level errors (e.g. a non-numeric id in the URL) return `401` instead of `400`, because `/error` isn't permitted in the security config. |
| Stock concurrency | Two stock removals at the exact same moment could both pass the stock check. No row locking yet. |
