package com.pharmago.dao;

import com.pharmago.config.DatabaseConfig;
import com.pharmago.model.Purchase;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PurchaseDao {
    private final DatabaseConfig databaseConfig;

    public PurchaseDao(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public void addPurchase(Purchase purchase) throws SQLException {
        String sql = """
                INSERT INTO purchases (medicine_id, supplier_name, quantity, purchase_price, purchase_date, batch_no)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, purchase.getMedicineId());
            statement.setString(2, purchase.getSupplierName());
            statement.setInt(3, purchase.getQuantity());
            statement.setBigDecimal(4, purchase.getPurchasePrice());
            statement.setDate(5, Date.valueOf(purchase.getPurchaseDate()));
            statement.setString(6, purchase.getBatchNo());
            statement.executeUpdate();
        }
    }

    public List<Purchase> getAllPurchases() throws SQLException {
        String sql = """
                SELECT p.purchase_id, p.medicine_id, m.name AS medicine_name, p.supplier_name, p.quantity,
                       p.purchase_price, p.purchase_date, p.batch_no
                FROM purchases p
                JOIN medicines m ON p.medicine_id = m.medicine_id
                ORDER BY p.purchase_date DESC
                """;
        List<Purchase> purchases = new ArrayList<>();

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Purchase purchase = new Purchase();
                purchase.setPurchaseId(resultSet.getInt("purchase_id"));
                purchase.setMedicineId(resultSet.getInt("medicine_id"));
                purchase.setMedicineName(resultSet.getString("medicine_name"));
                purchase.setSupplierName(resultSet.getString("supplier_name"));
                purchase.setQuantity(resultSet.getInt("quantity"));
                purchase.setPurchasePrice(resultSet.getBigDecimal("purchase_price"));
                purchase.setPurchaseDate(resultSet.getDate("purchase_date").toLocalDate());
                purchase.setBatchNo(resultSet.getString("batch_no"));
                purchases.add(purchase);
            }
        }
        return purchases;
    }

    public BigDecimal totalPurchaseValue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity * purchase_price), 0) FROM purchases";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getBigDecimal(1);
        }
    }
}
