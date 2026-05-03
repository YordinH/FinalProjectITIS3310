import contracts.IClothingRepository;
import contracts.IRandomizer;
import domain.*;
import repositories.ClothingRepository;
import services.RandomOutfitGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Program {
    static final Scanner scanner = new Scanner(System.in);
    static int nextId = 1;

    public static void main(String[] args) {
        IClothingRepository clothingRepo = ClothingRepository.getInstance();
        IRandomizer randomizer = new RandomOutfitGenerator();

        seedData(clothingRepo);

        System.out.println("Welcome to the Virtual Wardrobe System.");

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("\nEnter a menu option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1": addClothingItem(clothingRepo); break;
                case "2": viewWardrobe(clothingRepo); break;
                case "3": generateOutfit(clothingRepo, randomizer); break;
                case "4": running = false; break;
                default: System.out.println("Invalid option."); break;
            }
        }

        System.out.println("\nThank you for using the Virtual Wardrobe System!");
    }

    static void printMenu() {
        System.out.println("\n************** MENU **************");
        System.out.println("* 1. Add clothing item           *");
        System.out.println("* 2. View wardrobe               *");
        System.out.println("* 3. Generate random outfit      *");
        System.out.println("* 4. Quit                        *");
        System.out.println("**********************************");
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

        ClothingItem item;
        switch (typeInput) {
            case "1":
                System.out.print("Sleeve length: ");
                item = new Top(nextId++, 1, name, "", color, season, scanner.nextLine().trim());
                break;
            case "2":
                System.out.print("Pant length: ");
                item = new Bottoms(nextId++, 1, name, "", color, season, scanner.nextLine().trim());
                break;
            case "3":
                System.out.print("Shoe type: ");
                item = new Footwear(nextId++, 1, name, "", color, season, scanner.nextLine().trim());
                break;
            case "4":
                System.out.print("Brim type: ");
                item = new Headwear(nextId++, 1, name, "", color, season, scanner.nextLine().trim());
                break;
            default:
                System.out.println("Invalid type.");
                return;
        }

        repo.save(item);
        System.out.println(item.getDisplayLabel() + " added.");
    }

    static void viewWardrobe(IClothingRepository repo) {
        System.out.println("\nYour Wardrobe:");
        List<ClothingItem> items = repo.getAll();
        if (items.isEmpty()) {
            System.out.println("No items in wardrobe.");
            return;
        }
        for (ClothingItem item : items) {
            System.out.println("  [" + item.getCategory().getType() + "] " + item.getDisplayLabel());
        }
    }

    static void generateOutfit(IClothingRepository repo, IRandomizer randomizer) {
        System.out.println("\nGenerating random outfit...");
        List<ClothingItem> pool = repo.getAll();
        if (pool.isEmpty()) {
            System.out.println("No items in wardrobe.");
            return;
        }
        Outfit outfit = randomizer.generate(pool, new ArrayList<>(), new ArrayList<>());
        System.out.println("Outfit: " + outfit.getName());
        for (ClothingItem item : outfit.getItems()) {
            System.out.println("  - " + item.getDisplayLabel());
        }
    }

    static void seedData(IClothingRepository repo) {
        repo.save(new Top(nextId++, 1, "White T-Shirt", "", "White", Season.Summer, "Short"));
        repo.save(new Bottoms(nextId++, 1, "Blue Jeans", "", "Blue", Season.Fall, "Full"));
        repo.save(new Footwear(nextId++, 1, "White Sneakers", "", "White", Season.Summer, "Sneaker"));
        repo.save(new Headwear(nextId++, 1, "Baseball Cap", "", "Black", Season.Summer, "Curved"));
    }
}
