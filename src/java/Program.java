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
                    case "1": viewWardrobe(clothingRepo, imageViewer); break;
                    case "2": generateOutfit(clothingRepo, outfitRepo, randomizer, outfitViewer); break;
                    case "3": importClothing(clothingRepo, fileService, imageViewer); break;
                    case "4": viewSavedOutfits(outfitRepo, outfitViewer); break;
                    case "5": sessionRunning = false; break;
                    case "6": appRunning = false; sessionRunning = false; break;
                    case "7":
                        if (currentUser.getRole().equals("Admin"))
                            viewAllWardrobes(clothingRepo, userRepo);
                        else System.out.println("Invalid option.");
                        break;
                    case "8":
                        if (currentUser.getRole().equals("Admin"))
                            generateOutfitFromAll(clothingRepo, outfitRepo, randomizer, outfitViewer);
                        else System.out.println("Invalid option.");
                        break;
                    case "9":
                        if (currentUser.getRole().equals("Admin"))
                            deleteClient(userRepo, clothingRepo, outfitRepo);
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
        System.out.println("* 1. View wardrobe                 *");
        System.out.println("* 2. Generate random outfit        *");
        System.out.println("* 3. Import from images/           *");
        System.out.println("* 4. View saved outfits            *");
        System.out.println("* 5. Sign out                      *");
        System.out.println("* 6. Quit                          *");
        if (currentUser.getRole().equals("Admin")) {
            System.out.println("*--- Admin -------------------------*");
            System.out.println("* 7. View all wardrobes            *");
            System.out.println("* 8. Generate outfit from all      *");
            System.out.println("* 9. Delete client                 *");
        }
        System.out.println("************************************");
    }

    static void viewWardrobe(IClothingRepository repo, ImageViewer imageViewer) {
        List<ClothingItem> items = repo.getByOwner(currentUser.getId());
        if (items.isEmpty()) {
            System.out.println("No items in wardrobe.");
            return;
        }

        boolean browsing = true;
        while (browsing) {
            System.out.println("\nYour Wardrobe:");
            for (int i = 0; i < items.size(); i++) {
                ClothingItem item = items.get(i);
                System.out.println("  " + (i + 1) + ". [" + item.getCategory().getType() + "] "
                        + item.getDisplayLabel() + " (worn " + item.getWearCount() + "x)");
            }
            System.out.print("\nEnter number to manage item, or Q to return: ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("q")) {
                browsing = false;
            } else {
                try {
                    int choice = Integer.parseInt(input);
                    if (choice >= 1 && choice <= items.size()) {
                        ClothingItem selected = items.get(choice - 1);
                        System.out.println("\n" + selected.getDisplayLabel());
                        System.out.println("  1. View image");
                        System.out.println("  2. Edit metadata");
                        System.out.println("  3. Delete");
                        System.out.println("  Q. Back");
                        System.out.print("Choice: ");
                        String action = scanner.nextLine().trim();
                        switch (action) {
                            case "1":
                                String path = selected.getImageFilePath();
                                if (path != null && !path.isEmpty() && new File(path).exists()) {
                                    imageViewer.show(new File(path));
                                } else {
                                    System.out.println("No image available.");
                                }
                                break;
                            case "2":
                                editItem(selected);
                                break;
                            case "3":
                                repo.remove(selected.getId());
                                items.remove(selected);
                                System.out.println(selected.getDisplayLabel() + " deleted.");
                                break;
                            default: break;
                        }
                    } else {
                        System.out.println("Invalid number.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input.");
                }
            }
        }
    }

    static void editItem(ClothingItem item) {
        System.out.println("Leave blank to keep current value.");

        System.out.print("Name [" + item.getName() + "]: ");
        String name = scanner.nextLine().trim();
        if (!name.isEmpty()) item.setName(name);

        System.out.print("Color [" + item.getColor() + "]: ");
        String color = scanner.nextLine().trim();
        if (!color.isEmpty()) item.setColor(color);

        System.out.print("Season [" + item.getSeason() + "] (Summer/Fall/Winter/Spring): ");
        String seasonInput = scanner.nextLine().trim();
        if (!seasonInput.isEmpty()) {
            try {
                item.setSeason(Season.valueOf(seasonInput));
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid season, keeping current.");
            }
        }

        System.out.print("Subtype [" + item.getSubtypeAttribute() + "]: ");
        String subtype = scanner.nextLine().trim();
        if (!subtype.isEmpty()) item.setSubtypeAttribute(subtype);

        System.out.println("Updated: " + item.getDisplayLabel());
    }

    static void generateOutfit(IClothingRepository repo, IOutfitRepository outfitRepo,
                               IRandomizer randomizer, OutfitViewer viewer) {
        List<ClothingItem> pool = new ArrayList<>();
        for (ClothingItem item : repo.getByOwner(currentUser.getId())) {
            String path = item.getImageFilePath();
            if (path != null && !path.isEmpty() && new File(path).exists()) pool.add(item);
        }
        if (pool.isEmpty()) {
            System.out.println("No imported items with images. Use option 3 to import clothing.");
            return;
        }
        while (true) {
            System.out.println("\nGenerating random outfit...");
            Outfit outfit = randomizer.generate(pool, new ArrayList<>(), new ArrayList<>());
            for (ClothingItem item : outfit.getItems()) {
                System.out.println("  - " + item.getDisplayLabel());
            }
            viewer.update(outfit);

            System.out.print("Y=save  N=discard  Enter=regenerate: ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("y")) {
                saveOutfit(outfit, outfitRepo);
                break;
            } else if (input.equalsIgnoreCase("n")) {
                break;
            }
        }
    }

    static void viewSavedOutfits(IOutfitRepository outfitRepo, OutfitViewer viewer) {
        List<Outfit> outfits = outfitRepo.getByOwner(currentUser.getId());
        if (outfits.isEmpty()) {
            System.out.println("No saved outfits.");
            return;
        }

        boolean browsing = true;
        while (browsing) {
            System.out.println("\nSaved Outfits:");
            for (int i = 0; i < outfits.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + outfits.get(i).getName());
                for (ClothingItem item : outfits.get(i).getItems()) {
                    System.out.println("       - " + item.getDisplayLabel());
                }
            }
            System.out.print("\nEnter number to manage, or Q to return: ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("q")) {
                browsing = false;
            } else {
                try {
                    int choice = Integer.parseInt(input);
                    if (choice >= 1 && choice <= outfits.size()) {
                        Outfit selected = outfits.get(choice - 1);
                        System.out.println("\n" + selected.getName());
                        System.out.println("  1. Display");
                        System.out.println("  2. Delete");
                        System.out.println("  Q. Back");
                        System.out.print("Choice: ");
                        String action = scanner.nextLine().trim();
                        if (action.equals("1")) {
                            viewer.update(selected);
                            System.out.println("Displaying \"" + selected.getName() + "\".");
                        } else if (action.equals("2")) {
                            outfitRepo.remove(selected.getId());
                            outfits.remove(selected);
                            System.out.println("\"" + selected.getName() + "\" deleted.");
                        }
                    } else {
                        System.out.println("Invalid number.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input.");
                }
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
                    processed.getPath(), color, season, subtypeAttr);
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
        List<ClothingItem> pool = new ArrayList<>();
        for (ClothingItem item : repo.getAll()) {
            String path = item.getImageFilePath();
            if (path != null && !path.isEmpty() && new File(path).exists()) pool.add(item);
        }
        if (pool.isEmpty()) {
            System.out.println("No imported items with images across any wardrobe.");
            return;
        }
        while (true) {
            System.out.println("\nGenerating outfit from all users' clothing...");
            Outfit outfit = randomizer.generate(pool, new ArrayList<>(), new ArrayList<>());
            for (ClothingItem item : outfit.getItems()) {
                System.out.println("  - " + item.getDisplayLabel());
            }
            viewer.update(outfit);

            System.out.print("Y=save  N=discard  Enter=regenerate: ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("y")) {
                saveOutfit(outfit, outfitRepo);
                break;
            } else if (input.equalsIgnoreCase("n")) {
                break;
            }
        }
    }

    static void saveOutfit(Outfit outfit, IOutfitRepository outfitRepo) {
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

    static void deleteClient(IUserRepository userRepo, IClothingRepository clothingRepo, IOutfitRepository outfitRepo) {
        List<User> clients = new ArrayList<>();
        for (User u : userRepo.getAll()) {
            if (u.getRole().equals("Client")) clients.add(u);
        }
        if (clients.isEmpty()) {
            System.out.println("No clients to delete.");
            return;
        }
        System.out.println("\nClients:");
        for (int i = 0; i < clients.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + clients.get(i).getName());
        }
        System.out.print("Enter number to delete, or Q to cancel: ");
        String input = scanner.nextLine().trim();
        if (input.equalsIgnoreCase("q")) return;
        int choice;
        try { choice = Integer.parseInt(input); } catch (NumberFormatException e) { choice = -1; }
        if (choice < 1 || choice > clients.size()) {
            System.out.println("Invalid choice.");
            return;
        }
        User target = clients.get(choice - 1);
        for (ClothingItem item : clothingRepo.getByOwner(target.getId())) {
            clothingRepo.remove(item.getId());
        }
        for (Outfit outfit : outfitRepo.getByOwner(target.getId())) {
            outfitRepo.remove(outfit.getId());
        }
        userRepo.remove(target.getId());
        System.out.println(target.getName() + " and all their data deleted.");
    }

    static void seedData(IClothingRepository repo, int ownerId) {
        repo.save(ClothingFactory.create(ClothingType.TOP,      nextId++, ownerId, "White T-Shirt",   "", "White", Season.Summer, "Short"));
        repo.save(ClothingFactory.create(ClothingType.BOTTOMS,  nextId++, ownerId, "Blue Jeans",       "", "Blue",  Season.Fall,   "Full"));
        repo.save(ClothingFactory.create(ClothingType.FOOTWEAR, nextId++, ownerId, "White Sneakers",   "", "White", Season.Summer, "Sneaker"));
        repo.save(ClothingFactory.create(ClothingType.HEADWEAR, nextId++, ownerId, "Baseball Cap",     "", "Black", Season.Summer, "Curved"));
    }
}
