# Sprint 4 Contracts

Same deal as `Sprint1Contracts.md`, `Sprint2Contracts.md` and `Sprint3Contracts.md`: shared reference for everyone working in parallel. Don't code against something here until its "Status" is Confirmed.

**Sprint 4 Product Owner:** Filip (`FO`), matching the week 9–10 schedule in `Verkefni1.md`.

**Sprint Timeline:** Weeks 9–10 (Assignment 4 "Final" milestone)
- **A4 Presentation:** Week 9.
- **Final Review:** Week 10.
- **Milestone Goal:** final integration, and a submission-ready `main`.

Sprint 3 delivered the order subsystem and admin user management (UC11, UC12, UC17, UC18, UC7). Sprint 4 closes the remaining use cases from the plan: **UC13 (Remove an order)**, **UC15 (Admin deletes an account)** and **UC19 (Reassign an order)**, plus final integration and polish. **UC16 was already delivered in Sprint 1** and only needs regression coverage. **UC14 (Modify order)** is carried over from Sprint 3.
The missing order-status transition endpoint from Sprint 3 is also in scope as a supporting endpoint. Without it, `CONFIRMED`, `SHIPPED` and `CANCELLED` can never be reached through the API, which makes UC13 and the UC18 `PENDING` guard impossible to demo end-to-end.

---

## 0. Task sign-up

| Task | Covers | Use Case | Suggested owner | Est. hours |
|---|---|---|---|---|
| **A** | Status transition endpoint + Remove Order (with stock restoration) | UC13 + supporting | Aron | ~14h |
| **B** | Admin Deletes Account | UC15 | Open | ~8h |
| **C** | Reassign Order | UC19 | Open | ~8h |
| **D** | Modify order | UC14 | Open | ~10h |

### Additional required features not in original plan

| Task | Covers | Est. hours |
|---|---|---|
| **E** | Some feature related to one of the seven design patterns mentioned in class  | ~10h |

---

## 1. Decisions

| # | Question | Decision | Status |
|---|----------|----------|--------|
| 1 | Order status transitions | Add `PATCH /api/orders/{id}/status` with `UpdateOrderStatusRequest { status }`. Allowed moves: `PENDING -> CONFIRMED`, `PENDING -> CANCELLED`, `CONFIRMED -> SHIPPED`, `CONFIRMED -> CANCELLED`, `SHIPPED -> CANCELLED` (refund). `CANCELLED` is terminal. Same status or any other move returns `400` with a `status` field error. Returns `OrderResponse`. |Confirmed|
| 2 | Who can change status | Advancing to `CONFIRMED`/`SHIPPED` is open to any authenticated staff member, like the other order endpoints. Moving an order to `CANCELLED` is **ADMIN only** because it restores stock; a non-admin attempt returns `403`. | Confirmed |
| 3 | Stock restoration on cancel vs. delete | Moving an order to `CANCELLED` returns every item's quantity to `Product.stockQuantity` once. Deleting a `CANCELLED` order then only removes the record (no second restoration). Deleting an order in any other status returns `400` | Confirmed |
| 4 | UC13 authorization & response | `DELETE /api/orders/{id}` is **ADMIN only** (UC13 says an admin issues the command). `204 No Content` on success, `404` if the order doesn't exist. | Confirmed |
| 5 | UC15 deletion strategy | Hard-delete the `User` row (no `active` flag / soft delete). **ADMIN only**, `204 No Content`. A user who still owns orders cannot be deleted (`409 Conflict`) - reassign those orders with UC19 first. Stock movements are kept for inventory history, but their `performedBy` is cleared to `null` so the deleted user's identity is removed. | Confirmed |
| 6 | UC15 lockout guards | Same shape as UC7: an admin can't delete themselves and the last remaining `ADMIN` can't be deleted. Both return `400` with a field error on `id`. Unknown user is `404`; non-admin is `403`. | Confirmed |
| 7 | UC19 reassign endpoint | `PUT /api/orders/{id}/user`, **ADMIN only**, body `ReassignOrderRequest { userId }`. The target must be an existing user (`404` otherwise). Reassigning to the same user is an idempotent `200 OK`. No stock or status changes. Returns the updated `OrderResponse`. | Confirmed |
| 8 | Order ownership authorization | Existing Sprint 3 behaviour stands: any authenticated staff member can view/create/modify any order. Only UC13 and UC19 are admin-only. UC19 changes the `user` field on the order, not who is allowed to access it. | Confirmed |
| 9 | Order stock changes & `StockMovement` | Order creation, cancellation, deletion and UC14 modification keep changing `Product.stockQuantity` directly; they do not write `StockMovement` rows (Sprint 3 decision #2). `StockMovement` remains the manual UC1 audit trail. | Confirmed |
| 10 | UC14 carryover | `PUT /api/orders/{id}` Only `PENDING` orders, full replacement of `items`, old quantities restored and new quantities deducted atomically, total recalculated, and `unitPrice` re-snapshotted at modification time. | Confirmed |

---

## 2. Entity / repository changes

No new entities this sprint. The work is mostly service and repository logic on top of the Sprint 3 model.

- `Order` / `OrderItem`: no schema change. Status transitions, reassignment and deletion are service logic.
- `User`: no new column, because UC15 uses a hard delete (decision #5).
- `StockMovement.performedBy`: already nullable at the JPA level. UC15 clears it instead of deleting the movement, so inventory history survives. Update `StockMovementResponse` to tolerate a null performer if movements are ever exposed by a read endpoint - it currently calls `getPerformedBy().getId()`.
- `UserRepository`: add `void delete(User user)` (it extends `Repository`, not `JpaRepository`).
- `OrderRepository`: add the UC15 guard lookup, e.g. `boolean existsByUser(User user)`.
- `StockMovementRepository`: add a way to clear a deleted user's movements, e.g. a `@Modifying` query: `update StockMovement s set s.performedBy = null where s.performedBy = ?1`.

`OrderRepository` already extends `JpaRepository`, so `delete(order)` is available for UC13. Cascading and `orphanRemoval = true` on `Order.items` delete the line items with the order.

---

## 3. New DTOs

```
UpdateOrderStatusRequest  { status: OrderStatus }
ReassignOrderRequest      { userId: Long }
UpdateOrderRequest        { items: List<OrderItemRequest> } 
```

Reuse existing:
- `OrderResponse` / `OrderItemResponse`
- `PagedResponse<OrderResponse>`
- `ErrorResponse` & `FieldErrorDetail` for validation errors

UC13 and UC15 return `204 No Content`, so they need no response DTO. UC19 and the status endpoint return `OrderResponse`.

---

## 4. New endpoints

| Method | Path | Use Case | Auth required | Owner |
|---|---|---|---|---|
| DELETE | `/api/orders/{id}` | UC13 | ADMIN only | Task A |
| PATCH | `/api/orders/{id}/status` | Supporting (order lifecycle) | Authenticated staff; `CANCELLED` ADMIN only | Task A|
| DELETE | `/api/admin/users/{id}` | UC15 | ADMIN only | Task B |
| PUT | `/api/orders/{id}/user` | UC19 | ADMIN only | Task C |
| PUT | `/api/orders/{id}` | UC14 | Authenticated staff | Task D|

Existing Sprint 1–3 endpoints stay unchanged. UC16 (`POST /api/admin/users`) already exists and is not rebuilt.

### Response codes & validation rules

- `204 No Content` - delete order (UC13), delete user (UC15).
- `200 OK` - status update, reassign order (returns updated `OrderResponse`), modify order.
- `400 Bad Request` - invalid status transition, self-delete, last-admin delete, invalid request body. Continue using the standard manual validation pattern: throw `InvalidRequestException(List<FieldErrorDetail>)` to return `400` with `fieldErrors`.
- `401 Unauthorized` - missing/invalid token.
- `403 Forbidden` - non-admin calling an admin-only endpoint, or a non-admin trying to move an order to `CANCELLED`.
- `404 Not Found` - unknown order (`OrderNotFoundException`) or user (`UserNotFoundException`).
- `409 Conflict` - UC15 user still owns orders; reassign them with UC19 first.
- All methods that touch stock or multiple tables (`cancel`, `delete order`, `delete user`, UC14) must be `@Transactional` so a failure can't leave stock and records out of sync.

Specific rules:
- UC13: restore stock first, then delete the order and its items. `404` if the order doesn't exist.
- Status update: validate the current status and the requested target against decision #1. `400` on an illegal or same-status transition.
- UC15: check self-delete and last-admin guards, check for owned orders, clear the user's stock-movement performer, then delete. `404` for an unknown id, `409` for owned orders.
- UC19: validate the target user exists, then set `Order.user`. `404` for an unknown order or user. Reassigning to the current owner is a `200` no-op.
- UC14: only `PENDING` orders, all-or-nothing stock reconciliation, `400` if the order is not `PENDING` or stock is insufficient.

Security config needs explicit matchers for the two admin-only order routes so the existing `anyRequest().hasAnyRole("STAFF", "ADMIN")` rule doesn't accidentally open them:
- `DELETE /api/orders/*` -> `ROLE_ADMIN`
- `PUT /api/orders/*/user` -> `ROLE_ADMIN`

`DELETE /api/orders/{orderId}/items/{itemId}` (UC18) must stay available to staff.

---

## 5. Branch / Ownership Map

| Branch | Owner | Depends on |
|---|---|---|
| `feature/uc13-remove-order` | Task A| Existing `Order`/`OrderItem`; coordinate with Task C/D on `OrderService` |
| `feature/uc15-admin-delete-user` | Task B | Existing `User`/`AdminController`; reuses UC7 guards |
| `feature/uc19-reassign-order` | Task C | Existing `Order`/`User` |
| `feature/uc14-modify-order` | Task D | Task A's status/delete changes |

Merge order: Task A first because it introduces the order lifecycle and the stock-restoration logic, and it touches the same `OrderService`/`OrderController` that Task C/D build on. Task B is independent and can merge at any time. Final integration happens after all primary branches are in `dev`, then `dev` is merged to `main`.

---

## 6. Final integration & polish checklist

Do this before the submission:

- Update `README.md` with Sprint 3 and Sprint 4 endpoint tables and the current known issues.
- Add `REST_CLIENT/Sprint4.http` covering UC13, UC15, UC19, the status flow, and the UC16 regression check.
- Add integration tests for all new endpoints (`OrderControllerTests`, `AdminControllerTests`) and keep `./mvnw test` green.
- Re-check the known issues table:
  - Stock concurrency (two simultaneous removals) - add locking or keep it documented as a known limitation.
  - Product deletion when order items reference the product - handle the foreign-key case with a `409` instead of a `500`.
  - Malformed path variables - confirm they return `400` rather than `401`.
  - Image format/size limits (Sprint 1 decision #2) - implement or keep documented as open.
  - `StockMovementResponse` with a null performer after UC15.
- Verify `main` builds, all tests pass, and the seeded admin can still log in.

