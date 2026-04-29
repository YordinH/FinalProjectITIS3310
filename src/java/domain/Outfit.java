package domain;

import java.util.ArrayList;
import java.util.List;

public class Outfit {
    private int id;
    private String name;
    private boolean favorite;
    private int wearCount;
    private List<String> tags;
    private List<ClothingItem> items;

    public Outfit(int id, String name) {
        this.id = id;
        this.name = name;
        this.favorite = false;
        this.wearCount = 0;
        this.tags = new ArrayList<>();
        this.items = new ArrayList<>();
    }

    public int getId() {
        return id;
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

    public void markFavorite() {
        favorite = true;
    }

    public void incrementWearCount() {
        wearCount++;
    }
}
