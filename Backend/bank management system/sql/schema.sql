-- Create database
CREATE DATABASE IF NOT EXISTS bankdb;
USE bankdb;

-- Users table for login & roles
CREATE TABLE IF NOT EXISTS users (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL, -- store hashed in production (plain for demo)
  role VARCHAR(20) NOT NULL -- Admin, Employee, Customer
);

-- Customer table
CREATE TABLE IF NOT EXISTS customers (
  customer_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  address VARCHAR(200),
  phone VARCHAR(20),
  email VARCHAR(100),
  date_of_birth DATE,
  account_type VARCHAR(20)
);

-- Account table
CREATE TABLE IF NOT EXISTS accounts (
  account_no INT AUTO_INCREMENT PRIMARY KEY,
  customer_id INT NOT NULL,
  balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  opening_date DATE NOT NULL DEFAULT CURRENT_DATE,
  status VARCHAR(20) NOT NULL DEFAULT 'Active',
  FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
);

-- Transaction table
CREATE TABLE IF NOT EXISTS transactions (
  transaction_id INT AUTO_INCREMENT PRIMARY KEY,
  account_no INT NOT NULL,
  transaction_type VARCHAR(20) NOT NULL, -- Deposit, Withdrawal, Transfer
  amount DECIMAL(15,2) NOT NULL,
  transaction_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  note VARCHAR(255),
  FOREIGN KEY (account_no) REFERENCES accounts(account_no) ON DELETE CASCADE
);

-- Insert a default admin (username: admin, password: admin123)
INSERT INTO users (username, password, role)
VALUES ('admin','admin123','Admin')
ON DUPLICATE KEY UPDATE username=username;