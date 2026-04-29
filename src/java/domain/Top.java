package domain;

public class Top extends ClothingItem {
    private String sleeveLength;

    public Top(int id, int ownerId, String name, String imageFilePath, String color, Season season, String sleeveLength) {
        super(id, ownerId, name, imageFilePath, color, season, new Category(ClothingType.TOP, "Tops"));
        this.sleeveLength = sleeveLength;
    }

    @Override
    public String getDisplayLabel() {
        return getName() + " [" + sleeveLength + " sleeve]";
    }

    @Override
    public String getSubtypeAttribute() {
        return sleeveLength;
    }
}
