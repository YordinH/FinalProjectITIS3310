package domain;

public class Headwear extends ClothingItem {
    private String brimType;

    public Headwear(int id, int ownerId, String name, String imageFilePath, String color, Season season, String brimType) {
        super(id, ownerId, name, imageFilePath, color, season, new Category(ClothingType.HEADWEAR, "Headwear"));
        this.brimType = brimType;
    }

    @Override
    public String getDisplayLabel() {
        return getName() + " [" + brimType + " brim]";
    }

    @Override
    public String getSubtypeAttribute() {
        return brimType;
    }
}
