import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Combat {

    public static void combatSequenceInit(Player player, ArrayList<Enemy> enemies, Scanner scanner) {
        Enemy currentEnemy = enemies.get(0);
        System.out.println("\n============== COMBAT START ===============");
        
        player.clearAllStatusEffects();
        currentEnemy.clearAllStatusEffects();
        for (Ability a : player.getAbilities()) a.resetCooldown();
        for (Ability a : currentEnemy.getAbilities()) a.resetCooldown();

        int roundCounter = 0;
        while (player.getHealthPoints() > 0 && currentEnemy.getHealthPoints() > 0) {
            roundCounter++;
            System.out.println("\n---------------- Round " + roundCounter + " -----------------");

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
    
    private static void playerCombatSequence(Player player, Enemy enemy, Scanner scanner) {
        System.out.println("\n================== YOUR TURN ==================");
        player.tickStatusEffects();
        if (player.getHealthPoints() <= 0) return;

        if (player.hasStatus("stun")) {
            System.out.println("> You are stunned and cannot act!");
            tickPlayerCooldowns(player);
            return;
        }

        printCombatStatus(player, enemy);
            
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

            if (handleAbilityUsage(player, enemy, choice - 1)) {
                tickPlayerCooldowns(player);
                break;
            }
        }
    }

    private static void enemyCombatSequence(Player player, Enemy enemy) {
        System.out.println("\n================ ENEMY'S TURN ================");
        enemy.tickStatusEffects();
        if (enemy.getHealthPoints() <= 0) return;

        if (enemy.hasStatus("stun")) {
            System.out.println("> " + enemy.getName() + " is stunned and cannot act!");
            tickEnemyCooldowns(enemy);
            return; 
        }

        Ability chosenAbility = enemy.chooseBestAbility(player);

        if (chosenAbility != null) {
            int baseDamage = chosenAbility.getRandomDamage();
            double multiplier = DifficultyManager.getDifficulty().getEnemyDamageMultiplier();
            int finalDamage = (int) Math.round(baseDamage * multiplier);
            
            System.out.println("> " + enemy.getName() + " used " + chosenAbility.getAbilityName() + "!");
            if (finalDamage > 0) {
                 player.takeDamage(finalDamage);
                 System.out.println("> It dealt " + finalDamage + " damage to " + player.getName() + "!");
            }
           
            if (Math.random() < chosenAbility.getStatusChance()) {
                player.applyStatus(chosenAbility.getStatusInflicted(), 3);
            }

            chosenAbility.use();
        } else {
            System.out.println("> " + enemy.getName() + " has no abilities ready.");
        }
        
        tickEnemyCooldowns(enemy);
        delay(500);
    }
    
    private static List<String> determineTurnOrder(Player player, Enemy enemy) {
        Map<String, Integer> initiativeMap = new java.util.LinkedHashMap<>();
        initiativeMap.put(player.getName(), player.rollInitiative());
        initiativeMap.put(enemy.getName(), enemy.rollInitiative());

        return initiativeMap.entrySet().stream()
            .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    private static boolean handleAbilityUsage(Player player, Enemy enemy, int abilityIndex) {
        Ability chosen = player.getAbilities().get(abilityIndex);
        
        if (!chosen.isReady()) {
            System.out.println("> That ability is on cooldown for " + chosen.getCurrentCooldown() + " turn(s).");
            return false;
        }

        int mpCost = "wizard".equalsIgnoreCase(player.getPlayerClass()) ? (int)(chosen.getMinDamage() * 0.8) : 0;
        if (chosen.getAbilityName().equals("Mana Dart")) mpCost = 0;
        
        if (player.getMp() < mpCost) {
            System.out.println("> Not enough MP (requires " + mpCost + ").");
            return false;
        }
        
        System.out.println("\n> You use " + chosen.getAbilityName() + "!");
        delay(500);
        
        int baseDamage = chosen.getRandomDamage();
        double difficultyMultiplier = DifficultyManager.getDifficulty().getPlayerDamageMultiplier();
        int bonusDamage = player.getPermanentDamageBonus() + player.getTemporaryDamageBuff();
        int totalDamage = (int)((baseDamage + bonusDamage) * difficultyMultiplier);

        if(totalDamage > 0) {
            enemy.takeDamage(totalDamage);
            System.out.printf("> You dealt %d damage to %s!%n", totalDamage, enemy.getName());
        }
        
        if (Math.random() < chosen.getStatusChance()) {
            enemy.applyStatus(chosen.getStatusInflicted(), 3);
        }

        chosen.use();
        player.reduceMp(mpCost);
        delay(500);
        return true;
    }

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
                return true;
            }
        }
        
        System.out.println("> Cancelled using item. You do not lose your turn.");
        return false;
    }

    private static void displayPlayerActions(Player player) {
        System.out.println("\nChoose your action:");
        ArrayList<Ability> abilities = player.getAbilities();
        for (int i = 0; i < abilities.size(); i++) {
            Ability a = abilities.get(i);
            String mpCostStr = "";
            if ("wizard".equalsIgnoreCase(player.getPlayerClass())) {
                 int mpCost = (int)(a.getMinDamage() * 0.8);
                 if (a.getAbilityName().equals("Mana Dart")) mpCost = 0;
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

    private static void printCombatStatus(Player player, Enemy enemy) {
        printHealthBar(player.getName(), player.getHealthPoints(), player.getMaxHealth());
        printManaBar(player.getName(), player.getMp(), player.getMaxMp());
        System.out.println("---------------------------------------------");
        printHealthBar(enemy.getName(), enemy.getHealthPoints(), enemy.getMaxHealth());
        System.out.println("=============================================");
    }

    public static void printHealthBar(String name, int currentHealth, int maxHealth) {
        double fraction = Math.max(0, (double) currentHealth / maxHealth);
        int barLength = 20;
        int filledBars = (int) (fraction * barLength);
        
        StringBuilder bar = new StringBuilder(String.format("%-15s HP [%d/%d] ", name, currentHealth, maxHealth));
        bar.append("[\u001B[32m");
        for (int i = 0; i < filledBars; i++) bar.append("█");
        bar.append("\u001B[31m");
        for (int i = 0; i < barLength - filledBars; i++) bar.append("-");
        bar.append("\u001B[0m]");
        System.out.println(bar.toString());
    }

    public static void printManaBar(String name, int currentMp, int maxMp) {
        if (maxMp <= 0) return;
        double fraction = Math.max(0, (double) currentMp / maxMp);
        int barLength = 20;
        int filledBars = (int) (fraction * barLength);

        StringBuilder bar = new StringBuilder(String.format("%-15s MP [%d/%d] ", name, currentMp, maxMp));
        bar.append("[\u001B[34m");
        for (int i = 0; i < filledBars; i++) bar.append("█");
        bar.append("\u001B[31m");
        for (int i = 0; i < barLength - filledBars; i++) bar.append("-");
        bar.append("\u001B[0m]");
        System.out.println(bar.toString());
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

    private static void delay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
