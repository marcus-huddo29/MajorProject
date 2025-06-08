import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Shop {

    // Inner class to represent a single item in the shop.
    static class ShopItem {
        String name, type, description, restriction;
        int value, cost;
        ShopItem(String n, String t, int v, int c, String d, String r) {
            name = n; type = t; value = v; cost = c; description = d; restriction = r;
        }
    }
    
    /**
     * Opens the main shop interface for the player.
     * @param player The player interacting with the shop.
     * @param sc The shared scanner for user input.
     */
    public static void openShop(Player player, Scanner sc) {
        ArrayList<ShopItem> items = loadShopItems("shop.csv");
        if(items.isEmpty()) {
            System.out.println("The shop is currently closed. (Failed to load items)");
            return;
        }

        System.out.println("\n=== Welcome to the Shop ===");
        while (true) {
            ArrayList<ShopItem> availableItems = filterAvailableItems(items, player);

            System.out.printf("\nYour currency: %.1f\n", player.getCurrency());
            System.out.println("-------------------------");
            System.out.println("Available items:");
            displayItems(availableItems);
            System.out.println("0) Exit Shop");
            System.out.println("-------------------------");
            
            int choice = getSafeIntInput(sc, "Enter item number to buy: ", 0, availableItems.size());
            if (choice == 0) break; // Exit shop
            
            ShopItem selectedItem = availableItems.get(choice - 1);
            
            // Handle purchase logic.
            handlePurchase(player, selectedItem, sc);
        }
    }

    /**
     * Loads all possible shop items from a CSV file.
     * @param filename The path to the shop CSV file.
     * @return An ArrayList of ShopItem objects.
     */
    private static ArrayList<ShopItem> loadShopItems(String filename) {
        ArrayList<ShopItem> items = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 6) continue; // Skip malformed lines.
                String name = p[0].trim();
                String type = p[1].trim();
                int value = Integer.parseInt(p[2].trim());
                int cost = Integer.parseInt(p[3].trim());
                String desc = p[4].trim();
                String restriction = p[5].trim().toLowerCase();
                items.add(new ShopItem(name, type, value, cost, desc, restriction));
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Failed to load or parse " + filename + ": " + e.getMessage());
        }
        return items;
    }
    
    /**
     * Filters the master item list to show only items the player can buy.
     * @param allItems The list of all items from the CSV.
     * @param player The player character.
     * @return A filtered list of items.
     */
    private static ArrayList<ShopItem> filterAvailableItems(ArrayList<ShopItem> allItems, Player player) {
        ArrayList<ShopItem> availableItems = new ArrayList<>();
        String playerClass = player.getPlayerClass();

        for (ShopItem it : allItems) {
            // Check class restriction.
            if (!it.restriction.equals("all") && !it.restriction.equals(playerClass)) {
                continue; 
            }

            // Check if weapon is already owned.
            if (it.type.equals("weapon")) {
                if (player.getOwnedWeapons().contains(it.name)) {
                    continue; // Skip already owned unique weapons.
                }
            }
            
            availableItems.add(it);
        }
        return availableItems;
    }

    /**
     * Displays the list of items to the user in a formatted way.
     */
    private static void displayItems(ArrayList<ShopItem> items) {
        for (int i = 0; i < items.size(); i++) {
            ShopItem it = items.get(i);
            System.out.printf("%d) %-18s (Cost: %d) - %s%n",
                              i + 1, it.name, it.cost, it.description);
        }
    }
    
    /**
     * Handles the logic of purchasing an item, including quantity and cost checks.
     */
    private static void handlePurchase(Player player, ShopItem sel, Scanner sc) {
        int qty = 1; // Default to 1.
        if (!sel.type.equals("weapon")) { // Weapons are unique, can only buy 1.
            qty = getSafeIntInput(sc, "Enter quantity to buy: ", 1, 99);
        }

        int totalCost = sel.cost * qty;
        if (player.getCurrency() < totalCost) {
            System.out.println("> Not enough currency for " + qty + " " + sel.name + "(s). Need " + totalCost + ", have " + player.getCurrency());
            return;
        }

        player.addCurrency(-totalCost);

        for (int i = 0; i < qty; i++) {
            if (sel.type.equals("weapon")) {
                player.equipWeapon(sel); // Use the new, correct method.
            } else {
                player.addItemToInventory(sel);
            }
        }
        
        if (!sel.type.equals("weapon")) {
            System.out.println("Purchased " + qty + " x " + sel.name + " and added to inventory!");
        }
    }

    /**
     * A robust method for getting integer input from the user to prevent crashes.
     */
    private static int getSafeIntInput(Scanner scanner, String prompt, int min, int max) {
        int choice = -1;
        while (true) {
            System.out.print(prompt);
            try {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) {
                    System.out.println("> Invalid input. Please enter a number.");
                    continue;
                }
                choice = Integer.parseInt(line);
                if (choice >= min && choice <= max) {
                    break;
                } else {
                    System.out.println("> Invalid choice. Please enter a number between " + min + " and " + max + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("> Invalid input. Please enter a number.");
            }
        }
        return choice;
    }
}