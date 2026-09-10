-- HISAB KITAB database schema (MySQL 8/9)
-- The Spring Boot app auto-creates this DB via createDatabaseIfNotExist=true
-- and auto-creates tables via Hibernate (ddl-auto=update).
-- Run this file manually only if you want to pre-create everything:
--   mysql -u root < database/schema.sql

CREATE DATABASE IF NOT EXISTS hisabkitab
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE hisabkitab;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL COMMENT 'format: saltHex$sha256Hex',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS categories (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  name VARCHAR(60) NOT NULL,
  CONSTRAINT fk_cat_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT uq_cat_user_name UNIQUE (user_id, name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS budgets (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  budget_month VARCHAR(7) NOT NULL COMMENT 'YYYY-MM',
  amount DECIMAL(12,2) NOT NULL CHECK (amount > 0),
  CONSTRAINT fk_bud_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT uq_bud_user_month UNIQUE (user_id, budget_month),
  CONSTRAINT chk_month_fmt CHECK (budget_month REGEXP '^[0-9]{4}-(0[1-9]|1[0-2])$')
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS expenses (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  category_id BIGINT NOT NULL,
  amount DECIMAL(12,2) NOT NULL CHECK (amount > 0),
  note VARCHAR(255) DEFAULT '',
  expense_date DATE NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_exp_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_exp_cat FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
  INDEX idx_exp_user_date (user_id, expense_date)
) ENGINE=InnoDB;

-- Useful views / report queries:

-- Monthly category-wise summary (example for one user + month):
-- SELECT c.name AS category, SUM(e.amount) AS total
-- FROM expenses e JOIN categories c ON c.id = e.category_id
-- WHERE e.user_id = 1 AND DATE_FORMAT(e.expense_date, '%Y-%m') = '2026-09'
-- GROUP BY c.name ORDER BY total DESC;

-- Budget vs spent (example):
-- SELECT b.budget_month, b.amount AS budget,
--        COALESCE((SELECT SUM(e.amount) FROM expenses e
--                  WHERE e.user_id = b.user_id
--                    AND DATE_FORMAT(e.expense_date, '%Y-%m') = b.budget_month), 0) AS spent
-- FROM budgets b WHERE b.user_id = 1 ORDER BY b.budget_month DESC;
