package com.pharmago;

import com.pharmago.config.DatabaseConfig;
import com.pharmago.dao.MedicineDao;
import com.pharmago.dao.PurchaseDao;
import com.pharmago.dao.SaleDao;
import com.pharmago.dao.UserDao;
import com.pharmago.service.AlertService;
import com.pharmago.service.InventoryService;
import com.pharmago.service.ReportService;
import com.pharmago.ui.LoginPanel;
import com.pharmago.ui.PharmaGoDesktopUI;

import javax.swing.SwingUtilities;

public class PharmagoApplication {
    public static void main(String[] args) {
        DatabaseConfig databaseConfig = new DatabaseConfig();
        MedicineDao medicineDao = new MedicineDao(databaseConfig);
        PurchaseDao purchaseDao = new PurchaseDao(databaseConfig);
        SaleDao saleDao = new SaleDao(databaseConfig);
        UserDao userDao = new UserDao(databaseConfig);
        AlertService alertService = new AlertService(medicineDao);
        InventoryService inventoryService = new InventoryService(medicineDao, purchaseDao, saleDao, alertService);
        ReportService reportService = new ReportService(databaseConfig);
        PharmaGoDesktopUI mainUI = new PharmaGoDesktopUI(inventoryService, reportService);
        SwingUtilities.invokeLater(() -> new LoginPanel(userDao, mainUI::showWindow).show());
    }
}
