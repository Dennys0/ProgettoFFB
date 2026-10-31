package com.alchemist.hangover.view;

import java.util.Map;

/**
 * Provenienze narrative dei singoli oggetti.
 * I luoghi sono predefiniti dai capitoli, quindi rimangono validi anche dopo
 * aver caricato una partita H2 senza aggiungere nuovi campi ai salvataggi.
 * Aggiungere qui i futuri oggetti del capitolo 3.
 */
public final class ItemOriginCatalog {
    public record Origin(int chapter, String place, String scenePath) {
        public String chapterLabel() {
            return chapter <= 0 ? "Provenienza ignota" : "Capitolo " + roman(chapter);
        }
        public String shortLabel() {
            return chapter <= 0 ? "Luogo sconosciuto" : "Cap. " + roman(chapter) + "  ·  " + place;
        }
    }

    private static final Origin UNKNOWN = new Origin(0, "Luogo non ancora catalogato", null);

    private static final Map<String, Origin> ORIGINS = Map.ofEntries(
            Map.entry("MAPPA_CIELO", new Origin(1, "Stiva dello Skíðblaðnir", "/images/chapter1/room5_hold.png")),
            Map.entry("NEBBIA_NIFLHEIM", new Origin(1, "Stiva dello Skíðblaðnir", "/images/chapter1/room5_hold.png")),
            Map.entry("PIUMA_FOLGORE", new Origin(1, "Coffa dell'albero maestro", "/images/chapter1/room6_crowsnest.png")),
            Map.entry("POLLINE_YGGDRASIL", new Origin(1, "Prua del veliero volante", "/images/chapter1/room3_prow.png")),
            Map.entry("RUGIADA_ASGARD", new Origin(1, "Polena sulla prua", "/images/chapter1/room3_prow.png")),
            Map.entry("CHIAVE_DRAGO", new Origin(1, "Ricevuta da Martino, al timone", "/images/chapter1/room2_helm.png")),
            Map.entry("SOFFIO_HRAESVELGR", new Origin(1, "Dono di Hræsvelgr dopo lo scontro", "/images/chapter1/room7_boss.png")),
            Map.entry("DIARIO_CAPITANO", new Origin(2, "Balconata dei relitti", "/images/chapter2/room6_balcony.png")),
            Map.entry("CORALLO_RAN", new Origin(2, "Giardini sommersi", "/images/chapter2/room4_gardens.png")),
            Map.entry("INCHIOSTRO_KRAKEN", new Origin(2, "Giardini sommersi", "/images/chapter2/room4_gardens.png")),
            Map.entry("SALE_JORMUNGANDR", new Origin(2, "Fossa degli abissi", "/images/chapter2/room5_pit.png")),
            Map.entry("ACQUA_LETE", new Origin(2, "Fossa degli abissi", "/images/chapter2/room5_pit.png")),
            Map.entry("SCHIUMA_AEGIR", new Origin(2, "Dono di Rán dopo lo scontro", "/images/chapter2/room7_boss.png")),
            Map.entry("MUSCHIO_MIDGARD", new Origin(3, "Foresta Pietrificata", "/images/chapter3/room4_forest.png")),
            Map.entry("RADICE_NIDHOGG", new Origin(3, "Foresta Pietrificata", "/images/chapter3/room4_forest.png")),
            Map.entry("FRAMMENTO_YGGDRASIL", new Origin(3, "Miniere Basaltiche", "/images/chapter3/room5_miners.png")),
            Map.entry("POLVERE_OSSA_GIGANTE", new Origin(3, "Miniere Basaltiche", "/images/chapter3/room5_miners.png")),
            Map.entry("LACRIMA_YMIR", new Origin(3, "Cuore di Roccia — ricompensa di Ymir", "/images/chapter3/room7_boss.png"))
    );

    private ItemOriginCatalog() { }

    public static Origin find(String itemId) {
        return itemId == null ? UNKNOWN : ORIGINS.getOrDefault(itemId.toUpperCase(java.util.Locale.ROOT), UNKNOWN);
    }

    private static String roman(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            default -> Integer.toString(number);
        };
    }
}
