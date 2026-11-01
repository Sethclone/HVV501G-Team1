# Sprint 3 Contract Assessment & Comparison

## Executive Summary

Overall verdict: **Satisfied with the direction, domain modeling, and maturity (approx. 85–90% ready), but not yet 100% satisfied for coding kickoff** until several concrete ambiguities and edge cases are pinned down.

Compared to Sprints 1 and 2, Sprint 3 tackles significantly more complex domain requirements (relational aggregates, transactional state, and price snapshotting) while maintaining strong architectural consistency and learning from past sprint issues. However, missing endpoints (order status transitions), ambiguous response contracts (UC18), unaddressed audit logging (StockMovement integration), and workload imbalances need resolution.

---

## 1. Comparative Analysis: Evolution Across Sprints

| Dimension | Sprint 1 (Sprint1Contracts.md) | Sprint 2 (Sprint2Contracts.md) | Sprint 3 (Sprint3Contracts.md) |
|---|---|---|---|
| **Scope & Complexity** | Foundational scaffolding (`User`, `Product`), independent entities, simple CRUD and auth. | First relationship (`StockMovement` -> `Product`, `User`), single-table queries. | **Composite domain model** (`Order` <-> `OrderItem` <-> `Product`/`User`), transactional cascading, price snapshotting. |
| **Team Process & Leadership** | Democratic initial setup, tight entity dependencies (P1/P3 blocked P2/P4). | Continuity-based assignments, decoupled parallel branching off `dev`, introduced stretch goals. | Designated Product Owner (Siggi / `SK`, matching the `Verkefni1.md` schedule), formal 2-phase sprint structure. |
| **Pattern Reuse** | Created baseline DTOs and exceptions (`ErrorResponse`, `GlobalExceptionHandler`). | Standardized pagination (`PagedResponse<T>`), unified manual validation with `InvalidRequestException`. | **Direct pattern reuse**: Reuses `PagedResponse`, `ErrorResponse`, and `FieldErrorDetail` without reinventing standards. |
| **Lessons Learned** | Experienced an ordinal enum bug in `User.role`. | Warned against ordinal bug on `StockMovement.type`, but suffered merge conflicts on `dev` (PR #15). | Explicitly specifies `@Enumerated(EnumType.STRING)` on `OrderStatus`, defines explicit entity-first merge order. |

---

## 2. Strengths & Maturity in Sprint 3

1. **Defensive Domain Design**:
   - **Price Snapshotting (Decision #6)**: Capturing `unitPrice` on `OrderItem` at creation time prevents future price updates to a `Product` from corrupting past order totals.
   - **Atomic Stock Checks (Decision #2)**: Enforcing an all-or-nothing stock deduction prevents broken, partially fulfilled orders when an item exceeds available stock.
2. **JPA Relationship & Lifecycle Modeling**:
   - Proper cascading and orphan removal (`cascade = CascadeType.ALL, orphanRemoval = true`) on `Order.items` guarantees clean lifecycle management and prevents orphaned items on deletion.
3. **Continuity & Alignment**:
   - Matches the official course schedule in `Verkefni1.md` (Weeks 6–8 A3 Construction 2 milestone).
   - Keeps developers in their established domains (Filip handles queries/pagination; Unnar handles admin user management; Aron handles order item deletion/restoration; Siggi drives order creation).

---

## 3. Ambiguities, Gaps, and Concerns

### A. Unconfirmed Decision #1 & Missing Status Transition Endpoint
- **Issue**: Decision #1 (`OrderStatus { PENDING, CONFIRMED, SHIPPED, CANCELLED }`) is still marked as `Proposed`, violating the contract rule: *"Don't code against something here until its 'Status' is Confirmed."*
- **Missing Endpoint**: There is currently no endpoint to advance or change an order's status (e.g. `PATCH /api/orders/{id}/status` or confirm/cancel actions). If UC18 dictates that item removal is only permitted while an order is `PENDING`, there must be an explicit mechanism for orders to transition to `CONFIRMED` or `SHIPPED`.

### B. Ambiguous Response Contract for UC18 (Item Removal)
- **Issue**: Section 4 states: *"Return 204 No Content or updated OrderResponse."*
- **Problem**: API contracts must be deterministic. Clients and automated integration tests cannot rely on an "either/or" response shape.
- **Recommendation**: Standardize on `200 OK` returning `OrderResponse` so callers immediately receive the updated items array and the recalculated `totalPrice`.
- **Edge Case**: What happens if the last item is removed from an order? Does the order get cancelled/deleted, or remain as an empty order with `totalPrice = 0`?

### C. StockMovement Integration (Audit Logging)
- **Issue**: Sprint 2 introduced `StockMovement` (UC1) to track inventory changes with timestamp and user.
- **Problem**: When UC11 deducts stock or UC18 restores stock, does it also generate `StockMovement` audit records (`MovementType.REMOVE` / `ADD`), or does it only mutate `Product.stockQuantity` directly?
- **Impact**: Bypassing `StockMovement` breaks inventory history tracking, which goes against the core vision in `Verkefni1.md`.

### D. Total Price Recalculation
- `Order.totalPrice` is a persisted field. The contract should explicitly document that removing an item in UC18 (and modifying an item in stretch UC14) must trigger an automatic recalculation of `Order.totalPrice`.
- `OrderItemResponse` includes `subtotal`: Clarify that `subtotal` is calculated as `unitPrice * quantity`.

### E. Authorization Scope & Admin Safety (UC7)
- **Authorization**: Are endpoints for UC11, UC12, UC17, and UC18 open to any staff member for any order, or can staff only view/edit their own orders while admins can access all orders?
- **Admin Demotion Protection (UC7)**: `AdminUpdateUserRequest` allows updating roles. The system should prevent an admin from demoting themselves or the last remaining `ADMIN` in the database to prevent administrative lockout.
- **Response Shape (UC7)**: Clarify whether `PUT /api/admin/users/{id}` returns `200 OK` with `UserSummary`.

### F. Workload Imbalance & Test Debt
- **Estimates**: Task B (Filip) is estimated at ~18h (UC17 + UC12), while Task C (Unnar) and Task D (Aron) are ~8h each.
- **Test Commitments**: At the conclusion of Sprint 2 (`Sprint2Contracts.md`), UC10 and UC3 were marked as having "No tests yet". Sprint 3 should mandate unit/integration test coverage as a prerequisite for PR merging to prevent accumulating further test debt.

---

## 4. Recommended Action Items

1. Confirm Decision #1 (`OrderStatus`) and define a status update endpoint if needed.
2. Settle UC18 on `200 OK` with `OrderResponse`, and document behavior when the final item is removed.
3. Explicitly document whether stock deduction/replenishment must log `StockMovement` entities.
4. Specify the response DTO and self-demotion guards for UC7.
5. Clarify order ownership authorization (own orders vs all orders).
6. Set explicit testing requirements for PR acceptance.
