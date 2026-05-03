package contracts;

import domain.Outfit;
import java.util.List;

public interface IOutfitRepository {
    void save(Outfit outfit);
    void remove(int outfitId);
    Outfit getById(int outfitId);
    List<Outfit> getAll();
}
