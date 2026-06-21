package test.java.br.usp.icmc.scc0204.javacafe;

import main.java.br.usp.icmc.scc0204.javacafe.model.*;
import main.java.br.usp.icmc.scc0204.javacafe.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import java.io.File;
import main.java.br.usp.icmc.scc0204.javacafe.DataStorage;

public class CafeSystemTest {

    private CafeSystem system;
    private Product espresso;
    private Product cookie;

    @BeforeAll
    public static void setupGlobal() {
        // Avisa o sistema para usar arquivos falsos antes de rodar os testes
        DataStorage.setTestMode(true);
    }

    @AfterAll
    public static void tearDownGlobal() {
        // Limpa a sujeira apagando os arquivos de teste no final
        new File("data/test_estoque.csv").delete();
        new File("data/test_vendas.csv").delete();
        new File("data/test_vendas_itens.csv").delete();
        
        // Retorna o sistema ao estado normal (produção)
        DataStorage.setTestMode(false);
    }

    @BeforeEach
    public void setUp() throws Exception {
        // Initializes the system engine before each test.
        system = new CafeSystem();
        
        // Configure test products with controlled inventory.
        espresso = new Product("PROD-1", "Espresso", 6.00, 10);
        cookie = new Product("PROD-2", "Chocolate Cookie", 8.00, 1); // Apenas 1 no estoque
        
        system.addProduct(espresso);
        system.addProduct(cookie);
    }

    @Test
    public void testSuccessfulOrderAndStockDeduction() throws Exception {
        Order order = system.createOrder();
        system.addItemToOrder(order, espresso, 2); // 2 * 6.00 = 12.00
        
        // Complete your order with PIX.
        system.finalizeOrder(order, PaymentMethod.PIX);
        
        // Assert: Check if the espresso stock has dropped from 10 to 8.
        assertEquals(8, espresso.getStockQuantity(), "O estoque deveria ter sido reduzido para 8.");
    }

    @Test
    public void testOutOfStockException() throws Exception {
        Order order = system.createOrder();
        
        // AssertThrows: We tried adding 2 cookies, but we only have 1 in stock.
        assertThrows(OutOfStockException.class, () -> {
            system.addItemToOrder(order, cookie, 2);
        }, "Deveria lançar OutOfStockException ao pedir quantidade maior que o estoque.");
    }

    @Test
    public void testEmptyOrderExceptionOnFinalize() throws Exception {
        Order emptyOrder = system.createOrder(); // Pedido sem itens
        
        // AssertThrows: Try to complete an empty order.
        assertThrows(EmptyOrderException.class, () -> {
            system.finalizeOrder(emptyOrder, PaymentMethod.PIX);
        }, "Deveria lançar EmptyOrderException ao tentar finalizar um carrinho vazio.");
    }

    @Test
    public void testLowStockAlertIndicator() throws Exception {
        // The cookie starts with stock = 1. Since the default limit is 5, it should report low stock.
        assertTrue(cookie.isStockLow(), "O cookie deveria ser identificado com estoque baixo.");
        
        // The espresso starts at 10. The standard limit is 5, so it shouldn't indicate low stock.
        assertFalse(espresso.isStockLow(), "O espresso não deveria acusar estoque baixo.");
    }

    @Test
    public void testOrderMathCalculations() throws Exception {
        Order order = system.createOrder();
        
        // Add 2 Espressos (2 * 6.00 = 12.00) and 1 Cookie (8.00)
        system.addItemToOrder(order, espresso, 2);
        system.addItemToOrder(order, cookie, 1);
        
        // Check the Subtotal
        assertEquals(20.00, order.getSubtotal(), 0.01, "O subtotal calculado está incorreto.");
        
        // Check the Rate (Assuming the system applies a rate, e.g., 10% = 2.00)
        double expectedTax = order.getSubtotal() * 0.10; // Exemplo de lógica de teste de taxa
        assertEquals(expectedTax, order.getTax(), 0.01, "O cálculo da taxa está incorreto.");
        
        // Check the Final Total (Subtotal + Tax)
        double expectedTotal = 20.00 + expectedTax;
        assertEquals(expectedTotal, order.getTotal(), 0.01, "O total final da ordem está incorreto.");
    }

    @Test
    public void testInventoryRestockUpdatesQuantity() {
        // The initial espresso stock in the setup is 10.
        int initialStock = espresso.getStockQuantity();
        int restockAmount = 15;
        
        // Simulates an employee updating inventory.
        espresso.setStockQuantity(initialStock + restockAmount);
        
        // Check if the system has registered 25 units.
        assertEquals(25, espresso.getStockQuantity(), "O sistema não atualizou o estoque após o reabastecimento.");
        
        // Checks if the 'low stock' status has been removed (if it was active).
        assertFalse(espresso.isStockLow(), "O produto não deveria mais acusar estoque baixo após reabastecimento.");
    }



    // Shopping Cart Handling (Order)
    @Test
    public void testRemoveItemFromOrderCompletely() {
        Order order = system.createOrder();
        order.addItem(espresso, 2);
        order.removeItem(espresso, 2);
        
        // Assert: The item should disappear from the shopping cart map.
        assertFalse(order.getItems().containsKey(espresso), "O espresso deveria ter sido removido completamente do pedido.");
    }

    @Test
    public void testRemovePartialQuantityFromOrder() {
        Order order = system.createOrder();
        order.addItem(espresso, 3);
        order.removeItem(espresso, 1);
        
        // Assert: There should be 2 espressos left.
        assertEquals(2, order.getItems().get(espresso), "Deveriam sobrar 2 unidades de espresso no pedido.");
    }

    @Test
    public void testAddNegativeQuantityIgnored() {
        Order order = system.createOrder();
        order.addItem(espresso, -5);
        
        // Assert: Negative quantities should be ignored by the logic of the Order class.
        assertEquals(0, order.getItemCount(), "O pedido deve continuar vazio ao tentar adicionar quantidade negativa.");
    }

    @Test
    public void testRemoveItemNotInOrderIgnored() {
        Order order = system.createOrder();
        order.addItem(espresso, 1);
        
        // AssertDoesNotThrow: Trying to remove a cookie (that is not in the order) should not break the system.
        assertDoesNotThrow(() -> {
            order.removeItem(cookie, 1);
        }, "Remover um item inexistente no pedido não deve gerar exceção.");
        assertEquals(1, order.getItemCount(), "A contagem de itens do pedido não deveria ser alterada.");
    }

    // Product Handling (Product)

    @Test
    public void testDecreaseStockDirectlyThrowsException() {
        // AssertThrows: Attempting to reduce the stock from 10 espresso to 15 units should fail.
        assertThrows(OutOfStockException.class, () -> {
            espresso.decreaseStock(15);
        }, "Deveria lançar OutOfStockException ao diminuir o estoque além do disponível.");
    }

    @Test
    public void testUpdateProductPrice() {
        espresso.setPrice(7.50);
        
        // Assert: The price should be updated correctly.
        assertEquals(7.50, espresso.getPrice(), 0.01, "O preço do produto não foi atualizado corretamente.");
    }

    @Test
    public void testChangeLowStockThreshold() {
        // The current espresso stock is 10. The default limit is 5.
        espresso.setLowStockThreshold(15);
        
        // Assert: With the limit rising to 15, a stock of 10 is now considered low.
        assertTrue(espresso.isStockLow(), "O produto deveria acusar estoque baixo após alteração do threshold.");
    }

    // Inventory Management (Cafesystem)

    @Test
    public void testGetLowStockProductsList() {
        // Threshold set to 5. Cookie has 1, Espresso has 10.
        var lowStockList = system.getLowStockProducts(5);
        
        assertTrue(lowStockList.contains(cookie), "A lista deve conter o cookie (estoque = 1).");
        assertFalse(lowStockList.contains(espresso), "A lista não deve conter o espresso (estoque = 10).");
    }

    @Test
    public void testSystemUpdateStockMethod() {
        system.updateStock("PROD-1", 50);
        Product updatedProduct = null;
        for (Product p : system.getAllProducts()) {
            if (p.getId().equals("PROD-1")) {
                updatedProduct = p;
                break;
            }
        }
        
        // Assert:Verify that the returned product actually has 50 units in stock.
        assertNotNull(updatedProduct, "O produto PROD-1 deveria estar no sistema.");
        assertEquals(50, updatedProduct.getStockQuantity(), "O método updateStock do sistema não atualizou o produto.");
    }

    // Payment Validation (Payment)
    @Test
    public void testPaymentValidationThrowsExceptionForZeroAmount() {
        Payment payment = new Payment(PaymentMethod.CREDIT_CARD, 0.0);
        
        // AssertThrows: Pagamentos zerados ou negativos não devem passar
        assertThrows(InvalidPaymentException.class, () -> {
            payment.validatePayment();
        }, "Deveria lançar InvalidPaymentException para valor zerado.");
    }

    @Test
    public void testPaymentValidationThrowsExceptionForNegativeAmount() {
        Payment payment = new Payment(PaymentMethod.DEBIT_CARD, -15.50);
        
        assertThrows(InvalidPaymentException.class, () -> {
            payment.validatePayment();
        }, "Deveria lançar InvalidPaymentException para valor negativo.");
    }

    @Test
    public void testPaymentValidationSuccess() {
        Payment payment = new Payment(PaymentMethod.PIX, 25.00);
        
        // AssertDoesNotThrow: Pagamento válido deve passar silenciosamente
        assertDoesNotThrow(() -> {
            payment.validatePayment();
        }, "Pagamento com valor positivo não deveria lançar exceção.");
    }

    // Finalization And Receipt (Receipt / Status)
    @Test
    public void testOrderIsFinalizedStatus() throws Exception {
        Order order = system.createOrder();
        system.addItemToOrder(order, espresso, 1);
        system.finalizeOrder(order, PaymentMethod.DEBIT_CARD);
        
        // Assert: The "completed" status should be true after checkout.
        assertTrue(order.isFinalized(), "O pedido deveria constar como finalizado (isFinalized = true).");
    }

    @Test
    public void testGenerateReceiptContainsProductName() throws Exception {
        Order order = system.createOrder();
        system.addItemToOrder(order, espresso, 1);
        
        String receipt = system.generateReceipt(order);
        
        // Assert: The receipt string should contain the basic sales details.
        assertTrue(receipt.contains("Java Café"), "O recibo deve conter o cabeçalho do Java Café.");
        assertTrue(receipt.contains("Espresso"), "O recibo deve descrever o nome do produto vendido.");
    }
}