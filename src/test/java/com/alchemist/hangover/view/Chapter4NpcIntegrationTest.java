package com.alchemist.hangover.view;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class Chapter4NpcIntegrationTest {
    @Test void allMuspelheimRoomsAndDialoguesAreBundled() {
        var layout = ChapterLayouts.forChapter(4);
        assertEquals(7, layout.backgrounds().size());
        for (String image : layout.backgrounds().values()) {
            assertNotNull(getClass().getResource(image), "PNG mancante: " + image);
        }
        assertEquals(6, Chapter4Panel.NPCS.size());
        for (var npc : Chapter4Panel.NPCS) {
            assertNotNull(getClass().getResource(npc.dialogue()),
                    "Dialogo NPC mancante: " + npc.dialogue());
            assertTrue(npc.bounds().x >= 0 && npc.bounds().y >= 80);
            assertTrue(npc.bounds().x + npc.bounds().width <= 1280);
            assertTrue(npc.bounds().y + npc.bounds().height <= 720);
        }
        for (String sprite : List.of(
                "/images/chapter4/npc/berserker.png",
                "/images/chapter4/npc/valkyrie.png",
                "/images/chapter4/npc/oldman.png",
                "/images/chapter4/npc/blacksmith.png",
                "/images/chapter4/npc/boss.png",
                "/images/chapter4/items/fenrir_ash.png",
                "/images/chapter4/items/ember.png",
                "/images/chapter4/items/magma.png",
                "/images/chapter4/items/sulfur.png",
                "/images/chapter4/items/ice_tear.png")) {
            assertNotNull(getClass().getResource(sprite), "Sprite non trovato: " + sprite);
        }
    }
}
