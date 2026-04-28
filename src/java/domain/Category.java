package domain;

public class Category {
    private ClothingType type;
    private String description;

    public Category(ClothingType type, String description){
        this.type = type;
        this.description = description;
    }
}
