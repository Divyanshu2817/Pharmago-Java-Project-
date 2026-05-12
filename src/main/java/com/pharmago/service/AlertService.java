package com.pharmago.service;

import com.pharmago.dao.MedicineDao;
import com.pharmago.model.Medicine;

import java.sql.SQLException;
import java.util.List;

public class AlertService {
    static final int EXPIRY_ALERT_DAYS = 30;

    private final MedicineDao medicineDao;

    public AlertService(MedicineDao medicineDao) {
        this.medicineDao = medicineDao;
    }

    public List<Medicine> getLowStockMedicines() throws SQLException {
        return medicineDao.getLowStockMedicines();
    }

    public List<Medicine> getExpiringMedicines() throws SQLException {
        return medicineDao.getExpiringMedicines(EXPIRY_ALERT_DAYS);
    }
}
