import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors; // Import the Collectors class

public class Combat {

    /**
     * The main loop for a manual combat encounter.
     * @param player The player character.
     * @param enemies The list of enemies for this fight (usually one).
     * @param scanner The shared scanner for user input.
     */
    public static void combatSequenceInit(Player player, ArrayList<Enemy> enemies, Scanner scanner) {
        // This combat system is designed for 1v1, so we take the first enemy.
        Enemy currentEnemy = enemies.get(0);
        System.out.println("\n============== COMBAT START ===============");
        
        // Reset cooldowns for the fight.
        for (Ability a : player.getAbilities()) a.resetCooldown();
        for (Ability a : currentEnemy.getAbilities()) a.resetCooldown();

        int roundCounter = 0;
        while (player.getHealthPoints() > 0 && currentEnemy.getHealthPoints() > 0) {
            roundCounter++;
            System.out.println("\n---------------- Round " + roundCounter + " -----------------");

            // Determine turn order based on initiative rolls.
            List<String> turnOrder = determineTurnOrder(player, currentEnemy);
            System.out.println("> Turn order: " + String.join(" -> ", turnOrder));

            for (String actorName : turnOrder) {
                if (player.getHealthPoints() <= 0 || currentEnemy.getHealthPoints() <= 0) break;

                if (actorName.equals(player.getName())) {
                    playerCombatSequence(player, currentEnemy, scanner);
                } else if (actorName.equals(currentEnemy.getName())) {
                    enemyCombatSequence(player, currentEnemy);
                }
            }
        }
        System.out.println("\n============== COMBAT END ===============");
    }
    
    /**
     * Handles the player's turn in manual combat.
     */
    private static void playerCombatSequence(Player player, Enemy enemy, Scanner scanner) {
        System.out.println("\n================== YOUR TURN ==================");
        printCombatStatus(player, enemy);
            
        while (true) { // Loop until a valid action is taken.
            displayPlayerActions(player);
            int useItemOption = player.getAbilities().size() + 1;
            int choice = getSafeIntInput(scanner, "Enter action number: ", 1, useItemOption);

            if (choice == useItemOption) {
                // Returns true if an item was successfully used.
                if (handleItemUsage(player, scanner)) {
                    tickPlayerCooldowns(player);
                    break; // End turn after using item.
                }
                // If item usage was cancelled, loop again to choose another action.
                continue;
            }

            // Returns true if an ability was successfully used.
            if (handleAbilityUsage(player, enemy, choice - 1)) {
                tickPlayerCooldowns(player);
                break; // End turn after using ability.
            }
            // If ability was on cooldown or player lacked MP, loop again.
        }
    }

    /**
     * Handles the enemy's turn in combat.
     */
    private static int enemyCombatSequence(Player player, Enemy enemy) {
        System.out.println("\n================ ENEMY'S TURN ================");
        if (enemy.isStunned()) {
            System.out.println("> " + enemy.getName() + " is stunned and cannot act!");
            enemy.setStunned(false); // Stun wears off after one missed turn.
            return player.getHealthPoints();
        }

        // Simple AI: find the first available ability.
        Ability chosenAbility = null;
        for (Ability a : enemy.getAbilities()) {
            if (a.isReady()) {
                chosenAbility = a;
                break;
            }
        }

        if (chosenAbility != null) {
            int baseDamage = chosenAbility.getRandomDamage();
            // Apply difficulty damage multiplier.
            double multiplier = DifficultyManager.getDifficulty().getEnemyDamageMultiplier();
            int finalDamage = (int) Math.round(baseDamage * multiplier);
            
            System.out.println("> " + enemy.getName() + " used " + chosenAbility.getAbilityName() + "!");
            player.takeDamage(finalDamage);
            System.out.println("> It dealt " + finalDamage + " damage to " + player.getName() + "!");
            chosenAbility.use();
        } else {
            System.out.println("> " + enemy.getName() + " tried to use an ability but it was on cooldown.");
        }
        
        tickEnemyCooldowns(enemy);
        delay(500);
        return player.getHealthPoints();
    }
    
    /**
     * Rolls initiative for player and enemy to determine turn order.
     */
    private static List<String> determineTurnOrder(Player player, Enemy enemy) {
        Map<String, Integer> initiativeMap = new java.util.LinkedHashMap<>();
        initiativeMap.put(player.getName(), player.rollInitiative());
        initiativeMap.put(enemy.getName(), enemy.rollInitiative());

        // Sort entries by initiative value in descending order.
        // **FIXED**: Changed .toList() to .collect(Collectors.toList()) for compatibility.
        return initiativeMap.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    /**
     * Manages the logic for the player using an ability.
     * @return true if an ability was successfully used, false otherwise.
     */
    private static boolean handleAbilityUsage(Player player, Enemy enemy, int abilityIndex) {
        Ability chosen = player.getAbilities().get(abilityIndex);
        
        if (!chosen.isReady()) {
            System.out.println("> That ability is on cooldown for " + chosen.getCurrentCooldown() + " turn(s).");
            return false;
        }

        // Check for MP cost, specific to the Wizard class.
        int mpCost = 0;
        if ("wizard".equalsIgnoreCase(player.getPlayerClass())) {
            mpCost = chosen.getAbilityName().equals("Mana Dart") ? 0 : chosen.getMinDamage();
        }
        if (player.getMp() < mpCost) {
            System.out.println("> Not enough MP to cast " + chosen.getAbilityName() + " (requires " + mpCost + ").");
            return false;
        }
        
        System.out.println("\n> You cast " + chosen.getAbilityName() + "!");
        delay(500);
        
        int baseDamage = chosen.getRandomDamage();
        int bonusDamage = player.getPermanentDamageBonus() + player.getTemporaryDamageBuff();
        int totalDamage = baseDamage + bonusDamage;
        enemy.takeDamage(totalDamage);

        if (bonusDamage > 0) {
            System.out.printf("> You dealt %d (base) + %d (bonus) = %d damage to %s!%n", baseDamage, bonusDamage, totalDamage, enemy.getName());
        } else {
            System.out.println("> You dealt " + totalDamage + " damage to " + enemy.getName() + "!");
        }
        
        chosen.use();
        player.reduceMp(mpCost);

        // Handle special ability effects.
        if ("Shield Bash".equals(chosen.getAbilityName())) {
            if (Math.random() < 0.5) { // 50% stun chance.
                System.out.println("> " + enemy.getName() + " is STUNNED!");
                enemy.setStunned(true);
            }
        } else if ("Guard Stance".equals(chosen.getAbilityName())) {
            System.out.println("> " + player.getName() + " braces for the next attack!");
            player.setGuardRounds(1);
        }
        
        delay(500);
        return true; // Ability was used successfully.
    }

    /**
     * Manages the logic for the player using an item from their inventory.
     * @return true if an item was successfully used, false if cancelled.
     */
    private static boolean handleItemUsage(Player player, Scanner scanner) {
        List<Shop.ShopItem> inv = player.getInventory();
        if (inv.isEmpty()) {
            System.out.println("> Inventory is empty. You do not lose your turn.");
            return false; 
        }
        
        System.out.println("Choose item to use:");
        Map<String,Integer> counts = new java.util.LinkedHashMap<>();
        for (Shop.ShopItem it : inv) counts.merge(it.name, 1, Integer::sum);
        
        int idx = 1;
        List<String> names = new ArrayList<>();
        for (var entry : counts.entrySet()) {
            System.out.printf("%d) %s x%d%n", idx++, entry.getKey(), entry.getValue());
            names.add(entry.getKey());
        }
        System.out.println("0) Cancel");
        int itemChoice = getSafeIntInput(scanner, "Enter item number: ", 0, names.size());
        
        if (itemChoice > 0) {
            String chosenName = names.get(itemChoice-1);
            int invIdx = -1;
            for (int i = 0; i < inv.size(); i++) {
                if (inv.get(i).name.equals(chosenName)) {
                    invIdx = i;
                    break;
                }
            }
            if (invIdx != -1) {
                player.useInventoryItem(invIdx);
                return true; // Item was used.
            }
        }
        
        System.out.println("> Cancelled using item. You do not lose your turn.");
        return false;
    }

    /**
     * Displays the player's available actions (abilities and items).
     */
    private static void displayPlayerActions(Player player) {
        System.out.println("\nChoose your action:");
        ArrayList<Ability> abilities = player.getAbilities();
        for (int i = 0; i < abilities.size(); i++) {
            Ability a = abilities.get(i);
            String mpCostStr = "";
            if ("wizard".equalsIgnoreCase(player.getPlayerClass())) {
                int mpCost = a.getAbilityName().equals("Mana Dart") ? 0 : a.getMinDamage();
                mpCostStr = " [MP:" + mpCost + "]";
            }
            String cdStr = a.isReady() ? "" : " [CD:" + a.getCurrentCooldown() + "]";
            System.out.printf("%d) %-15s (Dmg: %d-%d)%s%s\n", i + 1, a.getAbilityName(), a.getMinDamage(), a.getMaxDamage(), mpCostStr, cdStr);
        }
        System.out.println((abilities.size() + 1) + ") Use Item");
        System.out.println("---------------------------------------------");
    }

    private static void tickPlayerCooldowns(Player player) {
        for (Ability a : player.getAbilities()) a.tickCooldown();
    }

    private static void tickEnemyCooldowns(Enemy enemy) {
        for (Ability a : enemy.getAbilities()) a.tickCooldown();
    }

    /**
     * Prints the current health and mana status for both player and enemy.
     */
    private static void printCombatStatus(Player player, Enemy enemy) {
        printHealthBar(player.getName(), player.getHealthPoints(), player.getMaxHealth());
        printManaBar(player.getName(), player.getMp(), player.getMaxMp());
        System.out.println("---------------------------------------------");
        printHealthBar(enemy.getName(), enemy.getHealthPoints(), enemy.getMaxHealth());
        System.out.println("=============================================");
    }

    /**
     * Prints a visual health bar to the console.
     */
    public static void printHealthBar(String name, int currentHealth, int maxHealth) {
        double fraction = Math.max(0, (double) currentHealth / maxHealth);
        int barLength = 20;
        int filledBars = (int) (fraction * barLength);
        
        StringBuilder bar = new StringBuilder(String.format("%-15s HP [%d/%d] ", name, currentHealth, maxHealth));
        bar.append("[\u001B[32m"); // Green color for health
        for (int i = 0; i < filledBars; i++) bar.append("█");
        bar.append("\u001B[31m"); // Red color for missing health
        for (int i = 0; i < barLength - filledBars; i++) bar.append("-");
        bar.append("\u001B[0m]"); // Reset color
        System.out.println(bar.toString());
    }

    /**
     * Prints a visual mana bar to the console.
     */
    public static void printManaBar(String name, int currentMp, int maxMp) {
        if (maxMp <= 0) return;
        double fraction = Math.max(0, (double) currentMp / maxMp);
        int barLength = 20;
        int filledBars = (int) (fraction * barLength);

        StringBuilder bar = new StringBuilder(String.format("%-15s MP [%d/%d] ", name, currentMp, maxMp));
        bar.append("[\u001B[34m"); // Blue color for mana
        for (int i = 0; i < filledBars; i++) bar.append("█");
        bar.append("\u001B[31m"); // Red color for missing mana
        for (int i = 0; i < barLength - filledBars; i++) bar.append("-");
        bar.append("\u001B[0m]"); // Reset color
        System.out.println(bar.toString());
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

    /**
     * A simple helper to pause execution, making the combat log easier to read.
     */
    private static void delay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}