package services;

import domain.*;

public class ClothingFactory {
    public static ClothingItem create(ClothingType type, int id, int ownerId,
            String name, String imageFilePath, String color,
            Season season, String subtypeAttr) {
        switch (type) {
            case TOP:      return new Top(id, ownerId, name, imageFilePath, color, season, subtypeAttr);
            case BOTTOMS:  return new Bottoms(id, ownerId, name, imageFilePath, color, season, subtypeAttr);
            case FOOTWEAR: return new Footwear(id, ownerId, name, imageFilePath, color, season, subtypeAttr);
            case HEADWEAR: return new Headwear(id, ownerId, name, imageFilePath, color, season, subtypeAttr);
            default: throw new IllegalArgumentException("Unknown clothing type: " + type);
        }
    }
}
