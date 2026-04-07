package com.pharmago.dao;

import com.pharmago.config.DatabaseConfig;
import com.pharmago.model.Medicine;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MedicineDao {
    private final DatabaseConfig databaseConfig;

    public MedicineDao(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public void addMedicine(Medicine medicine) throws SQLException {
        String sql = """
                INSERT INTO medicines
                (medicine_code, name, category, manufacturer, unit_price, stock_quantity, reorder_level, expiry_date)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, medicine.getMedicineCode());
            statement.setString(2, medicine.getName());
            statement.setString(3, medicine.getCategory());
            statement.setString(4, medicine.getManufacturer());
            statement.setBigDecimal(5, medicine.getUnitPrice());
            statement.setInt(6, medicine.getStockQuantity());
            statement.setInt(7, medicine.getReorderLevel());
            statement.setDate(8, Date.valueOf(medicine.getExpiryDate()));
            statement.executeUpdate();
        }
    }

    public List<Medicine> getAllMedicines() throws SQLException {
        String sql = "SELECT * FROM medicines ORDER BY name";
        List<Medicine> medicines = new ArrayList<>();

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                medicines.add(mapRow(resultSet));
            }
        }
        return medicines;
    }

    public List<Medicine> getLowStockMedicines() throws SQLException {
        String sql = "SELECT * FROM medicines WHERE stock_quantity <= reorder_level ORDER BY stock_quantity ASC";
        List<Medicine> medicines = new ArrayList<>();

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                medicines.add(mapRow(resultSet));
            }
        }
        return medicines;
    }

    public List<Medicine> getExpiringMedicines(int days) throws SQLException {
        String sql = "SELECT * FROM medicines WHERE expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY) ORDER BY expiry_date";
        List<Medicine> medicines = new ArrayList<>();

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, days);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    medicines.add(mapRow(resultSet));
                }
            }
        }
        return medicines;
    }

    public Medicine findMedicineById(int medicineId) throws SQLException {
        String sql = "SELECT * FROM medicines WHERE medicine_id = ?";

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, medicineId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }
        return null;
    }

    public void updateStock(int medicineId, int newQuantity) throws SQLException {
        String sql = "UPDATE medicines SET stock_quantity = ? WHERE medicine_id = ?";

        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, newQuantity);
            statement.setInt(2, medicineId);
            statement.executeUpdate();
        }
    }

    public void deleteMedicine(int medicineId) throws SQLException {
        String sql = "DELETE FROM medicines WHERE medicine_id = ?";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, medicineId);
            statement.executeUpdate();
        }
    }

    public int countMedicines() throws SQLException {
        String sql = "SELECT COUNT(*) FROM medicines";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    public int totalUnitsInStock() throws SQLException {
        String sql = "SELECT COALESCE(SUM(stock_quantity), 0) FROM medicines";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    public int countExpiringSoon(int days) throws SQLException {
        String sql = "SELECT COUNT(*) FROM medicines WHERE expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY)";
        try (Connection connection = databaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, days);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1);
            }
        }
    }

    private Medicine mapRow(ResultSet resultSet) throws SQLException {
        Medicine medicine = new Medicine();
        medicine.setMedicineId(resultSet.getInt("medicine_id"));
        medicine.setMedicineCode(resultSet.getString("medicine_code"));
        medicine.setName(resultSet.getString("name"));
        medicine.setCategory(resultSet.getString("category"));
        medicine.setManufacturer(resultSet.getString("manufacturer"));
        medicine.setUnitPrice(resultSet.getBigDecimal("unit_price"));
        medicine.setStockQuantity(resultSet.getInt("stock_quantity"));
        medicine.setReorderLevel(resultSet.getInt("reorder_level"));
        medicine.setExpiryDate(resultSet.getDate("expiry_date").toLocalDate());
        return medicine;
    }
}
