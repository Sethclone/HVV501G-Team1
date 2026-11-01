# HBV501G Team 1: Inventory Store API

REST API for a small store's inventory, built with Spring Boot, Spring Security (JWT) and JPA (H2).
Use cases are described in [docs/Verkefni1.md](docs/Verkefni1.md). Sprint plans and API decisions are in
[docs/Sprint1Contracts.md](docs/Sprint1Contracts.md), [docs/Sprint2Contracts.md](docs/Sprint2Contracts.md) and
[docs/Sprint3Contracts.md](docs/Sprint3Contracts.md).

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

## Sprint 2 (done)

| UC | Feature | Endpoint | Status |
|---|---|---|---|
| UC1 | Record stock movement (add/remove) | `POST /api/products/{id}/stock-movements` | Done |
| UC3 | Search and filter products, paginated | `GET /api/products?category=&minPrice=&maxPrice=&page=&size=` | Done |
| UC6 | Manage own account (name, password) | `PUT /api/users/me` | Done |
| UC10 | Update or remove product | `PATCH` / `DELETE /api/products/{id}` | Done |
| UC9 | Flag product for reorder (stretch goal) | `PATCH /api/products/{id}/reorder-flag` | Done |
| UC20 | Upload profile picture (stretch goal) | `POST /api/users/me/profile-picture` | Done (minimal) |

UC1, UC6 and UC9 went through a review and fix round and have integration tests
(`ProductControllerTests`, `UserControllerTests`). UC3, UC10 and UC20 don't have tests yet.

## Sprint 3

| UC | Feature | Endpoint | Status |
|---|---|---|---|
| UC11 | Create order, deducts stock | `POST /api/orders` | Done |
| UC17 | View order by id, with items | `GET /api/orders/{id}` | Done |
| UC12 | View all orders, paginated, filter by status | `GET /api/orders?status=&page=&size=` | Done |
| UC18 | Remove item from order, restores stock | `DELETE /api/orders/{orderId}/items/{itemId}` | Done |
| UC7 | Admin manages users (name, email, role) | `PUT /api/admin/users/{id}` | Done |
| UC7 | Admin resets a user's password | `POST /api/admin/users/{id}/reset-password` | Done |
| UC14 | Modify order (stretch goal) | `PUT /api/orders/{id}` | Not started |

After a password reset the user must set a new password with `PUT /api/users/me` before any other endpoint
works (`403` until then). Role and email are read from the database on every request, so an admin's changes
apply to tokens that were already issued (decisions #8 and #9 in the Sprint 3 contracts).

UC7 has integration tests (`AdminControllerTests`). The order endpoints (UC11, UC12, UC17, UC18) don't have tests yet.

## Exceptions and known issues

| Item | Notes |
|---|---|
| UC6 email | Users can't change their own email. It's a work email set by an admin (decision #5 in the Sprint 2 contracts). |
| Image limits (UC2, UC20) | Allowed image formats and max size are still undecided (Sprint 1 decision #2), so no limits are enforced. |
| Stock concurrency | Two stock removals or orders at the exact same moment could both pass the stock check. No row locking yet. |
| UC18 status check | Only `PENDING` orders can have items removed, but nothing changes an order's status yet (UC14 isn't done), so the rejection can't be demoed. |
| UC7 lockout guards | Not atomic: two admins demoting each other at the same moment could both succeed. |
| Deleting a product in an order | Returns `500` (foreign key violation) instead of a clear error. |
