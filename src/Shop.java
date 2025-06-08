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

    public static void openShop(Player player) {
        ArrayList<ShopItem> items = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("shop.csv"))) {
            String line;
            br.readLine(); // skip header
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                String restriction = p[5].trim().split("\\s+")[0].toLowerCase();
                items.add(new ShopItem(
                    p[0],
                    p[1],
                    Integer.parseInt(p[2]),
                    Integer.parseInt(p[3]),
                    p[4],
                    restriction
                ));
            }
        } catch (IOException e) {
            System.err.println("Failed to load shop.csv: " + e.getMessage());
            return;
        }

        Scanner sc = new Scanner(System.in);
        System.out.println("\n=== Welcome to the Shop ===");
        while (true) {
            ArrayList<ShopItem> availableItems = new ArrayList<>();
            for (ShopItem it : items) {
                String playerClass = player.getPlayerClass().toLowerCase();
                String itemRestriction = it.restriction.toLowerCase();

                if (!itemRestriction.equals("all") && !itemRestriction.equals(playerClass)) {
                    continue; 
                }

                if (it.type.equals("weapon")) {
                    boolean alreadyOwned = player.getInventory().stream()
                                                 .anyMatch(owned -> owned.name.equals(it.name));
                    if (alreadyOwned) {
                        continue;
                    }
                }
                
                if (it.type.equals("attack_buff")) {
                    if (player.hasUsedAttackBuffThisStage()) {
                        continue;
                    }
                     boolean alreadyBuffed = player.getInventory().stream()
                                                  .anyMatch(owned -> owned.type.equals("attack_buff"));
                    if(alreadyBuffed) {
                        continue;
                    }
                }
                
                availableItems.add(it);
            }

            System.out.println("\nYour currency: " + player.getCurrency());
            System.out.println("Available items:");
            for (int i = 0; i < availableItems.size(); i++) {
                ShopItem it = availableItems.get(i);
                System.out.printf("%d) %s (%s) – value: %d, cost: %d each%n    → %s%n",
                                  i+1, it.name, it.type, it.value, it.cost, it.description);
            }
            System.out.println("0) Exit Shop");
            
            // --- MODIFIED: Robust input handling for item choice ---
            int choice = -1;
            while(choice == -1) {
                System.out.print("Enter item number to buy: ");
                try {
                    String line = sc.nextLine();
                    if (line.isEmpty()) { // Handle empty input
                        System.out.println("Invalid input.");
                        continue;
                    }
                    choice = Integer.parseInt(line);
                     if (choice < 0 || choice > availableItems.size()) {
                        System.out.println("Invalid choice.");
                        choice = -1; // Reset to loop again
                    }
                } catch (NumberFormatException ex) {
                    System.out.println("Invalid input. Please enter a number.");
                }
            }
            
            if (choice == 0) break;
            
            ShopItem sel = availableItems.get(choice-1);
            
            // --- MODIFIED: Robust input handling for quantity ---
            int qty = -1;
             while(qty == -1) {
                System.out.print("Enter quantity to buy: ");
                try {
                     String line = sc.nextLine();
                     if (line.isEmpty()) {
                        System.out.println("Invalid quantity.");
                        continue;
                    }
                    qty = Integer.parseInt(line);
                     if (qty <= 0) {
                        System.out.println("Quantity must be at least 1.");
                        qty = -1; // Reset to loop again
                    }
                } catch (NumberFormatException ex) {
                    System.out.println("Invalid quantity. Please enter a number.");
                }
            }

            int totalCost = sel.cost * qty;
            if (player.getCurrency() < totalCost) {
                System.out.println("Not enough currency for " + qty + " " + sel.name + "(s).");
                continue;
            }
            player.currency -= totalCost;
            for (int i = 0; i < qty; i++) {
                player.addItemToInventory(sel);
                if (sel.type.equals("attack_buff")) {
                    player.setUsedAttackBuffThisStage(true);
                }
            }
            System.out.println("Purchased " + qty + " x " + sel.name + " and added to inventory!");
        }
    }
}
