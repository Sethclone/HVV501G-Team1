# Assignment 1 - Vision and Scope Document

**Team 1**

---

## Project Vision

### 1.5 Vision Statement

- **For** – Small, single location retail shop owners and staff
- **Who** – needs an accurate and reliable way to track stock without relying on error prone manual spreadsheet
- **The** – Inventory Management System
- **Is** – an inventory management API
- **That** – has accurate, centralized stock records with real-time updates when stock changes. Uses role based access so staff and owners see/do different things and has easy integration into other tools via an API
- **Unlike** – manual spreadsheets or paper tracking
- our product is built API first, so it can be integrated into existing POS or ecommerce tools.

### 1.3 Business Objectives

- Reduce stock-count discrepancies caused by manual, spreadsheet based tracking by providing a single source of for inventory data.
- Reduce the time staff spend manually checking stock levels, by automatically updating quantities as stock movements are recorded.
- Enable shop owners to integrate inventory data into other tools like POS or e-commerce platforms via an API, rather than duplicating data entry across systems.
- Provide role-based access so that staff and owners can be granted appropriate levels of control, reducing the risk of unauthorized or accidental changes to inventory data.

### 2.2 Scope of Initial Release

- The initial release will consist of a backend API only, with no accompanying frontend or user interface.
- The system will support a single retail establishment per account.
- Core functionality will include: account management (creation, authentication, and role-based access for staff and admins), product management (creation, retrieval, updating, and deletion), stock movement tracking (recording additions and removals with a logged history), and search/filtering of products with paginated results.
- Product images will be supported as a core feature of product records, handled via binary data upload.

---

## Use Case Document

### Recording of a stock movement [UC1]

**1. Use Case Name:** Record a Stock Movement

**2. Primary Actor:** Staff Member

**3. Preconditions:** Staff member is authenticated. Product already exists in the system.

**4. Success Guarantee:** Stock quantity is updated to reflect the movement. The movement is logged with timestamp and actor.

**5. Main Success Scenario:**

1. Staff member selects a product and specifies a stock movement (add or remove) with a quantity.
2. System validates the requested quantity against current stock.
3. System updates the product's stock level.
4. System logs the movement, including staff member, timestamp, and quantity changed.
5. System confirms the updated stock level to the staff member.

**6. Extensions:**

- **2a.** Requested removal exceeds current stock:
  1. System rejects the request and displays the current stock level.
  2. Staff member adjusts the quantity or cancels.
- **2b.** Quantity entered is invalid (e.g. zero, negative, non-numeric):
  1. System rejects the request and prompts for a valid quantity.
- **1a.** Selected product does not exist:
  1. System displays an error indicating the product was not found.

**7. Miscellaneous / Open Issues:**

- TBD

---

### Add a New Product to Inventory [UC2]

**1. Use Case Name:** Add a New Product to Inventory

**2. Primary Actor:** Staff Member

**3. Preconditions:** Staff member is authenticated.

**4. Success Guarantee:** The product is created and visible in the inventory, with its stock level, details, and an image.

**5. Main Success Scenario:**

1. Staff member submits product details (name, category, price, initial stock quantity).
2. System validates the submitted details.
3. Staff member uploads a product image.
4. System stores the image and associates it with the product.
5. System creates the product record and adds it to the inventory.
6. System confirms the product was created and displays it in the inventory list.

**6. Extensions:**

- **2a.** Required field is missing or invalid (e.g. negative price, empty name):
  1. System rejects the submission and indicates which field(s) are invalid.
  2. Staff member corrects and resubmits.
- **2b.** Product with the same identifier/SKU already exists:
  1. System rejects the submission and notifies the staff member of the duplicate.
- **3a.** Staff member does not upload an image:
  1. System creates the product with a default placeholder image.
- **4a.** Uploaded file is not a valid image format or exceeds size limit:
  1. System rejects the upload and prompts for a valid file.
  2. Staff member retries or skips the image (resume at 3a).

**7. Miscellaneous / Open Issues:**

- What image formats and maximum file size will be supported?
- Should product creation require admin approval, or can any staff member add products directly?

---

### Search and Filter Products [UC3]

**1. Use Case Name:** Search and Filter Products

**2. Primary Actor:** Staff Member

**3. Preconditions:** Staff member is authenticated.

**4. Success Guarantee:** A paginated list of products matching the given filter criteria is returned to the user.

**5. Main Success Scenario:**

1. User specifies filter criteria (e.g. category, price range, stock status) and requests a page of results.
2. System validates the filter criteria and pagination parameters.
3. System retrieves the products matching the criteria.
4. System returns the requested page of matching products, along with pagination details (e.g. total results, current page, total pages).
5. User reviews the results and may request another page or adjust the filters.

**6. Extensions:**

- **2a.** Filter criteria is invalid (e.g. illegal price range, unknown category):
  1. System rejects the request and indicates which parameter is invalid.
- **2b.** Requested page number is out of range (e.g. beyond the last page):
  1. System returns an empty result set with pagination details indicating no further pages.
- **3a.** No products match the given criteria:
  1. System returns an empty result set.

**7. Miscellaneous / Open Issues:**

- What is the default and maximum page size?
- Should filters be combinable (e.g. category AND price range simultaneously), or applied one at a time?

---

### Brief Use Cases

- **Updating stock (PATCH) [UC4]:**
  - A staff member encounters some amount of spoiled product. He sends in a request for decreasing the stock in the system, providing the amount and id of the product. The request was sent from an account with staff privileges. The system updates the stock.

- **Register an account (POST/PUT) [UC5]:** An admin enters a user's details (name, email, role, and password) to create a new profile. The system checks the info and sets up the account.
- **Sign in (POST) [UC6]:** A user logs in with their credentials. The system verifies them and grants API access.
- **Manage Own Account (PATCH) [UC7]:** A logged-in user updates their personal details, like their password. The system checks the new info and saves the changes.
- **Admin Manages User Accounts (PATCH) [UC8]:** An admin modifies or deletes a staff member's account. The system checks the admin's permissions and applies the changes.
- **View Product Details (GET) [UC9]:** A user looks up a product by its ID to see its full details, including stock levels, category, price, and images.
- **View Own Profile/Activity (GET) [UC10]:** A user views their own profile to see their account details and a history of their recent activity, like stock changes they've logged.
- **Flag product for Reorder (PATCH) [UC11]:** A staff member flags a product that's running low. The system updates the product's status so admins know to reorder it.
- **Update or Remove a Product (PATCH/DELETE) [UC12]:** An authorized user updates a product's details (like changing the price) or deletes it entirely. The system updates or removes the item from the inventory.
- **Create an order [UC13]:**
    - A customer submits an order through an agent (i.e sales person, online store or whatever) the agent creates an order in the system, providing the customer information (e.g what products were ordered, amount etc) and possibly additional information that may be provided or inferred. The system updates the stock of items in the system based on the order and returns a confirmation that everything was successful.
- **View all orders [UC14]:**
    - A staff member may need to get a list of all orders in order to find a specific one. The staff sends a request to the system and the system returns a paginated list of all order.
- **Filter orders [UC15]:**
    - A staff member may need to filter by status (e.g complete and incomplete). He sends the request, along with the wanted status, and the system returns a paginated list of complete or incomplete orders.
- **Mark order as complete [UC16]:**
    - A staff member gets an order and gathers the products in it. He then sends a request, providing the id of the order and the system marks it as complete.
- **View orders established/completed by a specific user [UC17]:**
    - An admin or staff member may want to see the orders a specific user established or completed. The user sends a request, providing the id of the user's account who established/completed the order and the system returns a paginated list of orders.
- **Get the user who established/completed an order [UC18]**
    - An admin or staff member may want to know the user associated with establishing/completing an order. The user sends a request, providing the id of the order and the system returns the id of the user who established/completed it.
- **Remove an order [UC19]:**
    - An admin may need to delete an order because it got refunded or it causes issues in the system. The admin sends a request to the system from an authorized account and the system removes it, updating the inventory stock as well.
- **Modify an order [UC20]:**
    - A staff member may need to modify an order, like its state or some incorrect information. The staff sends a request from his account, the system validates that the user has the authority for the operation and changes the properties of the order that were requested for change, leaving the ones that were not specified unchanged.

---

## Project Estimation and Prioritization

Below is a table with prioritized use cases:

*Note what kind of priority system is used. In order? grouped? Is a lower number a higher priority? (Recommended to group and use lower number = higher priority; E.g. P1 is higher priority than P2).*

*Note what kind of metric you use for time estimation, if its person hours, or days, or abstract effort.*

| Use Case | Time Estimation | Priority |
|----------|------------------|----------|
| UC1 | 10 | P1 |
| UC2 | 15 | P1 |
| UC3 | 12 | P2 |

---

## Project Plan and Schedule

The example schedule for the 10-week project timeline (starting the week after assignment 1 turn-in) is as follows:

*Note that there are 4 sprints, one for each following assignment. Sprint 1 is two weeks, Sprint 2 and Sprint 3 are three weeks, and the final sprint is two weeks.*

*Decide who is going to be the P.O. for each of the sprints.*

*This is a template for a schedule, adjust as needed.*

| Week | Use Cases | Expected Hours | P.O. (Initials) | Sprint | Consultation |
|------|-----------|-----------------|------------------|--------|---------------|
| 1 | None | XX | AB | 1 | **A1 Presentation** |
| 2 | UC1, Android skeleton | XX | AB | 1 | Model Drafts |
| 3 | UC2, UC3 | XX | CD | 2 | **A2 Presentation** |
| 4 | UC4, UC5, UC6 | XX | CD | 2 | Dev support |
| 5 | UC7, UC8 | XX | CD | 2 | Dev support |
| 6 | UC9, UC10 | XX | EF | 3 | **A3 Presentation** |

---

## Project skeleton
### Link to the github Repository
https://github.com/Sethclone/HVV501G-Team1
