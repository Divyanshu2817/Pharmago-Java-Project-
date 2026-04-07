package com.pharmago.service;

import com.pharmago.dao.MedicineDao;
import com.pharmago.dao.PurchaseDao;
import com.pharmago.dao.SaleDao;
import com.pharmago.model.BusinessSummary;
import com.pharmago.model.Medicine;
import com.pharmago.model.Purchase;
import com.pharmago.model.Sale;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class InventoryService {
    private static final int EXPIRY_ALERT_DAYS = 30;

    private final MedicineDao medicineDao;
    private final PurchaseDao purchaseDao;
    private final SaleDao saleDao;

    public InventoryService(MedicineDao medicineDao, PurchaseDao purchaseDao, SaleDao saleDao) {
        this.medicineDao = medicineDao;
        this.purchaseDao = purchaseDao;
        this.saleDao = saleDao;
    }

    public void addMedicine(Medicine medicine) throws SQLException {
        medicineDao.addMedicine(medicine);
    }

    public void deleteMedicine(int medicineId) throws SQLException {
        if (purchaseDao.countByMedicineId(medicineId) > 0 || saleDao.countByMedicineId(medicineId) > 0) {
            throw new IllegalArgumentException("Delete purchase and sales history for this medicine first.");
        }
        medicineDao.deleteMedicine(medicineId);
    }

    public List<Medicine> getAllMedicines() throws SQLException {
        return medicineDao.getAllMedicines();
    }

    public List<Medicine> getLowStockMedicines() throws SQLException {
        return medicineDao.getLowStockMedicines();
    }

    public List<Medicine> getExpiringMedicines() throws SQLException {
        return medicineDao.getExpiringMedicines(EXPIRY_ALERT_DAYS);
    }

    public void recordPurchase(Purchase purchase) throws SQLException {
        Medicine medicine = getMedicineOrThrow(purchase.getMedicineId());
        purchaseDao.addPurchase(purchase);
        medicineDao.updateStock(medicine.getMedicineId(), medicine.getStockQuantity() + purchase.getQuantity());
    }

    public void recordSale(Sale sale) throws SQLException {
        Medicine medicine = getMedicineOrThrow(sale.getMedicineId());
        if (sale.getQuantity() > medicine.getStockQuantity()) {
            throw new IllegalArgumentException("Insufficient stock available for this sale.");
        }
        saleDao.addSale(sale);
        medicineDao.updateStock(medicine.getMedicineId(), medicine.getStockQuantity() - sale.getQuantity());
    }

    public List<Purchase> getAllPurchases() throws SQLException {
        return purchaseDao.getAllPurchases();
    }

    public void deletePurchase(int purchaseId) throws SQLException {
        Purchase purchase = purchaseDao.findPurchaseById(purchaseId);
        if (purchase == null) {
            throw new IllegalArgumentException("Purchase record not found.");
        }
        Medicine medicine = getMedicineOrThrow(purchase.getMedicineId());
        if (medicine.getStockQuantity() < purchase.getQuantity()) {
            throw new IllegalArgumentException("This purchase cannot be deleted because the stock has already been consumed.");
        }
        purchaseDao.deletePurchase(purchaseId);
        medicineDao.updateStock(medicine.getMedicineId(), medicine.getStockQuantity() - purchase.getQuantity());
    }

    public List<Sale> getAllSales() throws SQLException {
        return saleDao.getAllSales();
    }

    public void deleteSale(int saleId) throws SQLException {
        Sale sale = saleDao.findSaleById(saleId);
        if (sale == null) {
            throw new IllegalArgumentException("Sale record not found.");
        }
        Medicine medicine = getMedicineOrThrow(sale.getMedicineId());
        saleDao.deleteSale(saleId);
        medicineDao.updateStock(medicine.getMedicineId(), medicine.getStockQuantity() + sale.getQuantity());
    }

    public BusinessSummary getBusinessSummary() throws SQLException {
        int totalMedicines = medicineDao.countMedicines();
        int totalUnits = medicineDao.totalUnitsInStock();
        int lowStockItems = medicineDao.getLowStockMedicines().size();
        int expiringSoonItems = medicineDao.countExpiringSoon(EXPIRY_ALERT_DAYS);
        BigDecimal purchaseValue = purchaseDao.totalPurchaseValue();
        BigDecimal salesValue = saleDao.totalSalesValue();
        return new BusinessSummary(totalMedicines, totalUnits, lowStockItems, expiringSoonItems, purchaseValue, salesValue);
    }

    private Medicine getMedicineOrThrow(int medicineId) throws SQLException {
        Medicine medicine = medicineDao.findMedicineById(medicineId);
        if (medicine == null) {
            throw new IllegalArgumentException("Medicine with ID " + medicineId + " was not found.");
        }
        return medicine;
    }
}
