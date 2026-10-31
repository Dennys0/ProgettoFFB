package com.alchemist.hangover.view;

import javax.swing.*;

/**
 * Capitolo III - Terra: Il Dominio di Ymir.
 *
 * Navigazione tra sette stanze e raccolta degli ingredienti della Terra.
 * Gunnar, l'enigma di Ymir e il calderone arriveranno nelle tappe successive.
 */
public final class Chapter3Panel extends AbstractChapterPanel {

    public Chapter3Panel(JFrame mainFrame) {
        initializeChapterBase(mainFrame,
                Chapter3World.backgrounds(),
                Chapter3World.exits());
        // Stanza 4: Foresta Pietrificata.
        addItemWorldButton("MUSCHIO_MIDGARD", "/images/items/item_moss.png",
                350, 445, 132, 98, 4, "Muschio di Midgard",
                "Un lichene brillante che emana un profumo amarognolo di terra pulita.");
        addItemWorldButton("RADICE_NIDHOGG", "/images/items/item_nidhogg.png",
                858, 460, 135, 110, 4, "Radice Nera di Nidhogg",
                "Una radice nerastra e pulsante, attraversata da vene cremisi.");

        // Stanza 5: Miniere Basaltiche.
        addItemWorldButton("FRAMMENTO_YGGDRASIL", "/images/items/item_fragment_yggdrasil.png",
                370, 485, 125, 90, 5, "Frammento di Yggdrasil",
                "Una scheggia di legno millenario che emette una debole luce dorata.");
        addItemWorldButton("POLVERE_OSSA_GIGANTE", "/images/items/item_bone.png",
                830, 465, 140, 100, 5, "Polvere di Ossa di Gigante",
                "Una polvere biancastra di ossa antiche, straordinariamente pesante.");

        loadRoom(1);
    }
}
