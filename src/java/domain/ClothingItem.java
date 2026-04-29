package domain;

import java.util.ArrayList;
import java.util.List;

public abstract class ClothingItem {
    private int id;
    private int ownerId;
    private String name;
    private String imageFilePath;
    private String color;
    private boolean favorite;
    private int wearCount;
    private Season season;
    private Category category;
    private List<String> tags;
    private WearCounter counter;

    protected ClothingItem(int id, int ownerId, String name, String imageFilePath, String color, Season season, Category category) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.imageFilePath = imageFilePath;
        this.color = color;
        this.season = season;
        this.category = category;
        this.favorite = false;
        this.wearCount = 0;
        this.tags = new ArrayList<>();
        this.counter = new WearCounter();
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public List<String> getTags() {
        return tags;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void incrementWearCount() {
        wearCount++;
        counter.incrementWear();
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    // Subclasses return a formatted label for display (e.g. "White T-Shirt [Short Sleeve]")
    public abstract String getDisplayLabel();

    // Subclasses return their unique subtype attribute value
    public abstract String getSubtypeAttribute();
}
