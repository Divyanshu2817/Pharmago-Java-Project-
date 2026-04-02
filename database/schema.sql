CREATE DATABASE IF NOT EXISTS pharmago;
USE pharmago;

CREATE TABLE IF NOT EXISTS medicines (
    medicine_id INT PRIMARY KEY AUTO_INCREMENT,
    medicine_code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(80) NOT NULL,
    manufacturer VARCHAR(120) NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    reorder_level INT NOT NULL DEFAULT 10,
    expiry_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS purchases (
    purchase_id INT PRIMARY KEY AUTO_INCREMENT,
    medicine_id INT NOT NULL,
    supplier_name VARCHAR(120) NOT NULL,
    quantity INT NOT NULL,
    purchase_price DECIMAL(10, 2) NOT NULL,
    purchase_date DATE NOT NULL,
    batch_no VARCHAR(40) NOT NULL,
    FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id)
);

CREATE TABLE IF NOT EXISTS sales (
    sale_id INT PRIMARY KEY AUTO_INCREMENT,
    medicine_id INT NOT NULL,
    customer_name VARCHAR(120) NOT NULL,
    quantity INT NOT NULL,
    sale_price DECIMAL(10, 2) NOT NULL,
    sale_date DATE NOT NULL,
    prescription_required BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id)
);
