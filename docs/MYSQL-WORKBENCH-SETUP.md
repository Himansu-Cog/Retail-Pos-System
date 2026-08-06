# MySQL Workbench Setup

## 1. Create the connection

1. Start MySQL Server 8.
2. Open MySQL Workbench.
3. Select the local connection, normally `localhost:3306`.
4. Sign in using the MySQL username and password used by the Spring Boot application.

## 2. Create the Retail System database

1. Choose **File → Open SQL Script**.
2. Open `backend/database/schema.sql`.
3. Click **Execute** or press `Ctrl+Shift+Enter`.
4. In the left **Schemas** panel, click refresh.
5. Expand `retailflow_pos` and confirm the tables:
   `users`, `product`, `inventory`, `stock_log`, `transactions`,
   `transaction_item`, `promotion`, and `settlement`.

## 3. Load demonstration data

1. Open `backend/database/sample-data.sql`.
2. Click **Execute**.
3. Run `SELECT * FROM retailflow_pos.users;`.
4. Confirm the `admin`, `manager`, `cashier`, and `inventory` accounts exist.

All demonstration passwords are `password`; the database stores BCrypt hashes.

## 4. Configure Spring Boot

The default configuration is:

```properties
DB_URL=jdbc:mysql://localhost:3306/retailflow_pos
DB_USERNAME=root
DB_PASSWORD=root
```

If the Workbench connection uses another password, set it before starting:

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your-mysql-password"
cd backend
mvn spring-boot:run
```

Hibernate is configured with `ddl-auto=validate`. Startup stops with a clear error if the Workbench schema is missing or incorrect.

## 5. Useful Workbench checks

Open `docs/database-queries.sql` in Workbench to inspect:

- catalogue and inventory values;
- low-stock products;
- stock audit history;
- daily sales by payment method;
- top-selling products;
- promotions and settlement reconciliation.
