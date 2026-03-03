-- 01_schema.sql (MySQL 8.0)
-- Shop Project - Excel-based canonical schema

CREATE DATABASE IF NOT EXISTS shop
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE shop;



SET FOREIGN_KEY_CHECKS = 1;

-- =========================
-- users
-- =========================
CREATE TABLE users (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '유저 PK',
  email VARCHAR(120) NOT NULL COMMENT '이메일(로그인)',
  password_hash VARCHAR(255) NOT NULL COMMENT '비밀번호 해시',
  name VARCHAR(60) NOT NULL COMMENT '이름',
  role VARCHAR(20) NOT NULL DEFAULT 'ADMIN' COMMENT '권한(ADMIN/USER)',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='users';

-- =========================
-- brands
-- =========================
CREATE TABLE brands (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '브랜드 PK',
  name VARCHAR(50) NOT NULL COMMENT '브랜드명',
  slug VARCHAR(60) NOT NULL COMMENT '브랜드 슬러그',
  logo_url VARCHAR(300) NULL COMMENT '브랜드 로고 URL',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
  PRIMARY KEY (id),
  UNIQUE KEY uk_brands_name (name),
  UNIQUE KEY uk_brands_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='brands';

-- =========================
-- nav_menu (category)
-- =========================
CREATE TABLE nav_menu (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '네비/카테고리 PK',
  name VARCHAR(100) NOT NULL COMMENT '카테고리명',
  path VARCHAR(255) NULL COMMENT '라우팅 경로',
  parent_id BIGINT NULL COMMENT '부모 카테고리',
  depth INT NOT NULL DEFAULT 1 COMMENT '메뉴 깊이(1~3)',
  sort_order INT NOT NULL DEFAULT 1 COMMENT '정렬 순서',
  visible_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '노출 여부(Y/N)',
  PRIMARY KEY (id),
  KEY idx_nav_menu_parent_id (parent_id),
  KEY idx_nav_menu_depth (depth),
  KEY idx_nav_menu_sort_order (sort_order),
  CONSTRAINT fk_nav_menu_parent
    FOREIGN KEY (parent_id) REFERENCES nav_menu(id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='nav_menu';

-- =========================
-- products
-- =========================
CREATE TABLE products (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '상품 PK',
  brand_id BIGINT NOT NULL COMMENT '브랜드 FK',
  title VARCHAR(100) NOT NULL COMMENT '상품명/모델명',
  series VARCHAR(80) NULL COMMENT '시리즈',
  description TEXT NULL COMMENT '설명',
  price INT NOT NULL DEFAULT 0 COMMENT '판매가(원)',
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '상태(DRAFT/ACTIVE/HIDDEN)',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
  slug VARCHAR(150) NOT NULL COMMENT '슬러그(고유)',
  category_id BIGINT NOT NULL COMMENT '카테고리 FK(1~3 depth)',
  image_url VARCHAR(300) NOT NULL COMMENT '대표 이미지 URL',
  image_path VARCHAR(300) NOT NULL COMMENT '대표 이미지 서버경로',
  PRIMARY KEY (id),
  UNIQUE KEY uk_products_slug (slug),
  KEY idx_products_brand_id (brand_id),
  KEY idx_products_category_id (category_id),
  CONSTRAINT fk_products_brand
    FOREIGN KEY (brand_id) REFERENCES brands(id)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_products_category
    FOREIGN KEY (category_id) REFERENCES nav_menu(id)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='products';

-- =========================
-- product_images
-- =========================
CREATE TABLE product_images (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '이미지 PK',
  product_id BIGINT NOT NULL COMMENT '상품 FK',
  url VARCHAR(300) NOT NULL COMMENT '이미지 URL',
  sort_order INT NOT NULL DEFAULT 1 COMMENT '정렬순서',
  is_main TINYINT(1) NOT NULL DEFAULT 0 COMMENT '대표 여부',
  PRIMARY KEY (id),
  KEY idx_product_images_product_id (product_id),
  CONSTRAINT fk_product_images_product
    FOREIGN KEY (product_id) REFERENCES products(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='product_images';

-- =========================
-- product_specs (PK=FK 1:1)
-- =========================
CREATE TABLE product_specs (
  product_id BIGINT NOT NULL COMMENT 'PK=FK (1:1) products.id',
  head_size_sq_in INT NULL COMMENT '헤드사이즈(in^2)',
  unstrung_weight_g INT NULL COMMENT '무게(언스트링, g)',
  balance_mm INT NULL COMMENT '밸런스(mm)',
  length_in DECIMAL(4,1) NULL COMMENT '길이(inch)',
  pattern_main INT NULL COMMENT '스트링 패턴(main)',
  pattern_cross INT NULL COMMENT '스트링 패턴(cross)',
  stiffness_ra INT NULL COMMENT '강성(RA)',
  PRIMARY KEY (product_id),
  CONSTRAINT fk_product_specs_product
    FOREIGN KEY (product_id) REFERENCES products(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='product_specs';

-- =========================
-- skus
-- =========================
CREATE TABLE skus (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT 'SKU PK',
  product_id BIGINT NOT NULL COMMENT '상품 FK',
  price INT NOT NULL COMMENT '옵션 가격(원)',
  sku_code VARCHAR(80) NOT NULL COMMENT 'SKU 코드(고유)',
  is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '활성 여부',
  grip_size VARCHAR(4) NOT NULL COMMENT '그립 사이즈',
  PRIMARY KEY (id),
  UNIQUE KEY uk_skus_sku_code (sku_code),
  KEY idx_skus_product_id (product_id),
  KEY idx_skus_grip_size (grip_size),
  CONSTRAINT fk_skus_product
    FOREIGN KEY (product_id) REFERENCES products(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='skus';

-- =========================
-- inventory (PK=FK 1:1)
-- =========================
CREATE TABLE inventory (
  sku_id BIGINT NOT NULL COMMENT 'PK=FK (1:1) skus.id',
  stock_qty INT NOT NULL DEFAULT 0 COMMENT '재고 수량',
  safety_stock_qty INT NOT NULL DEFAULT 0 COMMENT '안전재고 수량',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
  PRIMARY KEY (sku_id),
  CONSTRAINT fk_inventory_sku
    FOREIGN KEY (sku_id) REFERENCES skus(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='inventory';

-- =========================
-- carts
-- =========================
CREATE TABLE carts (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '장바구니 PK',
  user_id BIGINT NOT NULL COMMENT '유저 FK(1:1)',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
  PRIMARY KEY (id),
  UNIQUE KEY uk_carts_user_id (user_id),
  CONSTRAINT fk_carts_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='carts';

-- =========================
-- cart_items
-- =========================
CREATE TABLE cart_items (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '장바구니아이템 PK',
  cart_id BIGINT NOT NULL COMMENT '장바구니 FK',
  sku_id BIGINT NOT NULL COMMENT 'SKU FK',
  quantity INT NOT NULL DEFAULT 1 COMMENT '수량',
  PRIMARY KEY (id),
  KEY idx_cart_items_cart_id (cart_id),
  KEY idx_cart_items_sku_id (sku_id),
  CONSTRAINT fk_cart_items_cart
    FOREIGN KEY (cart_id) REFERENCES carts(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_cart_items_sku
    FOREIGN KEY (sku_id) REFERENCES skus(id)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='cart_items';

-- =========================
-- orders
-- =========================
CREATE TABLE orders (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '주문 PK',
  user_id BIGINT NOT NULL COMMENT '주문자 FK',
  order_no VARCHAR(40) NOT NULL COMMENT '주문번호',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '주문상태',
  total_price INT NOT NULL DEFAULT 0 COMMENT '총액',
  receiver_name VARCHAR(60) NOT NULL COMMENT '수령인',
  receiver_phone VARCHAR(30) NOT NULL COMMENT '연락처',
  zip VARCHAR(10) NULL COMMENT '우편번호',
  address1 VARCHAR(200) NOT NULL COMMENT '주소1',
  address2 VARCHAR(200) NULL COMMENT '주소2',
  memo VARCHAR(200) NULL COMMENT '메모',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일시',
  PRIMARY KEY (id),
  UNIQUE KEY uk_orders_order_no (order_no),
  KEY idx_orders_user_id (user_id),
  CONSTRAINT fk_orders_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='orders';

-- =========================
-- order_items
-- =========================
CREATE TABLE order_items (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '주문아이템 PK',
  order_id BIGINT NOT NULL COMMENT '주문 FK',
  sku_id BIGINT NOT NULL COMMENT 'SKU FK',
  product_name_snapshot VARCHAR(120) NOT NULL COMMENT '상품명 스냅샷',
  brand_name_snapshot VARCHAR(50) NOT NULL COMMENT '브랜드 스냅샷',
  grip_snapshot VARCHAR(4) NOT NULL COMMENT '그립 스냅샷',
  unit_price INT NOT NULL DEFAULT 0 COMMENT '단가',
  quantity INT NOT NULL DEFAULT 1 COMMENT '수량',
  line_total INT NOT NULL DEFAULT 0 COMMENT '라인합계',
  PRIMARY KEY (id),
  KEY idx_order_items_order_id (order_id),
  KEY idx_order_items_sku_id (sku_id),
  CONSTRAINT fk_order_items_order
    FOREIGN KEY (order_id) REFERENCES orders(id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_order_items_sku
    FOREIGN KEY (sku_id) REFERENCES skus(id)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='order_items';