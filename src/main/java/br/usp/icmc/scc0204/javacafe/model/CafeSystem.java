package main.java.br.usp.icmc.scc0204.javacafe.model;

import main.java.br.usp.icmc.scc0204.javacafe.exceptions.*;
import main.java.br.usp.icmc.scc0204.javacafe.DataStorage;

import java.io.BufferedReader;
import java.io.FileReader;
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

    @Override public void updateStock(String productId, int newQuantity) {
        for (Product product : inventory) {
            if (product.getId().equals(productId)) {
                product.setStockQuantity(newQuantity);
                break;
            }
        }
        try {
            DataStorage.saveInventory(inventory);
        } catch (IOException e) {
            System.err.println("Erro ao salvar estoque.");
        }
    }

    @Override public Report getDailyReport() {
        return generateFilteredReport("DAILY");
    }
    
    @Override public Report getWeeklyReport() {
        return generateFilteredReport("WEEKLY");
    }
    
    @Override public Report getMonthlyReport() {
        return generateFilteredReport("MONTHLY");
    }

    private boolean isWithinTimeFrame(String orderId, String timeframe, java.time.LocalDate today) {
        if (orderId == null || orderId.length() < 12) return false;
        
        try {
            // Extracts the date from the order code (e.g., ORD-yyyyMMdd-HHmmss)
            String dateString = orderId.substring(4, 12);
            java.time.LocalDate orderDate = java.time.LocalDate.parse(dateString, 
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

            switch (timeframe) {
                case "DAILY":
                    return orderDate.isEqual(today);
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
     * Generates a complete filtered report with revenue, transaction count, top products,
     * and detailed sales quantities.
     */
    private Report generateFilteredReport(String timeframe) {
        double totalRevenue = 0;
        int transactionCount = 0;
        Map<String, Integer> productSalesCount = new java.util.HashMap<>();
        java.time.LocalDate today = java.time.LocalDate.now();

        // Read sales data to calculate revenue and transaction count
        try (BufferedReader reader = new BufferedReader(new FileReader("data/vendas.csv"))) {
            String header = reader.readLine(); // Skip header
            if (header == null) {
                System.err.println("Vendas file is empty or has no header.");
                return new Report(0, 0, new java.util.ArrayList<>(), new java.util.HashMap<>());
            }

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
            return new Report(0, 0, new java.util.ArrayList<>(), new java.util.HashMap<>());
        }

        // Read sold items to calculate product sales quantities
        try (BufferedReader reader = new BufferedReader(new FileReader("data/vendas_itens.csv"))) {
            String header = reader.readLine(); // Skip header
            if (header == null) {
                System.err.println("Vendas itens file is empty or has no header.");
                return new Report(totalRevenue, transactionCount, new java.util.ArrayList<>(), productSalesCount);
            }

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
                    
                    productSalesCount.put(productId, 
                        productSalesCount.getOrDefault(productId, 0) + quantity);
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing quantity: " + parts[2]);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading sold items file: " + e.getMessage());
            return new Report(totalRevenue, transactionCount, new java.util.ArrayList<>(), productSalesCount);
        }

        // Calculate top 3 products based on quantity sold
        List<Product> topProducts = new java.util.ArrayList<>();
        
        // Sort products by sales count and get top 3
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

        return new Report(totalRevenue, transactionCount, topProducts, productSalesCount);
    }
}