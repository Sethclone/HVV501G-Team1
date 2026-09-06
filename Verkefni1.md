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

- **Register an account (POST) [UC4]:** An admin enters a new user's details into the system. The system validates the information and creates the new profile.

- **Sign in (POST) [UC5]:** A user enters their credentials. The system verifies the information and grants API access.

- **Manage Own Account (PUT) [UC6]:** A logged-in user submits updated personal details. The system validates the input and overwrites the existing account information.

- **Admin Manages User Accounts (POST) [UC7]:** An admin accesses a staff member's profile and submits changes to their details. The system confirms the admin's authorization and applies the modifications.

- **View Product Details by ID (GET) [UC8]:** A user requests information for a specific product using its ID. The system retrieves and presents the full item details.

- **Flag product for Reorder (PATCH) [UC9]:** A staff member flags a product experiencing low inventory. The system updates the product's internal status to alert admins.

- **Update or Remove a Product (PATCH/DELETE) [UC10]:** An authorized user modifies a product's details or requests its deletion. The system applies the changes or permanently removes the item from the catalog.

- **Create an order (POST) [UC11]:** An agent submits a new customer order containing selected products. The system records the order, deducts the items from available stock, and returns a confirmation.

- **View all orders (GET) [UC12]:** A staff member requests a list of all system orders. The system retrieves and returns a paginated list of order records.

- **Remove an order (DELETE) [UC13]:** An admin issues a command to delete a refunded order. The system erases the order record and automatically restores the inventory stock.

- **Modify an order (PUT) [UC14]:** A staff member submits corrections to an existing order. The system verifies their authority and overwrites the specified properties.

- **Admin Deletes an Account (DELETE) [UC15]:** An admin requests the permanent deletion of a profile. The system removes their credentials and associations.

- **Create Admin Account (POST) [UC16]:** An administrator inputs details to establish a new user profile. The system provisions the account and explicitly assigns it elevated privileges.

- **View Order with Products (GET) [UC17]:** A user looks up a specific order record. The system retrieves the main order details along with the fully expanded data for every product included in it.

- **Remove Item from Order (DELETE) [UC18]:** A user selects a product to remove from their pending order. The system deletes the relationship between the order and the item.

- **Reassign Order (PUT) [UC19]:** An admin selects an existing order and assigns it to a different user ID. The system transfers ownership of the record to the new account.

- **Upload Profile Picture (POST) [UC20]:** A user uploads an image file to personalize their account. The system saves the file and updates the profile with the new image.

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
