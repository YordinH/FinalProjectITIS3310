package contracts;

import domain.ClothingItem;
import java.util.List;

public interface IClothingRepository {
    void save(ClothingItem item);
    void remove(int itemId);
    ClothingItem getById(int itemId);
    List<ClothingItem> getAll();
}
