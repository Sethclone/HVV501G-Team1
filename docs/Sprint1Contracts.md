# Sprint 1 Contracts

Shared reference for everyone working in parallel on UC4, UC5, UC2, UC8, UC16. Edit this
via PR/comments as decisions get made - don't code against something here until
its "Status" is Confirmed.

**Sprint 1 deadline:** Sunday 20/09/2026

---

## 0. Task sign-up

Pick one task
by **Saturday 12/09/2026 EOD** so work can actually start Sunday morning. The task
codes (P1-P4) below match the "owner" labels used everywhere else in this doc, so
once you've claimed one, every section already tells you what's yours.

| Task | Covers | Est. hours | Claimed by |
|---|---|---|---|
| **P1** | Project setup + `User`/`Role` entity + admin seeding + UC4 (Register) | ~15h | _open_ |
| **P2** | UC5 (Sign in) + JWT/role-guard security config + UC16 (Create admin) | ~15h | _open_ |
| **P3** | `Product` entity + UC2 (Add product, incl. image upload) | ~15h | _open_ |
| **P4** | UC8 (View product by id) + class diagram + sequence diagram | ~11h | _open_ |

To claim: edit this row's "Claimed by" cell with your name and push it

---

## 1. Decisions

| # | Question | Decision | Status |
|---|----------|----------|--------|
| 1 | Auth mechanism | **JWT** (stateless, fits API-first vision) | Confirmed |
| 2 | UC2 image format/size limits | e.g. JPEG/PNG only, max 5MB - exact values TBD | Open |
| 3 | UC2 product creation authorization | **Any authenticated staff member can create a product directly** (no approval workflow in Sprint 1) | Confirmed |
| 4 | Admin account creation (UC16) | **Per TA feedback: UC16 is pulled into Sprint 1 (was P2/week 9).** Public registration (UC4) can only ever create `STAFF` accounts - it must not accept a caller-supplied role. Creating an `ADMIN` account requires an existing admin (UC16, admin-only endpoint). To bootstrap the very first admin, the app seeds one `ADMIN` account on startup (see 4). | Confirmed |

Whoever resolves #2, update this table directly in a PR rather than posting it elsewhere - this file is the source of truth.

---

## 2. Package layout

```
is.hi.store
├── config        # security config, web config
├── controller     # REST controllers
├── dto            # request/response objects
├── entity         # JPA entities
├── repository     # Spring Data repositories
├── service        # business logic
└── exception      # error handling, custom exceptions
```

---

## 3. Entities

### `User` (owner: P1)

| Field | Type | Notes |
|---|---|---|
| id | Long | PK, generated |
| name | String | required |
| email | String | required, unique |
| passwordHash | String | never returned in any response |
| role | `Role` enum | `STAFF` or `ADMIN` |
| createdAt | Instant | set on creation |

```java
enum Role { STAFF, ADMIN }
```

No separate entity is needed for admins - UC16 just creates a `User` row with
`role = ADMIN`. The distinction is entirely in who's allowed to call which
endpoint, not in the data model.

### `Product` (owner: P3)

| Field | Type | Notes |
|---|---|---|
| id | Long | PK, generated |
| name | String | required |
| category | String | required |
| price | BigDecimal | required, > 0 |
| stockQuantity | Integer | required, >= 0, set at creation |
| imageData / imageUrl | byte[] or String | TBD by decision #2 - placeholder image if none uploaded |
| reorderFlagged | boolean | default false (used later by UC9, not Sprint 1) |
| createdAt | Instant | set on creation |

> `StockMovement` and `Order`/`OrderItem` entities show up in the class diagram
> (Sprint 2+) but are not built in Sprint 1 - don't scaffold them yet, just
> avoid naming things in a way that collides with them later (e.g. avoid a
> generic `Item` class name).

---

## 4. Admin seeding (owner: P1)

On startup, if no `ADMIN` user exists, create one automatically (e.g. a
`CommandLineRunner`/`ApplicationRunner` bean) so there's an admin who can log in
(UC5) and call UC16 to create further admins.

- Credentials come from config (`application.properties` / env vars), never
  hardcoded: e.g. `store.admin.seed.email`, `store.admin.seed.password`, with
  a documented dev-only default and a note that it must be overridden for
  anything beyond local dev.
- Reuse the same password-hashing path as normal registration - don't write a
  second, divergent way of creating a `User`.

---

## 5. DTOs

### Auth (owner: P1 / P2)

```
RegisterRequest    { name, email, password }              // no role field - always creates STAFF
RegisterResponse   { id, name, email, role, createdAt }     // no password fields, ever

LoginRequest        { email, password }
LoginResponse       { token, expiresAt, user: { id, name, email, role } }

CreateAdminRequest  { name, email, password }              // UC16 - role is implicit (ADMIN), not caller-supplied
```

### Product (owner: P3 / P4)

```
ProductCreateRequest  { name, category, price, stockQuantity, image? }  // multipart/form-data
ProductResponse         { id, name, category, price, stockQuantity, imageUrl, reorderFlagged, createdAt }
```

### Errors (shared shape, everyone uses this)

```
ErrorResponse {
  timestamp: Instant,
  status: int,
  error: String,        // e.g. "Bad Request"
  message: String,       // human-readable summary
  path: String,
  fieldErrors?: [ { field, message } ]   // present on validation failures
}
```

---

## 6. Endpoints

| Method | Path | Use Case | Auth required | Owner |
|---|---|---|---|---|
| POST | `/api/auth/register` | UC4 | Yes - ADMIN only | P1 |
| POST | `/api/auth/login` | UC5 | No | P2 |
| POST | `/api/admin/users` | UC16 | Yes - ADMIN only | P2 |
| POST | `/api/products` | UC2 | Yes - any authenticated staff | P3 |
| GET | `/api/products/{id}` | UC8 | Yes - any authenticated staff | P4 |

Auth header: `Authorization: Bearer <token>`.

### Response codes

- `201 Created` - register, create admin, create product (successful)
- `200 OK` - login, get product (successful)
- `400 Bad Request` - validation failure (`fieldErrors` populated)
- `401 Unauthorized` - missing/invalid token
- `403 Forbidden` - valid token but wrong role (e.g. STAFF calling `/api/admin/users`)
- `404 Not Found` - product id doesn't exist
- `409 Conflict` - duplicate email (register/create admin) or duplicate SKU (create product, if SKU is added)

---

## 7. Branch / ownership map

| Branch | Owner | Depends on |
|---|---|---|
| `feature/uc4-register` | P1 | - (push `User`/`Role` entity + admin seed early so P2 can branch off it) |
| `feature/uc5-signin` | P2 | P1's `User` entity |
| `feature/uc16-create-admin` | P2 | Same branch/PR as UC5 - shares the role-based auth guard |
| `feature/uc2-add-product` | P3 | - (push `Product` entity early so P4 can branch off it) |
| `feature/uc8-view-product` | P4 | P3's `Product` entity |
| Class diagram | P4 | This file, section 3 |
| Sequence diagram (UC2) | P4 | P3's finished endpoint - do this last, once the real flow exists |

Merge order: entity-only commits from P1 and P3 first, then each endpoint PR independently.

