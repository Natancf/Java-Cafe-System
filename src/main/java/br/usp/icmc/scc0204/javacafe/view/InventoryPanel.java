package main.java.br.usp.icmc.scc0204.javacafe.view;

import main.java.br.usp.icmc.scc0204.javacafe.model.Product;
import main.java.br.usp.icmc.scc0204.javacafe.controller.CafeController;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * Inventory Management Panel for Java Café POS System.
 * Allows staff to view products, update stock levels, and add new products.
 * Displays warning when stock falls below configurable threshold.
 */
public class InventoryPanel extends JPanel {
    
    // Configuration
    private static final int LOW_STOCK_THRESHOLD = 5;
    private static final String IMAGES_DIR = "data/product_images/";
    
    // Dependencies
    private final CafeController controller;
    
    // UI Components
    private DefaultTableModel inventoryModel;
    private JTable inventoryTable;
    private JLabel lblLowStockWarning;
    private JSpinner spinnerThreshold;
    
    // Dialog components
    private JDialog addProductDialog;
    private JTextField txtProductId;
    private JTextField txtProductName;
    private JTextField txtProductPrice;
    private JTextField txtProductStock;
    private JLabel lblImagePreview;
    private JLabel lblImagePath;
    private File selectedImageFile;
    
    // Listener for inventory updates
    private PropertyChangeListener inventoryListener;
    
    /**
     * Constructor with controller injection.
     * @param controller The cafe controller instance
     */
    public InventoryPanel(CafeController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Create images directory if it doesn't exist
        createImagesDirectory();
        
        initializeComponents();
        setupInventoryListener();
        refreshInventoryTable();
        updateLowStockWarning();
    }
    
    /**
     * Creates the product images directory if it doesn't exist.
     */
    private void createImagesDirectory() {
        File dir = new File(IMAGES_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    /**
     * Generates a unique product ID following the pattern "PROD-X".
     * Where X is a sequential number based on existing products.
     * Examples: PROD-1, PROD-2, PROD-7, PROD-15
     * 
     * @return A unique product ID (e.g., "PROD-7")
     */
    private String generateProductId() {
        List<Product> products = controller.getAvailableProducts();
        String prefix = "PROD-";
        int maxNumber = 0;
        
        if (products.isEmpty()) {
            return prefix + "1";
        }
        
        // Find the highest existing product number
        for (Product p : products) {
            String id = p.getId();
            
            // Check if ID follows the "PROD-X" pattern
            if (id != null && id.startsWith(prefix)) {
                try {
                    // Extract number after "PROD-"
                    String numberPart = id.substring(prefix.length());
                    int num = Integer.parseInt(numberPart);
                    if (num > maxNumber) {
                        maxNumber = num;
                    }
                } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                    // If ID doesn't follow the pattern, skip it
                    System.err.println("Skipping non-standard ID: " + id);
                }
            } else if (id != null) {
                // Try to extract any number from non-standard IDs as fallback
                try {
                    String numberPart = id.replaceAll("[^0-9]", "");
                    if (!numberPart.isEmpty()) {
                        int num = Integer.parseInt(numberPart);
                        if (num > maxNumber) {
                            maxNumber = num;
                        }
                    }
                } catch (NumberFormatException e) {
                    // Ignore - can't extract number
                }
            }
        }
        
        // Generate next sequential number
        int nextNumber = maxNumber + 1;
        return prefix + nextNumber;
    }
    
    /**
     * Sets up a listener to refresh the inventory when changes occur.
     */
    private void setupInventoryListener() {
        inventoryListener = evt -> {
            if (CafeController.INVENTORY_UPDATED.equals(evt.getPropertyName())) {
                SwingUtilities.invokeLater(() -> {
                    refreshInventoryTable();
                    updateLowStockWarning();
                });
            }
        };
        controller.addPropertyChangeListener(inventoryListener);
    }
    
    /**
     * Initializes all UI components.
     */
    private void initializeComponents() {
        // Top panel: Controls (threshold and add button)
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel: Inventory table
        JPanel tablePanel = createInventoryTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // Bottom panel: Warning messages
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Initialize add product dialog
        createAddProductDialog();
    }
    
    /**
     * Creates the top panel with threshold control and add button.
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        // Left side: Low stock threshold configuration
        JPanel thresholdPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        thresholdPanel.add(new JLabel("Low Stock Alert Threshold:"));
        
        spinnerThreshold = new JSpinner(new SpinnerNumberModel(LOW_STOCK_THRESHOLD, 1, 50, 1));
        spinnerThreshold.addChangeListener(e -> refreshInventoryTable());
        thresholdPanel.add(spinnerThreshold);
        
        thresholdPanel.add(Box.createHorizontalStrut(20));
        thresholdPanel.add(new JLabel("(Stock ≤ threshold = Warning)"));
        
        // Right side: Add product button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAddProduct = new JButton("+ Add New Product");
        btnAddProduct.setBackground(new Color(46, 204, 113));
        btnAddProduct.setForeground(Color.WHITE);
        btnAddProduct.setFocusPainted(false);
        btnAddProduct.addActionListener(e -> showAddProductDialog());
        buttonPanel.add(btnAddProduct);
        
        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> {
            refreshInventoryTable();
            updateLowStockWarning();
        });
        buttonPanel.add(btnRefresh);
        
        panel.add(thresholdPanel, BorderLayout.WEST);
        panel.add(buttonPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Creates the inventory table panel.
     */
    private JPanel createInventoryTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Product Inventory"));
        
        // Table columns
        String[] columns = {"ID", "Product Name", "Price (R$)", "Stock", "Status"};
        inventoryModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;
            }
        };
        
        inventoryTable = new JTable(inventoryModel);
        inventoryTable.setRowHeight(28);
        inventoryTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        inventoryTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        inventoryTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        inventoryTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        inventoryTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        inventoryTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        
        // Custom renderer for status column
        inventoryTable.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());
        
        // Handle stock updates when user edits the stock column
        inventoryTable.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 3 && e.getFirstRow() >= 0) {
                int row = e.getFirstRow();
                String productId = (String) inventoryModel.getValueAt(row, 0);
                Object stockValue = inventoryModel.getValueAt(row, 3);
                
                try {
                    int newStock = Integer.parseInt(stockValue.toString());
                    if (newStock >= 0) {
                        controller.updateStock(productId, newStock);
                    } else {
                        JOptionPane.showMessageDialog(InventoryPanel.this,
                            "Stock cannot be negative.", "Invalid Input",
                            JOptionPane.ERROR_MESSAGE);
                        refreshInventoryTable();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(InventoryPanel.this,
                        "Please enter a valid number for stock.", "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                    refreshInventoryTable();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Help text
        JLabel lblHelp = new JLabel("Tip: Double-click the Stock column to edit stock quantity.");
        lblHelp.setFont(new Font("Arial", Font.ITALIC, 10));
        lblHelp.setForeground(Color.GRAY);
        panel.add(lblHelp, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Creates the bottom panel with warning messages.
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(231, 76, 60), 1),
            "Alerts",
            TitledBorder.CENTER,
            TitledBorder.TOP
        ));
        panel.setPreferredSize(new Dimension(0, 80));
        
        lblLowStockWarning = new JLabel();
        lblLowStockWarning.setFont(new Font("Arial", Font.BOLD, 12));
        lblLowStockWarning.setForeground(new Color(231, 76, 60));
        
        JTextArea txtHelp = new JTextArea(
            "• Products with stock below the threshold will appear with a warning status.\n" +
            "• Double-click the Stock column to update inventory levels.\n" +
            "• Use 'Add New Product' to expand your menu."
        );
        txtHelp.setEditable(false);
        txtHelp.setBackground(null);
        txtHelp.setFont(new Font("Arial", Font.PLAIN, 10));
        txtHelp.setForeground(Color.GRAY);
        
        panel.add(lblLowStockWarning, BorderLayout.NORTH);
        panel.add(txtHelp, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Creates the dialog for adding new products with image support.
     */
    private void createAddProductDialog() {
        addProductDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Product", true);
        addProductDialog.setLayout(new BorderLayout(10, 10));
        addProductDialog.setSize(500, 550);
        addProductDialog.setLocationRelativeTo(this);
        
        // Main form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Product ID (auto-generated, non-editable)
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Product ID:"), gbc);
        gbc.gridx = 1;
        txtProductId = new JTextField(15);
        txtProductId.setEditable(false);
        txtProductId.setBackground(new Color(240, 240, 240));
        formPanel.add(txtProductId, gbc);
        
        // Product Name
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Product Name:*"), gbc);
        gbc.gridx = 1;
        txtProductName = new JTextField(15);
        formPanel.add(txtProductName, gbc);
        
        // Price
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Price (R$):*"), gbc);
        gbc.gridx = 1;
        txtProductPrice = new JTextField(15);
        formPanel.add(txtProductPrice, gbc);
        
        // Initial Stock
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Initial Stock:*"), gbc);
        gbc.gridx = 1;
        txtProductStock = new JTextField(15);
        formPanel.add(txtProductStock, gbc);
        
        // Product Image section
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2;
        JPanel imagePanel = createImageSelectionPanel();
        formPanel.add(imagePanel, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnSave = new JButton("Save Product");
        btnSave.setBackground(new Color(46, 204, 113));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(this::saveNewProduct);
        
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> addProductDialog.setVisible(false));
        
        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);
        
        addProductDialog.add(formPanel, BorderLayout.CENTER);
        addProductDialog.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the image selection panel with preview and file chooser.
     */
    private JPanel createImageSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Product Image (Optional)"));
        
        // Image preview label
        lblImagePreview = new JLabel();
        lblImagePreview.setPreferredSize(new Dimension(120, 120));
        lblImagePreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagePreview.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        lblImagePreview.setBackground(Color.WHITE);
        lblImagePreview.setOpaque(true);
        lblImagePreview.setText("No Image\nSelected");
        lblImagePreview.setFont(new Font("Arial", Font.ITALIC, 10));
        lblImagePreview.setForeground(Color.GRAY);
        
        // Image path label
        lblImagePath = new JLabel("No file selected");
        lblImagePath.setFont(new Font("Arial", Font.PLAIN, 10));
        lblImagePath.setForeground(Color.GRAY);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnSelectImage = new JButton("📷 Select Image");
        btnSelectImage.addActionListener(e -> selectProductImage());
        
        JButton btnClearImage = new JButton("Clear");
        btnClearImage.addActionListener(e -> clearSelectedImage());
        
        buttonPanel.add(btnSelectImage);
        buttonPanel.add(btnClearImage);
        
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.add(lblImagePath, BorderLayout.CENTER);
        infoPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        panel.add(lblImagePreview, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Opens file chooser for product image selection.
     */
    private void selectProductImage() {
        JFileChooser fileChooser = new JFileChooser(".");
        fileChooser.setDialogTitle("Select Product Image");
        fileChooser.setFileFilter(new FileNameExtensionFilter(
            "Image Files (JPG, PNG, JPEG)", "jpg", "jpeg", "png"));
        
        int result = fileChooser.showOpenDialog(addProductDialog);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = fileChooser.getSelectedFile();
            
            // Update path label
            lblImagePath.setText(selectedImageFile.getName());
            
            // Show preview
            try {
                ImageIcon icon = new ImageIcon(selectedImageFile.getPath());
                Image scaledImage = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                lblImagePreview.setIcon(new ImageIcon(scaledImage));
                lblImagePreview.setText("");
            } catch (Exception ex) {
                lblImagePreview.setIcon(null);
                lblImagePreview.setText("Preview\nError");
                JOptionPane.showMessageDialog(addProductDialog,
                    "Could not load image preview.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Clears the selected image.
     */
    private void clearSelectedImage() {
        selectedImageFile = null;
        lblImagePath.setText("No file selected");
        lblImagePreview.setIcon(null);
        lblImagePreview.setText("No Image\nSelected");
    }
    
    /**
     * Saves the selected image to the product images directory.
     * @param productId The product ID to use as filename
     * @return true if image was saved successfully, false otherwise
     */
    private boolean saveProductImage(String productId) {
        if (selectedImageFile == null) {
            return true; // No image to save
        }
        
        try {
            // Determine file extension from original file
            String fileName = selectedImageFile.getName();
            String extension = fileName.substring(fileName.lastIndexOf('.'));
            
            // Validate extension
            if (!extension.equalsIgnoreCase(".jpg") && 
                !extension.equalsIgnoreCase(".jpeg") && 
                !extension.equalsIgnoreCase(".png")) {
                JOptionPane.showMessageDialog(addProductDialog,
                    "Only JPG, JPEG, and PNG images are supported.",
                    "Invalid Format", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            // Target path: data/product_images/{productId}{extension}
            String targetFileName = productId + extension.toLowerCase();
            Path targetPath = Paths.get(IMAGES_DIR, targetFileName);
            
            // Copy file (overwrite if exists)
            Files.copy(selectedImageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            return true;
            
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(addProductDialog,
                "Error saving image: " + ex.getMessage(),
                "Image Save Error", JOptionPane.WARNING_MESSAGE);
            return false; // Image failed but product can still be saved
        }
    }
    
    /**
     * Shows the add product dialog with auto-generated ID.
     */
    private void showAddProductDialog() {
        // Generate and set auto product ID
        String newId = generateProductId();
        txtProductId.setText(newId);
        
        // Clear form fields
        txtProductName.setText("");
        txtProductPrice.setText("");
        txtProductStock.setText("");
        clearSelectedImage();
        
        addProductDialog.setVisible(true);
    }
    
    /**
     * Saves a new product to the inventory.
     */
    private void saveNewProduct(ActionEvent e) {
        try {
            String id = txtProductId.getText().trim();
            String name = txtProductName.getText().trim();
            double price = Double.parseDouble(txtProductPrice.getText().trim());
            int stock = Integer.parseInt(txtProductStock.getText().trim());
            
            // Validation
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(addProductDialog,
                    "Product Name is required.", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (price < 0) {
                JOptionPane.showMessageDialog(addProductDialog,
                    "Price cannot be negative.", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (stock < 0) {
                JOptionPane.showMessageDialog(addProductDialog,
                    "Stock cannot be negative.", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check for duplicate ID (should not happen with auto-generation)
            for (Product p : controller.getCafeSystem().getAllProducts()) {
                if (p.getId().equals(id)) {
                    JOptionPane.showMessageDialog(addProductDialog,
                        "Product ID already exists. Please try again.",
                        "Duplicate ID", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            // Save product image if selected
            saveProductImage(id);
            
            // Create and save product
            Product newProduct = new Product(id, name, price, stock);
            controller.registerNewProduct(newProduct);
            
            JOptionPane.showMessageDialog(addProductDialog,
                "Product '" + name + "' added successfully!\n" +
                "Product ID: " + id,
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            addProductDialog.setVisible(false);
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(addProductDialog,
                "Please enter valid numbers for Price and Stock.",
                "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Refreshes the inventory table with current data.
     */
    private void refreshInventoryTable() {
        inventoryModel.setRowCount(0);
        
        if (controller == null || controller.getCafeSystem() == null) {
            inventoryModel.addRow(new Object[]{"-", "System unavailable", "-", "-", "Error"});
            return;
        }
        
        List<Product> products = controller.getAvailableProducts();
        int threshold = (int) spinnerThreshold.getValue();
        
        for (Product product : products) {
            int stock = product.getStockQuantity();
            String status;
            
            if (stock <= 0) {
                status = "❌ OUT OF STOCK";
            } else if (stock <= threshold) {
                status = "⚠️ LOW STOCK";
            } else {
                status = "✓ OK";
            }
            
            inventoryModel.addRow(new Object[]{
                product.getId(),
                product.getName(),
                String.format("R$ %.2f", product.getPrice()),
                stock,
                status
            });
        }
    }
    
    /**
     * Updates the low stock warning message.
     */
    private void updateLowStockWarning() {
        if (controller == null || controller.getCafeSystem() == null) {
            lblLowStockWarning.setText("⚠️ System not available.");
            return;
        }
        
        int threshold = (int) spinnerThreshold.getValue();
        List<Product> lowStockProducts = controller.getCafeSystem().getLowStockProducts(threshold);
        
        if (lowStockProducts.isEmpty()) {
            lblLowStockWarning.setText("✓ All products are above the low stock threshold.");
            lblLowStockWarning.setForeground(new Color(46, 204, 113));
        } else {
            StringBuilder warning = new StringBuilder("⚠️ WARNING: ");
            warning.append(lowStockProducts.size()).append(" product(s) below threshold");
            warning.append(" (≤ ").append(threshold).append(" units): ");
            
            for (int i = 0; i < Math.min(3, lowStockProducts.size()); i++) {
                if (i > 0) warning.append(", ");
                warning.append(lowStockProducts.get(i).getName());
                warning.append(" (").append(lowStockProducts.get(i).getStockQuantity()).append(")");
            }
            
            if (lowStockProducts.size() > 3) {
                warning.append(", and ").append(lowStockProducts.size() - 3).append(" more.");
            }
            
            lblLowStockWarning.setText(warning.toString());
            lblLowStockWarning.setForeground(new Color(231, 76, 60));
        }
    }
    
    /**
     * Clean up listener when panel is disposed.
     */
    @Override
    public void removeNotify() {
        if (inventoryListener != null) {
            controller.removePropertyChangeListener(inventoryListener);
        }
        super.removeNotify();
    }
    
    /**
     * Custom cell renderer for the status column.
     */
    private class StatusCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value != null && !isSelected) {
                String status = value.toString();
                if (status.contains("OUT")) {
                    c.setForeground(Color.RED);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (status.contains("LOW")) {
                    c.setForeground(new Color(230, 126, 34));
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (status.contains("OK")) {
                    c.setForeground(new Color(46, 204, 113));
                }
            }
            
            return c;
        }
    }
}