package repositories;

import contracts.IClothingRepository;
import domain.ClothingItem;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class ClothingRepository implements IClothingRepository {
    private static ClothingRepository instance;
    private Map<Integer, ClothingItem> items;

    private ClothingRepository() {
        this.items = new HashMap<>();
    }

    public static ClothingRepository getInstance() {
        if (instance == null) {
            instance = new ClothingRepository();
        }
        return instance;
    }

    @Override
    public void save(ClothingItem item) {
        items.put(item.getId(), item);
    }

    @Override
    public void remove(int itemId) {
        items.remove(itemId);
    }

    @Override
    public ClothingItem getById(int itemId) {
        return items.get(itemId);
    }

    @Override
    public List<ClothingItem> getAll() {
        return new ArrayList<>(items.values());
    }

    @Override
    public List<ClothingItem> getByOwner(int ownerId) {
        List<ClothingItem> result = new ArrayList<>();
        for (ClothingItem item : items.values()) {
            if (item.getOwnerId() == ownerId) result.add(item);
        }
        return result;
    }


}
