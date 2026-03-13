USE shop;

-- Users
INSERT INTO users (id, email, password_hash, name, role)
VALUES (10, 'admin@shop.com', 'CHANGE_ME', 'Admin', 'ADMIN')
ON DUPLICATE KEY UPDATE email = VALUES(email);

-- Brands
INSERT INTO brands (id, name, slug, logo_url)
VALUES (3, 'YONEX', 'yonex', '/uploads/brands/yonex.png')
ON DUPLICATE KEY UPDATE name = VALUES(name), slug = VALUES(slug);

-- Nav menu (category)
INSERT INTO nav_menu (id, name, path, parent_id, depth, sort_order, visible_yn)
VALUES (100, 'Rackets', NULL, NULL, 1, 1, 'Y')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Product
INSERT INTO products (
  id, brand_id, title, series, description, price, status,
  slug, category_id, image_url, image_path
)
VALUES (
  1203, 3, 'VCORE 100 2023', 'VCORE', '...', 289000, 'ACTIVE',
  'vcore-100-2023', 100, '/uploads/products/1203/main.jpg', 'D:/uploads/.../main.jpg'
)
ON DUPLICATE KEY UPDATE title = VALUES(title), price = VALUES(price);

-- Product specs (PK=FK)
INSERT INTO product_specs (
  product_id, head_size_sq_in, unstrung_weight_g, balance_mm, length_in,
  pattern_main, pattern_cross, stiffness_ra
)
VALUES (1203, 100, 300, 320, 27.0, 16, 19, 65)
ON DUPLICATE KEY UPDATE stiffness_ra = VALUES(stiffness_ra);

-- SKU
INSERT INTO skus (id, product_id, price, sku_code, is_active, grip_size)
VALUES (2001, 1203, 289000, 'VCORE100-2023-G2', 1, 'G2')
ON DUPLICATE KEY UPDATE sku_code = VALUES(sku_code);

-- Inventory (PK=FK)
INSERT INTO inventory (sku_id, stock_qty, safety_stock_qty)
VALUES (2001, 10, 2)
ON DUPLICATE KEY UPDATE stock_qty = VALUES(stock_qty);

-- G3
--INSERT INTO skus (id, product_id, price, sku_code, is_active, grip_size)
--VALUES (2002, 1203, 289000, 'VCORE100-2023-G3', 1, 'G3')
--ON DUPLICATE KEY UPDATE sku_code = VALUES(sku_code);
--
--INSERT INTO inventory (sku_id, stock_qty, safety_stock_qty)
--VALUES (2002, 8, 2)
--ON DUPLICATE KEY UPDATE stock_qty = VALUES(stock_qty);

-- Cart (1:1 user)
INSERT INTO carts (id, user_id)
VALUES (1, 10)
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

-- Cart item
INSERT INTO cart_items (id, cart_id, sku_id, quantity)
VALUES (100, 1, 2001, 2)
ON DUPLICATE KEY UPDATE quantity = VALUES(quantity);

-- Order
INSERT INTO orders (
  id, user_id, order_no, status, total_price, receiver_name, receiver_phone,
  zip, address1, address2, memo
)
VALUES (
  200, 10, '20260226-0001', 'PENDING', 578000, '김OO', '010-0000-0000',
  '06236', '...', NULL, '문앞에 두세요'
)
ON DUPLICATE KEY UPDATE status = VALUES(status);

-- Order item
INSERT INTO order_items (
  id, order_id, sku_id, product_name_snapshot, brand_name_snapshot, grip_snapshot,
  unit_price, quantity, line_total
)
VALUES (
  300, 200, 2001, 'VCORE 100 2023', 'YONEX', 'G2',
  289000, 2, 578000
)
ON DUPLICATE KEY UPDATE line_total = VALUES(line_total);