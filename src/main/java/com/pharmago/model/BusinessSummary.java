package com.pharmago.model;

import java.math.BigDecimal;

public class BusinessSummary {
    private final int totalMedicines;
    private final int totalUnitsInStock;
    private final int lowStockItems;
    private final int expiringSoonItems;
    private final BigDecimal purchaseValue;
    private final BigDecimal salesValue;

    public BusinessSummary(int totalMedicines, int totalUnitsInStock, int lowStockItems,
                        int expiringSoonItems, BigDecimal purchaseValue, BigDecimal salesValue) {
        this.totalMedicines = totalMedicines;
        this.totalUnitsInStock = totalUnitsInStock;
        this.lowStockItems = lowStockItems;
        this.expiringSoonItems = expiringSoonItems;
        this.purchaseValue = purchaseValue;
        this.salesValue = salesValue;
    }

    public int getTotalMedicines() {
        return totalMedicines;
    }

    public int getTotalUnitsInStock() {
        return totalUnitsInStock;
    }

    public int getLowStockItems() {
        return lowStockItems;
    }

    public int getExpiringSoonItems() {
        return expiringSoonItems;
    }

    public BigDecimal getPurchaseValue() {
        return purchaseValue;
    }

    public BigDecimal getSalesValue() {
        return salesValue;
    }
}
