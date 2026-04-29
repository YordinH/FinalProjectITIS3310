package domain;

public class Footwear extends ClothingItem {
    private String shoeType;

    public Footwear(int id, int ownerId, String name, String imageFilePath, String color, Season season, String shoeType) {
        super(id, ownerId, name, imageFilePath, color, season, new Category(ClothingType.FOOTWEAR, "Footwear"));
        this.shoeType = shoeType;
    }

    @Override
    public String getDisplayLabel() {
        return getName() + " [" + shoeType + "]";
    }

    @Override
    public String getSubtypeAttribute() {
        return shoeType;
    }
}
