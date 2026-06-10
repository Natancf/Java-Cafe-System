package main.java.br.usp.icmc.scc0204.javacafe;

import main.java.br.usp.icmc.scc0204.javacafe.controller.CafeController;
import main.java.br.usp.icmc.scc0204.javacafe.model.*;
import main.java.br.usp.icmc.scc0204.javacafe.exceptions.*;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            // 1. Inicializa o sistema (Model)
            // O construtor carrega o estoque automaticamente via DataStorage
            CafeSystemContract sistema = new CafeSystem();

            // 2. Inicializa o Controlador passando o sistema
            CafeController controller = new CafeController(sistema);

            System.out.println("--- TESTANDO VIA CONTROLLER ---");

            // 3. Verifica o estoque existente
            List<Product> estoque = controller.getAvailableProducts();
            System.out.println("Produtos carregados no estoque: " + estoque.size());

            // Se for a primeira execução e o CSV estiver vazio, cria massa de teste
            if (estoque.isEmpty()) {
                System.out.println("Estoque vazio. Cadastrando produtos iniciais...");
                Product cafe = new Product("PROD-1", "Café Expresso", 5.00, 10);
                Product pao = new Product("PROD-2", "Pão de Queijo", 4.50, 15);
                
                controller.registerNewProduct(cafe);
                controller.registerNewProduct(pao);
                
                // Grava o inventário inicial no arquivo
                DataStorage.saveInventory(controller.getAvailableProducts());
                System.out.println("Produtos cadastrados com sucesso!");
                
                estoque = controller.getAvailableProducts();
            }

            // 4. Simulação do fluxo da Interface Gráfica (Janela de Caixa)
            System.out.println("\n[UI] Usuário clicou em: 'Novo Pedido'");
            controller.startNewOrder();

            // Simula a seleção de itens e quantidades na tela
            Product produto1 = estoque.get(0); 
            System.out.println("[UI] Selecionou: " + produto1.getName() + " (Qtd: 2)");
            controller.addItemToCurrentOrder(produto1, 2);

            if (estoque.size() > 1) {
                Product produto2 = estoque.get(1); 
                System.out.println("[UI] Selecionou: " + produto2.getName() + " (Qtd: 1)");
                controller.addItemToCurrentOrder(produto2, 1);
            }

            // Interface atualiza o valor total em tempo real na tela
            Order pedidoAtual = controller.getCurrentOrder();
            System.out.println("[UI] Total em tempo real na tela: R$ " + pedidoAtual.getTotal());

            // 5. Finalização da venda (Simula clique no botão "Pagar via PIX")
            System.out.println("\n[UI] Usuário clicou em: 'Finalizar com PIX'");
            String recibo = controller.finalizeCurrentOrder(PaymentMethod.PIX);

            // Interface recebe a String do recibo e joga em um componente de texto
            System.out.println("\n--- RECIBO GERADO PARA A INTERFACE ---");
            System.out.print(recibo);
            System.out.println("---------------------------------------");
            
            System.out.println("\nFluxo do Controller validado. Arquivos CSV atualizados.");

        } catch (Exception e) {
            System.err.println("\nErro detectado no fluxo:");
            e.printStackTrace();
        }
    }
}