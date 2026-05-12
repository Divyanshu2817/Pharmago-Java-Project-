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
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.pharmago.model.Medicine;
import com.pharmago.model.Purchase;
import com.pharmago.service.InventoryService;
import com.pharmago.util.ConsoleFormatter;

public class PurchasePanel extends JPanel {

    private static final Color PANEL_BG = new Color(255, 255, 255);
    private static final Color PRIMARY = new Color(14, 116, 144);
    private static final Color DANGER = new Color(185, 28, 28);

    private final InventoryService inventoryService;
    private final Runnable onRefresh;
    private final Consumer<String> onStatus;

    private JTable purchaseTable;
    private JComboBox<MedicineItem> purchaseMedicineCombo;
    private JTextField supplierField;
    private JTextField purchaseQuantityField;
    private JTextField purchasePriceField;
    private JTextField purchaseDateField;
    private JTextField batchField;

    private List<Purchase> cachedPurchases = List.of();
    private JTextField filterFromField;
    private JTextField filterToField;

    public PurchasePanel(InventoryService inventoryService, Runnable onRefresh, Consumer<String> onStatus) {
        this.inventoryService = inventoryService;
        this.onRefresh = onRefresh;
        this.onStatus = onStatus;
        setBackground(PANEL_BG);
        setLayout(new BorderLayout(14, 14));
        purchaseTable = createTable(new String[]{"ID", "Medicine", "Supplier", "Qty", "Price", "Date", "Batch"});
        JPanel topArea = new JPanel();
        topArea.setOpaque(false);
        topArea.setLayout(new BoxLayout(topArea, BoxLayout.Y_AXIS));
        topArea.add(buildFilterBar());
        topArea.add(createDeleteBar("Delete Selected Purchase", e -> handleDeletePurchase()));
        add(topArea, BorderLayout.NORTH);
        add(wrapTable(purchaseTable), BorderLayout.CENTER);
        add(buildPurchaseForm(), BorderLayout.SOUTH);
    }

    public void loadPurchaseTable(List<Purchase> purchases) {
        cachedPurchases = purchases;
        filterFromField.setText("");
        filterToField.setText("");
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

    public void updateMedicineCombo(List<Medicine> medicines) {
        Vector<MedicineItem> items = new Vector<>();
        for (Medicine medicine : medicines) {
            items.add(new MedicineItem(medicine.getMedicineId(), medicine.getName() + " [" + medicine.getMedicineCode() + "]"));
        }
        purchaseMedicineCombo.setModel(new javax.swing.DefaultComboBoxModel<>(items));
    }

    private JComponent buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        bar.setOpaque(false);

        JLabel fromLabel = new JLabel("From:");
        fromLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterFromField = new JTextField(10);
        filterFromField.setToolTipText("YYYY-MM-DD");

        JLabel toLabel = new JLabel("To:");
        toLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterToField = new JTextField(10);
        filterToField.setToolTipText("YYYY-MM-DD");

        JButton applyBtn = new JButton("Apply Filter");
        applyBtn.setFocusPainted(false);
        applyBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        applyBtn.addActionListener(e -> applyDateFilter());

        JButton clearBtn = new JButton("Clear");
        clearBtn.setFocusPainted(false);
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clearBtn.addActionListener(e -> {
            filterFromField.setText("");
            filterToField.setText("");
            applyDateFilter();
        });

        bar.add(fromLabel);
        bar.add(filterFromField);
        bar.add(toLabel);
        bar.add(filterToField);
        bar.add(applyBtn);
        bar.add(clearBtn);
        return bar;
    }

    private void applyDateFilter() {
        try {
            String fromText = filterFromField.getText().trim();
            String toText = filterToField.getText().trim();
            LocalDate from = fromText.isEmpty() ? null : LocalDate.parse(fromText);
            LocalDate to = toText.isEmpty() ? null : LocalDate.parse(toText);
            DefaultTableModel model = (DefaultTableModel) purchaseTable.getModel();
            model.setRowCount(0);
            for (Purchase purchase : cachedPurchases) {
                LocalDate date = purchase.getPurchaseDate();
                if ((from == null || !date.isBefore(from)) && (to == null || !date.isAfter(to))) {
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
        } catch (Exception exception) {
            handleError("Invalid date filter", exception);
        }
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
        saveButton.addActionListener(e -> handleRecordPurchase());

        formCard.add(fields, BorderLayout.CENTER);
        formCard.add(createButtonBar(saveButton), BorderLayout.SOUTH);
        return formCard;
    }

    private void handleRecordPurchase() {
        try {
            MedicineItem item = (MedicineItem) purchaseMedicineCombo.getSelectedItem();
            if (item == null) throw new IllegalArgumentException("Select a medicine before recording the purchase.");
            Purchase purchase = new Purchase();
            purchase.setMedicineId(item.id());
            purchase.setSupplierName(requireText(supplierField, "Supplier"));
            purchase.setQuantity(Integer.parseInt(requireText(purchaseQuantityField, "Quantity")));
            purchase.setPurchasePrice(new BigDecimal(requireText(purchasePriceField, "Purchase price")));
            purchase.setPurchaseDate(LocalDate.parse(requireText(purchaseDateField, "Purchase date")));
            purchase.setBatchNo(requireText(batchField, "Batch number"));
            inventoryService.recordPurchase(purchase);
            clearForm();
            onRefresh.run();
            showInfo("Purchase recorded successfully.");
        } catch (Exception exception) {
            handleError("Unable to record purchase", exception);
        }
    }

    private void handleDeletePurchase() {
        try {
            int selectedRow = purchaseTable.getSelectedRow();
            if (selectedRow < 0) throw new IllegalArgumentException("Select a purchase row first.");
            int purchaseId = Integer.parseInt(String.valueOf(purchaseTable.getValueAt(selectedRow, 0)));
            if (!confirmDeletion("Delete the selected purchase record?")) return;
            inventoryService.deletePurchase(purchaseId);
            onRefresh.run();
            showInfo("Purchase deleted successfully.");
        } catch (Exception exception) {
            handleError("Unable to delete purchase", exception);
        }
    }

    private void clearForm() {
        supplierField.setText("");
        purchaseQuantityField.setText("");
        purchasePriceField.setText("");
        purchaseDateField.setText(LocalDate.now().toString());
        batchField.setText("");
    }

    private boolean confirmDeletion(String message) {
        return JOptionPane.showConfirmDialog(
                SwingUtilities.getWindowAncestor(this),
                message, "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        ) == JOptionPane.YES_OPTION;
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), message, "PharmaGo", JOptionPane.INFORMATION_MESSAGE);
        onStatus.accept(message);
    }

    private void handleError(String title, Exception exception) {
        String message = unwrapMessage(exception);
        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), message, title, JOptionPane.ERROR_MESSAGE);
        onStatus.accept(title + ": " + message);
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

    private String requireText(JTextField field, String fieldName) {
        String value = field.getText().trim();
        if (value.isEmpty()) throw new IllegalArgumentException(fieldName + " is required.");
        return value;
    }

    private JTable createTable(String[] columns) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
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

    private JButton createDangerButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(new Color(254, 226, 226));
        button.setForeground(DANGER);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
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

    private JPanel createButtonBar(JButton button) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bar.setOpaque(false);
        bar.add(button);
        return bar;
    }

    private JComponent createDeleteBar(String buttonText, java.awt.event.ActionListener listener) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bar.setOpaque(false);
        JButton btn = createDangerButton(buttonText);
        btn.addActionListener(listener);
        bar.add(btn);
        return bar;
    }

    private record MedicineItem(int id, String label) {
        @Override
        public String toString() {
            return label;
        }
    }
}
