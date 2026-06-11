package main.java.br.usp.icmc.scc0204.javacafe.view;

import main.java.br.usp.icmc.scc0204.javacafe.model.Product;
import main.java.br.usp.icmc.scc0204.javacafe.model.Report;
import main.java.br.usp.icmc.scc0204.javacafe.controller.CafeController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Dashboard Panel for Sales Reports.
 * Displays: total revenue, transaction count, and top 3 best-selling items.
 * Supports: Daily, Weekly, and Monthly reports.
 */
public class DashboardPanel extends JPanel {
    
    private final CafeController controller;
    
    // UI Components
    private JLabel lblRevenueValue;
    private JLabel lblTransactionsValue;
    private JComboBox<String> reportPeriodCombo;
    private DefaultTableModel topProductsModel;
    
    /**
     * Constructor with controller injection.
     * @param controller The cafe controller instance
     */
    public DashboardPanel(CafeController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        initializeComponents();
        loadReportData();
    }
    
    /**
     * Initializes all UI components.
     */
    private void initializeComponents() {
        // Top panel: Period selector
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel: KPI display
        JPanel kpiPanel = createKPIPanel();
        add(kpiPanel, BorderLayout.CENTER);
        
        // Bottom panel: Top products table
        JPanel tablePanel = createTopProductsPanel();
        add(tablePanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the top panel with period selector.
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        panel.add(new JLabel("Report Period:"));
        
        String[] periods = {"Daily Report", "Weekly Report", "Monthly Report"};
        reportPeriodCombo = new JComboBox<>(periods);
        reportPeriodCombo.addActionListener(e -> loadReportData());
        panel.add(reportPeriodCombo);
        
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setToolTipText("Reload report data from saved sales");
        btnRefresh.addActionListener(e -> loadReportData());
        panel.add(btnRefresh);
        
        return panel;
    }
    
    /**
     * Creates the KPI panel showing revenue and transaction count.
     */
    private JPanel createKPIPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Summary"));
        panel.setPreferredSize(new Dimension(0, 100));
        
        // Revenue panel
        JPanel revenuePanel = new JPanel(new BorderLayout());
        revenuePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        revenuePanel.setBackground(new Color(46, 204, 113));
        
        JLabel lblRevenueTitle = new JLabel("Total Revenue", SwingConstants.CENTER);
        lblRevenueTitle.setForeground(Color.WHITE);
        lblRevenueTitle.setFont(new Font("Arial", Font.BOLD, 14));
        
        lblRevenueValue = new JLabel("R$ 0.00", SwingConstants.CENTER);
        lblRevenueValue.setForeground(Color.WHITE);
        lblRevenueValue.setFont(new Font("Arial", Font.BOLD, 20));
        
        revenuePanel.add(lblRevenueTitle, BorderLayout.NORTH);
        revenuePanel.add(lblRevenueValue, BorderLayout.CENTER);
        
        // Transactions panel
        JPanel transactionsPanel = new JPanel(new BorderLayout());
        transactionsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        transactionsPanel.setBackground(new Color(52, 152, 219));
        
        JLabel lblTransactionsTitle = new JLabel("Transactions", SwingConstants.CENTER);
        lblTransactionsTitle.setForeground(Color.WHITE);
        lblTransactionsTitle.setFont(new Font("Arial", Font.BOLD, 14));
        
        lblTransactionsValue = new JLabel("0", SwingConstants.CENTER);
        lblTransactionsValue.setForeground(Color.WHITE);
        lblTransactionsValue.setFont(new Font("Arial", Font.BOLD, 20));
        
        transactionsPanel.add(lblTransactionsTitle, BorderLayout.NORTH);
        transactionsPanel.add(lblTransactionsValue, BorderLayout.CENTER);
        
        panel.add(revenuePanel);
        panel.add(transactionsPanel);
        
        return panel;
    }
    
    /**
     * Creates the table panel for top 3 products.
     */
    private JPanel createTopProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Top 3 Best-Selling Items"));
        panel.setPreferredSize(new Dimension(0, 200));
        
        String[] columns = {"Rank", "Product", "Units Sold"};
        topProductsModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(topProductsModel);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Loads report data based on selected period.
     */
    private void loadReportData() {
        if (controller == null || controller.getCafeSystem() == null) {
            showEmptyData();
            return;
        }
        
        Report report = null;
        String selectedPeriod = (String) reportPeriodCombo.getSelectedItem();
        
        if (selectedPeriod.contains("Daily")) {
            report = controller.getCafeSystem().getDailyReport();
        } else if (selectedPeriod.contains("Weekly")) {
            report = controller.getCafeSystem().getWeeklyReport();
        } else if (selectedPeriod.contains("Monthly")) {
            report = controller.getCafeSystem().getMonthlyReport();
        }
        
        if (report != null) {
            lblRevenueValue.setText(String.format("R$ %.2f", report.getTotalRevenue()));
            lblTransactionsValue.setText(String.valueOf(report.getTransactionCount()));
            updateTopProductsTable(report); // Pass the entire report, not just the list
        } else {
            showEmptyData();
        }
    }
    
    /**
     * Updates the top products table using the report to get units sold.
     * @param report The report containing sales data
     */
    private void updateTopProductsTable(Report report) {
        topProductsModel.setRowCount(0);
        
        List<Product> topProducts = report.getTopThreeItems();
        
        if (topProducts == null || topProducts.isEmpty()) {
            topProductsModel.addRow(new Object[]{"-", "No sales data", "-"});
            return;
        }
        
        String[] medals = {"🥇 1st", "🥈 2nd", "🥉 3rd"};
        
        for (int i = 0; i < topProducts.size() && i < 3; i++) {
            Product product = topProducts.get(i);
            int unitsSold = report.getProductSalesCount(product); // Now using the report!
            
            topProductsModel.addRow(new Object[]{
                medals[i],
                product.getName(),
                unitsSold > 0 ? unitsSold : "N/A"
            });
        }
    }
    
    /**
     * Shows empty data when controller is not available.
     */
    private void showEmptyData() {
        lblRevenueValue.setText("R$ 0.00");
        lblTransactionsValue.setText("0");
        topProductsModel.setRowCount(0);
        topProductsModel.addRow(new Object[]{"-", "No data available", "-"});
    }
}