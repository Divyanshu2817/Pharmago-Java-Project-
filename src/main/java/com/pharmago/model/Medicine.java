package com.pharmago.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Medicine {
    private int medicineId;
    private String medicineCode;
    private String name;
    private String category;
    private String manufacturer;
    private BigDecimal unitPrice;
    private int stockQuantity;
    private int reorderLevel;
    private LocalDate expiryDate;

    public Medicine() {
    }

    public Medicine(String medicineCode, String name, String category, String manufacturer,
                    BigDecimal unitPrice, int stockQuantity, int reorderLevel, LocalDate expiryDate) {
        this.medicineCode = medicineCode;
        this.name = name;
        this.category = category;
        this.manufacturer = manufacturer;
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity;
        this.reorderLevel = reorderLevel;
        this.expiryDate = expiryDate;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(int medicineId) {
        this.medicineId = medicineId;
    }

    public String getMedicineCode() {
        return medicineCode;
    }

    public void setMedicineCode(String medicineCode) {
        this.medicineCode = medicineCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
}
