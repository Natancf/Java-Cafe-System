package main.java.br.usp.icmc.scc0204.javacafe.controller;

import main.java.br.usp.icmc.scc0204.javacafe.model.*;
import main.java.br.usp.icmc.scc0204.javacafe.exceptions.*;

import java.util.List;

public class CafeController {
    
    private CafeSystemContract cafeSystem;
    private Order currentOrder;

    public CafeController(CafeSystemContract cafeSystem) {
        this.cafeSystem = cafeSystem;
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

    public String finalizeCurrentOrder(PaymentMethod method) throws InvalidPaymentException, OutOfStockException {
        if (this.currentOrder == null) {
            throw new IllegalStateException("Não há nenhum pedido em andamento para finalizar.");
        }
        
        cafeSystem.finalizeOrder(this.currentOrder, method);
        String receipt = cafeSystem.generateReceipt(this.currentOrder);
        
        // Limpa para o próximo cliente
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
    }
}