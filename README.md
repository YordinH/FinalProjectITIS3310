# Virtual Wardrobe System

## 1. Project Overview

The Virtual Wardrobe System is a console-based Java application that lets users manage a personal clothing wardrobe and generate randomized outfits from imported clothing images. Users create a profile (Client or Admin), import clothing photos with automatic background removal, browse and edit their wardrobe, generate outfit combinations, and save favorites — all persisted across sessions via JSON.

**Core features:**
- Multi-user accounts with Client and Admin roles
- Import clothing images with rembg background removal
- Persistent outfit viewer (Swing) stacking Headwear → Top → Bottoms → Footwear
- Random outfit generation with save / discard / regenerate flow
- JSON persistence for users, clothing items, and saved outfits
- Admin view: inspect all users' wardrobes and generate cross-user outfits

**Assumptions / constraints:**
- Background removal requires `rembg` installed via Homebrew Python (`/opt/homebrew/bin/rembg`). If unavailable the original image is used as a fallback.
- Images must be placed in the `images/` folder before importing.
- Java 11+ required.

---

## 2. Build & Run Instructions

**Tools:** JDK 11+, Gson 2.10.1 (included in `lib/gson.jar`)

**Step 1 — Create the output directory (first time only):**
```bash
mkdir -p src/java/out
```

**Step 2 — Compile:**
```bash
javac -cp lib/gson.jar -d src/java/out $(find src/java -name "*.java" | grep -v out)
```

**Step 3 — Run:**
```bash
java -cp lib/gson.jar:src/java/out Program
```

**Windows (semicolon separator, explicit source paths):**
```bat
mkdir src\java\out
javac -cp lib/gson.jar -d src/java/out src/java/Program.java src/java/domain/*.java src/java/services/*.java src/java/repositories/*.java src/java/contracts/*.java
java -cp "lib/gson.jar;src/java/out" Program
```

**Optional — rembg background removal:**
```bash
pip install "rembg[cpu,cli]"
```
If not installed, clothing images are imported without background removal and the program continues normally.

---

## 3. Required OOP Features

| OOP Feature | File | Lines | Reasoning / Purpose |
|---|---|---|---|
| **Inheritance #1 — base** | `domain/ClothingItem.java` | 6–93 | Abstract base class holding all shared clothing fields (id, name, color, season, wearCount, tags). Declares abstract methods `getDisplayLabel()` and `getSubtypeAttribute()` that all subclasses must implement. |
| **Inheritance #1 — derived** | `domain/Top.java`, `domain/Bottoms.java`, `domain/Footwear.java`, `domain/Headwear.java` | 1–end each | Each subclass extends `ClothingItem`, calls `super(...)` to set shared state, adds a type-specific field (e.g. `sleeveLength`), and overrides both abstract methods with clothing-appropriate formatting. |
| **Inheritance #2 — base** | `domain/User.java` | 6–35 | Abstract base class for system accounts, holding id, name, and wardrobe list. Declares `getRole()` abstract so every user type must identify itself. |
| **Inheritance #2 — derived** | `domain/Admin.java` (3–11), `domain/Client.java` (6–21) | 3–11, 6–21 | `Admin` returns `"Admin"` from `getRole()`; `Client` returns `"Client"` and adds an `ownedOutfits` list. Both inherit all `User` state via `super(id, name)`. |
| **Interface #1** | `contracts/IClothingRepository.java` (6–12) → `repositories/ClothingRepository.java` (10–52) | 6–12 / 10–52 | Defines the clothing storage contract (save, remove, getById, getAll, getByOwner). `ClothingRepository` implements it with a private `HashMap` and Singleton access, so callers depend only on the interface. |
| **Interface #2** | `contracts/IRandomizer.java` (8–9) → `services/RandomOutfitGenerator.java` (11–44) | 8–9 / 11–44 | Defines a single `generate()` method for outfit creation. `RandomOutfitGenerator` implements it by randomly picking one item per clothing type from a pool. Any generation strategy can be substituted without changing callers. |
| **Interface #3** | `contracts/IOutfitRepository.java` (6–12) → `repositories/OutfitRepository.java` (10–52) | 6–12 / 10–52 | Defines outfit storage operations including `getByOwner()`. `OutfitRepository` implements it with the same Singleton + HashMap pattern, persisting saved outfits per user. |
| **Polymorphism #1** | `domain/ClothingItem.java` (91), `Program.java` (170, 181, 260) | 91 / 170, 181, 260 | `getDisplayLabel()` is abstract in `ClothingItem` and overridden in every subclass. Throughout the UI, it is called on `ClothingItem` references in a `List<ClothingItem>` — the correct subclass implementation is dispatched at runtime without any type-checking. |
| **Polymorphism #2** | `domain/User.java` (27), `Program.java` (67, 83, 109) | 27 / 67, 83, 109 | `getRole()` is abstract in `User` and overridden by `Admin` and `Client`. Called on `User` references to drive Admin-only menu options at runtime — the caller never casts or checks the concrete type. |
| **Access Modifiers** | `domain/ClothingItem.java` (7–17), `repositories/ClothingRepository.java` (11–14) | 7–17 / 11–14 | All domain fields are `private`, exposed through `public` getters and selective setters. The `ClothingItem` constructor is `protected` so only subclasses and the factory can create instances. Repository constructors are `private` to enforce Singleton access. |
| **Struct Equivalent** | `domain/Category.java` | 3–18 | A plain value object with two immutable fields (`ClothingType type`, `String description`) and only getters — no mutable state or behavior. Equivalent to a C# struct; used to attach type metadata to each `ClothingItem`. |
| **Enum #1** | `domain/ClothingType.java` | 3–7 | Enumerates the four clothing categories (TOP, BOTTOMS, FOOTWEAR, HEADWEAR). Used to type-check items, order the outfit viewer slots, and drive factory creation. |
| **Enum #2** | `domain/Season.java` | 3–7 | Enumerates the four seasons (Summer, Fall, Winter, Spring). Stored on every `ClothingItem` and persisted in JSON for future season-based filtering. |
| **Data Structure #1** | `repositories/ClothingRepository.java` (12) | 12 | `HashMap<Integer, ClothingItem>` gives O(1) item lookup by ID. The same pattern is used in `OutfitRepository` and `UserRepository`. |
| **Data Structure #2** | `services/OutfitViewer.java` (22–28) | 22–28 | `EnumMap<ClothingType, BufferedImage>` maps each clothing type slot to its loaded image for ordered, efficient rendering in the custom Swing panel. |
| **I/O — Console** | `Program.java` | 23, 72–73 | `Scanner` reads all user input from `System.in`. Menu output and status messages go to `System.out` throughout the session loop. |
| **I/O — File** | `services/WardrobeStore.java` | 17–end | `FileWriter` / `FileReader` write and read `wardrobe.json` using Gson, persisting users, clothing items, and saved outfits across sessions. `FileService` additionally uses `ProcessBuilder` for subprocess I/O with rembg. |

---

## 4. Design Patterns

| Pattern | Category | File | Lines | Rationale |
|---|---|---|---|---|
| **Singleton** | Creational | `repositories/ClothingRepository.java` | 11–22 | A single shared clothing store is required for the lifetime of the application. The private constructor and static `getInstance()` method guarantee exactly one instance, preventing duplicate or inconsistent item collections. The same pattern is applied in `OutfitRepository.java` (11–22) and `UserRepository.java` (11–17), ensuring all repositories remain consistent singletons throughout the session. |
| **Factory Method** | Creational | `services/ClothingFactory.java` | 5–16 | Clothing item creation requires selecting the correct subclass (`Top`, `Bottoms`, `Footwear`, or `Headwear`) based on a `ClothingType` value. `ClothingFactory.create()` centralizes this decision so that `Program` and `WardrobeStore` never directly instantiate subclasses — callers pass a type and receive a fully constructed `ClothingItem` without knowing its concrete class. |
| **Strategy** | Behavioral | `contracts/IRandomizer.java` / `services/RandomOutfitGenerator.java` | IRandomizer: 8–9, Generator: 11–44 | Outfit generation is defined as a pluggable interface rather than hard-coded logic. `Program.main()` holds a reference typed to `IRandomizer`, and `RandomOutfitGenerator` is the concrete strategy injected at startup. A different algorithm (e.g., season-aware or color-matched generation) can replace it at any time without modifying `Program` or any other class. |

---

## 5. Design Decisions

**Repository pattern for all data access.**
Every domain object type has its own repository interface and implementation. `Program` depends on `IClothingRepository`, `IOutfitRepository`, and `IUserRepository` rather than concrete classes, keeping business logic decoupled from storage. All three repositories use the Singleton pattern to ensure consistent shared state without passing references through every method call.

**Centralized JSON persistence via WardrobeStore.**
Rather than scattering serialization across domain classes, `WardrobeStore` owns all read/write operations against a single `wardrobe.json` file. Outfits are stored as lists of clothing item IDs and reconstructed via `ClothingRepository.getById()` on load, keeping the file compact and avoiding circular references. Format migration is handled gracefully — old single-user and array-format files are detected and read correctly.

**Persistent Swing OutfitViewer.**
The outfit viewer window is created once at startup and updated in-place via `update(Outfit)` rather than spawning a new window per generation. This lets the user reposition and resize the window once and keep that layout across multiple outfit generations. Images are rendered with `Graphics2D` in a custom `OutfitPanel extends JPanel` with configurable per-slot heights and 30px overlap so the full outfit fits on a standard laptop screen.

**Owner-scoped wardrobe filtering.**
Every `ClothingItem` and `Outfit` stores an `ownerId` matching a `User` ID. Repositories expose `getByOwner(int ownerId)` to scope queries to the current user, while Admin accounts call `getAll()` to operate across the full dataset. This enables clean multi-user support without a separate database or schema — a single flat JSON file holds all users' data.

**Background removal via subprocess.**
`FileService.removeBackground()` invokes `rembg` as an external process via `ProcessBuilder` rather than bundling a Java image-processing library. This keeps the Java dependency surface minimal and delegates the ML workload to best-available Python tooling. If `rembg` is unavailable or exits with an error, the original image is copied to the wardrobe folder and the import flow continues — no crash, no data loss.
