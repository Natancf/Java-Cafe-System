package main.java.br.usp.icmc.scc0204.javacafe.model;

import java.util.HashMap;
import java.util.Map;

public class Order {
    
    private String orderId;
    private int orderNumber;
    private Map<Product, Integer> items; // Armazena produto e quantidade do pedido
    private boolean finalized;

    public Order(String orderId, int orderNumber) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.items = new HashMap<>();
        this.finalized = false;
    }

    public void addItem(Product product, int quantity) {
        // Se o produto já existe no pedido, soma a quantidade. Se não, adiciona.
        this.items.put(product, this.items.getOrDefault(product, 0) + quantity);
    }

    public void removeItem(Product product, int quantity) {
        // Verificação de existência
        if (this.items.containsKey(product)) {
            
            // Extração e cálculo
            int currentQuantity = this.items.get(product);
            int newQuantity = currentQuantity - quantity;
    
            // Remocao ou atualizacao da quantidade
            if (newQuantity <= 0) {
                this.items.remove(product);
            } else {
                this.items.put(product, newQuantity);
            }
        }
    }

    public double getSubtotal() {
        double subtotal = 0.0;
        for (Map.Entry<Product, Integer> entry : items.entrySet()) {
            subtotal += entry.getKey().getPrice() * entry.getValue();
        }
        return subtotal;
    }

    public int getOrderNumber() {
        return orderNumber;
    }

    public double getTax() {
        // Exemplo: 10% de taxa
        return getSubtotal() * 0.10; 
    }

    public double getTotal() {
        return getSubtotal() + getTax();
    }

    public boolean isFinalized() { return finalized; }
    public void setFinalized(boolean finalized) { this.finalized = finalized; }
    public String getOrderId() { return orderId; }
    public Map<Product, Integer> getItems() { return items; }
}