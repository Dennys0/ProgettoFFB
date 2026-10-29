package com.alchemist.hangover.model;

public class Item {
    private String id;
    private String name;
    private String description;
    private String iconPath;

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