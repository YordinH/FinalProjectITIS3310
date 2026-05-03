package repositories;

import contracts.IOutfitRepository;
import domain.Outfit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OutfitRepository implements IOutfitRepository {
    private static OutfitRepository instance;
    private final Map<Integer, Outfit> outfits;

    private OutfitRepository() {
        this.outfits = new HashMap<>();
    }

    public static OutfitRepository getInstance() {
        if (instance == null) {
            instance = new OutfitRepository();
        }
        return instance;
    }

    @Override
    public void save(Outfit outfit) {
        outfits.put(outfit.getId(), outfit);
    }

    @Override
    public void remove(int outfitId) {
        outfits.remove(outfitId);
    }

    @Override
    public Outfit getById(int outfitId) {
        return outfits.get(outfitId);
    }

    @Override
    public List<Outfit> getAll() {
        return new ArrayList<>(outfits.values());
    }
}
