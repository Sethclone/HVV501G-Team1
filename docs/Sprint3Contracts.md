# Sprint 3 Contracts

Same deal as `Sprint1Contracts.md` and `Sprint2Contracts.md`: shared reference for everyone working in parallel. Don't code against something here until its "Status" is Confirmed.

**Sprint 3 Product Owner ("Dear Leader"):** Siggi (`SK`)

**Sprint Timeline:** Weeks 6–8 (Assignment 3 "Construction 2" milestone)
- **Consultation & Check-in:** Weekly progress check-in on Thursday — demoable working endpoints pushed to feature branches.
- **Milestone Goal:** A3 Presentation and Submission.

Sprint 1 (Auth, User/Product scaffolding, Admin seed) and Sprint 2 (StockMovement, Product Update/Delete, Search/Pagination, Self-Account Management, Reorder Flag, Profile Picture) are completed and merged into `main`. Sprint 3 tackles the **Order** management subsystem and **Admin User Management** (UC7).

---

## 0. Task sign-up

Assignments follow continuity from previous sprints so nobody has to ramp up on unfamiliar subsystems. All work is directed under the watchful guidance of our Sprint 3 Dear Leader, Siggi.

| Task | Covers | Use Case | Suggested owner | Est. hours |
|---|---|---|---|---|
| **A** | `Order` & `OrderItem` entities + Create Order endpoint (stock deduction) | UC11 | Siggi (Dear Leader) | ~15h |
| **B** | View Order by ID (with expanded items) + View All Orders (paginated) | UC17, UC12 | Filip | ~18h |
| **C** | Admin Manages User Accounts (update roles, details) | UC7 | Unnar | ~8h |
| **D** | Remove Item from Order (stock replenishment) | UC18 | Aron | ~8h |

### Stretch Goals / Secondary Phase (Week 8)
Once primary tasks are completed and merged:

| Stretch | Covers | Use Case | Suggested owner | Est. hours |
|---|---|---|---|---|
| **E** | Modify order details / quantities | UC14 | Open / Siggi | ~10h |

---

## 1. Decisions

| # | Question | Decision | Status |
|---|----------|----------|--------|
| 1 | Order status lifecycle | Enum `OrderStatus { PENDING, CONFIRMED, SHIPPED, CANCELLED }`. New orders start as `PENDING`. | Confirmed |
| 2 | Stock deduction timing | UC11 deducts `Product.stockQuantity` directly upon order creation (`StockMovement` is only for manual UC1 adjustments; the order itself records the actor, timestamp, and quantities). If any item's requested quantity exceeds available `stockQuantity`, reject the whole order with `400 Bad Request` (no partial order creation). | Confirmed |
| 3 | Stock restoration on item removal | Removing an item via UC18 restores that item's quantity back to `Product.stockQuantity` and recalculates `Order.totalPrice`. Deleting an item is only allowed if the order is `PENDING`. | Confirmed |
| 4 | Order pagination | Reuses `PagedResponse<T>` from Sprint 2 (`page=0, size=20, max=100`). | Confirmed |
| 5 | UC7 authorization & editable fields | Only users with `ROLE_ADMIN` can access `/api/admin/users/**`. Admins can update a user's `name`, `email` and `role` (`STAFF`/`ADMIN`), returning `UserSummary`. Email is a company owned work address: format checked only (no domain lock), `409` if already taken. Same name rules as UC6 (not blank, max 255). | Confirmed |
| 6 | Price snapshotting | `OrderItem` records the unit price at the time of purchase (`unitPrice`), shielding past orders from future product price changes. | Confirmed |
| 7 | UC7 lockout guards | An admin can't demote themselves, and the last remaining `ADMIN` can't be demoted. Both reject with `400` and a field error on `role` (standard `InvalidRequestException` pattern). | Confirmed |
| 8 | UC7 password reset | Closed system, so a locked out user can only be recovered by an admin. `POST /api/admin/users/{id}/reset-password` generates a random temporary password, stores only its hash and returns it once. The user is flagged `mustChangePassword`: they can still log in, but every endpoint except `PUT /api/users/me` returns `403` until they set a new password there (temp password goes in `currentPassword`). | Confirmed |
| 9 | Role/email source of truth | `JwtAuthenticationFilter` loads the user from the DB (by the token's `id` claim) on every request instead of trusting the token's `role` claim. Demotions and resets apply on the next request, an email change doesn't invalidate the user's token, and a token for a deleted user gets `401`. | Confirmed |

---

## 2. New Entities

### `Order` (owner: Task A)

Table name: `@Table(name = "orders")` (`order` is a reserved SQL keyword in H2).

| Field | Type | Notes |
|---|---|---|
| id | Long | PK, generated |
| user | `User` (ManyToOne) | The staff member / agent who created the order (`@JoinColumn(name = "user_id")`) |
| status | `OrderStatus` enum | `PENDING`, `CONFIRMED`, `SHIPPED`, `CANCELLED` |
| totalPrice | BigDecimal | Computed total of all line items |
| items | `List<OrderItem>` (OneToMany) | `mappedBy = "order"`, `cascade = CascadeType.ALL`, `orphanRemoval = true` |
| createdAt | Instant | Set on creation (`Instant.now()`) |

```java
public enum OrderStatus { PENDING, CONFIRMED, SHIPPED, CANCELLED }
```
Remember `@Enumerated(EnumType.STRING)` on `status`.

### `OrderItem` (owner: Task A)

Table name: `@Table(name = "order_items")`.

| Field | Type | Notes |
|---|---|---|
| id | Long | PK, generated |
| order | `Order` (ManyToOne) | Required (`@JoinColumn(name = "order_id")`) |
| product | `Product` (ManyToOne) | Required (`@JoinColumn(name = "product_id")`) |
| quantity | Integer | Required, > 0 |
| unitPrice | BigDecimal | Captured product price at time of order creation |

---

## 3. New DTOs

```
OrderItemRequest       { productId: Long, quantity: Integer }
CreateOrderRequest     { items: List<OrderItemRequest> }

OrderItemResponse      { id: Long, productId: Long, productName: String, quantity: Integer, unitPrice: BigDecimal, subtotal: BigDecimal }
OrderResponse          { id: Long, userId: Long, status: OrderStatus, totalPrice: BigDecimal, createdAt: Instant, items: List<OrderItemResponse> }

AdminUpdateUserRequest    { name?: String, email?: String, role?: Role }   // only set fields change, responds with UserSummary
TemporaryPasswordResponse { temporaryPassword: String }                   // UC7 reset, returned once (decision #8)
```

`UserSummary` gains `mustChangePassword: boolean` (also in `LoginResponse.user`) so a client knows to prompt for a new password after a reset.

Reuse existing:
- `PagedResponse<OrderResponse>`
- `UserSummary` / `RegisterResponse`
- `ErrorResponse` & `FieldErrorDetail` for validation errors

---

## 4. New Endpoints

| Method | Path | Use Case | Auth required | Owner |
|---|---|---|---|---|
| POST | `/api/orders` | UC11 | Authenticated staff | Task A (Siggi) |
| GET | `/api/orders/{id}` | UC17 | Authenticated staff | Task B (Filip) |
| GET | `/api/orders?page=&size=&status=` | UC12 | Authenticated staff | Task B (Filip) |
| DELETE | `/api/orders/{orderId}/items/{itemId}` | UC18 | Authenticated staff | Task D (Aron) |
| PUT | `/api/admin/users/{id}` | UC7 | ADMIN only | Task C (Unnar) |
| POST | `/api/admin/users/{id}/reset-password` | UC7 | ADMIN only | Task C (Unnar) |
| PUT | `/api/orders/{id}` | UC14 | Authenticated staff | Task E (Stretch) |

### Response codes & Validation rules
- `201 Created` - create order (`POST /api/orders`, returns `OrderResponse`)
- `200 OK` - get order(s), remove item from order (returns updated `OrderResponse`), admin update user (returns `UserSummary`), modify order (returns `OrderResponse`)
- Continue using the standard manual validation pattern: throw `InvalidRequestException(List<FieldErrorDetail>)` to return `400 Bad Request` with `fieldErrors`.
- Throw `404 Not Found` (`OrderNotFoundException`, `ProductNotFoundException`) when referenced IDs do not exist.
- UC11: Reject with `400` if `items` is empty, if any `quantity <= 0`, or if any item's quantity exceeds current `stockQuantity`.
- UC18: Reject with `400` if the order is not `PENDING` (e.g., already `CONFIRMED`, `SHIPPED`, or `CANCELLED`). Return `200 OK` with the updated `OrderResponse`.
- UC7: `200 OK` with `UserSummary` (update) or `TemporaryPasswordResponse` (reset). `400` for invalid name/email or a blocked demotion (decision #7), `404` (`UserNotFoundException`) for an unknown user id, `409` for a taken email, `403` for non admins.

---

## 5. Branch / Ownership Map

| Branch | Owner | Depends on |
|---|---|---|
| `feature/uc11-create-order` | Task A (Siggi) | - (push `Order` & `OrderItem` entities first) |
| `feature/uc12-uc17-view-orders` | Task B (Filip) | Task A's entities |
| `feature/uc7-admin-manage-users` | Task C (Unnar) | Existing `User` entity & `AdminController` |
| `feature/uc18-remove-order-item` | Task D (Aron) | Task A's entities |
| `feature/uc14-modify-order` | Task E (Stretch) | Task A's entities |

Merge order: Siggi pushes the initial `Order` & `OrderItem` entities to unblock Filip and Aron, then endpoints merge independently into `dev`.
