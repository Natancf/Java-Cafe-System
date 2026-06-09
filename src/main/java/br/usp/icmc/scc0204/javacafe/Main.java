package main.java.br.usp.icmc.scc0204.javacafe;

import main.java.br.usp.icmc.scc0204.javacafe.model.*;
import main.java.br.usp.icmc.scc0204.javacafe.DataStorage;
import main.java.br.usp.icmc.scc0204.javacafe.model.CafeSystem;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            // Teste de Carga (DataStorage)
            System.out.println("Carregando estoque...");
            List<Product> estoque = DataStorage.loadInventory();
            System.out.println("Produtos carregados: " + estoque.size());

            // Teste de Sistema (CafeSystem)
            CafeSystem sistema = new CafeSystem();
            
            // Adicionando um produto de teste se a lista estiver vazia
            if (estoque.isEmpty()) {
                Product cafe = new Product("1", "Café Expresso", 5.00, 10);
                sistema.addProduct(cafe);
                DataStorage.saveInventory(sistema.getAllProducts());
                System.out.println("Produto criado e salvo no CSV!");
            }

            // Teste de Ordem
            Order pedido = sistema.createOrder();
            sistema.addItemToOrder(pedido, sistema.getAllProducts().get(0), 1);
            
            System.out.println("Pedido criado! Total: R$" + pedido.getTotal());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}