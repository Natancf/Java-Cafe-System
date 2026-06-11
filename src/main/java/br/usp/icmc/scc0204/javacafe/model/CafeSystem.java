package main.java.br.usp.icmc.scc0204.javacafe.model;

import main.java.br.usp.icmc.scc0204.javacafe.exceptions.*;
import main.java.br.usp.icmc.scc0204.javacafe.DataStorage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CafeSystem implements CafeSystemContract {

    private List<Product> inventory = new ArrayList<>();

    public CafeSystem() {
        try {
            this.inventory = DataStorage.loadInventory();
        } catch (IOException e) {
            System.err.println("Aviso: Iniciando com estoque vazio.");
        }
    }

    @Override
    public Order createOrder() {
        String orderCode = generateOrderCode(); 
        
        int orderNumber = 1; 
        try {
            orderNumber = DataStorage.getLastOrderNumber() + 1;
        } catch (IOException e) {
            System.err.println("Erro ao buscar o número do pedido.");
        }
        
        return new Order(orderCode, orderNumber); 
    }

    private String generateOrderCode() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        String timestamp = LocalDateTime.now().format(formatter);
        return "ORD-" + timestamp; 
    }

    @Override
    public void addItemToOrder(Order order, Product product, int quantity) throws OutOfStockException {
        if (product.getStockQuantity() < quantity) {
            throw new OutOfStockException("Estoque insuficiente para o produto: " + product.getName());
        }

        order.addItem(product, quantity);
    }

    @Override
    public void removeItemFromOrder(Order order, Product product, int quantityToRemove) {
        // Current cart
        Map<Product, Integer> items = order.getItems();

        if (items.containsKey(product)) {
            int currentQuantity = items.get(product);

            if (quantityToRemove >= currentQuantity) {
                items.remove(product);
            } else {
                items.put(product, currentQuantity - quantityToRemove);
            }
        }
    }

    @Override
    public void finalizeOrder(Order order, PaymentMethod paymentMethod) throws InvalidPaymentException, OutOfStockException, EmptyOrderException{

        if (order.getItems().isEmpty()) {
            throw new EmptyOrderException("Cannot finalize an empty order.");
        }

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
            throw new RuntimeException("Falha ao salvar a venda no CSV.");
        }
    }

    @Override
    public String generateReceipt(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Java Café Receipt ===\n");
        sb.append("Pedido #").append(order.getOrderNumber()).append("\n");

        for (Map.Entry<Product, Integer> entry : order.getItems().entrySet()) {
            Product p = entry.getKey();
            int qtd = entry.getValue();
            double subtotalItem = p.getPrice() * qtd;

            sb.append(p.getName())
              .append(" x").append(qtd)
              .append(" - R$ ").append(String.format("%.2f", subtotalItem))
              .append("\n");
        }

        sb.append("----------------------\n");
        sb.append("Total: R$ ").append(String.format("%.2f", order.getTotal())).append("\n");

        return sb.toString();
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
    
    @Override public double calculateTotal(Order order) { return order.getTotal(); }
    @Override public void addProduct(Product product) { inventory.add(product); }
    @Override public List<Product> getAllProducts() { return inventory; }
    

    @Override public double calculateSubtotal(Order order) { return order.getSubtotal(); }
    @Override public double calculateTax(Order order) { return order.getTax(); }


    @Override public void updateStock(String productId, int newQuantity) {}
    @Override public Report getDailyReport() { return null; }
    @Override public Report getWeeklyReport() { return null; }
    @Override public Report getMonthlyReport() { return null; }
}