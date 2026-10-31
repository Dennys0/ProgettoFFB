package com.alchemist.hangover.view;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ItemOriginCatalogTest {
    @Test
    void allExistingItemsHaveAnExistingRoomPreview() {
        List<String> ids = List.of(
                "MAPPA_CIELO", "NEBBIA_NIFLHEIM", "PIUMA_FOLGORE",
                "POLLINE_YGGDRASIL", "RUGIADA_ASGARD", "CHIAVE_DRAGO",
                "SOFFIO_HRAESVELGR", "DIARIO_CAPITANO", "CORALLO_RAN",
                "INCHIOSTRO_KRAKEN", "SALE_JORMUNGANDR", "ACQUA_LETE",
                "SCHIUMA_AEGIR",
                "MUSCHIO_MIDGARD", "RADICE_NIDHOGG", "FRAMMENTO_YGGDRASIL",
                "POLVERE_OSSA_GIGANTE", "LACRIMA_YMIR"
        );

        for (String id : ids) {
            ItemOriginCatalog.Origin origin = ItemOriginCatalog.find(id);
            assertTrue(origin.chapter() > 0, "Origine mancante per " + id);
            assertNotNull(origin.scenePath());
            assertNotNull(getClass().getResource(origin.scenePath()),
                    "Anteprima dello scenario mancante per " + id);
        }
    }

    @Test
    void futureItemsHaveASafeFallback() {
        ItemOriginCatalog.Origin unknown = ItemOriginCatalog.find("CH3_FUTURE_ITEM");
        assertEquals(0, unknown.chapter());
        assertNull(unknown.scenePath());
        assertNotNull(unknown.place());
    }
}
