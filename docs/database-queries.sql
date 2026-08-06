USE retailflow_pos;

-- Product catalogue with current inventory
SELECT p.id, p.barcode, p.name, p.supplier_master, p.category, p.price,
       p.product_status, i.quantity, i.reorder_level
FROM product p
JOIN inventory i ON i.product_id = p.id
ORDER BY p.name;

-- Low and out-of-stock products
SELECT p.id, p.name, i.quantity, i.reorder_level,
       CASE WHEN i.quantity = 0 THEN 'OUT OF STOCK' ELSE 'LOW STOCK' END AS stock_state
FROM inventory i
JOIN product p ON p.id = i.product_id
WHERE i.quantity <= i.reorder_level
ORDER BY i.quantity;

-- Stock audit history
SELECT sl.created_at, p.name, sl.update_reason, sl.previous_quantity,
       sl.quantity_change, sl.new_quantity, sl.remarks, u.full_name AS performed_by
FROM stock_log sl
JOIN product p ON p.id = sl.product_id
LEFT JOIN users u ON u.id = sl.performed_by
ORDER BY sl.created_at DESC;

-- Today's completed sales by payment mode
SELECT payment_mode, COUNT(*) AS bills, SUM(total_amount) AS collected
FROM transactions
WHERE status = 'COMPLETED' AND DATE(transaction_date) = CURRENT_DATE
GROUP BY payment_mode;

-- Top-selling products
SELECT p.name, SUM(ti.quantity) AS units_sold, SUM(ti.line_total) AS revenue
FROM transaction_item ti
JOIN product p ON p.id = ti.product_id
JOIN transactions t ON t.id = ti.transaction_id
WHERE t.status = 'COMPLETED'
GROUP BY p.id, p.name
ORDER BY units_sold DESC;

-- Active promotion rules
SELECT code, name, discount_percentage, applicable_category, product_id, end_date
FROM promotion
WHERE status = 'ACTIVE' AND NOW() BETWEEN start_date AND end_date
ORDER BY end_date;

-- Cashier settlement reconciliation
SELECT s.settlement_date, u.full_name, s.transaction_count,
       s.cash_amount, s.card_amount, s.upi_amount, s.total_amount, s.closed
FROM settlement s
JOIN users u ON u.id = s.cashier_id
ORDER BY s.settlement_date DESC;
