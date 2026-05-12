package com.pharmago.model;

public class MedicineSalesTotal {
    private final String medicineName;
    private final int totalUnitsSold;

    public MedicineSalesTotal(String medicineName, int totalUnitsSold) {
        this.medicineName = medicineName;
        this.totalUnitsSold = totalUnitsSold;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public int getTotalUnitsSold() {
        return totalUnitsSold;
    }
}
