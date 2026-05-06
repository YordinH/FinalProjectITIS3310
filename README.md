# Virtual Wardrobe System

A console-based Java application for managing a clothing wardrobe and generating random outfits.

## How to Run

Requires Java 11+

```bash
# Compile
javac -cp "lib/*" -d src/java/out $(find src/java -name "*.java" | grep -v out)

# Run
java -cp "src/java/out:lib/*" Program
```

The program loads 4 sample items on startup. Use the numbered menu to add clothing, view your wardrobe, or generate a random outfit.

---

## Requirements Checklist

### 1. Console Input / Output
`Program.java` - `main()` runs a `while` loop reading user input via `Scanner` (line 13). `printMenu()` prints a numbered menu (line 42), and each helper method reads from and writes to the console.

### 2. First Instance of Inheritance
`User.java` (line 6) is an abstract base class. `Admin.java` (line 3) and `Client.java` (line 6) both extend `User`, inheriting `id`, `name`, `wardrobe`, and the `getRole()` contract.

### 3. Second Instance of Inheritance
`ClothingItem.java` (line 6) is an abstract base class. `Top.java` (line 3), `Bottoms.java` (line 3), `Footwear.java` (line 3), and `Headwear.java` (line 3) all extend `ClothingItem`, inheriting shared fields and overriding the abstract methods.

### 4. First Interface / Implementation
`IClothingRepository.java` defines `save`, `remove`, `getById`, and `getAll`. `ClothingRepository.java` (line 11) implements it with a `HashMap`-backed Singleton.

### 5. Second Interface / Implementation
`IRandomizer.java` defines `generate(pool, locked, disabled)`. `RandomOutfitGenerator.java` (line 11) implements it, randomly selecting one item per `ClothingType` from the pool while respecting locked items and disabled categories.

### 6. Third Interface / Implementation
`IOutfitRepository.java` defines `save`, `remove`, `getById`, and `getAll`. `OutfitRepository.java` (line 10) implements it with the same Singleton + `HashMap` pattern as `ClothingRepository`.

### 7. First Use of Polymorphism
`getRole()` is declared abstract in `User.java` (line 27). Calling it on a `User` reference dispatches to `Admin.getRole()` (line 9) or `Client.getRole()` (line 15) at runtime.

### 8. Second Use of Polymorphism
`getDisplayLabel()` is declared abstract in `ClothingItem.java` (line 71). Each subclass provides its own format. `viewWardrobe()` in `Program.java` (line 103) calls it on a `List<ClothingItem>` without knowing the subtype.

### 9. Struct (Language Equivalent)
`Category.java` is a value object with two fields (`type: ClothingType`, `description: String`) and only getters, no behavior. This is the Java equivalent of a C# struct.

### 10. Enum
`ClothingType.java` (line 3) defines `TOP`, `BOTTOMS`, `FOOTWEAR`, `HEADWEAR`. `Season.java` (line 3) defines `Summer`, `Fall`, `Winter`, `Spring`.

### 11. First Design Pattern - Singleton
`ClothingRepository.getInstance()` (line 20) and `OutfitRepository.getInstance()` (line 18) ensure only one instance of each repository exists for the lifetime of the application.

### 12. Second Design Pattern - Factory
`ClothingFactory.create(type, ...)` (line 6) takes a `ClothingType` and returns the correct `ClothingItem` subclass. Called in `Program.addClothingItem()` so the UI never directly instantiates clothing subclasses.

### 13. Third Design Pattern - Strategy
`IRandomizer` is a Strategy interface. `RandomOutfitGenerator` (line 11) is the concrete strategy. `Program.main()` holds a reference typed to `IRandomizer`, so the generation algorithm can be swapped without touching any other code.

### 14. Data Structure
`ClothingRepository` stores items in a `HashMap<Integer, ClothingItem>` (line 17) for O(1) lookup by ID. `ClothingItem.tags` and `Outfit.items` use `ArrayList`. `RandomOutfitGenerator.generate()` builds candidate lists using `ArrayList` (line 32).
