package com.alchemist.hangover.view.inventory;

import java.util.Map;

/**
 * Provenienze narrative dei singoli oggetti.
 * I luoghi sono predefiniti dai capitoli, quindi rimangono validi anche dopo
 * aver caricato una partita H2 senza aggiungere nuovi campi ai salvataggi.
 * Aggiungere qui la provenienza di ogni nuovo oggetto dei capitoli.
 */
public final class ItemOriginCatalog {
    /** Provenienza dell’oggetto, con etichette brevi e anteprima della stanza. */
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
            Map.entry("MAPPA_CIELO", new Origin(1, "Scaffale nella stiva dello Skíðblaðnir", "/images/chapter1/room5_hold.png")),
            Map.entry("NEBBIA_NIFLHEIM", new Origin(1, "Stiva dello Skíðblaðnir", "/images/chapter1/room5_hold.png")),
            Map.entry("PIUMA_FOLGORE", new Origin(1, "Dono di Huginn nella coffa", "/images/chapter1/room6_crowsnest.png")),
            Map.entry("POLLINE_YGGDRASIL", new Origin(1, "Dono del mozzo sul ponte", "/images/chapter1/room1_deck.png")),
            Map.entry("RUGIADA_ASGARD", new Origin(1, "Cassa d’ottone a prua", "/images/chapter1/room3_prow.png")),
            Map.entry("CHIAVE_DRAGO", new Origin(1, "Ricevuta da Martino, al timone", "/images/chapter1/room2_helm.png")),
            Map.entry("SOFFIO_HRAESVELGR", new Origin(1, "Dono di Hræsvelgr dopo lo scontro", "/images/chapter1/room7_boss.png")),
            Map.entry("DIARIO_CAPITANO", new Origin(2, "Dono del Capitano sulla Balconata", "/images/chapter2/room6_balcony.png")),
            Map.entry("CORALLO_RAN", new Origin(2, "Giardini sommersi", "/images/chapter2/room4_gardens.png")),
            Map.entry("INCHIOSTRO_KRAKEN", new Origin(2, "Giardini sommersi", "/images/chapter2/room4_gardens.png")),
            Map.entry("SALE_JORMUNGANDR", new Origin(2, "Dono di Jörmungandr nella Fossa", "/images/chapter2/room5_pit.png")),
            Map.entry("ACQUA_LETE", new Origin(2, "Dono di Marmæl nella sala sommersa", "/images/chapter2/room1_hall.png")),
            Map.entry("SCHIUMA_AEGIR", new Origin(2, "Dono di Rán dopo lo scontro", "/images/chapter2/room7_boss.png")),
            Map.entry("MUSCHIO_MIDGARD", new Origin(3, "Foresta Pietrificata", "/images/chapter3/room4_forest.png")),
            Map.entry("RADICE_NIDHOGG", new Origin(3, "Dono del Vanir nella Foresta Pietrificata", "/images/chapter3/room4_forest.png")),
            Map.entry("FRAMMENTO_YGGDRASIL", new Origin(3, "Miniere Basaltiche", "/images/chapter3/room5_miners.png")),
            Map.entry("POLVERE_OSSA_GIGANTE", new Origin(3, "Dono di Billingr nelle miniere", "/images/chapter3/room5_miners.png")),
            Map.entry("LACRIMA_YMIR", new Origin(3, "Cuore di Roccia — ricompensa di Ymir", "/images/chapter3/room7_boss.png")),
            Map.entry("CENERE_FENRIR", new Origin(4, "La Cenere dei Mondi", "/images/chapter4/room_4.png")),
            Map.entry("ZOLFO_LOKI", new Origin(4, "Dono del vagabondo", "/images/chapter4/room_4.png")),
            Map.entry("MAGMA_SVARTALFAHEIMR", new Origin(4, "Dono del Figlio di Ivaldi", "/images/chapter4/room_5.png")),
            Map.entry("LACRIMA_HEL", new Origin(4, "Dono di Hela sulla rupe", "/images/chapter4/room_6.png")),
            Map.entry("BRACE_MUSPELHEIM", new Origin(4, "Ricompensa di Surtur nella Fornace", "/images/chapter4/room_boss.png"))
    );

    private ItemOriginCatalog() { }

    /**
     * Restituisce la provenienza ignorando maiuscole e minuscole; per identificatori sconosciuti
     * usa una descrizione neutra.
     */
    public static Origin find(String itemId) {
        return itemId == null ? UNKNOWN : ORIGINS.getOrDefault(itemId.toUpperCase(java.util.Locale.ROOT), UNKNOWN);
    }

    private static String roman(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            default -> Integer.toString(number);
        };
    }
}
