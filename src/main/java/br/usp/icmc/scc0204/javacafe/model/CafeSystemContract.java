package main.java.br.usp.icmc.scc0204.javacafe.model;

import java.util.List;

import main.java.br.usp.icmc.scc0204.javacafe.exceptions.*;

/**
 * Main contract for the Java Café Point of Sale System.
 * Defines core operations for Order Processing, Inventory Management,
 * and Sales Reporting.
 */
public interface CafeSystemContract {

    // ==================== ORDER PROCESSING ====================
    
    Order createOrder();
    
    void addItemToOrder(Order order, Product product, int quantity) throws OutOfStockException;
    
    void removeItemFromOrder(Order order, Product product, int quantity);
    
    double calculateSubtotal(Order order);
    
    double calculateTax(Order order);
    
    double calculateTotal(Order order);

    void finalizeOrder(Order order, PaymentMethod paymentMethod) 
            throws InvalidPaymentException, OutOfStockException, EmptyOrderException;
    
    String generateReceipt(Order order);

    // ==================== INVENTORY MANAGEMENT ====================
    
    void addProduct(Product product);
    
    void updateStock(String productId, int newQuantity);
    
    List<Product> getAllProducts();
    
    List<Product> getLowStockProducts(int threshold);

    // ==================== SALES REPORTING ====================
    
    Report getDailyReport();
    
    Report getWeeklyReport();
    
    Report getMonthlyReport();
}