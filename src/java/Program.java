import contracts.IClothingRepository;
import contracts.IFileService;
import contracts.IOutfitRepository;
import contracts.IRandomizer;
import contracts.IUserRepository;
import domain.*;
import repositories.ClothingRepository;
import repositories.OutfitRepository;
import repositories.UserRepository;
import services.ClothingFactory;
import services.FileService;
import services.ImageViewer;
import services.OutfitViewer;
import services.RandomOutfitGenerator;
import services.WardrobeStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Program {
    static final Scanner scanner = new Scanner(System.in);
    static int nextId = 1;
    static int nextUserId = 1;
    static int nextOutfitId = 1;
    static User currentUser;

    public static void main(String[] args) {
        IClothingRepository clothingRepo = ClothingRepository.getInstance();
        IUserRepository userRepo = UserRepository.getInstance();
        IOutfitRepository outfitRepo = OutfitRepository.getInstance();
        IRandomizer randomizer = new RandomOutfitGenerator();
        IFileService fileService = new FileService();
        ImageViewer imageViewer = new ImageViewer();
        OutfitViewer outfitViewer = new OutfitViewer();
        WardrobeStore store = new WardrobeStore();

        List<User> savedUsers = store.loadUsers();
        List<ClothingItem> savedItems = store.loadItems();

        for (User u : savedUsers) {
            userRepo.save(u);
            if (u.getId() >= nextUserId) nextUserId = u.getId() + 1;
        }
        for (ClothingItem item : savedItems) {
            clothingRepo.save(item);
            if (item.getId() >= nextId) nextId = item.getId() + 1;
        }

        for (Outfit outfit : store.loadOutfits(clothingRepo)) {
            outfitRepo.save(outfit);
            if (outfit.getId() >= nextOutfitId) nextOutfitId = outfit.getId() + 1;
        }

        System.out.println("Welcome to the Virtual Wardrobe System.");

        boolean appRunning = true;
        while (appRunning) {
            currentUser = selectOrCreateUser(userRepo);
            if (currentUser == null) break;

            if (savedItems.isEmpty() && savedUsers.isEmpty()) {
                seedData(clothingRepo, currentUser.getId());
            }

            System.out.println("Signed in as " + currentUser.getName() + " [" + currentUser.getRole() + "].");

            boolean sessionRunning = true;
            while (sessionRunning) {
                printMenu();
                System.out.print("\nEnter a menu option: ");
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1": addClothingItem(clothingRepo); break;
                    case "2": viewWardrobe(clothingRepo); break;
                    case "3": generateOutfit(clothingRepo, outfitRepo, randomizer, outfitViewer); break;
                    case "4": importClothing(clothingRepo, fileService, imageViewer); break;
                    case "5": viewSavedOutfits(outfitRepo); break;
                    case "6": sessionRunning = false; break;
                    case "7": appRunning = false; sessionRunning = false; break;
                    case "8":
                        if (currentUser.getRole().equals("Admin"))
                            viewAllWardrobes(clothingRepo, userRepo);
                        else System.out.println("Invalid option.");
                        break;
                    case "9":
                        if (currentUser.getRole().equals("Admin"))
                            generateOutfitFromAll(clothingRepo, outfitRepo, randomizer, outfitViewer);
                        else System.out.println("Invalid option.");
                        break;
                    default: System.out.println("Invalid option."); break;
                }
            }

            store.save(userRepo.getAll(), clothingRepo.getAll(), outfitRepo.getAll());
        }

        outfitViewer.dispose();
        System.out.println("\nThank you for using the Virtual Wardrobe System!");
        System.exit(0);
    }

    static User selectOrCreateUser(IUserRepository userRepo) {
        List<User> users = userRepo.getAll();
        System.out.println("\n--- User Selection ---");
        for (int i = 0; i < users.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + users.get(i).getName()
                    + " [" + users.get(i).getRole() + "]");
        }
        int newOpt = users.size() + 1;
        System.out.println("  " + newOpt + ". Create new user");
        System.out.println("  0. Quit");
        System.out.print("Choice: ");
        String input = scanner.nextLine().trim();
        int choice;
        try { choice = Integer.parseInt(input); } catch (NumberFormatException e) { choice = -1; }

        if (choice == 0) return null;
        if (choice >= 1 && choice <= users.size()) return users.get(choice - 1);
        if (choice == newOpt) {
            User u = createUser();
            userRepo.save(u);
            return u;
        }
        System.out.println("Invalid choice.");
        return selectOrCreateUser(userRepo);
    }

    static User createUser() {
        System.out.println("\nNew profile setup.");
        System.out.print("Your name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) name = "User";
        System.out.print("Role (1=Client  2=Admin): ");
        String roleInput = scanner.nextLine().trim();
        int id = nextUserId++;
        return roleInput.equals("2") ? new Admin(id, name) : new Client(id, name);
    }

    static void printMenu() {
        System.out.println("\n*************** MENU ***************");
        System.out.println("* 1. Add clothing item             *");
        System.out.println("* 2. View wardrobe                 *");
        System.out.println("* 3. Generate random outfit        *");
        System.out.println("* 4. Import from images/           *");
        System.out.println("* 5. View saved outfits            *");
        System.out.println("* 6. Sign out                      *");
        System.out.println("* 7. Quit                          *");
        if (currentUser.getRole().equals("Admin")) {
            System.out.println("*--- Admin -------------------------*");
            System.out.println("* 8. View all wardrobes            *");
            System.out.println("* 9. Generate outfit from all      *");
        }
        System.out.println("************************************");
    }

    static void addClothingItem(IClothingRepository repo) {
        System.out.println("\nAdd Clothing Item");
        System.out.println("Type: 1=Top  2=Bottoms  3=Footwear  4=Headwear");
        System.out.print("Type: ");
        String typeInput = scanner.nextLine().trim();

        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }

        System.out.print("Color: ");
        String color = scanner.nextLine().trim();

        System.out.print("Season (Summer/Fall/Winter/Spring): ");
        Season season;
        try {
            season = Season.valueOf(scanner.nextLine().trim());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid season.");
            return;
        }

        ClothingType clothingType;
        String subtypePrompt;
        switch (typeInput) {
            case "1": clothingType = ClothingType.TOP;      subtypePrompt = "Sleeve length: "; break;
            case "2": clothingType = ClothingType.BOTTOMS;  subtypePrompt = "Pant length: ";   break;
            case "3": clothingType = ClothingType.FOOTWEAR; subtypePrompt = "Shoe type: ";     break;
            case "4": clothingType = ClothingType.HEADWEAR; subtypePrompt = "Brim type: ";     break;
            default:
                System.out.println("Invalid type.");
                return;
        }

        System.out.print(subtypePrompt);
        String subtypeAttr = scanner.nextLine().trim();
        ClothingItem item = ClothingFactory.create(clothingType, nextId++, currentUser.getId(), name, "", color, season, subtypeAttr);

        repo.save(item);
        System.out.println(item.getDisplayLabel() + " added.");
    }

    static void viewWardrobe(IClothingRepository repo) {
        System.out.println("\nYour Wardrobe:");
        List<ClothingItem> items = repo.getByOwner(currentUser.getId());
        if (items.isEmpty()) {
            System.out.println("No items in wardrobe.");
            return;
        }
        for (ClothingItem item : items) {
            System.out.println("  [" + item.getCategory().getType() + "] " + item.getDisplayLabel());
        }
    }

    static void generateOutfit(IClothingRepository repo, IOutfitRepository outfitRepo,
                               IRandomizer randomizer, OutfitViewer viewer) {
        System.out.println("\nGenerating random outfit...");
        List<ClothingItem> pool = new ArrayList<>();
        for (ClothingItem item : repo.getByOwner(currentUser.getId())) {
            String path = item.getImageFilePath();
            if (path != null && !path.isEmpty() && new File(path).exists()) pool.add(item);
        }
        if (pool.isEmpty()) {
            System.out.println("No imported items with images. Use option 4 to import clothing.");
            return;
        }
        Outfit outfit = randomizer.generate(pool, new ArrayList<>(), new ArrayList<>());
        System.out.println("Outfit: " + outfit.getName());
        for (ClothingItem item : outfit.getItems()) {
            System.out.println("  - " + item.getDisplayLabel());
        }
        viewer.update(outfit);

        System.out.print("Save this outfit? (y/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.print("Name: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) name = "Outfit " + nextOutfitId;
            Outfit saved = new Outfit(nextOutfitId++, currentUser.getId(), name);
            for (ClothingItem item : outfit.getItems()) {
                saved.addItem(item);
                item.incrementWearCount();
            }
            outfitRepo.save(saved);
            System.out.println("\"" + saved.getName() + "\" saved.");
        }
    }

    static void viewSavedOutfits(IOutfitRepository outfitRepo) {
        System.out.println("\nSaved Outfits:");
        List<Outfit> outfits = outfitRepo.getByOwner(currentUser.getId());
        if (outfits.isEmpty()) {
            System.out.println("No saved outfits.");
            return;
        }
        for (Outfit outfit : outfits) {
            System.out.println("  [" + outfit.getId() + "] " + outfit.getName());
            for (ClothingItem item : outfit.getItems()) {
                System.out.println("    - " + item.getDisplayLabel());
            }
        }
    }

    static void importClothing(IClothingRepository repo, IFileService fileService, ImageViewer imageViewer) {
        List<File> files = fileService.scanUnimported();
        if (files.isEmpty()) {
            System.out.println("No new images found in images/.");
            return;
        }
        System.out.println("Found " + files.size() + " new image(s).");

        for (File file : files) {
            System.out.println("\nProcessing: " + file.getName());
            File processed = fileService.removeBackground(file);
            imageViewer.show(processed);

            System.out.println("Type: 1=Top  2=Bottoms  3=Footwear  4=Headwear  (Enter to skip)");
            System.out.print("Type: ");
            String typeInput = scanner.nextLine().trim();
            if (typeInput.isEmpty()) {
                imageViewer.close();
                continue;
            }

            System.out.print("Name: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                imageViewer.close();
                continue;
            }

            System.out.print("Color: ");
            String color = scanner.nextLine().trim();

            System.out.print("Season (Summer/Fall/Winter/Spring): ");
            Season season;
            try {
                season = Season.valueOf(scanner.nextLine().trim());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid season, skipping.");
                imageViewer.close();
                continue;
            }

            ClothingType clothingType;
            String subtypePrompt;
            switch (typeInput) {
                case "1": clothingType = ClothingType.TOP;      subtypePrompt = "Sleeve length: "; break;
                case "2": clothingType = ClothingType.BOTTOMS;  subtypePrompt = "Pant length: ";   break;
                case "3": clothingType = ClothingType.FOOTWEAR; subtypePrompt = "Shoe type: ";     break;
                case "4": clothingType = ClothingType.HEADWEAR; subtypePrompt = "Brim type: ";     break;
                default:
                    System.out.println("Invalid type, skipping.");
                    imageViewer.close();
                    continue;
            }

            System.out.print(subtypePrompt);
            String subtypeAttr = scanner.nextLine().trim();

            ClothingItem item = ClothingFactory.create(
                    clothingType, nextId++, currentUser.getId(), name,
                    processed.getAbsolutePath(), color, season, subtypeAttr);
            repo.save(item);
            fileService.markImported(file.getName());
            imageViewer.close();
            System.out.println(item.getDisplayLabel() + " added.");
        }

        System.out.println("\nImport complete.");
    }

    static void viewAllWardrobes(IClothingRepository repo, IUserRepository userRepo) {
        System.out.println("\nAll Wardrobes:");
        for (User user : userRepo.getAll()) {
            System.out.println("  " + user.getName() + " [" + user.getRole() + "]:");
            List<ClothingItem> items = repo.getByOwner(user.getId());
            if (items.isEmpty()) {
                System.out.println("    (empty)");
            } else {
                for (ClothingItem item : items) {
                    System.out.println("    [" + item.getCategory().getType() + "] " + item.getDisplayLabel());
                }
            }
        }
    }

    static void generateOutfitFromAll(IClothingRepository repo, IOutfitRepository outfitRepo,
                                      IRandomizer randomizer, OutfitViewer viewer) {
        System.out.println("\nGenerating outfit from all users' clothing...");
        List<ClothingItem> pool = new ArrayList<>();
        for (ClothingItem item : repo.getAll()) {
            String path = item.getImageFilePath();
            if (path != null && !path.isEmpty() && new File(path).exists()) pool.add(item);
        }
        if (pool.isEmpty()) {
            System.out.println("No imported items with images across any wardrobe.");
            return;
        }
        Outfit outfit = randomizer.generate(pool, new ArrayList<>(), new ArrayList<>());
        System.out.println("Outfit: " + outfit.getName());
        for (ClothingItem item : outfit.getItems()) {
            System.out.println("  - " + item.getDisplayLabel());
        }
        viewer.update(outfit);

        System.out.print("Save this outfit? (y/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.print("Name: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) name = "Outfit " + nextOutfitId;
            Outfit saved = new Outfit(nextOutfitId++, currentUser.getId(), name);
            for (ClothingItem item : outfit.getItems()) {
                saved.addItem(item);
                item.incrementWearCount();
            }
            outfitRepo.save(saved);
            System.out.println("\"" + saved.getName() + "\" saved.");
        }
    }

    static void seedData(IClothingRepository repo, int ownerId) {
        repo.save(ClothingFactory.create(ClothingType.TOP,      nextId++, ownerId, "White T-Shirt",   "", "White", Season.Summer, "Short"));
        repo.save(ClothingFactory.create(ClothingType.BOTTOMS,  nextId++, ownerId, "Blue Jeans",       "", "Blue",  Season.Fall,   "Full"));
        repo.save(ClothingFactory.create(ClothingType.FOOTWEAR, nextId++, ownerId, "White Sneakers",   "", "White", Season.Summer, "Sneaker"));
        repo.save(ClothingFactory.create(ClothingType.HEADWEAR, nextId++, ownerId, "Baseball Cap",     "", "Black", Season.Summer, "Curved"));
    }
}
