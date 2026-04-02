USE pharmago;

INSERT INTO medicines (medicine_code, name, category, manufacturer, unit_price, stock_quantity, reorder_level, expiry_date)
VALUES
('MED101', 'Paracetamol 650', 'Tablet', 'HealWell Pharma', 35.00, 120, 25, '2026-10-15'),
('MED102', 'Azithromycin 500', 'Antibiotic', 'CureLine Labs', 110.00, 45, 15, '2026-06-20'),
('MED103', 'Vitamin C Syrup', 'Syrup', 'NutraPlus', 85.00, 30, 10, '2026-05-11'),
('MED104', 'Insulin FlexPen', 'Injection', 'GlucoLife', 540.00, 18, 8, '2026-04-18'),
('MED105', 'Cetirizine', 'Tablet', 'AllerFree', 28.00, 75, 20, '2026-08-02');

INSERT INTO purchases (medicine_id, supplier_name, quantity, purchase_price, purchase_date, batch_no)
VALUES
(1, 'MediSupply India', 100, 28.00, '2026-01-12', 'BATCH-PAR-01'),
(2, 'CareSource Distributors', 50, 90.00, '2026-02-05', 'BATCH-AZI-04'),
(4, 'Prime Medico', 20, 470.00, '2026-03-01', 'BATCH-INS-02');

INSERT INTO sales (medicine_id, customer_name, quantity, sale_price, sale_date, prescription_required)
VALUES
(1, 'Rahul Sharma', 10, 35.00, '2026-03-16', FALSE),
(2, 'Sneha Verma', 5, 110.00, '2026-03-20', TRUE),
(5, 'Aman Joshi', 8, 28.00, '2026-03-23', FALSE);
