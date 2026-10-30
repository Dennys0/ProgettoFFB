package com.alchemist.hangover;

import com.alchemist.hangover.database.DatabaseManager;
import com.alchemist.hangover.view.MainFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Inizializza il database H2 locale e carica lo schema
        DatabaseManager.initDatabase();
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseManager::close));

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}