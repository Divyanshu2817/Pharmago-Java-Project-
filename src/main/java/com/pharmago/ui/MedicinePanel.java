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
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import com.pharmago.model.Medicine;
import com.pharmago.service.InventoryService;
import com.pharmago.util.ConsoleFormatter;

public class MedicinePanel extends JPanel {

    private static final Color PANEL_BG = new Color(255, 255, 255);
    private static final Color PRIMARY = new Color(14, 116, 144);
    private static final Color DANGER = new Color(185, 28, 28);

    private final InventoryService inventoryService;
    private final Runnable onRefresh;
    private final Consumer<String> onStatus;

    private JTable medicineTable;
    private JTextField medicineSearchField;
    private List<Medicine> cachedMedicines = List.of();

    private JTextField codeField;
    private JTextField nameField;
    private JTextField categoryField;
    private JTextField manufacturerField;
    private JTextField unitPriceField;
    private JTextField stockField;
    private JTextField reorderField;
    private JTextField expiryField;

    private int editingMedicineId = 0;
    private JButton saveButton;
    private JButton cancelEditButton;

    public MedicinePanel(InventoryService inventoryService, Runnable onRefresh, Consumer<String> onStatus) {
        this.inventoryService = inventoryService;
        this.onRefresh = onRefresh;
        this.onStatus = onStatus;
        setBackground(PANEL_BG);
        setLayout(new BorderLayout(14, 14));
        medicineTable = createTable(new String[]{"ID", "Code", "Name", "Category", "Manufacturer", "Price", "Stock", "Reorder", "Expiry"});
        add(buildTopBar(), BorderLayout.NORTH);
        add(wrapTable(medicineTable), BorderLayout.CENTER);
        add(buildMedicineForm(), BorderLayout.SOUTH);
    }

    public void loadMedicineTable(List<Medicine> medicines) {
        cachedMedicines = medicines;
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

    private void filterMedicineTable() {
        String query = medicineSearchField == null ? "" : medicineSearchField.getText().trim().toLowerCase();
        DefaultTableModel model = (DefaultTableModel) medicineTable.getModel();
        model.setRowCount(0);
        for (Medicine medicine : cachedMedicines) {
            if (query.isEmpty()
                    || medicine.getMedicineCode().toLowerCase().contains(query)
                    || medicine.getName().toLowerCase().contains(query)
                    || medicine.getCategory().toLowerCase().contains(query)
                    || medicine.getManufacturer().toLowerCase().contains(query)) {
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
    }

    private JComponent buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setOpaque(false);

        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setOpaque(false);
        JLabel searchLabel = new JLabel("Search Medicine:");
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        medicineSearchField = new JTextField();
        medicineSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filterMedicineTable(); }
            @Override public void removeUpdate(DocumentEvent e) { filterMedicineTable(); }
            @Override public void changedUpdate(DocumentEvent e) { filterMedicineTable(); }
        });
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(medicineSearchField, BorderLayout.CENTER);

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionBar.setOpaque(false);
        JButton editButton = new JButton("Edit Selected");
        editButton.setFocusPainted(false);
        editButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        editButton.addActionListener(e -> handleEditMedicine());
        JButton deleteButton = createDangerButton("Delete Selected");
        deleteButton.addActionListener(e -> handleDeleteMedicine());
        actionBar.add(editButton);
        actionBar.add(deleteButton);

        topBar.add(searchPanel, BorderLayout.CENTER);
        topBar.add(actionBar, BorderLayout.EAST);
        return topBar;
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

        saveButton = createPrimaryButton("Add Medicine");
        saveButton.addActionListener(e -> handleSave());
        cancelEditButton = new JButton("Cancel Edit");
        cancelEditButton.setFocusPainted(false);
        cancelEditButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cancelEditButton.setVisible(false);
        cancelEditButton.addActionListener(e -> exitEditMode());

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonBar.setOpaque(false);
        buttonBar.add(cancelEditButton);
        buttonBar.add(saveButton);

        formCard.add(fields, BorderLayout.CENTER);
        formCard.add(buttonBar, BorderLayout.SOUTH);
        return formCard;
    }

    private void handleSave() {
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
            if (editingMedicineId != 0) {
                medicine.setMedicineId(editingMedicineId);
                inventoryService.updateMedicine(medicine);
                exitEditMode();
                onRefresh.run();
                showInfo("Medicine updated successfully.");
            } else {
                inventoryService.addMedicine(medicine);
                clearForm();
                onRefresh.run();
                showInfo("Medicine added successfully.");
            }
        } catch (Exception exception) {
            handleError(editingMedicineId != 0 ? "Unable to update medicine" : "Unable to add medicine", exception);
        }
    }

    private void handleEditMedicine() {
        int selectedRow = medicineTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this),
                    "Select a medicine row to edit.", "Edit Medicine", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int medicineId = Integer.parseInt(String.valueOf(medicineTable.getValueAt(selectedRow, 0)));
        Medicine medicine = cachedMedicines.stream()
                .filter(m -> m.getMedicineId() == medicineId)
                .findFirst().orElse(null);
        if (medicine != null) {
            enterEditMode(medicine);
        }
    }

    private void enterEditMode(Medicine medicine) {
        editingMedicineId = medicine.getMedicineId();
        codeField.setText(medicine.getMedicineCode());
        nameField.setText(medicine.getName());
        categoryField.setText(medicine.getCategory());
        manufacturerField.setText(medicine.getManufacturer());
        unitPriceField.setText(medicine.getUnitPrice().toPlainString());
        stockField.setText(String.valueOf(medicine.getStockQuantity()));
        reorderField.setText(String.valueOf(medicine.getReorderLevel()));
        expiryField.setText(medicine.getExpiryDate().toString());
        saveButton.setText("Update Medicine");
        cancelEditButton.setVisible(true);
    }

    private void exitEditMode() {
        editingMedicineId = 0;
        saveButton.setText("Add Medicine");
        cancelEditButton.setVisible(false);
        clearForm();
    }

    private void handleDeleteMedicine() {
        try {
            int selectedRow = medicineTable.getSelectedRow();
            if (selectedRow < 0) throw new IllegalArgumentException("Select a medicine row first.");
            int medicineId = Integer.parseInt(String.valueOf(medicineTable.getValueAt(selectedRow, 0)));
            if (!confirmDeletion("Delete the selected medicine?")) return;
            inventoryService.deleteMedicine(medicineId);
            onRefresh.run();
            showInfo("Medicine deleted successfully.");
        } catch (Exception exception) {
            handleError("Unable to delete medicine", exception);
        }
    }

    private void clearForm() {
        codeField.setText("");
        nameField.setText("");
        categoryField.setText("");
        manufacturerField.setText("");
        unitPriceField.setText("");
        stockField.setText("");
        reorderField.setText("");
        expiryField.setText(LocalDate.now().plusMonths(6).toString());
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
}
