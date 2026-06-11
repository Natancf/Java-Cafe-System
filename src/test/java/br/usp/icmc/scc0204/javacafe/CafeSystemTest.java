package test.java.br.usp.icmc.scc0204.javacafe;

import main.java.br.usp.icmc.scc0204.javacafe.model.*;
import main.java.br.usp.icmc.scc0204.javacafe.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CafeSystemTest {

    private CafeSystem system;
    private Product espresso;
    private Product cookie;

    @BeforeEach
    public void setUp() throws Exception {
        // Inicializa o motor do sistema antes de cada teste
        system = new CafeSystem();
        
        // Configura produtos de teste com estoques controlados
        espresso = new Product("PROD-1", "Espresso", 6.00, 10);
        cookie = new Product("PROD-2", "Chocolate Cookie", 8.00, 1); // Apenas 1 no estoque
        
        system.addProduct(espresso);
        system.addProduct(cookie);
    }

    @Test
    public void testSuccessfulOrderAndStockDeduction() throws Exception {
        Order order = system.createOrder();
        system.addItemToOrder(order, espresso, 2); // 2 * 6.00 = 12.00
        
        // Finaliza o pedido com PIX
        system.finalizeOrder(order, PaymentMethod.PIX);
        
        // Assert: Verifica se o estoque do espresso baixou de 10 para 8
        assertEquals(8, espresso.getStockQuantity(), "O estoque deveria ter sido reduzido para 8.");
    }

    @Test
    public void testOutOfStockException() throws Exception {
        Order order = system.createOrder();
        
        // AssertThrows: Tenta adicionar 2 cookies, mas só temos 1 no estoque
        assertThrows(OutOfStockException.class, () -> {
            system.addItemToOrder(order, cookie, 2);
        }, "Deveria lançar OutOfStockException ao pedir quantidade maior que o estoque.");
    }

    @Test
    public void testEmptyOrderExceptionOnFinalize() throws Exception {
        Order emptyOrder = system.createOrder(); // Pedido sem itens
        
        // AssertThrows: Tenta finalizar um pedido vazio
        assertThrows(EmptyOrderException.class, () -> {
            system.finalizeOrder(emptyOrder, PaymentMethod.PIX);
        }, "Deveria lançar EmptyOrderException ao tentar finalizar um carrinho vazio.");
    }

    @Test
    public void testLowStockAlertIndicator() throws Exception {
        // O cookie começa com estoque = 1. Como o limite padrão é 5, ele deve acusar estoque baixo.
        assertTrue(cookie.isStockLow(), "O cookie deveria ser identificado com estoque baixo.");
        
        // O espresso começa com 10. Limite padrão é 5, então não deve acusar estoque baixo.
        assertFalse(espresso.isStockLow(), "O espresso não deveria acusar estoque baixo.");
    }

    @Test
    public void testOrderMathCalculations() throws Exception {
        Order order = system.createOrder();
        
        // Adiciona 2 Espressos (2 * 6.00 = 12.00) e 1 Cookie (8.00)
        system.addItemToOrder(order, espresso, 2);
        system.addItemToOrder(order, cookie, 1);
        
        // Verifica o Subtotal
        assertEquals(20.00, order.getSubtotal(), 0.01, "O subtotal calculado está incorreto.");
        
        // Verifica a Taxa (Assumindo que o sistema aplique uma taxa, ex: 10% = 2.00)
        double expectedTax = order.getSubtotal() * 0.10; // Exemplo de lógica de teste de taxa
        assertEquals(expectedTax, order.getTax(), 0.01, "O cálculo da taxa está incorreto.");
        
        // Verifica o Total Final (Subtotal + Taxa)
        double expectedTotal = 20.00 + expectedTax;
        assertEquals(expectedTotal, order.getTotal(), 0.01, "O total final da ordem está incorreto.");
    }

    @Test
    public void testInventoryRestockUpdatesQuantity() {
        // O estoque inicial do espresso no setUp é 10.
        int initialStock = espresso.getStockQuantity();
        int restockAmount = 15;
        
        // Simula o funcionário atualizando o estoque
        espresso.setStockQuantity(initialStock + restockAmount);
        
        // Valida se o sistema registrou 25 unidades
        assertEquals(25, espresso.getStockQuantity(), "O sistema não atualizou o estoque após o reabastecimento.");
        
        // Valida se o status de 'estoque baixo' foi removido (caso estivesse ativo)
        assertFalse(espresso.isStockLow(), "O produto não deveria mais acusar estoque baixo após reabastecimento.");
    }
}