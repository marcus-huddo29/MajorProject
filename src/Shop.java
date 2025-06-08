// Shop.java

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Shop {

    static class ShopItem {
        String name, type, description, restriction;
        int value, cost, levelRequirement;
        ShopItem(String n, String t, int v, int c, String d, String r, int lr) {
            name = n; type = t; value = v; cost = c; description = d; restriction = r; levelRequirement = lr;
        }
    }
    
    public static void openShop(Player player, Scanner sc) {
        ArrayList<ShopItem> items = loadShopItems("shop.csv");
        if(items.isEmpty()) {
            System.out.println("The shop is currently closed. (Failed to load items)");
            return;
        }

        System.out.println("\n=== Welcome to the Shop ===");
        while (true) {
            ArrayList<ShopItem> availableItems = filterAvailableItems(items, player);

            System.out.printf("\nYour currency: %.1f | Your Level: %d\n", player.getCurrency(), player.getLevelsGained() + 1);
            System.out.println("-------------------------");
            if (availableItems.isEmpty()) {
                System.out.println("No new items available at your level.");
            } else {
                System.out.println("Available items:");
                displayItems(availableItems);
            }
            System.out.println("0) Exit Shop");
            System.out.println("-------------------------");
            
            if (availableItems.isEmpty()) {
                getSafeIntInput(sc, "Enter 0 to exit: ", 0, 0);
                break;
            }

            int choice = getSafeIntInput(sc, "Enter item number to buy: ", 0, availableItems.size());
            if (choice == 0) break; 
            
            ShopItem selectedItem = availableItems.get(choice - 1);
            handlePurchase(player, selectedItem, sc);
        }
    }

    private static ArrayList<ShopItem> loadShopItems(String filename) {
        ArrayList<ShopItem> items = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 7) continue;
                String name = p[0].trim();
                String type = p[1].trim();
                int value = Integer.parseInt(p[2].trim());
                int cost = Integer.parseInt(p[3].trim());
                String desc = p[4].trim();
                String restriction = p[5].trim().toLowerCase();
                int levelReq = Integer.parseInt(p[6].trim());
                items.add(new ShopItem(name, type, value, cost, desc, restriction, levelReq));
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Failed to load or parse " + filename + ": " + e.getMessage());
        }
        return items;
    }
    
    private static ArrayList<ShopItem> filterAvailableItems(ArrayList<ShopItem> allItems, Player player) {
        ArrayList<ShopItem> availableItems = new ArrayList<>();
        String playerClass = player.getPlayerClass();
        int playerLevel = player.getLevelsGained() + 1;

        for (ShopItem it : allItems) {
            if (it.levelRequirement > playerLevel) {
                continue;
            }

            if (!it.restriction.equals("all") && !it.restriction.equals(playerClass)) {
                continue; 
            }

            if (it.type.equals("weapon")) {
                if (player.getOwnedWeapons().contains(it.name)) {
                    continue;
                }
            }
            
            availableItems.add(it);
        }
        return availableItems;
    }

    private static void displayItems(ArrayList<ShopItem> items) {
        for (int i = 0; i < items.size(); i++) {
            ShopItem it = items.get(i);
            System.out.printf("%d) %-18s (Cost: %d, Lvl: %d) - %s%n",
                              i + 1, it.name, it.cost, it.levelRequirement, it.description);
        }
    }
    
    private static void handlePurchase(Player player, ShopItem sel, Scanner sc) {
        int qty = 1;
        if (!sel.type.equals("weapon")) {
            qty = getSafeIntInput(sc, "Enter quantity to buy: ", 1, 99);
        }

        int totalCost = sel.cost * qty;
        if (player.getCurrency() < totalCost) {
            System.out.println("> Not enough currency. Need " + totalCost + ", have " + player.getCurrency());
            return;
        }

        player.addCurrency(-totalCost);

        for (int i = 0; i < qty; i++) {
            if (sel.type.equals("weapon")) {
                player.equipWeapon(sel);
            } else {
                player.addItemToInventory(sel);
            }
        }
        
        if (sel.type.equals("weapon")) {
        } else {
            System.out.println("Purchased " + qty + " x " + sel.name + " and added to inventory!");
        }
    }

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