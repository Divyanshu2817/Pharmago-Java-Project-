package com.pharmago.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
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

import javax.swing.Box;
import javax.swing.BoxLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
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
import com.pharmago.model.Sale;
import com.pharmago.service.InventoryService;
import com.pharmago.util.ConsoleFormatter;

public class SalePanel extends JPanel {

    private static final Color PANEL_BG = new Color(255, 255, 255);
    private static final Color PRIMARY = new Color(14, 116, 144);
    private static final Color DANGER = new Color(185, 28, 28);

    private final InventoryService inventoryService;
    private final Runnable onRefresh;
    private final Consumer<String> onStatus;

    private JTable salesTable;
    private List<Sale> cachedSales = List.of();
    private JTextField filterFromField;
    private JTextField filterToField;
    private JComboBox<MedicineItem> saleMedicineCombo;
    private JTextField customerField;
    private JTextField saleQuantityField;
    private JTextField salePriceField;
    private JTextField saleDateField;
    private JCheckBox prescriptionBox;

    public SalePanel(InventoryService inventoryService, Runnable onRefresh, Consumer<String> onStatus) {
        this.inventoryService = inventoryService;
        this.onRefresh = onRefresh;
        this.onStatus = onStatus;
        setBackground(PANEL_BG);
        setLayout(new BorderLayout(14, 14));
        salesTable = createTable(new String[]{"ID", "Medicine", "Customer", "Qty", "Price", "Date", "Prescription"});
        JPanel topArea = new JPanel();
        topArea.setOpaque(false);
        topArea.setLayout(new BoxLayout(topArea, BoxLayout.Y_AXIS));
        topArea.add(buildFilterBar());
        topArea.add(createDeleteBar("Delete Selected Sale", e -> handleDeleteSale()));
        add(topArea, BorderLayout.NORTH);
        add(wrapTable(salesTable), BorderLayout.CENTER);
        add(buildSaleForm(), BorderLayout.SOUTH);
    }

    public void loadSalesTable(List<Sale> sales) {
        cachedSales = sales;
        filterFromField.setText("");
        filterToField.setText("");
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

    public void updateMedicineCombo(List<Medicine> medicines) {
        Vector<MedicineItem> items = new Vector<>();
        for (Medicine medicine : medicines) {
            items.add(new MedicineItem(medicine.getMedicineId(), medicine.getName() + " [" + medicine.getMedicineCode() + "]"));
        }
        saleMedicineCombo.setModel(new javax.swing.DefaultComboBoxModel<>(items));
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
            DefaultTableModel model = (DefaultTableModel) salesTable.getModel();
            model.setRowCount(0);
            for (Sale sale : cachedSales) {
                LocalDate date = sale.getSaleDate();
                if ((from == null || !date.isBefore(from)) && (to == null || !date.isAfter(to))) {
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
        } catch (Exception exception) {
            handleError("Invalid date filter", exception);
        }
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
        saveButton.addActionListener(e -> handleRecordSale());

        formCard.add(fields, BorderLayout.CENTER);
        formCard.add(createButtonBar(saveButton), BorderLayout.SOUTH);
        return formCard;
    }

    private void handleRecordSale() {
        try {
            MedicineItem item = (MedicineItem) saleMedicineCombo.getSelectedItem();
            if (item == null) throw new IllegalArgumentException("Select a medicine before recording the sale.");
            Sale sale = new Sale();
            sale.setMedicineId(item.id());
            sale.setCustomerName(requireText(customerField, "Customer"));
            sale.setQuantity(Integer.parseInt(requireText(saleQuantityField, "Quantity")));
            sale.setSalePrice(new BigDecimal(requireText(salePriceField, "Sale price")));
            sale.setSaleDate(LocalDate.parse(requireText(saleDateField, "Sale date")));
            sale.setPrescriptionRequired(prescriptionBox.isSelected());
            inventoryService.recordSale(sale);
            showSaleReceipt(sale, item.toString());
            clearForm();
            onRefresh.run();
            onStatus.accept("Sale recorded successfully.");
        } catch (Exception exception) {
            handleError("Unable to record sale", exception);
        }
    }

    private void showSaleReceipt(Sale sale, String medicineName) {
        BigDecimal total = sale.getSalePrice().multiply(BigDecimal.valueOf(sale.getQuantity()));

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Sale Receipt", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(255, 255, 255));
        root.setBorder(new EmptyBorder(24, 28, 20, 28));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel titleLabel = new JLabel("PharmaGo");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(14, 116, 144));
        titleLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        JLabel subtitleLabel = new JLabel("Sale Receipt");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(71, 85, 105));
        subtitleLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        header.add(titleLabel);
        header.add(Box.createVerticalStrut(2));
        header.add(subtitleLabel);
        header.add(Box.createVerticalStrut(14));

        JPanel body = new JPanel(new GridLayout(0, 2, 8, 8));
        body.setOpaque(false);
        addReceiptRow(body, "Medicine", medicineName);
        addReceiptRow(body, "Customer", sale.getCustomerName());
        addReceiptRow(body, "Date", sale.getSaleDate().toString());
        addReceiptRow(body, "Quantity", String.valueOf(sale.getQuantity()));
        addReceiptRow(body, "Unit Price", ConsoleFormatter.money(sale.getSalePrice()));
        addReceiptRow(body, "Total", ConsoleFormatter.money(total));
        addReceiptRow(body, "Prescription", sale.isPrescriptionRequired() ? "Required" : "Not required");

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        JLabel thanks = new JLabel("Thank you for your purchase.");
        thanks.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        thanks.setForeground(new Color(100, 116, 139));
        footer.add(thanks);

        JPanel closeBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        closeBar.setOpaque(false);
        JButton closeButton = new JButton("Close");
        closeButton.setFocusPainted(false);
        closeButton.setBackground(new Color(14, 116, 144));
        closeButton.setForeground(Color.BLACK);
        closeButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeButton.addActionListener(e -> dialog.dispose());
        closeBar.add(closeButton);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.setBorder(new EmptyBorder(12, 0, 0, 0));
        south.add(footer, BorderLayout.NORTH);
        south.add(closeBar, BorderLayout.SOUTH);

        root.add(header, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
    }

    private void addReceiptRow(JPanel panel, String label, String value) {
        JLabel keyLabel = new JLabel(label);
        keyLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        keyLabel.setForeground(new Color(71, 85, 105));
        JLabel valLabel = new JLabel(value);
        valLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(keyLabel);
        panel.add(valLabel);
    }

    private void handleDeleteSale() {
        try {
            int selectedRow = salesTable.getSelectedRow();
            if (selectedRow < 0) throw new IllegalArgumentException("Select a sale row first.");
            int saleId = Integer.parseInt(String.valueOf(salesTable.getValueAt(selectedRow, 0)));
            if (!confirmDeletion("Delete the selected sale record?")) return;
            inventoryService.deleteSale(saleId);
            onRefresh.run();
            showInfo("Sale deleted successfully.");
        } catch (Exception exception) {
            handleError("Unable to delete sale", exception);
        }
    }

    private void clearForm() {
        customerField.setText("");
        saleQuantityField.setText("");
        salePriceField.setText("");
        saleDateField.setText(LocalDate.now().toString());
        prescriptionBox.setSelected(false);
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
