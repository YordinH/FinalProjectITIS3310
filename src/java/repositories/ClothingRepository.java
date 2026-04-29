package repositories;

import contracts.IClothingRepository;
import contracts.IFileService;
import domain.ClothingItem;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class ClothingRepository implements IClothingRepository {
    private static ClothingRepository instance;
    private Map<Integer, ClothingItem> items;
    private IFileService fileService;

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

    public void loadFromDisk() {
    }

    public void persistToDisk() {
    }
}
