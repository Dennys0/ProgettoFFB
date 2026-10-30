package com.alchemist.hangover.database;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.h2.tools.RunScript;

public class DatabaseManager {

    private static final String DB_DIR = "saves";
    private static final String DB_URL = "jdbc:h2:./" + DB_DIR + "/alchemist;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    /** Solo i test usano questa proprieta': sono ammessi esclusivamente DB H2 in memoria. */
    public static final String TEST_DATABASE_PROPERTY = "alchemist.test.database.url";

    /**
     * Restituisce una nuova connessione attiva al database H2.
     * Ciascun chiamante è responsabile della chiusura (tramite try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        String testingUrl = System.getProperty(TEST_DATABASE_PROPERTY);
        if (testingUrl != null) {
            if (!testingUrl.startsWith("jdbc:h2:mem:")) {
                throw new SQLException("Il database di test deve essere H2 in memoria");
            }
            return DriverManager.getConnection(testingUrl, DB_USER, DB_PASSWORD);
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Inizializza il database e applica lo schema SQL.
     */
    public static void initDatabase() {
        try {
            // Crea la cartella dei salvataggi se non esiste
            File dir = new File(DB_DIR);
            if (System.getProperty(TEST_DATABASE_PROPERTY) == null && !dir.exists()) {
                dir.mkdirs();
            }

            try (Connection conn = getConnection();
                 InputStream in = DatabaseManager.class.getResourceAsStream("/db/schema.sql")) {
                if (in != null) {
                    RunScript.execute(conn, new InputStreamReader(in, StandardCharsets.UTF_8));
                    System.out.println("[DatabaseManager] Schema H2 inizializzato"
                            + (System.getProperty(TEST_DATABASE_PROPERTY) == null
                                    ? " in: " + dir.getAbsolutePath()
                                    : " (solo memoria per test)"));
                } else {
                    System.err.println("[DatabaseManager] File /db/schema.sql non trovato tra le risorse!");
                }
            }
        } catch (Exception e) {
            System.err.println("[DatabaseManager] Errore inizializzazione database H2: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Esegue lo shutdown pulito del database H2.
     */
    public static void close() {
        try (Connection conn = getConnection();
             Statement st = conn.createStatement()) {
            st.execute("SHUTDOWN");
            System.out.println("[DatabaseManager] Database H2 chiuso correttamente.");
        } catch (Exception ignored) {}
    }
}
