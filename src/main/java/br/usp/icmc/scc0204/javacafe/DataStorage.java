package main.java.br.usp.icmc.scc0204.javacafe;

import main.java.br.usp.icmc.scc0204.javacafe.model.Product;
import main.java.br.usp.icmc.scc0204.javacafe.model.Order;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataStorage {
    private static final String ESTOQUE_PATH = "data/estoque.csv";
    private static final String VENDAS_PATH = "data/vendas.csv";
    private static final String VENDAS_ITENS_PATH = "data/vendas_itens.csv"; // Novo arquivo relacional


    // Stock
    
    public static void saveInventory(List<Product> products) throws IOException {

        try (PrintWriter writer = new PrintWriter(new FileWriter(ESTOQUE_PATH))) {
            // Write the header to the file.
            writer.println("id,nome,preco,qtd");
            
            for (Product p : products) {
                writer.println(p.getId() + "," + p.getName() + "," + p.getPrice() + "," + p.getStockQuantity());
            }
        }
    }

    public static List<Product> loadInventory() throws IOException {
        List<Product> products = new ArrayList<>();
        File file = new File(ESTOQUE_PATH);
        if (!file.exists()) return products;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Reads and discards the first line (header) before the loop.
            String header = reader.readLine(); 
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split(",");
                products.add(new Product(data[0], data[1], Double.parseDouble(data[2]), Integer.parseInt(data[3])));
            }
        }
        return products;
    }

    // Sales
    
    public static void appendOrder(Order order, String paymentMethod) throws IOException {
        new File("data").mkdirs();

        // Checking if the files already exist to decide whether to write the header.
        File arquivoVendas = new File(VENDAS_PATH);
        boolean precisaHeaderVendas = !arquivoVendas.exists() || arquivoVendas.length() == 0;

        File arquivoItens = new File(VENDAS_ITENS_PATH);
        boolean precisaHeaderItens = !arquivoItens.exists() || arquivoItens.length() == 0;

        // Saving to the main file (sales.csv)
        try (PrintWriter writerVendas = new PrintWriter(new FileWriter(arquivoVendas, true))) {
            if (precisaHeaderVendas) {
                writerVendas.println("id_pedido,numero_pedido,total,metodo_pagamento");
            }
            writerVendas.println(order.getOrderId() + "," + order.getOrderNumber() + "," + 
                                 order.getTotal() + "," + paymentMethod);
        }

        // Saves the items associated with the order (sales_items.csv)
        try (PrintWriter writerItens = new PrintWriter(new FileWriter(arquivoItens, true))) {
            if (precisaHeaderItens) {
                writerItens.println("id_pedido,id_produto,quantidade,preco_unitario");
            }

            for (Map.Entry<Product, Integer> entry : order.getItems().entrySet()) {
                Product p = entry.getKey();
                int quantidade = entry.getValue();
                
                writerItens.println(order.getOrderId() + "," + 
                                    p.getId() + "," + 
                                    quantidade + "," + 
                                    p.getPrice());
            }
        }
    }


    // Search for the last order number, skipping the header.
    public static int getLastOrderNumber() throws IOException {
        int lastNumber = 0;
        File file = new File(VENDAS_PATH);
        if (!file.exists()) return 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // Pula o cabeçalho
            reader.readLine();
            
            String lastLine = "", line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lastLine = line;
                }
            }
            
            if (!lastLine.isEmpty()) {
                lastNumber = Integer.parseInt(lastLine.split(",")[1]);
            }
        }
        return lastNumber;
    }
}