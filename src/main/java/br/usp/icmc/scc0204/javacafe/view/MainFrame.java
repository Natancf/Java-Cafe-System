package main.java.br.usp.icmc.scc0204.javacafe.view;

import javax.swing.*;
import java.awt.*;
import main.java.br.usp.icmc.scc0204.javacafe.controller.CafeController;

/**
 * Main application window for Java Café POS System.
 * Manages the tabbed interface for POS, Inventory, and Dashboard modules.y
 */
public class MainFrame extends JFrame {
    
    private static final String WINDOW_TITLE = "Java Café - Management and POS System";
    private static final int DEFAULT_WIDTH = 1024;
    private static final int DEFAULT_HEIGHT = 768;
    
    private final CafeController controller;
    private final JTabbedPane tabbedPane;
    
    /**
     * Constructs the main application frame.
     * @param controller The cafe controller instance (cannot be null)
     * @throws IllegalArgumentException if controller is null
     */
    public MainFrame(CafeController controller) {
        if (controller == null) {
            throw new IllegalArgumentException("Controller cannot be null");
        }
        
        this.controller = controller;
        this.tabbedPane = new JTabbedPane();
        
        initializeFrame();
        createTabs();
        setupWindowCloseHandler();
    }
    
    /**
     * Configures basic frame properties.
     */
    private void initializeFrame() {
        setTitle(WINDOW_TITLE);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Custom close handling
        setLocationRelativeTo(null); // Center on screen
        setLayout(new BorderLayout());
    }
    
    /**
     * Creates and adds all application tabs.
     */
    private void createTabs() {
        JPanel posPanel = new POSPanel(controller);
        JPanel inventoryPanel = new InventoryPanel(controller);
        JPanel dashboardPanel = new DashboardPanel(controller);
        
        tabbedPane.addTab("Painel de Venda", posPanel);
        tabbedPane.addTab("Estoque", inventoryPanel);
        tabbedPane.addTab("Dashboards & Reports", dashboardPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    /**
     * Sets up custom window closing handler with confirmation dialog.
     */
    private void setupWindowCloseHandler() {
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                confirmExit();
            }
        });
    }
    
    /**
     * Shows confirmation dialog before exiting the application.
     */
    private void confirmExit() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit Java Café?",
            "Confirm Exit",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            dispose(); // Release resources
            System.exit(0);
        }
    }
    
    /**
     * Returns the controller instance.
     * @return The cafe controller
     */
    public CafeController getController() {
        return controller;
    }
    
    /**
     * Returns the tabbed pane for programmatic tab selection.
     * @return The tabbed pane
     */
    public JTabbedPane getTabbedPane() {
        return tabbedPane;
    }
    
    /**
     * Programmatically switches to a specific tab.
     * @param tabIndex Index of the tab to select (0-based)
     */
    public void switchToTab(int tabIndex) {
        if (tabIndex >= 0 && tabIndex < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(tabIndex);
        }
    }
    
    /**
     * Programmatically switches to a specific tab by title.
     * @param tabTitle Title of the tab to select
     */
    public void switchToTab(String tabTitle) {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (tabbedPane.getTitleAt(i).equals(tabTitle)) {
                tabbedPane.setSelectedIndex(i);
                break;
            }
        }
    }
}