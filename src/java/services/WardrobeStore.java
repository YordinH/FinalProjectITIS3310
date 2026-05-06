package services;

import com.google.gson.*;
import contracts.IClothingRepository;
import domain.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WardrobeStore {
    private static final String STORE_FILE = "wardrobe.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void save(List<User> users, List<ClothingItem> items, List<Outfit> outfits) {
        JsonObject root = new JsonObject();

        JsonArray usersArr = new JsonArray();
        for (User user : users) {
            JsonObject userObj = new JsonObject();
            userObj.addProperty("id", user.getId());
            userObj.addProperty("name", user.getName());
            userObj.addProperty("role", user.getRole());
            usersArr.add(userObj);
        }
        root.add("users", usersArr);

        JsonArray arr = new JsonArray();
        for (ClothingItem item : items) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", item.getId());
            obj.addProperty("ownerId", item.getOwnerId());
            obj.addProperty("name", item.getName());
            obj.addProperty("imageFilePath", item.getImageFilePath() != null ? item.getImageFilePath() : "");
            obj.addProperty("color", item.getColor());
            obj.addProperty("season", item.getSeason().name());
            obj.addProperty("type", item.getCategory().getType().name());
            obj.addProperty("subtypeAttr", item.getSubtypeAttribute());
            obj.addProperty("favorite", item.isFavorite());
            obj.addProperty("wearCount", item.getWearCount());
            JsonArray tags = new JsonArray();
            for (String tag : item.getTags()) tags.add(tag);
            obj.add("tags", tags);
            arr.add(obj);
        }
        root.add("items", arr);

        JsonArray outfitsArr = new JsonArray();
        for (Outfit outfit : outfits) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", outfit.getId());
            obj.addProperty("ownerId", outfit.getOwnerId());
            obj.addProperty("name", outfit.getName());
            obj.addProperty("favorite", outfit.isFavorite());
            obj.addProperty("wearCount", outfit.getWearCount());
            JsonArray itemIds = new JsonArray();
            for (ClothingItem item : outfit.getItems()) itemIds.add(item.getId());
            obj.add("itemIds", itemIds);
            outfitsArr.add(obj);
        }
        root.add("outfits", outfitsArr);

        try (FileWriter fw = new FileWriter(STORE_FILE)) {
            gson.toJson(root, fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        File f = new File(STORE_FILE);
        if (!f.exists()) return users;
        try (FileReader fr = new FileReader(f)) {
            JsonElement root = gson.fromJson(fr, JsonElement.class);
            if (!root.isJsonObject()) return users;
            JsonObject obj = root.getAsJsonObject();
            // new format: "users" array
            if (obj.has("users")) {
                for (JsonElement el : obj.getAsJsonArray("users")) {
                    users.add(parseUser(el.getAsJsonObject()));
                }
            // old format: single "user" object
            } else if (obj.has("user")) {
                users.add(parseUser(obj.getAsJsonObject("user")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    private User parseUser(JsonObject userObj) {
        int id = userObj.get("id").getAsInt();
        String name = userObj.get("name").getAsString();
        String role = userObj.get("role").getAsString();
        return role.equals("Admin") ? new Admin(id, name) : new Client(id, name);
    }

    public List<ClothingItem> loadItems() {
        List<ClothingItem> items = new ArrayList<>();
        File f = new File(STORE_FILE);
        if (!f.exists()) return items;
        try (FileReader fr = new FileReader(f)) {
            JsonElement root = gson.fromJson(fr, JsonElement.class);
            JsonArray arr;
            if (root.isJsonArray()) {
                arr = root.getAsJsonArray(); // old format
            } else if (root.isJsonObject() && root.getAsJsonObject().has("items")) {
                arr = root.getAsJsonObject().getAsJsonArray("items");
            } else {
                return items;
            }
            for (JsonElement el : arr) {
                JsonObject obj = el.getAsJsonObject();
                int id = obj.get("id").getAsInt();
                int ownerId = obj.get("ownerId").getAsInt();
                String name = obj.get("name").getAsString();
                String imageFilePath = obj.get("imageFilePath").getAsString();
                String color = obj.get("color").getAsString();
                Season season = Season.valueOf(obj.get("season").getAsString());
                ClothingType type = ClothingType.valueOf(obj.get("type").getAsString());
                String subtypeAttr = obj.get("subtypeAttr").getAsString();
                ClothingItem item = ClothingFactory.create(type, id, ownerId, name, imageFilePath, color, season, subtypeAttr);
                JsonArray tags = obj.get("tags").getAsJsonArray();
                for (JsonElement tag : tags) item.addTag(tag.getAsString());
                items.add(item);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return items;
    }

    public List<Outfit> loadOutfits(IClothingRepository clothingRepo) {
        List<Outfit> outfits = new ArrayList<>();
        File f = new File(STORE_FILE);
        if (!f.exists()) return outfits;
        try (FileReader fr = new FileReader(f)) {
            JsonElement root = gson.fromJson(fr, JsonElement.class);
            if (!root.isJsonObject()) return outfits;
            JsonObject rootObj = root.getAsJsonObject();
            if (!rootObj.has("outfits")) return outfits;
            for (JsonElement el : rootObj.getAsJsonArray("outfits")) {
                JsonObject obj = el.getAsJsonObject();
                int id = obj.get("id").getAsInt();
                int ownerId = obj.get("ownerId").getAsInt();
                String name = obj.get("name").getAsString();
                Outfit outfit = new Outfit(id, ownerId, name);
                for (JsonElement idEl : obj.get("itemIds").getAsJsonArray()) {
                    ClothingItem item = clothingRepo.getById(idEl.getAsInt());
                    if (item != null) outfit.addItem(item);
                }
                outfits.add(outfit);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outfits;
    }
}
