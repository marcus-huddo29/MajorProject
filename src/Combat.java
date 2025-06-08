import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Combat {

    // --- CHANGE --- Removed the static scanner to avoid potential issues. A scanner instance is passed from Client.

    public static void delay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Delay was interrupted!");
        }
    }

    public static void printHealthBarStatus(String name, int currentHealth, int maxHealth) {
        double fraction = (double) currentHealth / maxHealth;
        int barLength = 20;
        int filledBars = (int) Math.max(0, (int) (fraction * barLength));
        
        StringBuilder bar = new StringBuilder(String.format("%-15s HP [%d/%d] ", name, currentHealth, maxHealth));
        bar.append("[\u001B[32m"); // Green color for health
        for (int i = 0; i < filledBars; i++) bar.append("█");
        bar.append("\u001B[31m"); // Red color for missing health
        for (int i = 0; i < barLength - filledBars; i++) bar.append("-");
        bar.append("\u001B[0m]"); // Reset color
        System.out.println(bar.toString());
    }

    public static void printManaBarStatus(String name, int currentMp, int maxMp) {
        if (maxMp <= 0) return;
        double fraction = (double) currentMp / maxMp;
        int barLength = 20;
        int filledBars = (int) Math.max(0, (int) (fraction * barLength));

        StringBuilder bar = new StringBuilder(String.format("%-15s MP [%d/%d] ", name, currentMp, maxMp));
        bar.append("[\u001B[34m"); // Blue color for mana
        for (int i = 0; i < filledBars; i++) bar.append("█");
        bar.append("\u001B[31m"); // Red color for missing mana
        for (int i = 0; i < barLength - filledBars; i++) bar.append("-");
        bar.append("\u001B[0m]"); // Reset color
        System.out.println(bar.toString());
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

    public static int playerCombatSequence(ArrayList<Ability> abilities, Enemy enemy, Player player1, Scanner scanner) {
        while (true) {
            System.out.println("\n================== YOUR TURN ==================");
            printHealthBarStatus(player1.getName(), player1.getHealthPoints(), player1.getMaxHealth());
            printManaBarStatus(player1.getName(), player1.getMp(), player1.getMaxMp());
            System.out.println("---------------------------------------------");
            printHealthBarStatus(enemy.getName(), enemy.getHealthPoints(), enemy.getMaxHealth());
            System.out.println("=============================================");
            
            System.out.println("\nChoose your action:");
            for (int i = 0; i < abilities.size(); i++) {
                Ability a = abilities.get(i);
                // --- CHANGE --- Improved display of ability information.
                String mpCostStr = "";
                if (player1.getPlayerClass().equals("wizard")) {
                    int mpCost = a.getAbilityName().equals("Mana Dart") ? 0 : a.getMinDamage();
                    mpCostStr = " [MP:" + mpCost + "]";
                }
                String cdStr = a.isReady() ? "" : " [CD:" + a.getCurrentCooldown() + "]";
                System.out.printf("%d) %-15s (Dmg: %d-%d)%s%s\n", i + 1, a.getAbilityName(), a.getMinDamage(), a.getMaxDamage(), mpCostStr, cdStr);
            }
            int useItemOption = abilities.size() + 1;
            System.out.println(useItemOption + ") Use Item");
            System.out.println("---------------------------------------------");

            int choice = getSafeIntInput(scanner, "Enter action number: ", 1, useItemOption);

            if (choice == useItemOption) {
                List<Shop.ShopItem> inv = player1.getInventory();
                if (inv.isEmpty()) {
                    System.out.println("> Inventory is empty. You do not lose your turn.");
                    continue; 
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
                    for (int i = 0; i < inv.size(); i++) { if (inv.get(i).name.equals(chosenName)) { invIdx = i; break; } }
                    if (invIdx != -1) {
                        player1.useInventoryItem(invIdx);
                        return enemy.getHealthPoints(); // End turn after using item
                    }
                } else {
                     System.out.println("> Cancelled using item. You do not lose your turn.");
                     continue; 
                }
            }

            Ability chosen = abilities.get(choice - 1);
            if (chosen.isReady() || player1.hasCooldownBuff()) {
                
                int mpCost = 0;
                if (player1.getPlayerClass().equals("wizard")) {
                    mpCost = chosen.getAbilityName().equals("Mana Dart") ? 0 : chosen.getMinDamage();
                }

                if (player1.getMp() < mpCost) {
                    System.out.println("> Not enough MP to cast " + chosen.getAbilityName() + " (requires " + mpCost + ").");
                    continue; // Let the player choose again
                }
                
                System.out.println("\n> You cast " + chosen.getAbilityName() + "!");
                delay(500);
                int base = chosen.getRandomDamage();
                int extra = player1.getExtraDamage();
                int damage = base + extra;
                enemy.takeDamage(damage);

                if (extra > 0) {
                    System.out.println("> You dealt " + base + " (base) + " + extra + " (extra) = " + damage + " damage to " + enemy.getName() + "!");
                } else {
                    System.out.println("> You dealt " + damage + " damage to " + enemy.getName() + "!");
                }
                
                chosen.use();
                player1.reduceMp(mpCost);

                if (chosen.getAbilityName().equals("Shield Bash")) {
                    if (Math.random() < 0.5) { // 50% stun chance
                        System.out.println("> " + enemy.getName() + " is STUNNED!");
                        enemy.setStunned(true);
                    }
                } else if (chosen.getAbilityName().equals("Guard Stance")) {
                    System.out.println("> " + player1.getName() + " braces for the next attack!");
                    player1.setGuardRounds(1);
                }

                if (player1.hasCooldownBuff()) {
                    player1.decrementCooldownBuff();
                }
                delay(500);
                return enemy.getHealthPoints(); // Successfully used ability, end turn
            } else {
                System.out.println("> That ability is on cooldown for " + chosen.getCurrentCooldown() + " turn(s).");
                continue; // Let the player choose again
            }
        }
    }

    public static int enemyCombatSequence(Player player1, Enemy enemy) {
        if (enemy.isStunned()) {
            System.out.println("> " + enemy.getName() + " is stunned and cannot act!");
            enemy.setStunned(false); // Stun wears off after one missed turn
            return player1.getHealthPoints();
        }

        ArrayList<Ability> abilities = enemy.getAbilities();
        if (abilities.isEmpty()) {
            System.out.println(enemy.getName() + " has no abilities to use!");
            return player1.getHealthPoints();
        }

        int randomIndex = (int) (Math.random() * abilities.size());
        Ability chosen = abilities.get(randomIndex);

        if (chosen.isReady()) {
            int baseDamage = chosen.getRandomDamage();
            double mult = DifficultyManager.getDifficulty().getEnemyDamageMultiplier();
            int damage = (int) Math.round(baseDamage * mult);
            
            System.out.println("> " + enemy.getName() + " used " + chosen.getAbilityName() + "!");
            player1.takeDamage(damage);
            System.out.println("> It dealt " + damage + " damage to " + player1.getName() + "!");
            chosen.use();
        } else {
            System.out.println("> " + enemy.getName() + " tried to use " + chosen.getAbilityName() + " but it was on cooldown.");
        }
        delay(500);
        return player1.getHealthPoints();
    }

    public static void combatSequenceInit(Player plyr, ArrayList<Enemy> enemies, ArrayList<Ability> plyrAbilities, Scanner scanner) {
        Enemy currentEnemy = enemies.get(0);
        System.out.println("\n============== COMBAT START ===============");
        
        for (Ability a : plyr.getAbilities()) a.resetCooldown();
        for (Ability a : currentEnemy.getAbilities()) a.resetCooldown();

        int roundCounter = 0;

        while (plyr.getHealthPoints() > 0 && currentEnemy.getHealthPoints() > 0) {
            roundCounter++;
            System.out.println("\n---------------- Round " + roundCounter + " -----------------");

            Map<String, Integer> initMap = new java.util.LinkedHashMap<>();
            initMap.put(plyr.getName(), plyr.rollInitiative());
            initMap.put(currentEnemy.getName(), currentEnemy.rollInitiative());

            List<String> turnOrder = initMap.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .map(Map.Entry::getKey)
                .toList();
            
            System.out.println("> Turn order: " + String.join(" -> ", turnOrder));

            for (String actor : turnOrder) {
                if (plyr.getHealthPoints() <= 0 || currentEnemy.getHealthPoints() <= 0) break;

                if (actor.equals(plyr.getName())) {
                    playerCombatSequence(plyrAbilities, currentEnemy, plyr, scanner);
                    for (Ability a : plyrAbilities) a.tickCooldown();
                } else if (actor.equals(currentEnemy.getName())) {
                    if (currentEnemy.getHealthPoints() <= 0) continue;
                    
                    System.out.println("\n================ ENEMY'S TURN ================");
                    enemyCombatSequence(plyr, currentEnemy);
                    for (Ability a : currentEnemy.getAbilities()) a.tickCooldown();
                }
            }
        }
        System.out.println("\n============== COMBAT END ===============");
    }
}
