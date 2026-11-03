package com.alchemist.hangover.model;

/**
 * Dati descrittivi di un oggetto: identificatore, nome, descrizione e percorso dell’icona.
 * GameState gestisce raccolta e unicità nell’inventario.
 */
public class Item {
    private final String id;
    private final String name;
    private final String description;
    private final String iconPath;

    /**
     * Crea la descrizione dell’oggetto senza caricare l’immagine indicata dal percorso.
     */
    public Item(String id, String name, String description, String iconPath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconPath = iconPath;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getIconPath() { return iconPath; }
}