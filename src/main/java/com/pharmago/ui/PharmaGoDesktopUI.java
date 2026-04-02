package com.pharmago.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.pharmago.model.BusinessSummary;
import com.pharmago.model.Medicine;
import com.pharmago.model.Purchase;
import com.pharmago.model.Sale;
import com.pharmago.service.InventoryService;
import com.pharmago.util.ConsoleFormatter;

public class PharmaGoDesktopUI {
    private static final Color APP_BG = new Color(242, 246, 252);
    private static final Color PANEL_BG = new Color(255, 255, 255);
    private static final Color PRIMARY = new Color(14, 116, 144);
    private static final Color ACCENT = new Color(19, 78, 94);
    private static final Color ALERT = new Color(190, 24, 93);

    private final InventoryService inventoryService;

    private JFrame frame;
    private JLabel medicinesValueLabel;
    private JLabel stockValueLabel;
    private JLabel lowStockValueLabel;
    private JLabel expiryValueLabel;
    private JLabel purchaseValueLabel;
    private JLabel salesValueLabel;
    private JLabel statusLabel;

    private JTable medicineTable;
    private JTable purchaseTable;
    private JTable salesTable;
    private JTable lowStockTable;
    private JTable expiryTable;

    private JTextField codeField;
    private JTextField nameField;
    private JTextField categoryField;
    private JTextField manufacturerField;
    private JTextField unitPriceField;
    private JTextField stockField;
    private JTextField reorderField;
    private JTextField expiryField;

    private JComboBox<MedicineItem> purchaseMedicineCombo;
    private JTextField supplierField;
    private JTextField purchaseQuantityField;
    private JTextField purchasePriceField;
    private JTextField purchaseDateField;
    private JTextField batchField;

    private JComboBox<MedicineItem> saleMedicineCombo;
    private JTextField customerField;
    private JTextField saleQuantityField;
    private JTextField salePriceField;
    private JTextField saleDateField;
    private JCheckBox prescriptionBox;

    public PharmaGoDesktopUI(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
        setLookAndFeel();
        initializeComponents();
    }

    public void showWindow() {
        refreshAllData();
        frame.setVisible(true);
    }

    private void initializeComponents() {
        frame = new JFrame("PharmaGo | Medicine Inventory & Sales Tracker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1280, 760));
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(APP_BG);

        frame.add(buildHeader(), BorderLayout.NORTH);
        frame.add(buildMainContent(), BorderLayout.CENTER);
        frame.add(buildFooter(), BorderLayout.SOUTH);
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setBackground(ACCENT);
        header.setBorder(new EmptyBorder(18, 24, 18, 24));

        JPanel textBlock = new JPanel();
        textBlock.setOpaque(false);
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("PharmaGo");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));

        JLabel subtitle = new JLabel("Desktop Suite for Inventory, Purchases, Sales, and Expiry Intelligence");
        subtitle.setForeground(new Color(214, 234, 248));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        textBlock.add(title);
        textBlock.add(Box.createVerticalStrut(4));
        textBlock.add(subtitle);

        JButton refreshButton = createPrimaryButton("Refresh Workspace");
        refreshButton.addActionListener(event -> refreshAllData());

        header.add(textBlock, BorderLayout.WEST);
        header.add(refreshButton, BorderLayout.EAST);
        return header;
    }

    private JComponent buildMainContent() {
        JPanel content = new JPanel(new BorderLayout(18, 18));
        content.setBackground(APP_BG);
        content.setBorder(new EmptyBorder(18, 18, 18, 18));

        content.add(buildDashboardStrip(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildWorkbenchPanel(), buildAlertsPanel());
        splitPane.setResizeWeight(0.78);
        splitPane.setBorder(null);
        content.add(splitPane, BorderLayout.CENTER);
        return content;
    }

    private JComponent buildDashboardStrip() {
        JPanel dashboard = new JPanel(new GridLayout(2, 3, 14, 14));
        dashboard.setOpaque(false);

        medicinesValueLabel = new JLabel("0");
        stockValueLabel = new JLabel("0");
        lowStockValueLabel = new JLabel("0");
        expiryValueLabel = new JLabel("0");
        purchaseValueLabel = new JLabel("Rs. 0.00");
        salesValueLabel = new JLabel("Rs. 0.00");

        dashboard.add(createStatCard("Registered Medicines", medicinesValueLabel, new Color(14, 165, 233)));
        dashboard.add(createStatCard("Units In Stock", stockValueLabel, new Color(34, 197, 94)));
        dashboard.add(createStatCard("Low Stock Alerts", lowStockValueLabel, new Color(245, 158, 11)));
        dashboard.add(createStatCard("Expiry Alerts", expiryValueLabel, new Color(244, 63, 94)));
        dashboard.add(createStatCard("Purchase Value", purchaseValueLabel, new Color(99, 102, 241)));
        dashboard.add(createStatCard("Sales Value", salesValueLabel, new Color(6, 182, 212)));

        return dashboard;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color stripeColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 228, 238)),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JPanel stripe = new JPanel();
        stripe.setBackground(stripeColor);
        stripe.setPreferredSize(new Dimension(10, 10));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        heading.setForeground(new Color(71, 85, 105));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(new Color(15, 23, 42));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(heading);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(valueLabel);

        card.add(stripe, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildWorkbenchPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 228, 238)),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabs.addTab("Inventory", buildInventoryTab());
        tabs.addTab("Purchases", buildPurchasesTab());
        tabs.addTab("Sales", buildSalesTab());

        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private JComponent buildAlertsPanel() {
        JPanel alertPanel = new JPanel(new BorderLayout(0, 14));
        alertPanel.setBackground(APP_BG);

        JPanel titlePanel = createPanelCard("Risk Monitor");
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Expiry and Reorder Watch");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(15, 23, 42));
        JLabel note = new JLabel("This panel surfaces products that need attention quickly.");
        note.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        note.setForeground(new Color(71, 85, 105));
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(note);

        JTabbedPane riskTabs = new JTabbedPane();
        riskTabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lowStockTable = createTable(new String[]{"ID", "Code", "Medicine", "Stock", "Reorder"});
        expiryTable = createTable(new String[]{"ID", "Code", "Medicine", "Expiry"});
        riskTabs.addTab("Low Stock", wrapTable(lowStockTable));
        riskTabs.addTab("Expiring Soon", wrapTable(expiryTable));

        alertPanel.add(titlePanel, BorderLayout.NORTH);
        alertPanel.add(riskTabs, BorderLayout.CENTER);
        return alertPanel;
    }

    private JComponent buildInventoryTab() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(PANEL_BG);

        medicineTable = createTable(new String[]{"ID", "Code", "Name", "Category", "Manufacturer", "Price", "Stock", "Reorder", "Expiry"});
        panel.add(wrapTable(medicineTable), BorderLayout.CENTER);
        panel.add(buildMedicineForm(), BorderLayout.SOUTH);
        return panel;
    }

    private JComponent buildPurchasesTab() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(PANEL_BG);

        purchaseTable = createTable(new String[]{"ID", "Medicine", "Supplier", "Qty", "Price", "Date", "Batch"});
        panel.add(wrapTable(purchaseTable), BorderLayout.CENTER);
        panel.add(buildPurchaseForm(), BorderLayout.SOUTH);
        return panel;
    }

    private JComponent buildSalesTab() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(PANEL_BG);

        salesTable = createTable(new String[]{"ID", "Medicine", "Customer", "Qty", "Price", "Date", "Prescription"});
        panel.add(wrapTable(salesTable), BorderLayout.CENTER);
        panel.add(buildSaleForm(), BorderLayout.SOUTH);
        return panel;
    }

    private JComponent buildMedicineForm() {
        JPanel formCard = createPanelCard("Medicine Registration");
        formCard.setLayout(new BorderLayout(12, 12));

        JPanel fields = createFormGrid();
        codeField = new JTextField();
        nameField = new JTextField();
        categoryField = new JTextField();
        manufacturerField = new JTextField();
        unitPriceField = new JTextField();
        stockField = new JTextField();
        reorderField = new JTextField();
        expiryField = new JTextField(LocalDate.now().plusMonths(6).toString());

        fields.add(createLabeledField("Code", codeField));
        fields.add(createLabeledField("Name", nameField));
        fields.add(createLabeledField("Category", categoryField));
        fields.add(createLabeledField("Manufacturer", manufacturerField));
        fields.add(createLabeledField("Unit Price", unitPriceField));
        fields.add(createLabeledField("Opening Stock", stockField));
        fields.add(createLabeledField("Reorder Level", reorderField));
        fields.add(createLabeledField("Expiry (YYYY-MM-DD)", expiryField));

        JButton saveButton = createPrimaryButton("Add Medicine");
        saveButton.addActionListener(event -> handleAddMedicine());

        formCard.add(fields, BorderLayout.CENTER);
        formCard.add(createButtonBar(saveButton), BorderLayout.SOUTH);
        return formCard;
    }

    private JComponent buildPurchaseForm() {
        JPanel formCard = createPanelCard("Purchase Entry");
        formCard.setLayout(new BorderLayout(12, 12));

        JPanel fields = createFormGrid();
        purchaseMedicineCombo = new JComboBox<>();
        supplierField = new JTextField();
        purchaseQuantityField = new JTextField();
        purchasePriceField = new JTextField();
        purchaseDateField = new JTextField(LocalDate.now().toString());
        batchField = new JTextField();

        fields.add(createLabeledField("Medicine", purchaseMedicineCombo));
        fields.add(createLabeledField("Supplier", supplierField));
        fields.add(createLabeledField("Quantity", purchaseQuantityField));
        fields.add(createLabeledField("Purchase Price", purchasePriceField));
        fields.add(createLabeledField("Purchase Date", purchaseDateField));
        fields.add(createLabeledField("Batch No", batchField));

        JButton saveButton = createPrimaryButton("Record Purchase");
        saveButton.addActionListener(event -> handleRecordPurchase());

        formCard.add(fields, BorderLayout.CENTER);
        formCard.add(createButtonBar(saveButton), BorderLayout.SOUTH);
        return formCard;
    }

    private JComponent buildSaleForm() {
        JPanel formCard = createPanelCard("Sales Counter");
        formCard.setLayout(new BorderLayout(12, 12));

        JPanel fields = createFormGrid();
        saleMedicineCombo = new JComboBox<>();
        customerField = new JTextField();
        saleQuantityField = new JTextField();
        salePriceField = new JTextField();
        saleDateField = new JTextField(LocalDate.now().toString());
        prescriptionBox = new JCheckBox("Prescription required");
        prescriptionBox.setOpaque(false);

        fields.add(createLabeledField("Medicine", saleMedicineCombo));
        fields.add(createLabeledField("Customer", customerField));
        fields.add(createLabeledField("Quantity", saleQuantityField));
        fields.add(createLabeledField("Sale Price", salePriceField));
        fields.add(createLabeledField("Sale Date", saleDateField));
        fields.add(createLabeledField("Compliance", prescriptionBox));

        JButton saveButton = createPrimaryButton("Record Sale");
        saveButton.addActionListener(event -> handleRecordSale());

        formCard.add(fields, BorderLayout.CENTER);
        formCard.add(createButtonBar(saveButton), BorderLayout.SOUTH);
        return formCard;
    }

    private JPanel createButtonBar(JButton button) {
        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonBar.setOpaque(false);
        buttonBar.add(button);
        return buttonBar;
    }

    private JPanel createPanelCard(String title) {
        JPanel card = new JPanel();
        card.setBackground(PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 228, 238)),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), title),
                        new EmptyBorder(12, 12, 12, 12)
                )
        ));
        return card;
    }

    private JPanel createFormGrid() {
        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 12));
        grid.setOpaque(false);
        return grid;
    }

    private JPanel createLabeledField(String labelText, JComponent input) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 6));
        wrapper.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(new Color(51, 65, 85));
        wrapper.add(label, BorderLayout.NORTH);
        wrapper.add(input, BorderLayout.CENTER);
        return wrapper;
    }

    private JTable createTable(String[] columns) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(226, 232, 240));
        table.setGridColor(new Color(226, 232, 240));
        table.setSelectionBackground(new Color(224, 242, 254));
        return table;
    }

    private JScrollPane wrapTable(JTable table) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        return scrollPane;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return button;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(226, 232, 240));
        footer.setBorder(new EmptyBorder(8, 16, 8, 16));

        statusLabel = new JLabel("Workspace ready.");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setHorizontalAlignment(SwingConstants.LEFT);
        footer.add(statusLabel, BorderLayout.WEST);
        return footer;
    }

    private void refreshAllData() {
        setStatus("Refreshing workspace...");
        new SwingWorker<Void, Void>() {
            private BusinessSummary summary;
            private List<Medicine> medicines;
            private List<Medicine> lowStock;
            private List<Medicine> expiring;
            private List<Purchase> purchases;
            private List<Sale> sales;

            @Override
            protected Void doInBackground() throws Exception {
                summary = inventoryService.getBusinessSummary();
                medicines = inventoryService.getAllMedicines();
                lowStock = inventoryService.getLowStockMedicines();
                expiring = inventoryService.getExpiringMedicines();
                purchases = inventoryService.getAllPurchases();
                sales = inventoryService.getAllSales();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    populateSummary(summary);
                    loadMedicineTable(medicines);
                    loadLowStockTable(lowStock);
                    loadExpiryTable(expiring);
                    loadPurchaseTable(purchases);
                    loadSalesTable(sales);
                    loadMedicineCombos(medicines);
                    setStatus("Workspace refreshed successfully.");
                } catch (Exception exception) {
                    handleError("Unable to refresh records", exception);
                }
            }
        }.execute();
    }

    private void handleAddMedicine() {
        try {
            Medicine medicine = new Medicine(
                    requireText(codeField, "Medicine code"),
                    requireText(nameField, "Medicine name"),
                    requireText(categoryField, "Category"),
                    requireText(manufacturerField, "Manufacturer"),
                    new BigDecimal(requireText(unitPriceField, "Unit price")),
                    Integer.parseInt(requireText(stockField, "Opening stock")),
                    Integer.parseInt(requireText(reorderField, "Reorder level")),
                    LocalDate.parse(requireText(expiryField, "Expiry date"))
            );

            inventoryService.addMedicine(medicine);
            clearMedicineForm();
            refreshAllData();
            showInfo("Medicine added successfully.");
        } catch (Exception exception) {
            handleError("Unable to add medicine", exception);
        }
    }

    private void handleRecordPurchase() {
        try {
            MedicineItem item = requireSelectedMedicine(purchaseMedicineCombo, "purchase");
            Purchase purchase = new Purchase();
            purchase.setMedicineId(item.id());
            purchase.setSupplierName(requireText(supplierField, "Supplier"));
            purchase.setQuantity(Integer.parseInt(requireText(purchaseQuantityField, "Quantity")));
            purchase.setPurchasePrice(new BigDecimal(requireText(purchasePriceField, "Purchase price")));
            purchase.setPurchaseDate(LocalDate.parse(requireText(purchaseDateField, "Purchase date")));
            purchase.setBatchNo(requireText(batchField, "Batch number"));

            inventoryService.recordPurchase(purchase);
            clearPurchaseForm();
            refreshAllData();
            showInfo("Purchase recorded successfully.");
        } catch (Exception exception) {
            handleError("Unable to record purchase", exception);
        }
    }

    private void handleRecordSale() {
        try {
            MedicineItem item = requireSelectedMedicine(saleMedicineCombo, "sale");
            Sale sale = new Sale();
            sale.setMedicineId(item.id());
            sale.setCustomerName(requireText(customerField, "Customer"));
            sale.setQuantity(Integer.parseInt(requireText(saleQuantityField, "Quantity")));
            sale.setSalePrice(new BigDecimal(requireText(salePriceField, "Sale price")));
            sale.setSaleDate(LocalDate.parse(requireText(saleDateField, "Sale date")));
            sale.setPrescriptionRequired(prescriptionBox.isSelected());

            inventoryService.recordSale(sale);
            clearSaleForm();
            refreshAllData();
            showInfo("Sale recorded successfully.");
        } catch (Exception exception) {
            handleError("Unable to record sale", exception);
        }
    }

    private void populateSummary(BusinessSummary summary) {
        medicinesValueLabel.setText(String.valueOf(summary.getTotalMedicines()));
        stockValueLabel.setText(String.valueOf(summary.getTotalUnitsInStock()));
        lowStockValueLabel.setText(String.valueOf(summary.getLowStockItems()));
        lowStockValueLabel.setForeground(summary.getLowStockItems() > 0 ? ALERT : new Color(15, 23, 42));
        expiryValueLabel.setText(String.valueOf(summary.getExpiringSoonItems()));
        expiryValueLabel.setForeground(summary.getExpiringSoonItems() > 0 ? ALERT : new Color(15, 23, 42));
        purchaseValueLabel.setText(ConsoleFormatter.money(summary.getPurchaseValue()));
        salesValueLabel.setText(ConsoleFormatter.money(summary.getSalesValue()));
    }

    private void loadMedicineTable(List<Medicine> medicines) {
        DefaultTableModel model = (DefaultTableModel) medicineTable.getModel();
        model.setRowCount(0);
        for (Medicine medicine : medicines) {
            model.addRow(new Object[]{
                    medicine.getMedicineId(),
                    medicine.getMedicineCode(),
                    medicine.getName(),
                    medicine.getCategory(),
                    medicine.getManufacturer(),
                    ConsoleFormatter.money(medicine.getUnitPrice()),
                    medicine.getStockQuantity(),
                    medicine.getReorderLevel(),
                    ConsoleFormatter.formatDate(medicine.getExpiryDate())
            });
        }
    }

    private void loadLowStockTable(List<Medicine> medicines) {
        DefaultTableModel model = (DefaultTableModel) lowStockTable.getModel();
        model.setRowCount(0);
        for (Medicine medicine : medicines) {
            model.addRow(new Object[]{
                    medicine.getMedicineId(),
                    medicine.getMedicineCode(),
                    medicine.getName(),
                    medicine.getStockQuantity(),
                    medicine.getReorderLevel()
            });
        }
    }

    private void loadExpiryTable(List<Medicine> medicines) {
        DefaultTableModel model = (DefaultTableModel) expiryTable.getModel();
        model.setRowCount(0);
        for (Medicine medicine : medicines) {
            model.addRow(new Object[]{
                    medicine.getMedicineId(),
                    medicine.getMedicineCode(),
                    medicine.getName(),
                    ConsoleFormatter.formatDate(medicine.getExpiryDate())
            });
        }
    }

    private void loadPurchaseTable(List<Purchase> purchases) {
        DefaultTableModel model = (DefaultTableModel) purchaseTable.getModel();
        model.setRowCount(0);
        for (Purchase purchase : purchases) {
            model.addRow(new Object[]{
                    purchase.getPurchaseId(),
                    purchase.getMedicineName(),
                    purchase.getSupplierName(),
                    purchase.getQuantity(),
                    ConsoleFormatter.money(purchase.getPurchasePrice()),
                    ConsoleFormatter.formatDate(purchase.getPurchaseDate()),
                    purchase.getBatchNo()
            });
        }
    }

    private void loadSalesTable(List<Sale> sales) {
        DefaultTableModel model = (DefaultTableModel) salesTable.getModel();
        model.setRowCount(0);
        for (Sale sale : sales) {
            model.addRow(new Object[]{
                    sale.getSaleId(),
                    sale.getMedicineName(),
                    sale.getCustomerName(),
                    sale.getQuantity(),
                    ConsoleFormatter.money(sale.getSalePrice()),
                    ConsoleFormatter.formatDate(sale.getSaleDate()),
                    sale.isPrescriptionRequired() ? "Yes" : "No"
            });
        }
    }

    private void loadMedicineCombos(List<Medicine> medicines) {
        Vector<MedicineItem> items = new Vector<>();
        for (Medicine medicine : medicines) {
            items.add(new MedicineItem(medicine.getMedicineId(), medicine.getName() + " [" + medicine.getMedicineCode() + "]"));
        }
        purchaseMedicineCombo.setModel(new javax.swing.DefaultComboBoxModel<>(items));
        saleMedicineCombo.setModel(new javax.swing.DefaultComboBoxModel<>(items));
    }

    private String requireText(JTextField field, String fieldName) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value;
    }

    private MedicineItem requireSelectedMedicine(JComboBox<MedicineItem> comboBox, String actionName) {
        MedicineItem item = (MedicineItem) comboBox.getSelectedItem();
        if (item == null) {
            throw new IllegalArgumentException("Select a medicine before recording the " + actionName + ".");
        }
        return item;
    }

    private void clearMedicineForm() {
        codeField.setText("");
        nameField.setText("");
        categoryField.setText("");
        manufacturerField.setText("");
        unitPriceField.setText("");
        stockField.setText("");
        reorderField.setText("");
        expiryField.setText(LocalDate.now().plusMonths(6).toString());
    }

    private void clearPurchaseForm() {
        supplierField.setText("");
        purchaseQuantityField.setText("");
        purchasePriceField.setText("");
        purchaseDateField.setText(LocalDate.now().toString());
        batchField.setText("");
    }

    private void clearSaleForm() {
        customerField.setText("");
        saleQuantityField.setText("");
        salePriceField.setText("");
        saleDateField.setText(LocalDate.now().toString());
        prescriptionBox.setSelected(false);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(frame, message, "PharmaGo", JOptionPane.INFORMATION_MESSAGE);
        setStatus(message);
    }

    private void handleError(String title, Exception exception) {
        String message = unwrapMessage(exception);
        JOptionPane.showMessageDialog(frame, message, title, JOptionPane.ERROR_MESSAGE);
        setStatus(title + ": " + message);
    }

    private String unwrapMessage(Exception exception) {
        Throwable cause = exception instanceof SQLException ? exception : exception.getCause();
        if (cause != null && cause.getMessage() != null && !cause.getMessage().isBlank()) {
            return cause.getMessage();
        }
        if (exception.getMessage() != null && !exception.getMessage().isBlank()) {
            return exception.getMessage();
        }
        return "Unexpected application error.";
    }

    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
    }

    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    private record MedicineItem(int id, String label) {
        @Override
        public String toString() {
            return label;
        }
    }
}
