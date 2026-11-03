package com.alchemist.hangover.controller;

/** Diagnostica facoltativa: -Dalchemist.profile=true. Non modifica il gameplay. */
public final class PerformanceLog {
    private PerformanceLog() { }
    /**
     * Calcola i millisecondi trascorsi dal valore di System.nanoTime ricevuto. Scrive sul
     * terminale solo quando la misurazione è abilitata.
     */
    public static void finished(String operation, long started) {
        if (Boolean.getBoolean("alchemist.profile"))
            System.out.printf(java.util.Locale.ROOT, "[PERF] %s: %.2f ms (%s)%n",
                    operation, (System.nanoTime() - started) / 1_000_000.0,
                    Thread.currentThread().getName());
    }
}
