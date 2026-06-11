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

/**
 * Inventory Management Panel for Java Café POS System.
 * Allows staff to view products, update stock levels, add new products, and edit existing ones.
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
    
    // Dialog components (Shared for Add and Edit actions)
    private JDialog productDialog;
    private JTextField txtProductId;
    private JTextField txtProductName;
    private JTextField txtProductPrice;
    private JTextField txtProductStock;
    private JLabel lblImagePreview;
    private JLabel lblImagePath;
    private JButton btnSaveProduct;
    private File selectedImageFile;
    
    // State control flag
    private boolean isEditMode = false;
    
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
     * * @return A unique product ID (e.g., "PROD-7")
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
            
            if (id != null && id.startsWith(prefix)) {
                try {
                    String numberPart = id.substring(prefix.length());
                    int num = Integer.parseInt(numberPart);
                    if (num > maxNumber) {
                        maxNumber = num;
                    }
                } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                    System.err.println("Skipping non-standard ID: " + id);
                }
            } else if (id != null) {
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
        // Top panel: Controls (threshold, add and edit buttons)
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel: Inventory table
        JPanel tablePanel = createInventoryTablePanel();
        add(tablePanel, BorderLayout.CENTER);
        
        // Bottom panel: Warning messages
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Initialize the shared product dialog
        createProductDialog();
    }
    
    /**
     * Creates the top panel with threshold control and action buttons.
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
        
        // Right side: Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton btnAddProduct = new JButton("+ Add New Product");
        btnAddProduct.setBackground(new Color(46, 204, 113));
        btnAddProduct.setForeground(Color.WHITE);
        btnAddProduct.setFocusPainted(false);
        btnAddProduct.addActionListener(e -> showProductForm(null));
        buttonPanel.add(btnAddProduct);
        
        JButton btnEditProduct = new JButton("✏ Edit Product");
        btnEditProduct.setBackground(new Color(52, 152, 219));
        btnEditProduct.setForeground(Color.WHITE);
        btnEditProduct.setFocusPainted(false);
        btnEditProduct.addActionListener(e -> handleEditAction());
        buttonPanel.add(btnEditProduct);
        
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
     * Checks selection and prepares data to open the dialog in Edit Mode.
     */
    private void handleEditAction() {
        int selectedRow = inventoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a product from the table to edit.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String productId = (String) inventoryModel.getValueAt(selectedRow, 0);
        Product productToEdit = null;
        
        // Find the selected product instance
        for (Product p : controller.getAvailableProducts()) {
            if (p.getId().equals(productId)) {
                productToEdit = p;
                break;
            }
        }
        
        if (productToEdit != null) {
            showProductForm(productToEdit);
        }
    }
    
    /**
     * Creates the inventory table panel.
     */
    private JPanel createInventoryTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Product Inventory"));
        
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
        
        inventoryTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        inventoryTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        inventoryTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        inventoryTable.getColumnModel().getColumn(3).setPreferredWidth(80);
        inventoryTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        
        inventoryTable.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());
        
        // Handle inline stock updates
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
        
        JLabel lblHelp = new JLabel("Tip: Select a row and click 'Edit Product', or double-click the Stock column to edit stock directly.");
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
            "• Use 'Add New Product' to expand your menu or 'Edit Product' to change existing values."
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
     * Creates the shared dialog layout used for both adding and editing products.
     */
    private void createProductDialog() {
        productDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "", true);
        productDialog.setLayout(new BorderLayout(10, 10));
        productDialog.setSize(500, 550);
        productDialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Product ID (Always non-editable)
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
        
        // Initial / Current Stock
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Stock:*"), gbc);
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
        btnSaveProduct = new JButton();
        btnSaveProduct.setBackground(new Color(46, 204, 113));
        btnSaveProduct.setForeground(Color.WHITE);
        btnSaveProduct.setFocusPainted(false);
        btnSaveProduct.addActionListener(this::saveProduct);
        
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> productDialog.setVisible(false));
        
        buttonPanel.add(btnSaveProduct);
        buttonPanel.add(btnCancel);
        
        productDialog.add(formPanel, BorderLayout.CENTER);
        productDialog.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the image selection panel with preview and file chooser.
     */
    private JPanel createImageSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Product Image (Optional)"));
        
        lblImagePreview = new JLabel();
        lblImagePreview.setPreferredSize(new Dimension(120, 120));
        lblImagePreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagePreview.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        lblImagePreview.setBackground(Color.WHITE);
        lblImagePreview.setOpaque(true);
        
        lblImagePath = new JLabel();
        lblImagePath.setFont(new Font("Arial", Font.PLAIN, 10));
        lblImagePath.setForeground(Color.GRAY);
        
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
        
        int result = fileChooser.showOpenDialog(productDialog);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedImageFile = fileChooser.getSelectedFile();
            lblImagePath.setText(selectedImageFile.getName());
            updateImagePreview(selectedImageFile.getPath());
        }
    }
    
    /**
     * Updates the image component with a scaled preview.
     */
    private void updateImagePreview(String path) {
        try {
            ImageIcon icon = new ImageIcon(path);
            Image scaledImage = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            lblImagePreview.setIcon(new ImageIcon(scaledImage));
            lblImagePreview.setText("");
        } catch (Exception ex) {
            lblImagePreview.setIcon(null);
            lblImagePreview.setText("Preview\nError");
        }
    }
    
    /**
     * Clears the selected image tracking and UI preview.
     */
    private void clearSelectedImage() {
        selectedImageFile = null;
        lblImagePath.setText("No file selected");
        lblImagePreview.setIcon(null);
        lblImagePreview.setText("No Image\nSelected");
        lblImagePreview.setFont(new Font("Arial", Font.ITALIC, 10));
        lblImagePreview.setForeground(Color.GRAY);
    }
    
    /**
     * Searches for an existing image in the local directory for a given product ID.
     */
    private File findExistingImage(String productId) {
        String[] extensions = {".jpg", ".jpeg", ".png"};
        for (String ext : extensions) {
            File file = new File(IMAGES_DIR + productId + ext);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }
    
    /**
     * Saves the selected image to the product images directory.
     * @return true if image was saved successfully, false otherwise
     */
    private boolean saveProductImage(String productId) {
        if (selectedImageFile == null) {
            return true; // No new image to save
        }
        
        try {
            String fileName = selectedImageFile.getName();
            String extension = fileName.substring(fileName.lastIndexOf('.'));
            
            if (!extension.equalsIgnoreCase(".jpg") && 
                !extension.equalsIgnoreCase(".jpeg") && 
                !extension.equalsIgnoreCase(".png")) {
                JOptionPane.showMessageDialog(productDialog,
                    "Only JPG, JPEG, and PNG images are supported.",
                    "Invalid Format", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            
            String targetFileName = productId + extension.toLowerCase();
            Path targetPath = Paths.get(IMAGES_DIR, targetFileName);
            
            Files.copy(selectedImageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return true;
            
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(productDialog,
                "Error saving image: " + ex.getMessage(),
                "Image Save Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }
    
    /**
     * Configures and displays the product form dialog.
     * @param product The product to edit, or null to create a new one.
     */
    private void showProductForm(Product product) {
        isEditMode = (product != null);
        
        // Dynamically adjust titles and buttons depending on mode
        productDialog.setTitle(isEditMode ? "Edit Product" : "Add New Product");
        btnSaveProduct.setText(isEditMode ? "Update Product" : "Save Product");
        btnSaveProduct.setBackground(isEditMode ? new Color(52, 152, 219) : new Color(46, 204, 113));
        
        if (isEditMode) {
            // Fill fields with existing data
            txtProductId.setText(product.getId());
            txtProductName.setText(product.getName());
            txtProductPrice.setText(String.valueOf(product.getPrice()));
            txtProductStock.setText(String.valueOf(product.getStockQuantity()));
            
            selectedImageFile = null;
            File existingImage = findExistingImage(product.getId());
            if (existingImage != null) {
                lblImagePath.setText(existingImage.getName());
                updateImagePreview(existingImage.getPath());
            } else {
                clearSelectedImage();
            }
        } else {
            // Generate standard inputs for clean new item
            txtProductId.setText(generateProductId());
            txtProductName.setText("");
            txtProductPrice.setText("");
            txtProductStock.setText("");
            clearSelectedImage();
        }
        
        productDialog.setVisible(true);
    }
    
    /**
     * Processes saving or updating a product based on the current mode.
     */
    private void saveProduct(ActionEvent e) {
        try {
            String id = txtProductId.getText().trim();
            String name = txtProductName.getText().trim();
            double price = Double.parseDouble(txtProductPrice.getText().trim());
            int stock = Integer.parseInt(txtProductStock.getText().trim());
            
            // Common Validation
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(productDialog,
                    "Product Name is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (price < 0 || stock < 0) {
                JOptionPane.showMessageDialog(productDialog,
                    "Price and Stock cannot be negative.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Save or replace image if selected
            saveProductImage(id);
            Product targetProduct = new Product(id, name, price, stock);
            
            if (isEditMode) {
                // Update existing product logic
                controller.updateProduct(targetProduct);
                
                JOptionPane.showMessageDialog(productDialog,
                    "Product '" + name + "' updated successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Check for duplicate ID (Safety fallback)
                for (Product p : controller.getCafeSystem().getAllProducts()) {
                    if (p.getId().equals(id)) {
                        JOptionPane.showMessageDialog(productDialog,
                            "Product ID already exists. Please try again.",
                            "Duplicate ID", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                
                controller.registerNewProduct(targetProduct);
                
                JOptionPane.showMessageDialog(productDialog,
                    "Product '" + name + "' added successfully!\nProduct ID: " + id,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            
            productDialog.setVisible(false);
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(productDialog,
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