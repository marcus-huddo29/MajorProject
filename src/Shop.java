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
                // CSV columns: 0=name,1=type,2=value,3=cost,4=description,5=restriction
                // Trim and parse restriction: only take first token before any space or comment, and lowercase
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
            // Build eligible item list each loop to ensure up-to-date restrictions
            ArrayList<ShopItem> availableItems = new ArrayList<>();
            for (ShopItem it : items) {
                String r = it.restriction.toLowerCase();
                String cls = player.getPlayerClass().toLowerCase();
                if (r.equals("all") || r.equals(cls)) {
                    // Prevent repurchase of identical weapon
                    if (it.type.equals("weapon")) {
                        boolean alreadyOwned = false;
                        for (ShopItem invItem : player.getInventory()) {
                            if (invItem.type.equals("weapon") && invItem.name.equals(it.name)) {
                                alreadyOwned = true;
                                break;
                            }
                        }
                        if (alreadyOwned) continue;
                    }
                    // Prevent repurchase of attack_buff item (inventory), and also prevent stacking per stage
                    if (it.type.equals("attack_buff")) {
                        // Prevent multiple buffs per stage
                        if (player.hasUsedAttackBuffThisStage()) continue;
                        boolean alreadyBuffed = false;
                        for (ShopItem invItem : player.getInventory()) {
                            if (invItem.type.equals("attack_buff")) {
                                alreadyBuffed = true;
                                break;
                            }
                        }
                        if (alreadyBuffed) continue;
                    }
                    availableItems.add(it);
                }
            }
            System.out.println("\nYour currency: " + player.getCurrency());
            System.out.println("Available items:");
            for (int i = 0; i < availableItems.size(); i++) {
                ShopItem it = availableItems.get(i);
                System.out.printf("%d) %s (%s) – value: %d, cost: %d each%n    → %s%n",
                                  i+1, it.name, it.type, it.value, it.cost, it.description);
            }
            System.out.println("0) Exit Shop");
            System.out.print("Enter item number to buy: ");
            int choice;
            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException ex) {
                System.out.println("Invalid input.");
                continue;
            }
            if (choice == 0) break;
            if (choice < 1 || choice > availableItems.size()) {
                System.out.println("Invalid choice.");
                continue;
            }
            ShopItem sel = availableItems.get(choice-1);
            // Prompt for quantity
            System.out.print("Enter quantity to buy: ");
            int qty;
            try {
                qty = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException ex) {
                System.out.println("Invalid quantity.");
                continue;
            }
            if (qty <= 0) {
                System.out.println("Quantity must be at least 1.");
                continue;
            }
            int totalCost = sel.cost * qty;
            if (player.getCurrency() < totalCost) {
                System.out.println("Not enough currency for " + qty + " " + sel.name + "(s).");
                continue;
            }
            player.currency -= totalCost;
            for (int i = 0; i < qty; i++) {
                player.addItemToInventory(sel);
                // If attack_buff, mark as used for this stage (only allow one per stage)
                if (sel.type.equals("attack_buff")) {
                    player.setUsedAttackBuffThisStage(true);
                }
            }
            System.out.println("Purchased " + qty + " x " + sel.name + " and added to inventory!");
        }
        // Do not close sc, as it wraps System.in which may be used elsewhere
    }
}