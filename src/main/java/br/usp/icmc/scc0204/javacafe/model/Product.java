package main.java.br.usp.icmc.scc0204.javacafe.model;

import main.java.br.usp.icmc.scc0204.javacafe.exceptions.OutOfStockException;

/**
 * Represents a product in the Java Café inventory system.
 */
public class Product {
    
    private final String id;  // Immutable - once set, cannot change
    private String name;
    private double price;
    private int stockQuantity;
    private int lowStockThreshold = 5;

    public Product(String id, String name, double price, int stockQuantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getStockQuantity() { return stockQuantity; }
    public int getLowStockThreshold() { return lowStockThreshold; }
    
    // Setters (except for id - it cannot change)
    public void setName(String name) { this.name = name; }
    public void setPrice(double price) { this.price = price; }
    public void setStockQuantity(int quantity) { this.stockQuantity = quantity; }
    public void setLowStockThreshold(int threshold) { this.lowStockThreshold = threshold; }

    /**
     * Decreases stock by the specified quantity.
     * @throws OutOfStockException if insufficient stock
     */
    public void decreaseStock(int quantity) throws OutOfStockException {
        if (quantity > this.stockQuantity) {
            throw new OutOfStockException("Insufficient stock for: " + this.name);
        }
        this.stockQuantity -= quantity;
    }

    /**
     * Returns true if stock is below or equal to the low stock threshold.
     */
    public boolean isStockLow() {
        return this.stockQuantity <= this.lowStockThreshold;
    }
}