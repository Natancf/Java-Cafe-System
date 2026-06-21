# ☕ Java Café - POS & Inventory System

Projeto desenvolvido para a disciplina de Programação Orientada a Objetos, consistindo em um Sistema de Ponto de Venda (POS) e Gestão de Estoque completo com interface gráfica em Java Swing.

### 👥 Identificação do Grupo
* **Davi Azevedo Guedes de Sá**
* **Natanael Costa de Freitas** 
* **Pietro Gutiérrez García-Urrutia**

---

## 1. Requirements
O sistema atende a todos os requisitos propostos para o gerenciamento de um POS. Além dos requisitos base, implementamos validações customizadas para as formas de pagamento (rejeitando valores zerados/negativos) e um sistema de alerta configurável para o monitoramento de estoque baixo.

## 2. Project Description
O projeto adota o padrão de arquitetura MVC (Model-View-Controller)...

```mermaid
classDiagram
    %% Camada CONTROLLER
    class CafeController {
        -CafeSystemContract cafeSystem
        -Order currentOrder
        -PropertyChangeSupport propertyChangeSupport
        +startNewOrder() void
        +addItemToCurrentOrder(Product, int) void
        +removeItemFromCurrentOrder(Product, int) void
        +finalizeCurrentOrder(PaymentMethod) String
        +getAvailableProducts() List~Product~
        +updateStock(String, int) void
    }

    %% Camada MODEL (Contratos e Lógica)
    class CafeSystemContract {
        <<interface>>
        +createOrder() Order
        +addItemToOrder(Order, Product, int) void
        +finalizeOrder(Order, PaymentMethod) void
        +updateStock(String, int) void
        +getAllProducts() List~Product~
        +getLowStockProducts(int) List~Product~
    }

    class CafeSystem {
        -List~Product~ inventory
        +createOrder() Order
        +finalizeOrder(Order, PaymentMethod) void
        -generateFilteredReport(String) Report
    }

    class Product {
        -String id
        -String name
        -double price
        -int stockQuantity
        -int lowStockThreshold
        +decreaseStock(int) void
        +isStockLow() boolean
    }

    class Order {
        -double TAX_RATE
        -String orderId
        -int orderNumber
        -Map~Product, Integer~ items
        -boolean finalized
        +addItem(Product, int) void
        +removeItem(Product, int) void
        +getSubtotal() double
        +getTax() double
        +getTotal() double
    }

    class Payment {
        -PaymentMethod method
        -double amount
        +validatePayment() void
    }

    %% Camada VIEW (Interface)
    class MainFrame {
        -CafeController controller
        -JTabbedPane tabbedPane
        -createTabs() void
    }

    class POSPanel {
        -CafeController controller
        -JTable cartTable
        +rebuildMenuGrid() void
        +refreshCartUI() void
    }

    class InventoryPanel {
        -CafeController controller
        -JTable inventoryTable
        +refreshInventoryTable() void
        +saveProduct(ActionEvent) void
    }

    class DashboardPanel {
        -CafeController controller
        +loadReportData() void
    }

    %% Camada de INFRAESTRUTURA
    class DataStorage {
        <<utility>>
        -String ESTOQUE_PATH
        -String VENDAS_PATH
        +saveInventory(List~Product~) void
        +loadInventory() List~Product~
        +appendOrder(Order, String) void
        +setTestMode(boolean) void
    }

    %% RELACIONAMENTOS E DEPENDÊNCIAS
    CafeSystem ..|> CafeSystemContract : implements (Realization)
    CafeController --> CafeSystemContract : manages (Association)
    
    MainFrame --> CafeController : injects
    POSPanel --> CafeController : listens
    InventoryPanel --> CafeController : updates
    DashboardPanel --> CafeController : reads
    
    CafeSystem --> Product : contains
    Order --> Product : maps
    CafeSystem ..> DataStorage : persists via
```

## 3. Comments About the Code
O sistema aplica pilares fundamentais da Orientação a Objetos:
* **Polimorfismo:** A interface gráfica interage com o sistema através do contrato `CafeSystemContract`, facilitando a injeção de dependências e testes. Além disso, utilizamos polimorfismo de sobreposição (ex: `StatusCellRenderer`) para modificar o comportamento visual das tabelas.
* **Herança:** Utilizada na extensão dos painéis visuais (`JPanel`, `JFrame`) e na criação de uma arquitetura limpa de exceções de domínio (`OutOfStockException`, `EmptyOrderException`, `InvalidPaymentException`).
* **Encapsulamento:** Entidades protegem seu estado interno. Identificadores são imutáveis e modificações no carrinho (`Order`) ocorrem apenas via métodos controlados para garantir cálculos financeiros exatos.

## 4. Test Plan
O plano de qualidade focou em validar as regras de negócio críticas usando testes unitários automatizados. Utilizando a biblioteca **JUnit 5**, desenvolvemos 20 casos de teste que cobrem:
1. Adição e remoção de itens com recálculo matemático de taxas.
2. Tratamento de exceções por quebra de estoque ou de pagamentos.
3. Isolamento do banco de dados (inserção de flag `testMode` para que os testes rodem em arquivos paralelos, protegendo o banco oficial).

## 5. Test Results
Todos os 20 cenários de teste unitários foram executados com sucesso (100% de aprovação) no *Test Runner* do JUnit, assegurando que o sistema é resiliente a inputs inesperados.

**Log de Saída Real da Execução Técnica (JUnit Output Panel):**
```text
%TESTC  20 v2
%TSTTREE2,test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest,true,20,false,1,CafeSystemTest,,[engine:junit-jupiter]/[class:test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest]
%TSTTREE3,testEmptyOrderExceptionOnFinalize(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest),false,1,false,2,testEmptyOrderExceptionOnFinalize(),,[engine:junit-jupiter]/[class:test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest]/[method:testEmptyOrderExceptionOnFinalize()]
%TSTTREE4,testGetLowStockProductsList(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest),false,1,false,2,testGetLowStockProductsList(),,[engine:junit-jupiter]/[class:test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest]/[method:testGetLowStockProductsList()]
%TSTTREE5,testRemovePartialQuantityFromOrder(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest),false,1,false,2,testRemovePartialQuantityFromOrder(),,[engine:junit-jupiter]/[class:test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest]/[method:testRemovePartialQuantityFromOrder()]
%TSTTREE6,testUpdateProductPrice(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest),false,1,false,2,testUpdateProductPrice(),,[engine:junit-jupiter]/[class:test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest]/[method:testUpdateProductPrice()]
%TSTTREE7,testSystemUpdateStockMethod(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest),false,1,false,2,testSystemUpdateStockMethod(),,[engine:junit-jupiter]/[class:test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest]/[method:testSystemUpdateStockMethod()]
%TSTTREE8,testLowStockAlertIndicator(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest),false,1,false,2,testLowStockAlertIndicator(),,[engine:junit-jupiter]/[class:test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest]/[method:testLowStockAlertIndicator()]
%TSTTREE9,testOutOfStockException(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest),false,1,false,2,testOutOfStockException(),,[engine:junit-jupiter]/[class:test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest]/[method:testOutOfStockException()]
%TSTTREE10,testDecreaseStockDirectlyThrowsException(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest),false,1,false,2,testDecreaseStockDirectlyThrowsException(),,[engine:junit-jupiter]/[class:test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest]/[method:testDecreaseStockDirectlyThrowsException()]
%TSTTREE11,testPaymentValidationSuccess(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest),false,1,false,2,testPaymentValidationSuccess(),,[engine:junit-jupiter]/[class:test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest]/[method:testPaymentValidationSuccess()]
%TSTTREE12,testOrderMathCalculations(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest),false,1,false,2,testOrderMathCalculations(),,[engine:junit-jupiter]/[class:test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest]/[method:testOrderMathCalculations()]
%TSTTREE13,testAddNegativeQuantityIgnored(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest),false,1,false,2,testAddNegativeQuantityIgnored(),,[engine:junit-jupiter]/[class:test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest]/[method:testAddNegativeQuantityIgnored()]
%TSTTREE14,testChangeLowStockThreshold(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest),false,1,false,2,testChangeLowStockThreshold(),,[engine:junit-jupiter]/[class:test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest]/[method:testChangeLowStockThreshold()]
%TSTTREE15,testRemoveItemFromOrderCompletely(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest),false,1,false,2,testRemoveItemFromOrderCompletely(),,[engine:junit-jupiter]/[class:test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest]/[method:testRemoveItemFromOrderCompletely()]
%TSTTREE16,testOrderIsFinalizedStatus(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest),false,1,false,2,testOrderIsFinalizedStatus(),,[engine:junit-jupiter]/[class:test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest]/[method:testOrderIsFinalizedStatus()]
%TSTTREE17,testPaymentValidationThrowsExceptionForZeroAmount(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest),false,1,false,2,testPaymentValidationThrowsExceptionForZeroAmount(),,[engine:junit-jupiter]/[class:test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest]/[method:testPaymentValidationThrowsExceptionForZeroAmount()]
%TSTTREE18,testPaymentValidationThrowsExceptionForNegativeAmount(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest),false,1,false,2,testPaymentValidationThrowsExceptionForNegativeAmount(),,[engine:junit-jupiter]/[class:test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest]/[method:testPaymentValidationThrowsExceptionForNegativeAmount()]
%TSTTREE19,testInventoryRestockUpdatesQuantity(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest),false,1,false,2,testInventoryRestockUpdatesQuantity(),,[engine:junit-jupiter]/[class:test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest]/[method:testInventoryRestockUpdatesQuantity()]
%TSTTREE20,testSuccessfulOrderAndStockDeduction(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest),false,1,false,2,testSuccessfulOrderAndStockDeduction(),,[engine:junit-jupiter]/[class:test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest]/[method:testSuccessfulOrderAndStockDeduction()]
%TSTTREE21,testRemoveItemNotInOrderIgnored(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest),false,1,false,2,testRemoveItemNotInOrderIgnored(),,[engine:junit-jupiter]/[class:test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest]/[method:testRemoveItemNotInOrderIgnored()]
%TSTTREE22,testGenerateReceiptContainsProductName(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest),false,1,false,2,testGenerateReceiptContainsProductName(),,[engine:junit-jupiter]/[class:test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest]/[method:testGenerateReceiptContainsProductName()]
%TESTS  3,testEmptyOrderExceptionOnFinalize(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTE  3,testEmptyOrderExceptionOnFinalize(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTS  4,testGetLowStockProductsList(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTE  4,testGetLowStockProductsList(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTS  5,testRemovePartialQuantityFromOrder(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTE  5,testRemovePartialQuantityFromOrder(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTS  6,testUpdateProductPrice(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTE  6,testUpdateProductPrice(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTS  7,testSystemUpdateStockMethod(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTE  7,testSystemUpdateStockMethod(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTS  8,testLowStockAlertIndicator(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTE  8,testLowStockAlertIndicator(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTS  9,testOutOfStockException(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTE  9,testOutOfStockException(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTS  10,testDecreaseStockDirectlyThrowsException(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTE  10,testDecreaseStockDirectlyThrowsException(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTS  11,testPaymentValidationSuccess(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTE  11,testPaymentValidationSuccess(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTS  12,testOrderMathCalculations(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTE  12,testOrderMathCalculations(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTS  13,testAddNegativeQuantityIgnored(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTE  13,testAddNegativeQuantityIgnored(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTS  14,testChangeLowStockThreshold(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTE  14,testChangeLowStockThreshold(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTS  15,testRemoveItemFromOrderCompletely(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTE  15,testRemoveItemFromOrderCompletely(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTS  16,testOrderIsFinalizedStatus(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTE  16,testOrderIsFinalizedStatus(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTS  17,testPaymentValidationThrowsExceptionForZeroAmount(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTE  17,testPaymentValidationThrowsExceptionForZeroAmount(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTS  18,testPaymentValidationThrowsExceptionForNegativeAmount(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTE  18,testPaymentValidationThrowsExceptionForNegativeAmount(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTS  19,testInventoryRestockUpdatesQuantity(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTE  19,testInventoryRestockUpdatesQuantity(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTS  20,testSuccessfulOrderAndStockDeduction(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTE  20,testSuccessfulOrderAndStockDeduction(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTS  21,testRemoveItemNotInOrderIgnored(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTE  21,testRemoveItemNotInOrderIgnored(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTS  22,testGenerateReceiptContainsProductName(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%TESTE  22,testGenerateReceiptContainsProductName(test.java.br.usp.icmc.scc0204.javacafe.CafeSystemTest)
%RUNTIME302

...
[  20 tests successful      ]
[   0 tests failed          ]
BUILD SUCCESSFUL
```

## 6. Build Procedures
Instruções sequenciais para instalação e execução do sistema em qualquer ambiente local limpo. Exige **Java JDK 8 ou superior**.

**Passo 1: Clonar o repositório**
```bash
git clone [https://github.com/Natancf/Java-Cafe-System.git](https://github.com/Natancf/Java-Cafe-System.git)
cd Java-Cafe-System

```

**Passo 2: Preparar o ambiente local**
Crie as pastas necessárias para o armazenamento de imagens. Os arquivos de banco de dados (`.csv`) serão criados automaticamente pelo programa.

```bash
mkdir -p data/product_images
mkdir -p bin

```

**Passo 3: Compilar o código**
A partir da raiz do projeto, execute o comando apontando para a estrutura de pacotes:

```bash
javac -d bin -sourcepath src src/main/java/br/usp/icmc/scc0204/javacafe/Main.java

```

**Passo 4: Executar a aplicação**

```bash
java -cp bin main.java.br.usp.icmc.scc0204.javacafe.Main

```

## 7. Problems

O principal problema técnico enfrentado ocorreu durante a integração do JUnit com a persistência local. O método `@BeforeEach` dos testes estava injetando dados falsos nos arquivos `.csv` reais do projeto.
**Solução:** Implementamos o método estático `DataStorage.setTestMode(boolean)` para desviar a gravação dos testes para arquivos paralelos (`test_estoque.csv`), que são sumariamente excluídos via `@AfterAll` no fim da execução, protegendo a integridade do banco principal.

## 8. Comments

A documentação completa das classes e interfaces do sistema foi gerada utilizando a ferramenta **JavaDoc**. Acesse a documentação técnica interativa hospedada diretamente neste repositório.

```
https://natancf.github.io/Java-Cafe-System/
```
