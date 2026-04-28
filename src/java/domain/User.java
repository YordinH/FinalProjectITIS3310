package domain;

public abstract class User {
      private int id;
    private String name;
    private List<ClothingItem> wardrobe;
    private List<List<ClothingItem>> savedOutfits;

    public User(int id, String name){
        this.id = id;
        this.name = name;
        this.wardrobe = new ArrayList<>();
        this.savedOutfits = new ArrayList<>();
    }

    public List<ClothingItem> getWardrobe() {
        return wardrobe;
    }
    public void addClothingItem(ClothingItem item){
        this.wardrobe.add(item);
}
