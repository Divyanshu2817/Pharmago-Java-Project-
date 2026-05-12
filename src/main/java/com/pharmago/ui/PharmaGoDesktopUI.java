package com.pharmago.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.pharmago.model.BusinessSummary;
import com.pharmago.model.Medicine;
import com.pharmago.model.Purchase;
import com.pharmago.model.Sale;
import com.pharmago.model.User;
import com.pharmago.service.InventoryService;
import com.pharmago.service.ReportService;
import com.pharmago.util.ConsoleFormatter;
import com.pharmago.util.Session;

public class PharmaGoDesktopUI {
    private static final Color APP_BG = new Color(242, 246, 252);
    private static final Color PANEL_BG = new Color(255, 255, 255);
    private static final Color PRIMARY = new Color(14, 116, 144);
    private static final Color ACCENT = new Color(19, 78, 94);
    private static final Color ALERT = new Color(190, 24, 93);

    private final InventoryService inventoryService;
    private final ReportService reportService;

    private JFrame frame;
    private JLabel medicinesValueLabel;
    private JLabel stockValueLabel;
    private JLabel lowStockValueLabel;
    private JLabel expiryValueLabel;
    private JLabel purchaseValueLabel;
    private JLabel salesValueLabel;
    private JLabel profitValueLabel;
    private JLabel statusLabel;

    private JTable lowStockTable;
    private JTable expiryTable;

    private MedicinePanel medicinePanel;
    private PurchasePanel purchasePanel;
    private SalePanel salePanel;
    private ReportPanel reportPanel;

    public PharmaGoDesktopUI(InventoryService inventoryService, ReportService reportService) {
        this.inventoryService = inventoryService;
        this.reportService = reportService;
        setLookAndFeel();
        initializeComponents();
    }

    public void showWindow() {
        User user = Session.getCurrentUser();
        if (user != null) {
            frame.setTitle("PharmaGo | " + user.getUsername() + " (" + user.getRole() + ")");
        }
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

        medicinePanel = new MedicinePanel(inventoryService, this::refreshAllData, this::setStatus);
        purchasePanel = new PurchasePanel(inventoryService, this::refreshAllData, this::setStatus);
        salePanel = new SalePanel(inventoryService, this::refreshAllData, this::setStatus);
        reportPanel = new ReportPanel(reportService, this::setStatus);

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
        JPanel dashboard = new JPanel(new GridLayout(2, 4, 14, 14));
        dashboard.setOpaque(false);

        medicinesValueLabel = new JLabel("0");
        stockValueLabel = new JLabel("0");
        lowStockValueLabel = new JLabel("0");
        expiryValueLabel = new JLabel("0");
        purchaseValueLabel = new JLabel("Rs. 0.00");
        salesValueLabel = new JLabel("Rs. 0.00");
        profitValueLabel = new JLabel("Rs. 0.00");

        dashboard.add(createStatCard("Registered Medicines", medicinesValueLabel, new Color(14, 165, 233)));
        dashboard.add(createStatCard("Units In Stock", stockValueLabel, new Color(34, 197, 94)));
        dashboard.add(createStatCard("Low Stock Alerts", lowStockValueLabel, new Color(245, 158, 11)));
        dashboard.add(createStatCard("Expiry Alerts", expiryValueLabel, new Color(244, 63, 94)));
        dashboard.add(createStatCard("Purchase Value", purchaseValueLabel, new Color(99, 102, 241)));
        dashboard.add(createStatCard("Sales Value", salesValueLabel, new Color(6, 182, 212)));
        dashboard.add(createStatCard("Net Profit", profitValueLabel, new Color(16, 185, 129)));

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
        tabs.addTab("Inventory", medicinePanel);
        tabs.addTab("Purchases", purchasePanel);
        tabs.addTab("Sales", salePanel);
        tabs.addTab("Reports", reportPanel);

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
                    medicinePanel.loadMedicineTable(medicines);
                    loadLowStockTable(lowStock);
                    loadExpiryTable(expiring);
                    purchasePanel.loadPurchaseTable(purchases);
                    salePanel.loadSalesTable(sales);
                    purchasePanel.updateMedicineCombo(medicines);
                    salePanel.updateMedicineCombo(medicines);
                    reportPanel.refresh();
                    setStatus("Workspace refreshed successfully.");
                } catch (Exception exception) {
                    Throwable cause = exception.getCause();
                    String message = (cause != null && cause.getMessage() != null && !cause.getMessage().isBlank())
                            ? cause.getMessage()
                            : (exception.getMessage() != null ? exception.getMessage() : "Unexpected error.");
                    JOptionPane.showMessageDialog(frame, message, "Unable to refresh records", JOptionPane.ERROR_MESSAGE);
                    setStatus("Unable to refresh records: " + message);
                }
            }
        }.execute();
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
        java.math.BigDecimal profit = summary.getSalesValue().subtract(summary.getPurchaseValue());
        profitValueLabel.setText(ConsoleFormatter.money(profit));
        profitValueLabel.setForeground(profit.signum() < 0 ? ALERT : new Color(15, 23, 42));
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

    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
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
        scrollPane.setPreferredSize(new Dimension(1100, 140));
        return scrollPane;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(PRIMARY);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return button;
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

    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }
}
