package main.java.br.usp.icmc.scc0204.javacafe.model;

import main.java.br.usp.icmc.scc0204.javacafe.exceptions.OutOfStockException;

public class Product {
    
    private String id;
    private String name;
    private double price;
    private int stockQuantity;

    public Product(String id, String name, double price, int stockQuantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public String getId() { 
        return id; 
    }
    public String getName() { 
        return name;
    } 
    public double getPrice() { 
        return price; 
    }
    public int getStockQuantity() { 
        return stockQuantity; 
    }

    public void setStockQuantity(int quantity) {
        this.stockQuantity = quantity;
    }

    public void decreaseStock(int quantity) throws OutOfStockException {
        if (quantity > this.stockQuantity) {
            throw new OutOfStockException("Estoque insuficiente para o produto: " + this.name);
        }
        this.stockQuantity -= quantity;
    }
}
