package com.pharmago.service;

import com.pharmago.config.DatabaseConfig;
import com.pharmago.exceptions.PharmacyException;
import com.pharmago.model.MedicineSalesTotal;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportService {
    private final DatabaseConfig databaseConfig;

    public ReportService(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public BigDecimal getDailySalesTotal(LocalDate date) throws PharmacyException {
        String sql = "SELECT COALESCE(SUM(quantity * sale_price), 0) FROM sales WHERE sale_date = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(date));
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getBigDecimal(1);
            }
        } catch (SQLException exception) {
            throw new PharmacyException("Failed to calculate daily sales total.", exception);
        }
    }

    public BigDecimal getMonthlyRevenue(int year, int month) throws PharmacyException {
        String sql = """
                SELECT COALESCE(SUM(quantity * sale_price), 0)
                FROM sales
                WHERE YEAR(sale_date) = ? AND MONTH(sale_date) = ?
                """;
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, year);
            statement.setInt(2, month);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getBigDecimal(1);
            }
        } catch (SQLException exception) {
            throw new PharmacyException("Failed to calculate monthly revenue.", exception);
        }
    }

    public List<MedicineSalesTotal> getTopSellingMedicines(int limit) throws PharmacyException {
        String sql = """
                SELECT m.name, COALESCE(SUM(s.quantity), 0) AS total_units
                FROM medicines m
                LEFT JOIN sales s ON m.medicine_id = s.medicine_id
                GROUP BY m.medicine_id, m.name
                ORDER BY total_units DESC
                LIMIT ?
                """;
        List<MedicineSalesTotal> results = new ArrayList<>();
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    results.add(new MedicineSalesTotal(
                            resultSet.getString("name"),
                            resultSet.getInt("total_units")
                    ));
                }
            }
        } catch (SQLException exception) {
            throw new PharmacyException("Failed to retrieve top-selling medicines.", exception);
        }
        return results;
    }
}
