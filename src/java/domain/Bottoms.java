package domain;

public class Bottoms extends ClothingItem {
    private String pantLength;

    public Bottoms(int id, int ownerId, String name, String imageFilePath, String color, Season season, String pantLength) {
        super(id, ownerId, name, imageFilePath, color, season, new Category(ClothingType.BOTTOMS, "Bottoms"));
        this.pantLength = pantLength;
    }

    @Override
    public String getDisplayLabel() {
        return getName() + " [" + pantLength + "]";
    }

    @Override
    public String getSubtypeAttribute() {
        return pantLength;
    }
}
