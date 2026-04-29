package contracts;

import domain.ClothingItem;
import domain.ClothingType;
import domain.Outfit;
import java.util.List;

public interface IRandomizer {
    Outfit generate(List<ClothingItem> pool, List<ClothingItem> locked, List<ClothingType> disabled);
}
