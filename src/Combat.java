// Combat.java

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.Comparator;

/**
 * Manages the entire combat sequence.
 * UPDATED:
 * - Handles new class resources (Rage, Focus).
 * - Displays new resource bars.
 * - Player's dealDamage method is now used.
 */
public class Combat {

    public static void combatSequenceInit(Player player, ArrayList<Enemy> enemies, Scanner scanner) {
        System.out.println("\n============== COMBAT START ===============");
        
        player.clearAllStatusEffects();
        for (Ability a : player.getAbilities()) a.resetCooldown();
        for (Enemy e : enemies) {
            e.clearAllStatusEffects();
            for (Ability a : e.getAbilities()) a.resetCooldown();
        }

        int roundCounter = 0;
        while (player.getHealthPoints() > 0 && areEnemiesAlive(enemies)) {
            roundCounter++;
            System.out.println("\n---------------- Round " + roundCounter + " -----------------");

            List<Object> turnOrder = determineTurnOrder(player, enemies);
            System.out.print("> Turn order: ");
            System.out.println(turnOrder.stream()
                .map(actor -> actor instanceof Player ? ((Player) actor).getName() : ((Enemy) actor).getName())
                .collect(Collectors.joining(" -> ")));

            for (Object actor : turnOrder) {
                if (player.getHealthPoints() <= 0 || !areEnemiesAlive(enemies)) break;

                if (actor instanceof Player) {
                    playerCombatSequence((Player) actor, enemies, scanner);
                } else if (actor instanceof Enemy) {
                    if (((Enemy) actor).getHealthPoints() > 0) {
                        enemyCombatSequence(player, (Enemy) actor, enemies);
                    }
                }
            }
        }
        System.out.println("\n============== COMBAT END ===============");
    }
    
    private static void playerCombatSequence(Player player, ArrayList<Enemy> enemies, Scanner scanner) {
        System.out.println("\n================== YOUR TURN ==================");
        player.tickStatusEffects();
        if (player.getHealthPoints() <= 0) return;

        if (player.hasStatus("stun")) {
            System.out.println("> You are stunned and cannot act!");
            tickPlayerCooldowns(player);
            return;
        }

        printCombatStatus(player, enemies);
            
        while (true) {
            displayPlayerActions(player);
            int useItemOption = player.getAbilities().size() + 1;
            int choice = getSafeIntInput(scanner, "Enter action number: ", 1, useItemOption);

            if (choice == useItemOption) {
                if (handleItemUsage(player, scanner)) {
                    tickPlayerCooldowns(player);
                    break; 
                }
                continue; 
            }

            if (handleAbilityUsage(player, enemies, choice - 1, scanner)) {
                tickPlayerCooldowns(player);
                break;
            }
        }
    }

    private static void enemyCombatSequence(Player player, Enemy currentEnemy, ArrayList<Enemy> allEnemies) {
        System.out.println("\n================ " + currentEnemy.getName().toUpperCase() + "'S TURN ================");
        currentEnemy.tickStatusEffects();
        if (currentEnemy.getHealthPoints() <= 0) return;

        if (currentEnemy.hasStatus("stun")) {
            System.out.println("> " + currentEnemy.getName() + " is stunned and cannot act!");
            tickEnemyCooldowns(currentEnemy);
            return; 
        }

        Ability chosenAbility = currentEnemy.chooseBestAbility(player, allEnemies);

        if (chosenAbility != null) {
            System.out.println("> " + currentEnemy.getName() + " used " + chosenAbility.getAbilityName() + "!");
            String effect = chosenAbility.getStatusInflicted();

            if (effect.equalsIgnoreCase("Heal")) {
                Enemy targetToHeal = allEnemies.stream()
                    .filter(e -> e.getHealthPoints() > 0 && (double)e.getHealthPoints() / e.getMaxHealth() < 0.5)
                    .min(Comparator.comparingInt(Enemy::getHealthPoints)).orElse(null);
                if (targetToHeal != null) {
                    int healAmount = chosenAbility.getMaxDamage();
                    targetToHeal.heal(healAmount);
                    System.out.println("> It healed " + targetToHeal.getName() + " for " + healAmount + " HP!");
                }
            } else if (effect.equalsIgnoreCase("Buff")) {
                Enemy targetToBuff = allEnemies.stream()
                    .filter(e -> e.getHealthPoints() > 0 && e.getTemporaryDamageBuff() == 0)
                    .findFirst().orElse(null);
                if (targetToBuff != null) {
                    int buffAmount = chosenAbility.getMaxDamage();
                    targetToBuff.applyBuff(buffAmount);
                    System.out.println("> It granted " + targetToBuff.getName() + " a +" + buffAmount + " damage buff!");
                }
            } else {
                int baseDamage = chosenAbility.getRandomDamage() + currentEnemy.getTemporaryDamageBuff();
                double multiplier = DifficultyManager.getDifficulty().getEnemyDamageMultiplier();
                int finalDamage = (int) Math.round(baseDamage * multiplier);
                if (finalDamage > 0) {
                     player.takeDamage(finalDamage);
                     System.out.println("> It dealt " + finalDamage + " damage to " + player.getName() + "!");
                }
                if (Math.random() < chosenAbility.getStatusChance()) {
                    player.applyStatus(chosenAbility.getStatusInflicted(), 3);
                }
            }
            chosenAbility.use();
        } else {
            System.out.println("> " + currentEnemy.getName() + " has no abilities ready or no valid targets.");
        }
        tickEnemyCooldowns(currentEnemy);
        delay(500);
    }
    
    private static List<Object> determineTurnOrder(Player player, ArrayList<Enemy> enemies) {
        Map<Object, Integer> initiativeMap = new java.util.LinkedHashMap<>();
        initiativeMap.put(player, player.rollInitiative());
        for (Enemy enemy : enemies) {
            if (enemy.getHealthPoints() > 0) {
                initiativeMap.put(enemy, enemy.rollInitiative());
            }
        }
        return initiativeMap.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    private static boolean handleAbilityUsage(Player player, ArrayList<Enemy> enemies, int abilityIndex, Scanner scanner) {
        Ability chosen = player.getAbilities().get(abilityIndex);
        if (!chosen.isReady()) {
            System.out.println("> That ability is on cooldown for " + chosen.getCurrentCooldown() + " turn(s).");
            return false;
        }

        // Resource Check
        String playerClass = player.getPlayerClass();
        int cost = chosen.getMpCost();
        switch (playerClass) {
            case "wizard":
                if (player.getMp() < cost) {
                    System.out.println("> Not enough MP (requires " + cost + ").");
                    return false;
                }
                break;
            case "knight":
                 if (player.getRage() < cost) {
                    System.out.println("> Not enough Rage (requires " + cost + ").");
                    return false;
                }
                break;
            case "archer":
                 if (player.getFocus() < cost) {
                    System.out.println("> Not enough Focus (requires " + cost + ").");
                    return false;
                }
                break;
        }


        if (chosen.getTargetType().equalsIgnoreCase("All")) {
            System.out.println("\n> You use " + chosen.getAbilityName() + ", hitting all enemies!");
            delay(500);
            
            List<Enemy> livingEnemies = enemies.stream().filter(e -> e.getHealthPoints() > 0).collect(Collectors.toList());
            for (Enemy target : livingEnemies) {
                applyAbilityEffects(player, chosen, target);
            }

        } else {
            List<Enemy> livingEnemies = enemies.stream().filter(e -> e.getHealthPoints() > 0).collect(Collectors.toList());
            Enemy target = null;
            if (livingEnemies.size() == 1) {
                target = livingEnemies.get(0);
            } else {
                System.out.println("Choose your target:");
                for (int i = 0; i < livingEnemies.size(); i++) {
                    Enemy e = livingEnemies.get(i);
                    System.out.printf("%d) %s (HP: %d/%d)\n", i + 1, e.getName(), e.getHealthPoints(), e.getMaxHealth());
                }
                int targetChoice = getSafeIntInput(scanner, "Enter target number: ", 1, livingEnemies.size());
                target = livingEnemies.get(targetChoice - 1);
            }
            
            System.out.println("\n> You use " + chosen.getAbilityName() + " on " + target.getName() + "!");
            delay(500);
            applyAbilityEffects(player, chosen, target);
        }
        
        // Spend Resource
        switch (playerClass) {
            case "wizard": player.reduceMp(cost); break;
            case "knight": player.spendRage(cost); break;
            case "archer": player.spendFocus(cost); break;
        }

        chosen.use();
        delay(500);
        return true;
    }

    private static void applyAbilityEffects(Player player, Ability ability, Enemy target) {
        int baseDamage = ability.getRandomDamage();
        int bonusDamage = player.getPermanentDamageBonus() + player.getTemporaryDamageBuff();
        int totalDamage = (int)((baseDamage + bonusDamage) * DifficultyManager.getDifficulty().getPlayerDamageMultiplier());

        if(totalDamage > 0) {
            player.dealDamage(totalDamage, target); // Use player's dealDamage method
            System.out.printf("> Dealt %d damage to %s!%n", totalDamage, target.getName());
            if (target.getHealthPoints() <= 0) {
                System.out.println("> " + target.getName() + " has been defeated!");
            }
        }
        
        if (Math.random() < ability.getStatusChance()) {
            target.applyStatus(ability.getStatusInflicted(), 3);
        }
    }

    private static boolean handleItemUsage(Player player, Scanner scanner) {
        List<Shop.ShopItem> inv = player.getInventory();
        if (inv.isEmpty()) { System.out.println("> Inventory is empty. You do not lose your turn."); return false; }
        System.out.println("Choose item to use:");
        Map<String,Integer> counts = new java.util.LinkedHashMap<>();
        for (Shop.ShopItem it : inv) counts.merge(it.name, 1, Integer::sum);
        int idx = 1;
        List<String> names = new ArrayList<>();
        for (var entry : counts.entrySet()) { System.out.printf("%d) %s x%d%n", idx++, entry.getKey(), entry.getValue()); names.add(entry.getKey()); }
        System.out.println("0) Cancel");
        int itemChoice = getSafeIntInput(scanner, "Enter item number: ", 0, names.size());
        if (itemChoice > 0) {
            String chosenName = names.get(itemChoice-1);
            int invIdx = -1;
            for (int i = 0; i < inv.size(); i++) {
                if (inv.get(i).name.equals(chosenName)) { invIdx = i; break; }
            }
            if (invIdx != -1) { player.useInventoryItem(invIdx); return true; }
        }
        System.out.println("> Cancelled using item. You do not lose your turn.");
        return false;
    }

    private static void displayPlayerActions(Player player) {
        System.out.println("\nChoose your action:");
        ArrayList<Ability> abilities = player.getAbilities();
        for (int i = 0; i < abilities.size(); i++) {
            Ability a = abilities.get(i);
            String targetStr = a.getTargetType().equalsIgnoreCase("All") ? " (AoE)" : "";
            
            String costStr = "";
            String playerClass = player.getPlayerClass();
            int cost = a.getMpCost();
            switch (playerClass) {
                case "wizard": costStr = " [MP:" + cost + "]"; break;
                case "knight": costStr = " [Rage:" + cost + "]"; break;
                case "archer": costStr = " [Focus:" + cost + "]"; break;
            }

            String cdStr = a.isReady() ? "" : " [CD:" + a.getCurrentCooldown() + "]";
            System.out.printf("%d) %-15s (Dmg: %d-%d)%s%s%s\n", i + 1, a.getAbilityName(), a.getMinDamage(), a.getMaxDamage(), targetStr, costStr, cdStr);
        }
        System.out.println((abilities.size() + 1) + ") Use Item");
        System.out.println("---------------------------------------------");
    }

    private static void tickPlayerCooldowns(Player player) { for (Ability a : player.getAbilities()) a.tickCooldown(); }
    private static void tickEnemyCooldowns(Enemy enemy) { for (Ability a : enemy.getAbilities()) a.tickCooldown(); }

    private static void printCombatStatus(Player player, ArrayList<Enemy> enemies) {
        printHealthBar(player.getName(), player.getHealthPoints(), player.getMaxHealth());
        
        switch(player.getPlayerClass()){
            case "wizard": printResourceBar("MP", player.getMp(), player.getMaxMp(), "\u001B[34m"); break; // Blue
            case "knight": printResourceBar("Rage", player.getRage(), player.getMaxRage(), "\u001B[31m"); break; // Red
            case "archer": printResourceBar("Focus", player.getFocus(), player.getMaxFocus(), "\u001B[32m"); break; // Green
        }
        
        System.out.println("---------------------------------------------");
        for (Enemy enemy : enemies) {
            if (enemy.getHealthPoints() > 0) {
                printHealthBar(enemy.getName(), enemy.getHealthPoints(), enemy.getMaxHealth());
            }
        }
        System.out.println("=============================================");
    }

    public static void printHealthBar(String name, int current, int max) {
         printResourceBar(name + " HP", current, max, "\u001B[32m");
    }

    public static void printResourceBar(String name, int current, int max, String colorCode) {
        if (max <= 0) return;
        double fraction = Math.max(0, (double) current / max);
        int barLength = 20;
        int filledBars = (int) (fraction * barLength);
        StringBuilder bar = new StringBuilder(String.format("%-15s [%d/%d] ", name, current, max));
        bar.append("[").append(colorCode);
        for (int i = 0; i < filledBars; i++) bar.append("█");
        bar.append("\u001B[0m"); // Reset color
        for (int i = 0; i < barLength - filledBars; i++) bar.append("-");
        bar.append("]");
        System.out.println(bar.toString());
    }
    
    private static int getSafeIntInput(Scanner scanner, String prompt, int min, int max) {
        int choice = -1;
        while (true) {
            System.out.print(prompt);
            try {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) { System.out.println("> Invalid input. Please enter a number."); continue; }
                choice = Integer.parseInt(line);
                if (choice >= min && choice <= max) { break; } 
                else { System.out.println("> Invalid choice. Please enter a number between " + min + " and " + max + "."); }
            } catch (NumberFormatException e) {
                System.out.println("> Invalid input. Please enter a number.");
            }
        }
        return choice;
    }
    
    private static boolean areEnemiesAlive(ArrayList<Enemy> enemies) {
        return enemies.stream().anyMatch(e -> e.getHealthPoints() > 0);
    }

    private static void delay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
