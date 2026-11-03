package com.alchemist.hangover;

import com.alchemist.hangover.database.DatabaseManager;
import com.alchemist.hangover.view.MainFrame;
import javax.swing.SwingUtilities;

/**
 * Punto di avvio: inizializza H2, registra la chiusura delle risorse e crea la finestra sul
 * thread degli eventi Swing (EDT).
 */
public class Main {
    /**
     * Inizializza i servizi prima della finestra; alla chiusura termina audio e H2. La
     * costruzione dei componenti è accodata sul thread Swing.
     */
    public static void main(String[] args) {
        long started = System.nanoTime();
        // Inizializza il database H2 locale e carica lo schema
        DatabaseManager.initDatabase();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            com.alchemist.hangover.controller.AudioManager.getInstance().close();
            DatabaseManager.close();
        }));

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
            com.alchemist.hangover.controller.PerformanceLog.finished("Avvio finestra", started);
        });
    }
}