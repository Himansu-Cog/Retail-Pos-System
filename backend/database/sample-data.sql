USE retailflow_pos;

-- Password for every training account: password
INSERT IGNORE INTO users (id, username, full_name, email, password, role, active) VALUES
(1,'admin','System Admin','admin@retailflow.local','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.','ADMIN',TRUE),
(2,'manager','Store Manager','manager@retailflow.local','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.','STORE_MANAGER',TRUE),
(3,'cashier','Store Cashier','cashier@retailflow.local','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.','CASHIER',TRUE),
(4,'inventory','Inventory Associate','inventory@retailflow.local','$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.','INVENTORY_ASSOCIATE',TRUE);

INSERT IGNORE INTO product (id,barcode,name,supplier_master,category,price,product_status) VALUES
(1,'8901000000001','India Gate Basmati Rice','India Gate Foods','GROCERY',699.00,'ACTIVE'),
(2,'8901000000002','Tropicana Orange Juice','PepsiCo India','BEVERAGES',125.00,'ACTIVE'),
(3,'8901000000003','Dettol Liquid Hand Wash','Reckitt India','HOME_CARE',149.00,'ACTIVE'),
(4,'8901000000004','Happilo Roasted Almonds','Happilo Foods','SNACKS',299.00,'ACTIVE'),
(5,'8901000000005','Himalaya Herbal Shampoo','Himalaya Wellness','PERSONAL_CARE',225.00,'ACTIVE'),
(6,'8901000000006','Amul Taaza Milk','Amul Dairy','DAIRY',72.00,'ACTIVE'),
(7,'8901000000007','Britannia Whole Wheat Bread','Britannia Industries','BAKERY',55.00,'ACTIVE'),
(8,'8901000000008','Surf Excel Matic','Hindustan Unilever','HOME_CARE',435.00,'ACTIVE'),
(9,'8901000000009','Nescafe Classic Coffee','Nestle India','BEVERAGES',345.00,'ACTIVE'),
(10,'8901000000010','Lays Classic Salted','PepsiCo India','SNACKS',50.00,'ACTIVE'),
(11,'8901000000011','Dove Bathing Bar Pack','Hindustan Unilever','PERSONAL_CARE',210.00,'ACTIVE'),
(12,'8901000000012','Aashirvaad Whole Wheat Atta','ITC Foods','GROCERY',310.00,'ACTIVE');

INSERT IGNORE INTO inventory (id,product_id,quantity,reorder_level) VALUES
(1,1,46,12),(2,2,8,12),(3,3,31,10),(4,4,19,8),(5,5,7,10),(6,6,52,18),
(7,7,11,12),(8,8,24,8),(9,9,15,10),(10,10,63,20),(11,11,9,10),(12,12,37,12);

INSERT IGNORE INTO stock_log (id,product_id,performed_by,update_reason,quantity_change,previous_quantity,new_quantity,remarks) VALUES
(1,1,4,'NEW_ITEM',46,0,46,'Opening stock for demo store'),(2,2,4,'NEW_ITEM',8,0,8,'Opening stock for demo store'),
(3,3,4,'NEW_ITEM',31,0,31,'Opening stock for demo store'),(4,4,4,'NEW_ITEM',19,0,19,'Opening stock for demo store'),
(5,5,4,'NEW_ITEM',7,0,7,'Opening stock for demo store'),(6,6,4,'NEW_ITEM',52,0,52,'Opening stock for demo store'),
(7,7,4,'NEW_ITEM',11,0,11,'Opening stock for demo store'),(8,8,4,'NEW_ITEM',24,0,24,'Opening stock for demo store'),
(9,9,4,'NEW_ITEM',15,0,15,'Opening stock for demo store'),(10,10,4,'NEW_ITEM',63,0,63,'Opening stock for demo store'),
(11,11,4,'NEW_ITEM',9,0,9,'Opening stock for demo store'),(12,12,4,'NEW_ITEM',37,0,37,'Opening stock for demo store');

INSERT IGNORE INTO promotion (id,code,name,description,discount_percentage,start_date,end_date,status,minimum_purchase,product_id,applicable_category) VALUES
(1,'WELCOME5','Welcome Savings','Five percent off qualifying purchases',5.00,DATE_SUB(NOW(),INTERVAL 10 DAY),DATE_ADD(NOW(),INTERVAL 3 MONTH),'ACTIVE',500.00,NULL,NULL),
(2,'SNACK10','Snack Time Deal','Ten percent off qualifying snacks',10.00,DATE_SUB(NOW(),INTERVAL 2 DAY),DATE_ADD(NOW(),INTERVAL 28 DAY),'ACTIVE',150.00,NULL,'SNACKS'),
(3,'COFFEE15','Coffee Festival','Upcoming saving on Nescafe coffee',15.00,DATE_ADD(NOW(),INTERVAL 5 DAY),DATE_ADD(NOW(),INTERVAL 20 DAY),'SCHEDULED',0.00,9,NULL);

INSERT IGNORE INTO transactions (id,receipt_number,cashier_id,transaction_date,subtotal,discount_amount,tax_amount,total_amount,payment_mode,status) VALUES
(1,'R-DEMO-1001',3,DATE_SUB(NOW(),INTERVAL 2 HOUR),809.00,0,40.45,849.45,'UPI','COMPLETED'),
(2,'R-DEMO-1002',3,DATE_SUB(NOW(),INTERVAL 5 HOUR),549.00,0,65.88,614.88,'CARD','COMPLETED'),
(3,'R-DEMO-1003',3,DATE_SUB(NOW(),INTERVAL 1 DAY),299.00,0,44.82,343.82,'CASH','COMPLETED');

INSERT IGNORE INTO transaction_item (id,transaction_id,product_id,quantity,unit_price,discount_amount,tax_amount,line_total) VALUES
(1,1,1,1,699.00,0,34.95,733.95),(2,1,7,2,55.00,0,5.50,115.50),
(3,2,2,2,125.00,0,30.00,280.00),(4,2,4,1,299.00,0,35.88,334.88),
(5,3,3,1,149.00,0,26.82,175.82),(6,3,10,3,50.00,0,18.00,168.00);

INSERT IGNORE INTO settlement (id,settlement_date,cashier_id,transaction_count,cash_amount,card_amount,upi_amount,total_amount,closed,notes) VALUES
(1,DATE_SUB(CURRENT_DATE,INTERVAL 1 DAY),3,1,343.82,0,0,343.82,TRUE,'Demo end-of-day settlement');
