package domain;

public abstract class ClothingItem {
  private int id;
    private String name;
    private String imageFilePath;
    private String color;
    private Boolean favorite;
    private int wearCount;
    private List<String> tags = new ArrayList<>();
    private ClothingType category;
    private Season season;
    private WearCounter counter = new WearCounter();

    public String getName() {
        return name;
    }

    public void incrementWearCount() {
        this.wearCount++;
        counter.incrementWear();
    }

    public void addTag(String tag){
        tags.add(tag);
    }

    public void removeTag(String tag){
        tags.remove(tag);
    }
}
