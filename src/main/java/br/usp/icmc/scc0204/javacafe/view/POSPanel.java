package main.java.br.usp.icmc.scc0204.javacafe.view;

import main.java.br.usp.icmc.scc0204.javacafe.controller.CafeController;
import main.java.br.usp.icmc.scc0204.javacafe.model.Product;
import main.java.br.usp.icmc.scc0204.javacafe.model.PaymentMethod;
import main.java.br.usp.icmc.scc0204.javacafe.exceptions.OutOfStockException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.Map;
import java.util.List;

public class POSPanel extends JPanel {

    private CafeController controller;
    
    // UI Components
    private JPanel menuGridPanel; // Container dos cards para podermos recarregar
    private DefaultTableModel cartModel;
    private JTable cartTable;
    private JLabel lblSubtotal, lblTax, lblTotal;

    public POSPanel(CafeController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        this.controller.startNewOrder();

        // Center: Menu Area
        JPanel menuPanel = new JPanel(new BorderLayout());
        menuPanel.setBorder(BorderFactory.createTitledBorder("Menu"));
        
        menuGridPanel = new JPanel(new GridLayout(0, 3, 15, 15));
        JScrollPane scrollMenu = new JScrollPane(menuGridPanel);
        scrollMenu.getVerticalScrollBar().setUnitIncrement(16);
        menuPanel.add(scrollMenu, BorderLayout.CENTER);

        // Fill in the product grid for the first time.
        rebuildMenuGrid();

        // East: Summary of the Order (Side)
        JPanel sideCart = createSideCart();

        add(menuPanel, BorderLayout.CENTER);
        add(sideCart, BorderLayout.EAST);
    }

    /**
     * Recreates the product grid. Useful for resetting the buttons after a sale.
     */
    private void rebuildMenuGrid() {
        menuGridPanel.removeAll();
        List<Product> products = controller.getAvailableProducts();
        for (Product p : products) {
            menuGridPanel.add(createProductCard(p));
        }
        menuGridPanel.revalidate();
        menuGridPanel.repaint();
    }

    /**
     * Build the card with smart button swapping (Add <-> Quantity)
     */
    private JPanel createProductCard(Product product) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        card.setBackground(Color.WHITE);

        // Image
JLabel lblImage = new JLabel();
        lblImage.setAlignmentX(Component.CENTER_ALIGNMENT);
        String imagePath = "data/product_images/" + product.getId();
        
        File filePng = new File(imagePath + ".png");
        File fileJpg = new File(imagePath + ".jpg");
        File fileJpeg = new File(imagePath + ".jpeg");
        
        // Adicionando a verificação do .jpeg no final da cadeia
        String finalPath = filePng.exists() ? filePng.getPath() : 
                           (fileJpg.exists() ? fileJpg.getPath() : 
                           (fileJpeg.exists() ? fileJpeg.getPath() : null));

        if (finalPath != null) {
            ImageIcon icon = new ImageIcon(new ImageIcon(finalPath).getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH));
            lblImage.setIcon(icon);
        } else {
            lblImage.setText("[ Sem Foto ]");
            lblImage.setPreferredSize(new Dimension(120, 120));
        }

        JLabel lblName = new JLabel(product.getName());
        lblName.setFont(new Font("Arial", Font.BOLD, 14));
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblPrice = new JLabel();
        lblPrice.setAlignmentX(Component.CENTER_ALIGNMENT);
        boolean isAvailable = product.getStockQuantity() > 0;

        if (!isAvailable) {
            lblPrice.setText("ESGOTADO");
            lblPrice.setFont(new Font("Arial", Font.BOLD, 14));
            lblPrice.setForeground(new Color(231, 76, 60)); // Red flag
        } else if (product.isStockLow()) { 
            lblPrice.setText("R$ " + String.format("%.2f", product.getPrice()) + " (Apenas " + product.getStockQuantity() + " un!)");
            lblPrice.setFont(new Font("Arial", Font.BOLD, 12));
            lblPrice.setForeground(new Color(230, 126, 34)); // Orange flag
        } else {
            lblPrice.setText("R$ " + String.format("%.2f", product.getPrice()));
            lblPrice.setFont(new Font("Arial", Font.PLAIN, 14));
            lblPrice.setForeground(Color.BLACK);
        }

        JPanel actionContainer = new JPanel(new CardLayout());
        actionContainer.setBackground(Color.WHITE);
        actionContainer.setMaximumSize(new Dimension(200, 40));

        // Add Button
        JPanel panelAdd = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelAdd.setBackground(Color.WHITE);
        JButton btnAdd = new JButton("Adicionar");
        btnAdd.setBackground(new Color(52, 152, 219));
        btnAdd.setForeground(Color.WHITE);
        
        // Se estiver esgotado, desativa visualmente o botão de adicionar
        if (!isAvailable) {
            btnAdd.setEnabled(false);
            btnAdd.setBackground(Color.LIGHT_GRAY);
        }
        panelAdd.add(btnAdd);

        // Quantity Controls [- 1 +]
        JPanel panelQty = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelQty.setBackground(Color.WHITE);
        JButton btnMinus = new JButton("-");
        JLabel lblQty = new JLabel("1");
        lblQty.setPreferredSize(new Dimension(20, 20));
        lblQty.setHorizontalAlignment(SwingConstants.CENTER);
        JButton btnPlus = new JButton("+");
        
        panelQty.add(btnMinus);
        panelQty.add(lblQty);
        panelQty.add(btnPlus);

        // Add the two cards to the container.
        actionContainer.add(panelAdd, "ADD_STATE");
        actionContainer.add(panelQty, "QTY_STATE");

        CardLayout cl = (CardLayout) actionContainer.getLayout();

        // Clicks
        btnAdd.addActionListener(e -> {
            try {
                controller.addItemToCurrentOrder(product, 1);
                lblQty.setText("1");
                cl.show(actionContainer, "QTY_STATE"); // switch to + -
                refreshCartUI();
            } catch (OutOfStockException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Estoque Baixo", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnPlus.addActionListener(e -> {
            try {
                controller.addItemToCurrentOrder(product, 1);
                int currentQty = controller.getCurrentOrder().getItems().get(product);
                lblQty.setText(String.valueOf(currentQty));
                refreshCartUI();
            } catch (OutOfStockException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Estoque Baixo", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnMinus.addActionListener(e -> {
            controller.removeItemFromCurrentOrder(product, 1);
            Integer currentQty = controller.getCurrentOrder().getItems().get(product);
            
            if (currentQty == null || currentQty == 0) {
                cl.show(actionContainer, "ADD_STATE"); // back to "Adicionar"
            } else {
                lblQty.setText(String.valueOf(currentQty));
            }
            refreshCartUI();
        });

        card.add(Box.createVerticalStrut(10));
        card.add(lblImage);
        card.add(lblName);
        card.add(lblPrice);
        card.add(actionContainer); // It adds a dynamic area instead of fixed controls.
        card.add(Box.createVerticalStrut(10));

        return card;
    }

    private JPanel createSideCart() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setPreferredSize(new Dimension(300, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Resumo do Pedido"));

        String[] cols = {"Produto", "Qtd", "Subtotal"};
        cartModel = new DefaultTableModel(cols, 0);
        cartTable = new JTable(cartModel);
        cartTable.setDefaultEditor(Object.class, null); 
        
        JPanel totalsPanel = new JPanel(new GridLayout(4, 1));
        lblSubtotal = new JLabel("Subtotal: R$ 0.00");
        lblTax = new JLabel("Taxa: R$ 0.00");
        lblTotal = new JLabel("Total: R$ 0.00");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 16));
        
        JButton btnFinalize = new JButton("Finalizar Pedido");
        btnFinalize.setBackground(new Color(39, 174, 96));
        btnFinalize.setForeground(Color.WHITE);
        btnFinalize.addActionListener(e -> showPaymentScreen());

        totalsPanel.add(lblSubtotal);
        totalsPanel.add(lblTax);
        totalsPanel.add(lblTotal);
        totalsPanel.add(btnFinalize);

        panel.add(new JScrollPane(cartTable), BorderLayout.CENTER);
        panel.add(totalsPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshCartUI() {
        cartModel.setRowCount(0);
        Map<Product, Integer> items = controller.getCurrentOrder().getItems();
        for (Map.Entry<Product, Integer> entry : items.entrySet()) {
            cartModel.addRow(new Object[]{
                entry.getKey().getName(), 
                entry.getValue(), 
                "R$ " + String.format("%.2f", entry.getKey().getPrice() * entry.getValue())
            });
        }
        lblSubtotal.setText("Subtotal: R$ " + String.format("%.2f", controller.getCurrentOrder().getSubtotal()));
        lblTax.setText("Taxa: R$ " + String.format("%.2f", controller.getCurrentOrder().getTax()));
        lblTotal.setText("Total: R$ " + String.format("%.2f", controller.getCurrentOrder().getTotal()));
    }

    private void showPaymentScreen() {
        if (controller.getCurrentOrder().getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Carrinho vazio!");
            return;
        }

        Object[] options = {"PIX", "Cartão de Crédito", "Cartão de Débito", "Cancelar"};
        int selection = JOptionPane.showOptionDialog(this, 
                "Selecione a forma de pagamento:", 
                "Pagamento", 
                JOptionPane.DEFAULT_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                null, options, options[0]);

        if (selection >= 0 && selection < 3) {
            PaymentMethod method = (selection == 0) ? PaymentMethod.PIX: 
                                   (selection == 1) ? PaymentMethod.CREDIT_CARD : PaymentMethod.DEBIT_CARD;
            try {
                String receipt = controller.finalizeCurrentOrder(method);
                JOptionPane.showMessageDialog(this, receipt, "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                
                // Prepare the clean screen for the next customer.
                controller.startNewOrder();
                rebuildMenuGrid(); // Resets all buttons back to "Add"
                refreshCartUI();   // Clear the sidebar and the values.
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage());
            }
        }
    }
}