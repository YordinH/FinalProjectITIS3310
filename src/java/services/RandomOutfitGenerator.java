package services;

import contracts.IRandomizer;
import domain.ClothingItem;
import domain.ClothingType;
import domain.Outfit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomOutfitGenerator implements IRandomizer {
    private Random rng;

    public RandomOutfitGenerator() {
        this.rng = new Random();
    }

    @Override
    public Outfit generate(List<ClothingItem> pool, List<ClothingItem> locked, List<ClothingType> disabled) {
        Outfit outfit = new Outfit(0, "Random Outfit");

        List<ClothingType> filled = new ArrayList<>();
        for (ClothingItem item : locked) {
            outfit.addItem(item);
            filled.add(item.getCategory().getType());
        }

        for (ClothingType type : ClothingType.values()) {
            if (disabled.contains(type) || filled.contains(type)) {
                continue;
            }
            List<ClothingItem> candidates = new ArrayList<>();
            for (ClothingItem item : pool) {
                if (item.getCategory().getType() == type) {
                    candidates.add(item);
                }
            }
            if (!candidates.isEmpty()) {
                outfit.addItem(candidates.get(rng.nextInt(candidates.size())));
            }
        }

        return outfit;
    }
}
