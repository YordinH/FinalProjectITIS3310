package domain;

import java.util.ArrayList;
import java.util.List;

public class Outfit {
    private int id;
    private int ownerId;
    private String name;
    private boolean favorite;
    private int wearCount;
    private List<String> tags;
    private List<ClothingItem> items;

    public Outfit(int id, int ownerId, String name) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.favorite = false;
        this.wearCount = 0;
        this.tags = new ArrayList<>();
        this.items = new ArrayList<>();
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

    public List<ClothingItem> getItems() {
        return items;
    }

    public void addItem(ClothingItem item) {
        items.add(item);
    }

    public void removeItem(ClothingItem item) {
        items.remove(item);
    }

    public boolean isFavorite() {
        return favorite;
    }

    public int getWearCount() {
        return wearCount;
    }

    public void markFavorite() {
        favorite = true;
    }

    public void incrementWearCount() {
        wearCount++;
    }
}
