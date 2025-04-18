package models;

public class Perk {
    private String name;
    private String description;
    private String iconPath;

    public Perk(String name, String description, String iconPath) {
        this.name = name;
        this.description = description;
        this.iconPath = iconPath;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getIconPath() {
        return iconPath;
    }

    @Override
    public String toString() {
        return name + " - " + description;
    }
}