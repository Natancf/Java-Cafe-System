package main.java.br.usp.icmc.scc0204.javacafe.view;

import javax.swing.*;
import java.awt.*;
import main.java.br.usp.icmc.scc0204.javacafe.controller.CafeController;

public class MainFrame extends JFrame {

    private CafeController controller;
    private JTabbedPane tabbedPane;

    public MainFrame(CafeController controller) {
        this.controller = controller;

        // Basic window configurations
        setTitle("Java Café - Management and POS System");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centers the window on the screen

        // Initialize the tab manager
        tabbedPane = new JTabbedPane();

        JPanel POSPanel = new POSPanel(controller);
        JPanel inventoryPanel = createPlaceholder("Inventory Module - Under Development");
        JPanel dashboardPanel = createPlaceholder("Dashboard Module - Under Development");

        // Add tabs with their respective titles
        tabbedPane.addTab("Painel de Venda", POSPanel);
        tabbedPane.addTab("Estoque", inventoryPanel);
        tabbedPane.addTab("Dashboards & Reports", dashboardPanel);

        // Add the tabbed pane to the main window container
        add(tabbedPane);
    }

    /**
     * Helper method to create a temporary gray screen with a centered message.
     */
    private JPanel createPlaceholder(String message) {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel(message);
        label.setFont(new Font("Arial", Font.ITALIC, 16));
        label.setForeground(Color.GRAY);
        panel.add(label);
        return panel;
    }
}