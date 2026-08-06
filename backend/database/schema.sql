CREATE DATABASE IF NOT EXISTS retailflow_pos
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE retailflow_pos;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  full_name VARCHAR(100) NOT NULL,
  email VARCHAR(120) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  role ENUM('ADMIN','STORE_MANAGER','CASHIER','INVENTORY_ASSOCIATE') NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_users_role (role)
) ENGINE=InnoDB;

-- Product contains only catalogue identity and commercial details.
CREATE TABLE IF NOT EXISTS product (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  barcode VARCHAR(20) NOT NULL UNIQUE,
  name VARCHAR(120) NOT NULL UNIQUE,
  supplier_master VARCHAR(120) NOT NULL,
  category ENUM('GROCERY','BEVERAGES','DAIRY','BAKERY','SNACKS','PERSONAL_CARE','HOME_CARE','ELECTRONICS','OTHER') NOT NULL,
  price DECIMAL(12,2) NOT NULL,
  product_status ENUM('ACTIVE','INACTIVE','DISCONTINUED') NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_product_price CHECK (price > 0),
  INDEX idx_product_barcode (barcode),
  INDEX idx_product_name (name),
  INDEX idx_product_category (category)
) ENGINE=InnoDB;

-- Stock quantity and alert threshold belong only to inventory.
CREATE TABLE IF NOT EXISTS inventory (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL UNIQUE,
  quantity INT NOT NULL DEFAULT 0,
  reorder_level INT NOT NULL DEFAULT 10,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_inventory_product FOREIGN KEY (product_id) REFERENCES product(id),
  CONSTRAINT chk_inventory_quantity CHECK (quantity >= 0),
  CONSTRAINT chk_inventory_threshold CHECK (reorder_level >= 0),
  INDEX idx_inventory_quantity (quantity)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS stock_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  performed_by BIGINT,
  update_reason ENUM('NEW_ITEM','NEW_STOCK_DELIVERY','SALE','CUSTOMER_RETURN','DAMAGED_STOCK','STOCK_CORRECTION','PRODUCT_DETAIL_UPDATE') NOT NULL,
  quantity_change INT NOT NULL,
  previous_quantity INT NOT NULL,
  new_quantity INT NOT NULL,
  remarks VARCHAR(500),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_stocklog_product FOREIGN KEY (product_id) REFERENCES product(id),
  CONSTRAINT fk_stocklog_user FOREIGN KEY (performed_by) REFERENCES users(id),
  INDEX idx_stock_log_product (product_id),
  INDEX idx_stock_log_created (created_at)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS transactions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  receipt_number VARCHAR(40) NOT NULL UNIQUE,
  cashier_id BIGINT NOT NULL,
  transaction_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  subtotal DECIMAL(12,2) NOT NULL,
  discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  total_amount DECIMAL(12,2) NOT NULL,
  payment_mode ENUM('CASH','CARD','UPI') NOT NULL,
  status ENUM('COMPLETED','CANCELLED','REFUNDED') NOT NULL,
  CONSTRAINT fk_transaction_cashier FOREIGN KEY (cashier_id) REFERENCES users(id),
  INDEX idx_transaction_date (transaction_date),
  INDEX idx_transaction_status (status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS transaction_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  transaction_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  unit_price DECIMAL(12,2) NOT NULL,
  discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  line_total DECIMAL(12,2) NOT NULL,
  CONSTRAINT fk_item_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id) ON DELETE CASCADE,
  CONSTRAINT fk_item_product FOREIGN KEY (product_id) REFERENCES product(id),
  CONSTRAINT chk_item_quantity CHECK (quantity > 0),
  INDEX idx_transaction_item_transaction (transaction_id),
  INDEX idx_transaction_item_product (product_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS promotion (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(30) NOT NULL UNIQUE,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(500),
  discount_percentage DECIMAL(5,2) NOT NULL,
  start_date DATETIME NOT NULL,
  end_date DATETIME NOT NULL,
  status ENUM('SCHEDULED','ACTIVE','EXPIRED','INACTIVE') NOT NULL,
  minimum_purchase DECIMAL(12,2) NOT NULL DEFAULT 0,
  product_id BIGINT,
  applicable_category VARCHAR(60),
  CONSTRAINT fk_promotion_product FOREIGN KEY (product_id) REFERENCES product(id),
  CONSTRAINT chk_promotion_discount CHECK (discount_percentage > 0 AND discount_percentage <= 100),
  CONSTRAINT chk_promotion_dates CHECK (end_date > start_date),
  INDEX idx_promotion_dates (start_date, end_date),
  INDEX idx_promotion_status (status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS settlement (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  settlement_date DATE NOT NULL,
  cashier_id BIGINT NOT NULL,
  transaction_count INT NOT NULL DEFAULT 0,
  cash_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  card_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  upi_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
  closed BOOLEAN NOT NULL DEFAULT FALSE,
  notes VARCHAR(250),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT uk_settlement_date_cashier UNIQUE (settlement_date, cashier_id),
  CONSTRAINT fk_settlement_cashier FOREIGN KEY (cashier_id) REFERENCES users(id),
  INDEX idx_settlement_date (settlement_date)
) ENGINE=InnoDB;
