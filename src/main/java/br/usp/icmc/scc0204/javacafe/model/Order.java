package main.java.br.usp.icmc.scc0204.javacafe.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a customer order in the Java Café POS system.
 * Contains items, totals, and order status.
 */
public class Order {
    
    private static final double TAX_RATE = 0.10; // 10% tax
    
    private final String orderId;      // Unique order identifier (immutable)
    private final int orderNumber;     // Sequential order number
    private final Map<Product, Integer> items; // Product -> Quantity
    private boolean finalized;

    public Order(String orderId, int orderNumber) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.items = new HashMap<>();
        this.finalized = false;
    }

    /**
     * Adds a product to the order or increases its quantity.
     * @param product Product to add
     * @param quantity Quantity to add (must be positive)
     */
    public void addItem(Product product, int quantity) {
        if (quantity <= 0) return;
        this.items.put(product, this.items.getOrDefault(product, 0) + quantity);
    }

    /**
     * Removes a quantity of a product from the order.
     * If quantity reaches zero, product is completely removed.
     * @param product Product to remove
     * @param quantity Quantity to remove
     */
    public void removeItem(Product product, int quantity) {
        if (!this.items.containsKey(product) || quantity <= 0) return;
        
        int current = this.items.get(product);
        int newQuantity = current - quantity;
        
        if (newQuantity <= 0) {
            this.items.remove(product);
        } else {
            this.items.put(product, newQuantity);
        }
    }

    /**
     * Calculates subtotal (sum of all item prices × quantities).
     * @return Subtotal amount
     */
    public double getSubtotal() {
        double subtotal = 0.0;
        for (Map.Entry<Product, Integer> entry : items.entrySet()) {
            subtotal += entry.getKey().getPrice() * entry.getValue();
        }
        return subtotal;
    }

    /**
     * Calculates tax based on subtotal.
     * @return Tax amount
     */
    public double getTax() {
        return getSubtotal() * TAX_RATE;
    }

    /**
     * Calculates final total (subtotal + tax).
     * @return Total amount
     */
    public double getTotal() {
        return getSubtotal() + getTax();
    }

    /**
     * Returns the number of distinct items in the order.
     * @return Item count
     */
    public int getItemCount() {
        return items.size();
    }

    // Getters
    public String getOrderId() { return orderId; }
    public int getOrderNumber() { return orderNumber; }
    public Map<Product, Integer> getItems() { return items; }
    public boolean isFinalized() { return finalized; }
    
    // Setter
    public void setFinalized(boolean finalized) { this.finalized = finalized; }
}