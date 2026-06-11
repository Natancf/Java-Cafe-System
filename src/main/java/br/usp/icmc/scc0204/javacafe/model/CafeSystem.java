package main.java.br.usp.icmc.scc0204.javacafe.model;

import main.java.br.usp.icmc.scc0204.javacafe.exceptions.*;
import main.java.br.usp.icmc.scc0204.javacafe.DataStorage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core business logic implementation for Java Café POS System.
 * Manages orders, inventory, and sales reporting.
 */
public class CafeSystem implements CafeSystemContract {

    private List<Product> inventory = new ArrayList<>();

    public CafeSystem() {
        try {
            this.inventory = DataStorage.loadInventory();
        } catch (IOException e) {
            System.err.println("Warning: Starting with empty inventory.");
        }
    }

    // ==================== ORDER PROCESSING ====================

    @Override
    public Order createOrder() {
        String orderCode = generateOrderCode();
        int orderNumber = 1;
        
        try {
            orderNumber = DataStorage.getLastOrderNumber() + 1;
        } catch (IOException e) {
            System.err.println("Error getting order number.");
        }
        
        return new Order(orderCode, orderNumber);
    }

    private String generateOrderCode() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        return "ORD-" + timestamp;
    }

    @Override
    public void addItemToOrder(Order order, Product product, int quantity) throws OutOfStockException {
        if (product.getStockQuantity() < quantity) {
            throw new OutOfStockException("Insufficient stock for: " + product.getName());
        }
        order.addItem(product, quantity);
    }

    @Override
    public void removeItemFromOrder(Order order, Product product, int quantityToRemove) {
        order.removeItem(product, quantityToRemove);
    }

    @Override
    public void finalizeOrder(Order order, PaymentMethod paymentMethod) 
            throws InvalidPaymentException, OutOfStockException, EmptyOrderException {

        if (order.getItems().isEmpty()) {
            throw new EmptyOrderException("Cannot finalize an empty order.");
        }

        // Check and deduct stock
        for (Map.Entry<Product, Integer> entry : order.getItems().entrySet()) {
            Product product = entry.getKey();
            int qty = entry.getValue();

            if (product.getStockQuantity() < qty) {
                throw new OutOfStockException("Out of stock: " + product.getName());
            }
            product.setStockQuantity(product.getStockQuantity() - qty);
        }

        order.setFinalized(true);

        try {
            DataStorage.appendOrder(order, paymentMethod.name());
            DataStorage.saveInventory(this.inventory);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save sale to CSV.", e);
        }
    }

    @Override
    public String generateReceipt(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Java Café Receipt ===\n");
        sb.append("Order #").append(order.getOrderNumber()).append("\n\n");

        for (Map.Entry<Product, Integer> entry : order.getItems().entrySet()) {
            Product p = entry.getKey();
            int qtd = entry.getValue();
            double subtotalItem = p.getPrice() * qtd;

            sb.append(String.format("%s x%d - R$ %.2f\n", p.getName(), qtd, subtotalItem));
        }

        sb.append("----------------------\n");
        sb.append(String.format("Subtotal: R$ %.2f\n", order.getSubtotal()));
        sb.append(String.format("Tax (10%%): R$ %.2f\n", order.getTax()));
        sb.append(String.format("TOTAL: R$ %.2f\n", order.getTotal()));
        sb.append("======================\n");
        sb.append("Thank you! Come again!\n");

        return sb.toString();
    }

    // ==================== INVENTORY MANAGEMENT ====================

    @Override
    public void addProduct(Product product) {
        inventory.add(product);
        try {
            DataStorage.saveInventory(inventory);
        } catch (IOException e) {
            System.err.println("Error saving inventory.");
        }
    }

    @Override
    public void updateProduct(Product updatedProduct) {
        if (updatedProduct == null || updatedProduct.getId() == null) {
            return;
        }
        
        // Find the product by ID and replace it entirely in the list
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).getId().equals(updatedProduct.getId())) {
                inventory.set(i, updatedProduct);
                break;
            }
        }
        
        // Save the updated list
        try {
            DataStorage.saveInventory(inventory);
        } catch (IOException e) {
            System.err.println("Error saving inventory after product update: " + e.getMessage());
        }
    }

    @Override
    public void updateStock(String productId, int newQuantity) {
        for (Product product : inventory) {
            if (product.getId().equals(productId)) {
                product.setStockQuantity(newQuantity);
                break;
            }
        }
        try {
            DataStorage.saveInventory(inventory);
        } catch (IOException e) {
            System.err.println("Error saving inventory.");
        }
    }

    @Override
    public List<Product> getAllProducts() {
        return inventory;
    }

    @Override
    public List<Product> getLowStockProducts(int threshold) {
        List<Product> lowStockList = new ArrayList<>();
        for (Product product : inventory) {
            if (product.getStockQuantity() < threshold) {
                lowStockList.add(product);
            }
        }
        return lowStockList;
    }

    // ==================== CALCULATIONS (delegated to Order) ====================

    @Override 
    public double calculateTotal(Order order) { 
        return order.getTotal(); 
    }
    
    @Override 
    public double calculateSubtotal(Order order) { 
        return order.getSubtotal(); 
    }
    
    @Override 
    public double calculateTax(Order order) { 
        return order.getTax(); 
    }

    // ==================== SALES REPORTING ====================

    @Override 
    public Report getDailyReport() {
        return generateFilteredReport("DAILY");
    }
    
    @Override 
    public Report getWeeklyReport() {
        return generateFilteredReport("WEEKLY");
    }
    
    @Override 
    public Report getMonthlyReport() {
        return generateFilteredReport("MONTHLY");
    }

    /**
     * Filters orders by timeframe and generates report.
     */
    private Report generateFilteredReport(String timeframe) {
        double totalRevenue = 0;
        int transactionCount = 0;
        Map<String, Integer> productSalesCount = new HashMap<>();
        LocalDate today = LocalDate.now();

        // Read and filter sales data
        try (BufferedReader reader = new BufferedReader(new FileReader("data/vendas.csv"))) {
            reader.readLine(); // Skip header
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split(",");
                if (parts.length < 3) continue;
                
                String orderId = parts[0];
                if (!isWithinTimeFrame(orderId, timeframe, today)) continue;
                
                try {
                    totalRevenue += Double.parseDouble(parts[2]);
                    transactionCount++;
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing revenue: " + parts[2]);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading sales file: " + e.getMessage());
            return new Report(0, 0, new ArrayList<>(), new HashMap<>());
        }

        // Read and filter sold items
        try (BufferedReader reader = new BufferedReader(new FileReader("data/vendas_itens.csv"))) {
            reader.readLine(); // Skip header
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split(",");
                if (parts.length < 3) continue;
                
                String orderId = parts[0];
                if (!isWithinTimeFrame(orderId, timeframe, today)) continue;
                
                try {
                    String productId = parts[1].trim();
                    int quantity = Integer.parseInt(parts[2].trim());
                    productSalesCount.merge(productId, quantity, Integer::sum);
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing quantity: " + parts[2]);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading sold items file: " + e.getMessage());
            return new Report(totalRevenue, transactionCount, new ArrayList<>(), productSalesCount);
        }

        // Get top 3 products
        List<Product> topProducts = getTopProducts(productSalesCount);
        
        return new Report(totalRevenue, transactionCount, topProducts, productSalesCount);
    }

    /**
     * Checks if an order falls within the specified timeframe.
     */
    private boolean isWithinTimeFrame(String orderId, String timeframe, LocalDate today) {
        if (orderId == null || orderId.length() < 12) return false;
        
        try {
            String dateString = orderId.substring(4, 12);
            LocalDate orderDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMdd"));

            switch (timeframe) {
                case "DAILY":
                    return orderDate.equals(today);
                case "WEEKLY":
                    return !orderDate.isBefore(today.minusDays(7)) && !orderDate.isAfter(today);
                case "MONTHLY":
                    return orderDate.getYear() == today.getYear() && 
                           orderDate.getMonthValue() == today.getMonthValue();
                default:
                    return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns top 3 products based on sales quantity.
     */
    private List<Product> getTopProducts(Map<String, Integer> productSalesCount) {
        List<Product> topProducts = new ArrayList<>();
        
        productSalesCount.entrySet().stream()
            .sorted((a, b) -> b.getValue() - a.getValue())
            .limit(3)
            .forEach(entry -> {
                for (Product p : inventory) {
                    if (p.getId().equals(entry.getKey())) {
                        topProducts.add(p);
                        break;
                    }
                }
            });
        
        return topProducts;
    }
}