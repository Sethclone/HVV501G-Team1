# Sprint 2 Contracts

Same deal as `Sprint1Contracts.md`: shared reference for everyone working in parallel. Don't
code against something here until its "Status" is Confirmed.

**Real deadline:** Sunday 11 Oct (A3 "Construction 1" submission - repo link + status notes in
Canvas). **This Thursday** is the weekly progress check-in, not the hard deadline - "ready for
Thursday" means demoable working endpoints pushed, not full polish/tests. Validation edge
cases and tests can land through the weekend up to Sunday.

Sprint 1 (UC4, UC5, UC2, UC8, UC16) is done. This covers the next slice.

---

## 0. Task sign-up

Suggested split below is continuity-based - each person extends the area they already own
from Sprint 1, so nobody has to ramp up on someone else's code. Swap if you'd rather not,
just edit the table.

| Task | Covers | Use Case | Suggested owner |
|---|---|---|---|
| **A** | `StockMovement` entity + record-movement endpoint | UC1 | Aron |
| **B** | Update/remove product endpoints | UC10 | Siggi |
| **C** | Search/filter products (pagination) | UC3 | Filip |
| **D** | Manage own account | UC6 | Unnar |

UC9 (flag for reorder) and UC20 (upload profile picture) are **optional stretch goals, not
committed for Thursday** - lower priority (P3) in the original estimation, and spreading 4
people across 6 use cases this week isn't realistic as a baseline. Whoever finishes their
primary task (A-D) early picks one up if there's time before Sunday; don't start on these
until your primary task is done and pushed.

| Stretch | Covers | Use Case |
|---|---|---|
| **E** | Flag product for reorder | UC9 |
| **F** | Upload profile picture | UC20 |

---

## 1. Decisions

| # | Question | Decision | Status |
|---|----------|----------|--------|
| 1 | Pagination defaults (UC3) | `page=0, size=20`, max `size=100` | Proposed |
| 2 | Stock movement below zero | Reject with `400` if a `REMOVE` would take stock below 0 (per UC1 extension 2a in `Verkefni1.md`) | Confirmed |
| 3 | UC6 email change collision | `409 Conflict` if the new email belongs to another account - reuse the existing `EmailAlreadyExistsException` pattern from Sprint 1 | Confirmed |
| 4 | UC6 role field | `UpdateAccountRequest` never has a role field - a user can never self-promote via this endpoint, same reasoning as UC4's admin-only registration | Confirmed |

---

## 2. New entity

### `StockMovement` (owner: Task A)

| Field | Type | Notes |
|---|---|---|
| id | Long | PK, generated |
| product | `Product` (ManyToOne) | required |
| quantity | Integer | required, > 0 - magnitude only, direction comes from `type` |
| type | `MovementType` enum | `ADD` or `REMOVE` |
| performedBy | `User` (ManyToOne) | the staff member who recorded it |
| timestamp | Instant | set on creation |

```java
enum MovementType { ADD, REMOVE }
```

Don't forget `@Enumerated(EnumType.STRING)` on `type` - same ordinal-storage bug that bit
`User.role` in Sprint 1, avoid repeating it.

---

## 3. New DTOs

```
StockMovementRequest   { quantity, type }                 // productId comes from the path, not the body
StockMovementResponse  { id, productId, quantity, type, performedBy, timestamp, newStockLevel }

ProductUpdateRequest   { name?, category?, price?, stockQuantity? }   // all optional - PATCH semantics, only set fields are changed

PagedResponse<T>       { content, page, size, totalElements, totalPages }

UpdateAccountRequest   { name?, email?, password? }        // no role field, see decision #5
```

Reuse existing `ProductResponse`, `UserSummary`, `ErrorResponse` for responses - don't make new ones that duplicate them.

---

## 4. New endpoints

| Method | Path | Use Case | Auth required | Owner |
|---|---|---|---|---|
| POST | `/api/products/{id}/stock-movements` | UC1 | staff | Task A |
| PATCH | `/api/products/{id}` | UC10 | staff | Task B |
| DELETE | `/api/products/{id}` | UC10 | staff (see decision #1) | Task B |
| GET | `/api/products?category=&minPrice=&maxPrice=&page=&size=` | UC3 | staff | Task C |
| PUT | `/api/users/me` | UC6 | any authenticated user | Task D |

New response code: `204 No Content` on a successful delete. Everything else reuses the
400/401/403/404/409 pattern from Sprint 1 - same `ErrorResponse` shape, same
`GlobalExceptionHandler`/`ApiExceptionHandler` split (controller exceptions vs. filter-chain
auth failures).

### Extensions to handle (per `Verkefni1.md`)

- UC1: reject invalid quantity (zero/negative/non-numeric) and over-removal (`400`), reject if product doesn't exist (`404`)
- UC10: reject invalid field values on update (`400`), reject on missing product (`404`)
- UC3: out-of-range page returns an empty result with correct pagination metadata, not an error

---

## 5. Stretch goal endpoints (optional - only if a primary task finishes early)

| Method | Path | Use Case | Auth required | Notes |
|---|---|---|---|---|
| PATCH | `/api/products/{id}/reorder-flag` | UC9 | staff | Body: `{ flagged: boolean }`. `Product.reorderFlagged` already exists on the entity (Sprint 1), so this is just toggling an existing field - no new entity work. |
| POST | `/api/users/me/profile-picture` | UC20 | any authenticated user | `multipart/form-data`, same pattern as UC2's image upload - use `@ModelAttribute`, not `@RequestBody`. Decision #2 from Sprint 1 (image format/size limits) still needs a value before this can actually be built. |

---

## 6. Branch / ownership map

| Branch | Owner | Depends on |
|---|---|---|
| `feature/uc1-stock-movement` | Task A | - (push `StockMovement` entity early) |
| `feature/uc10-update-remove-product` | Task B | existing `Product`/`ProductController` - no new entity needed |
| `feature/uc3-search-filter` | Task C | existing `Product`/`ProductRepository` |
| `feature/uc6-manage-account` | Task D | existing `User`/`AuthService` |

All four branch off the latest `dev` (or `main`, whichever is current) independently - no
cross-dependencies between them this time, unlike Sprint 1's entity-first ordering. Merge
in any order as each is ready.
