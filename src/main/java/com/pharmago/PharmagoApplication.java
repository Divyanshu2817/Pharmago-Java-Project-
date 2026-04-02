package com.pharmago;

import com.pharmago.config.DatabaseConfig;
import com.pharmago.dao.MedicineDao;
import com.pharmago.dao.PurchaseDao;
import com.pharmago.dao.SaleDao;
import com.pharmago.service.InventoryService;
import com.pharmago.ui.PharmaGoDesktopUI;

import javax.swing.SwingUtilities;

public class PharmagoApplication {
    public static void main(String[] args) {
        DatabaseConfig databaseConfig = new DatabaseConfig();
        MedicineDao medicineDao = new MedicineDao(databaseConfig);
        PurchaseDao purchaseDao = new PurchaseDao(databaseConfig);
        SaleDao saleDao = new SaleDao(databaseConfig);
        InventoryService inventoryService = new InventoryService(medicineDao, purchaseDao, saleDao);
        SwingUtilities.invokeLater(() -> new PharmaGoDesktopUI(inventoryService).showWindow());
    }
}
