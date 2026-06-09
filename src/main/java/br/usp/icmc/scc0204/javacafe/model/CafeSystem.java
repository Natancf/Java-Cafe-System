package main.java.br.usp.icmc.scc0204.javacafe.model;

import main.java.br.usp.icmc.scc0204.javacafe.exceptions.*;
import java.util.ArrayList;
import java.util.List;

public class CafeSystem implements CafeSystemContract {
    private List<Product> inventory = new ArrayList<>();

    // Order Processing
    @Override public Order createOrder() { return new Order("TEMP-001", 1); }
    @Override public void addItemToOrder(Order order, Product product, int quantity) {}
    @Override public void removeItemFromOrder(Order order, Product product, int quantity) {}
    @Override public double calculateSubtotal(Order order) { return 0.0; }
    @Override public double calculateTax(Order order) { return 0.0; }
    @Override public double calculateTotal(Order order) { return 0.0; }
    @Override public void finalizeOrder(Order order, PaymentMethod paymentMethod) {}
    @Override public String generateReceipt(Order order) { return ""; }

    // Inventory Management
    @Override public void addProduct(Product product) { inventory.add(product); }
    @Override public void updateStock(String productId, int newQuantity) {}
    @Override public List<Product> getAllProducts() { return inventory; }
    @Override public List<Product> getLowStockProducts(int threshold) { return new ArrayList<>(); }

    // Sales Reporting
    @Override public Report getDailyReport() { return null; }
    @Override public Report getWeeklyReport() { return null; }
    @Override public Report getMonthlyReport() { return null; }

    // Data Persistence
    @Override public void saveData() {}
    @Override public void loadData() {}
}