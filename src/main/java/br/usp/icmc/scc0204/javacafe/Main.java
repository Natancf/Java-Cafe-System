package main.java.br.usp.icmc.scc0204.javacafe;

import main.java.br.usp.icmc.scc0204.javacafe.controller.CafeController;
import main.java.br.usp.icmc.scc0204.javacafe.model.*;
import main.java.br.usp.icmc.scc0204.javacafe.view.MainFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {

        
        try {
            // Initialize the engine and the CSV database
            CafeSystemContract system = new CafeSystem();
            CafeController controller = new CafeController(system);

            // Launch the GUI on the correct Swing thread
            SwingUtilities.invokeLater(() -> {
                MainFrame frame = new MainFrame(controller);
                frame.setVisible(true);
            });

        } catch (Exception e) {
            System.err.println("Error initializing the system:");
            e.printStackTrace();
        }
    }
}