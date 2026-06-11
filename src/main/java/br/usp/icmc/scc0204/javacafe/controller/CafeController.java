package main.java.br.usp.icmc.scc0204.javacafe.controller;

import main.java.br.usp.icmc.scc0204.javacafe.model.*;
import main.java.br.usp.icmc.scc0204.javacafe.exceptions.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

public class CafeController {
    
    private CafeSystemContract cafeSystem;
    private Order currentOrder;
    private PropertyChangeSupport propertyChangeSupport;
    
    // Event property names
    public static final String INVENTORY_UPDATED = "inventoryUpdated";
    public static final String ORDER_FINALIZED = "orderFinalized";

    public CafeController(CafeSystemContract cafeSystem) {
        this.cafeSystem = cafeSystem;
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }
    
    /**
     * Adds a property change listener to listen for inventory updates.
     * @param listener The listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }
    
    /**
     * Removes a property change listener.
     * @param listener The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
    
    /**
     * Notifies all listeners that inventory has been updated.
     */
    private void fireInventoryUpdated() {
        propertyChangeSupport.firePropertyChange(INVENTORY_UPDATED, false, true);
    }
    
    /**
     * Notifies all listeners that an order has been finalized.
     */
    private void fireOrderFinalized(Order order) {
        propertyChangeSupport.firePropertyChange(ORDER_FINALIZED, null, order);
    }

    // Command Orders

    public void startNewOrder() {
        this.currentOrder = cafeSystem.createOrder();
    }

    public void addItemToCurrentOrder(Product product, int quantity) throws OutOfStockException {
        if (this.currentOrder == null) {
            throw new IllegalStateException("Abra um novo pedido antes de adicionar itens.");
        }
        cafeSystem.addItemToOrder(this.currentOrder, product, quantity);
    }

    public void removeItemFromCurrentOrder(Product product, int quantity) {
        if (this.currentOrder != null) {
            cafeSystem.removeItemFromOrder(this.currentOrder, product, quantity);
        }
    }

    public String finalizeCurrentOrder(PaymentMethod method) throws InvalidPaymentException, OutOfStockException, EmptyOrderException {
        if (this.currentOrder == null) {
            throw new IllegalStateException("Não há nenhum pedido em andamento para finalizar.");
        }
        
        cafeSystem.finalizeOrder(this.currentOrder, method);
        String receipt = cafeSystem.generateReceipt(this.currentOrder);
        
        // Notify listeners that inventory has changed
        fireInventoryUpdated();
        fireOrderFinalized(this.currentOrder);
        
        // Clear for next customer
        this.currentOrder = null; 
        
        return receipt;
    }

    public Order getCurrentOrder() {
        return this.currentOrder;
    }

    // Inventory

    public List<Product> getAvailableProducts() {
        return cafeSystem.getAllProducts();
    }

    public void registerNewProduct(Product product) {
        cafeSystem.addProduct(product);
        fireInventoryUpdated(); // Notify after adding product
    }
    
    /**
     * Updates an existing product's details (e.g., name, price, stock).
     * Dispatches an inventory update event to refresh the UI.
     * * @param product The product containing the updated information.
     */
    public void updateProduct(Product product) {
        cafeSystem.updateProduct(product);
        fireInventoryUpdated(); // Notify UI to refresh after updating details
    }
    
    public void updateStock(String productId, int newQuantity) {
        cafeSystem.updateStock(productId, newQuantity);
        fireInventoryUpdated(); // Notify after updating stock
    }

    public CafeSystemContract getCafeSystem() {
        return this.cafeSystem;
    }
}