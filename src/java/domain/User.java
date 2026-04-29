package domain;

import java.util.ArrayList;
import java.util.List;

public abstract class User {
    private int id;
    private String name;
    private List<ClothingItem> wardrobe;
    private List<List<ClothingItem>> savedOutfits;

    public User(int id, String name) {
        this.id = id;
        this.name = name;
        this.wardrobe = new ArrayList<>();
        this.savedOutfits = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public abstract String getRole();

    public List<ClothingItem> getWardrobe() {
        return wardrobe;
    }

    public void addClothingItem(ClothingItem item) {
        wardrobe.add(item);
    }
}
