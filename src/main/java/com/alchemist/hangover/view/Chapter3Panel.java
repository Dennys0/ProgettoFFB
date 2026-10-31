package com.alchemist.hangover.view;

import javax.swing.*;

/**
 * Capitolo III - Terra: Il Dominio di Ymir.
 *
 * Prima milestone: navigazione delle sette stanze PNG.
 * I PNG di Gunnar, Ymir e degli oggetti saranno integrati nelle prossime
 * milestone, insieme a dialoghi, enigma runico e calderone.
 */
public final class Chapter3Panel extends AbstractChapterPanel {

    public Chapter3Panel(JFrame mainFrame) {
        initializeChapterBase(mainFrame,
                Chapter3World.backgrounds(),
                Chapter3World.exits());
        loadRoom(1);
    }
}
