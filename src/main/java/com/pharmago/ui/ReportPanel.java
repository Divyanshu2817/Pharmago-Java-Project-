package com.pharmago.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.pharmago.model.MedicineSalesTotal;
import com.pharmago.service.ReportService;
import com.pharmago.util.ConsoleFormatter;

public class ReportPanel extends JPanel {

    private static final Color PANEL_BG = new Color(255, 255, 255);
    private static final Color PRIMARY = new Color(14, 116, 144);

    private final ReportService reportService;
    private final Consumer<String> onStatus;

    private JLabel dailyTotalLabel;
    private JLabel monthlyRevenueLabel;
    private JTable topMedicinesTable;

    public ReportPanel(ReportService reportService, Consumer<String> onStatus) {
        this.reportService = reportService;
        this.onStatus = onStatus;
        setBackground(PANEL_BG);
        setLayout(new BorderLayout(14, 14));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        add(buildSummaryStrip(), BorderLayout.NORTH);
        add(buildTopMedicinesPanel(), BorderLayout.CENTER);
        add(buildRefreshBar(), BorderLayout.SOUTH);
    }

    public void refresh() {
        onStatus.accept("Loading reports...");
        new SwingWorker<Void, Void>() {
            private BigDecimal daily;
            private BigDecimal monthly;
            private List<MedicineSalesTotal> top;

            @Override
            protected Void doInBackground() throws Exception {
                LocalDate today = LocalDate.now();
                daily = reportService.getDailySalesTotal(today);
                monthly = reportService.getMonthlyRevenue(today.getYear(), today.getMonthValue());
                top = reportService.getTopSellingMedicines(5);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    dailyTotalLabel.setText(ConsoleFormatter.money(daily));
                    monthlyRevenueLabel.setText(ConsoleFormatter.money(monthly));
                    DefaultTableModel model = (DefaultTableModel) topMedicinesTable.getModel();
                    model.setRowCount(0);
                    for (MedicineSalesTotal entry : top) {
                        model.addRow(new Object[]{entry.getMedicineName(), entry.getTotalUnitsSold()});
                    }
                    onStatus.accept("Reports updated.");
                } catch (Exception exception) {
                    onStatus.accept("Unable to load reports: " + exception.getMessage());
                }
            }
        }.execute();
    }

    private JPanel buildSummaryStrip() {
        JPanel strip = new JPanel(new GridLayout(1, 2, 14, 0));
        strip.setOpaque(false);

        dailyTotalLabel = new JLabel("Rs. 0.00");
        monthlyRevenueLabel = new JLabel("Rs. 0.00");

        strip.add(createStatCard("Today's Sales Total", dailyTotalLabel, new Color(6, 182, 212)));
        strip.add(createStatCard("This Month's Revenue", monthlyRevenueLabel, new Color(99, 102, 241)));
        return strip;
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

    private JPanel buildTopMedicinesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        JLabel title = new JLabel("Top 5 Selling Medicines");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(new Color(15, 23, 42));
        title.setBorder(new EmptyBorder(12, 0, 6, 0));

        DefaultTableModel model = new DefaultTableModel(new String[]{"Medicine", "Units Sold"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        topMedicinesTable = new JTable(model);
        topMedicinesTable.setRowHeight(28);
        topMedicinesTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        topMedicinesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        topMedicinesTable.getTableHeader().setBackground(new Color(226, 232, 240));
        topMedicinesTable.setGridColor(new Color(226, 232, 240));
        topMedicinesTable.setSelectionBackground(new Color(224, 242, 254));

        JScrollPane scrollPane = new JScrollPane(topMedicinesTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildRefreshBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bar.setOpaque(false);
        JButton btn = new JButton("Refresh Report");
        btn.setFocusPainted(false);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.addActionListener(e -> refresh());
        bar.add(btn);
        return bar;
    }
}
