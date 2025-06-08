import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Shop {
    static class ShopItem {
        String name, type, description, restriction;
        int value, cost;
        ShopItem(String n, String t, int v, int c, String d, String r) {
            name = n;
            type = t;
            value = v;
            cost = c;
            description = d;
            restriction = r;
        }
    }
    
    // --- CHANGE --- Added a safe method for getting integer input to prevent crashes.
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

    public static void openShop(Player player, Scanner sc) {
        ArrayList<ShopItem> items = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("shop.csv"))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 6) continue; // Skip malformed lines
                String restriction = p[5].trim().split("\\s+")[0].toLowerCase();
                items.add(new ShopItem(p[0], p[1], Integer.parseInt(p[2]), Integer.parseInt(p[3]), p[4], restriction));
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Failed to load or parse shop.csv: " + e.getMessage());
            return;
        }

        System.out.println("\n=== Welcome to the Shop ===");
        while (true) {
            ArrayList<ShopItem> availableItems = new ArrayList<>();
            for (ShopItem it : items) {
                String playerClass = player.getPlayerClass().toLowerCase();
                if (!it.restriction.equals("all") && !it.restriction.equals(playerClass)) {
                    continue; 
                }

                // --- CHANGE --- Simplified logic for item availability.
                // A weapon is a permanent purchase. An attack buff is per-stage.
                if (it.type.equals("weapon")) {
                    boolean alreadyOwned = player.getAbilities().stream().anyMatch(a -> a.getAbilityName().equals(it.name));
                    if (alreadyOwned) continue;
                }
                
                availableItems.add(it);
            }

            System.out.printf("\nYour currency: %.1f\n", player.getCurrency());
            System.out.println("-------------------------");
            System.out.println("Available items:");
            for (int i = 0; i < availableItems.size(); i++) {
                ShopItem it = availableItems.get(i);
                System.out.printf("%d) %-18s (Cost: %d) - %s%n",
                                  i+1, it.name, it.cost, it.description);
            }
            System.out.println("0) Exit Shop");
            System.out.println("-------------------------");
            
            int choice = getSafeIntInput(sc, "Enter item number to buy: ", 0, availableItems.size());
            
            if (choice == 0) break;
            
            ShopItem sel = availableItems.get(choice-1);
            
            int qty = 1; // Default to 1
            if (!sel.type.equals("weapon")) { // Weapons are unique, can only buy 1
                qty = getSafeIntInput(sc, "Enter quantity to buy: ", 1, 99);
            }

            int totalCost = sel.cost * qty;
            if (player.getCurrency() < totalCost) {
                System.out.println("> Not enough currency for " + qty + " " + sel.name + "(s). Need " + totalCost + ", have " + player.getCurrency());
                continue;
            }

            player.addCurrency(-totalCost);

            for (int i = 0; i < qty; i++) {
                // If it's a weapon, add it as a permanent ability/buff. Otherwise, add to inventory.
                if (sel.type.equals("weapon")) {
                    // This is a simple implementation. A better one would add a permanent passive effect.
                    // For now, we'll just buff damage directly.
                    player.getAbilities().add(new Ability(sel.name, sel.value, sel.value, 0, "None", 0));
                     System.out.println("You bought the " + sel.name + ", permanently increasing your power!");
                } else {
                    player.addItemToInventory(sel);
                }
            }
            if (!sel.type.equals("weapon")) {
                System.out.println("Purchased " + qty + " x " + sel.name + " and added to inventory!");
            }
        }
    }
}
