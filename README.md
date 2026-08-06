# Retail System

Retail System is a training-friendly full-stack POS and store-operations application built with Java 21, Spring Boot, MySQL 8, HTML5, CSS3 and Vanilla JavaScript. It uses a responsive light Frost UI, fixed horizontal navigation and dark professional component accents.

## Project structure

```text
Retails-Pos-System/
|-- frontend/                 one HTML, CSS and JS file per UI module
|-- backend/                  Spring MVC REST application
|   |-- database/
|   |   |-- schema.sql
|   |   `-- sample-data.sql
|   `-- src/
`-- docs/
    |-- Retail-System-API.postman_collection.json
    |-- database-queries.sql
    |-- MYSQL-WORKBENCH-SETUP.md
    `-- Retail-System-Module-Documentation.docx
```

The backend intentionally keeps Entity, Repository, Service and Controller classes separate because the project specification requires MVC and Spring Data JPA layers. Reports and Settlement are one functional module: one controller, one service, one frontend page and `/api/reports/...` endpoints.

## Prerequisites

- Java 21
- Maven 3.9+
- MySQL Server 8 and MySQL Workbench 8
- Visual Studio Code with Live Server

## Database with MySQL Workbench

Default connection:

```properties
DB_URL=jdbc:mysql://localhost:3306/retailflow_pos
DB_USERNAME=root
DB_PASSWORD=root
```

Create and inspect the database through MySQL Workbench:

1. Open MySQL Workbench and connect to the local MySQL 8 server.
2. Choose **File → Open SQL Script** and open `backend/database/schema.sql`.
3. Click the lightning-bolt **Execute** button.
4. Open `backend/database/sample-data.sql` and execute it.
5. Refresh **Schemas** and confirm that `retailflow_pos` contains all eight tables.

Hibernate uses `ddl-auto=validate`, so it verifies the Workbench-created structure without silently changing it. See [MySQL Workbench setup](docs/MYSQL-WORKBENCH-SETUP.md) for the complete walkthrough.

## Run the backend

```powershell
cd backend
mvn spring-boot:run
```

Backend URL: `http://127.0.0.1:8081`

Optional environment overrides:

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your-password"
$env:SEED_DEMO_DATA="false"
mvn spring-boot:run
```

## Run the frontend

1. Open the `frontend` folder in Visual Studio Code.
2. Right-click `index.html`.
3. Choose **Open with Live Server**.
4. Use `http://127.0.0.1:5500` (not a `file://` URL).

Each page loads Axios from its CDN and uses the small shared `api.js` helper to call `http://127.0.0.1:8081/api` with session credentials. CORS permits both `127.0.0.1:5500` and `localhost:5500`.

## Training accounts

All passwords are `password`.

| Username | Role | Access |
|---|---|---|
| `admin` | ADMIN | All modules |
| `manager` | STORE_MANAGER | Operations, promotions, reports and settlement |
| `cashier` | CASHIER | Product viewing and POS billing |
| `inventory` | INVENTORY_ASSOCIATE | Product viewing and inventory updates |

Users are MySQL records and passwords are stored as BCrypt hashes.

## Main workflow

- Product stores generated ID, generated barcode, name, supplier master, category enum, price, status enum and creation time.
- Inventory stores quantity and safety threshold.
- Product creation atomically creates its inventory row and a `NEW_ITEM` stock log.
- Product names are unique without blocking named variants such as `Coffee 100gm` and `Coffee 200gm`.
- Product and stock changes are performed only on the Inventory page.
- Inventory supports product renaming, Add/Remove stock controls and deletion of unused products.
- Each inventory update records reason, remarks, previous quantity, change and new quantity.
- Inventory provides a simple searchable stock list and audited quantity adjustment form.
- Billing keeps products hidden until searched and provides a small cart, coupon apply/remove, payment selection and checkout.
- Finalized sales open a printable Retail System receipt.
- Reports support date-range sales generation and settlement creation.

## Validation

Frontend forms use required fields, length limits, patterns, number ranges and cross-field date checks. Backend entities repeat important validation with Jakarta Bean Validation so invalid requests cannot bypass the browser.

## Build and test

```powershell
cd backend
mvn test
mvn package
```

## Documentation

- [API collection](docs/Retail-System-API.postman_collection.json)
- [Database schema](backend/database/schema.sql)
- [Sample data](backend/database/sample-data.sql)
- [Useful DB queries](docs/database-queries.sql)
- [Detailed module documentation](docs/Retail-System-Module-Documentation.docx)
- [MySQL Workbench setup](docs/MYSQL-WORKBENCH-SETUP.md)

## Author
Himanshu Joshi
