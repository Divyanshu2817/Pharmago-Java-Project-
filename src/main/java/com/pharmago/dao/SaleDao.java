package com.pharmago.dao;

import com.pharmago.config.DatabaseConfig;
import com.pharmago.model.Sale;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SaleDao {
    private final DatabaseConfig databaseConfig;

    public SaleDao(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public void addSale(Sale sale) throws SQLException {
        String sql = """
                INSERT INTO sales (medicine_id, customer_name, quantity, sale_price, sale_date, prescription_required)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, sale.getMedicineId());
            statement.setString(2, sale.getCustomerName());
            statement.setInt(3, sale.getQuantity());
            statement.setBigDecimal(4, sale.getSalePrice());
            statement.setDate(5, Date.valueOf(sale.getSaleDate()));
            statement.setBoolean(6, sale.isPrescriptionRequired());
            statement.executeUpdate();
        }
    }

    public List<Sale> getAllSales() throws SQLException {
        String sql = """
                SELECT s.sale_id, s.medicine_id, m.name AS medicine_name, s.customer_name, s.quantity,
                       s.sale_price, s.sale_date, s.prescription_required
                FROM sales s
                JOIN medicines m ON s.medicine_id = m.medicine_id
                ORDER BY s.sale_date DESC
                """;
        List<Sale> sales = new ArrayList<>();

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Sale sale = new Sale();
                sale.setSaleId(resultSet.getInt("sale_id"));
                sale.setMedicineId(resultSet.getInt("medicine_id"));
                sale.setMedicineName(resultSet.getString("medicine_name"));
                sale.setCustomerName(resultSet.getString("customer_name"));
                sale.setQuantity(resultSet.getInt("quantity"));
                sale.setSalePrice(resultSet.getBigDecimal("sale_price"));
                sale.setSaleDate(resultSet.getDate("sale_date").toLocalDate());
                sale.setPrescriptionRequired(resultSet.getBoolean("prescription_required"));
                sales.add(sale);
            }
        }
        return sales;
    }

    public BigDecimal totalSalesValue() throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity * sale_price), 0) FROM sales";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getBigDecimal(1);
        }
    }
}
