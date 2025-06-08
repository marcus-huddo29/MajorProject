import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean playAgain = true;
            while(playAgain) {
                Player player1 = setupPlayer(scanner);
                if (player1 == null) { // Handle case where player data can't be loaded
                    System.err.println("Failed to create player. Exiting.");
                    return;
                }
                
                boolean playerWonGame = gameLoop(player1, scanner);

                if (playerWonGame) {
                     System.out.println("\n*******************************************");
                     System.out.println("Congratulations on completing the game!");
                     System.out.println("*******************************************");
                }
                
                System.out.print("\nDo you want to restart the game? (yes/no): ");
                String reply = scanner.nextLine().trim().toLowerCase();
                if (!reply.startsWith("y")) {
                    playAgain = false;
                }
            }
            System.out.println("\nThanks for playing!");

        } catch (Exception e) {
            System.err.println("An unexpected error occurred. The game will now exit.");
            e.printStackTrace();
        }
    }

    private static boolean gameLoop(Player player1, Scanner scanner) {
        int worldNumber = 1;
        int stageNumber = 1;
        ArrayList<Enemy> allEnemies = Enemy.generateEnemiesFromCSV("enemyStats.csv");

        if (allEnemies.isEmpty()) {
            System.err.println("Could not load any enemies. Exiting game loop.");
            return false;
        }

        while (true) {
            if (stageNumber > 7) {
                worldNumber++;
                stageNumber = 1;
                System.out.println("\n=================================");
                System.out.println("      World " + worldNumber + " Begins!      ");
                System.out.println("=================================");
                System.out.println("The enemies have grown stronger!");
                System.out.print("Current difficulty is " + DifficultyManager.getDifficulty().name() + ". Change? (easy, normal, hard, impossible, or 'no'): ");
                String diffChoice = scanner.nextLine().trim().toLowerCase();
                 switch (diffChoice) {
                    case "easy": DifficultyManager.setDifficulty(Difficulty.EASY); break;
                    case "normal": DifficultyManager.setDifficulty(Difficulty.NORMAL); break;
                    case "hard": DifficultyManager.setDifficulty(Difficulty.HARD); break;
                    case "impossible": DifficultyManager.setDifficulty(Difficulty.IMPOSSIBLE); break;
                }
            }

            if (stageNumber > allEnemies.size()) {
                return true; 
            }
            
            // --- Stage Setup (UPDATED with scaling) ---
            ArrayList<Enemy> stageEnemies = generateStageEnemies(allEnemies, stageNumber, worldNumber);
            
            System.out.println("\n----------------- Stage " + stageNumber + " -----------------");
            System.out.println("Enemies this stage:");
            for(Enemy e : stageEnemies) {
                System.out.println("- " + e.getName() + " (HP: " + e.getHealthPoints() + ", Armour: " + e.getArmour() + ")");
            }

            for (int i = 0; i < stageEnemies.size(); i++) {
                Enemy currentEnemy = stageEnemies.get(i);
                System.out.println("\n--- Encounter " + (i + 1) + "/" + stageEnemies.size() + ": " + currentEnemy.getName() + " ---");
                
                handlePreCombatActions(player1, scanner);
                
                if (player1.isAutoMode()) {
                    AutoBattle.runStage(player1, new ArrayList<>(List.of(currentEnemy)));
                } else {
                    Combat.combatSequenceInit(player1, new ArrayList<>(List.of(currentEnemy)), scanner);
                }

                if (player1.getHealthPoints() <= 0) {
                    System.out.println("\n> " + player1.getName() + " has been defeated... Game Over.");
                    return false; 
                }

                handlePostCombatRewards(player1, currentEnemy, stageNumber, worldNumber);

                if (player1.canLevelUp()) {
                    handleLevelUp(player1, scanner);
                } else {
                    // UPDATED: Increased post-combat healing
                    int recovery = (int)(player1.getMaxHealth() * 0.25);
                    player1.heal(recovery);
                    System.out.printf("You recovered %d HP.\n", recovery);
                }
                 System.out.printf("Current HP: %d/%d\n", player1.getHealthPoints(), player1.getMaxHealth());
            }

            System.out.printf("\n> Stage %d cleared!\n", stageNumber);
            player1.resetTemporaryBuffs();
            
            stageNumber++;
        }
    }
    
    private static Player setupPlayer(Scanner scanner) {
        System.out.print("Choose starting difficulty (easy, normal, hard, impossible): ");
        String chosenDiff = scanner.nextLine().trim().toLowerCase();
        switch (chosenDiff) {
            case "normal": DifficultyManager.setDifficulty(Difficulty.NORMAL); break;
            case "hard": DifficultyManager.setDifficulty(Difficulty.HARD); break;
            case "impossible": DifficultyManager.setDifficulty(Difficulty.IMPOSSIBLE); break;
            default: DifficultyManager.setDifficulty(Difficulty.EASY);
        }

        System.out.print("Enter your player name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) name = "Adventurer";

        String playerClass;
        while (true) {
            System.out.print("Choose your class (knight, wizard, archer): ");
            playerClass = scanner.nextLine().trim().toLowerCase();
            if (playerClass.equals("knight") || playerClass.equals("wizard") || playerClass.equals("archer")) break;
            System.out.println("> Invalid class. Please enter 'knight', 'wizard', or 'archer'.");
        }
        
        // --- UPDATED: Load player stats from CSV ---
        Player player = loadPlayerFromCSV("playerStats.csv", name, playerClass);
        if (player != null) {
            System.out.println("\nWelcome, " + name + "! Starting as a " + playerClass + " with HP=" + player.getMaxHealth() + ", Armour=" + player.getArmour() + ", MP=" + player.getMaxMp());
            delay(500);
            System.out.println("\n--- Your Abilities ---");
            for (Ability a : player.getAbilities()) System.out.println("- " + a.getAbilityName());
        }
        return player;
    }

    private static Player loadPlayerFromCSV(String filename, String playerName, String playerClass) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p[1].trim().equalsIgnoreCase(playerClass)) {
                    int maxHP = Integer.parseInt(p[2].trim());
                    int armour = Integer.parseInt(p[3].trim());
                    int init = Integer.parseInt(p[4].trim());
                    int maxMp = Integer.parseInt(p[5].trim());
                    return new Player(playerName, playerClass, maxHP, armour, init, maxMp);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Failed to load player data from " + filename + ": " + e.getMessage());
        }
        return null; // Return null if class not found or error occurs
    }

    private static void handlePreCombatActions(Player player, Scanner scanner) {
        while (true) {
            System.out.print("\nChoose action: [start] combat, [auto] combat, [shop], or [use] item: ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (input.equals("shop")) {
                Shop.openShop(player, scanner);
                continue;
            } 
            if (input.equals("use")) {
                useItemFromInventory(player, scanner);
                continue;
            }
            if (input.equals("auto")) {
                player.setAutoMode(true);
                break;
            }
            if (input.equals("start")) {
                player.setAutoMode(false);
                break;
            }
            System.out.println("> Invalid entry.");
        }
    }

    private static ArrayList<Enemy> generateStageEnemies(ArrayList<Enemy> allEnemies, int stageNumber, int worldNumber) {
        ArrayList<Enemy> stageEnemies = new ArrayList<>();
        Enemy template = allEnemies.get((stageNumber - 1) % allEnemies.size()); // Use modulo to avoid index out of bounds
        
        // --- UPDATED: Re-balanced scaling multipliers ---
        double hpMultiplier = 1.0 + (0.1 * (stageNumber - 1)) + (0.25 * (worldNumber - 1));
        double armourMultiplier = 1.0 + (0.04 * (stageNumber - 1)) + (0.10 * (worldNumber - 1));
        double damageMultiplier = 1.0 + (0.06 * (stageNumber - 1)) + (0.15 * (worldNumber - 1));

        int finalHp = (int) Math.round(template.getMaxHealth() * hpMultiplier);
        int finalArmour = (int) Math.round(template.getArmour() * armourMultiplier);
        
        // Create a new set of abilities and scale their damage
        ArrayList<Ability> scaledAbilities = new ArrayList<>();
        for (Ability baseAbility : template.getAbilities()) {
            Ability newAbility = new Ability(baseAbility.getAbilityName(), baseAbility.getMinDamage(), baseAbility.getMaxDamage(), baseAbility.getStatusInflicted(), baseAbility.getCooldown());
            newAbility.applyDamageMultiplier(damageMultiplier);
            scaledAbilities.add(newAbility);
        }
        
        stageEnemies.add(new Enemy(template.getName(), finalHp, finalArmour, template.getInitiative(), template.getCurrencyDrop(), template.getExperienceDrop(), scaledAbilities));
        
        Difficulty diff = DifficultyManager.getDifficulty();
        if ((diff == Difficulty.HARD || diff == Difficulty.IMPOSSIBLE) && stageNumber > 2) {
            stageEnemies.add(new Enemy(template.getName(), finalHp, finalArmour, template.getInitiative(), template.getCurrencyDrop(), template.getExperienceDrop(), scaledAbilities));
        }
        return stageEnemies;
    }

    private static void handlePostCombatRewards(Player player, Enemy enemy, int stageNumber, int worldNumber) {
        // --- NEW: Diminishing returns for grinding ---
        double levelDifference = player.getLevelsGained() - (stageNumber + (worldNumber - 1) * 7);
        double penalty = 1.0;
        if (levelDifference > 3) {
            penalty = Math.max(0.1, 1.0 - (levelDifference - 3) * 0.2); // 20% reduction per level above 3 levels higher
            System.out.printf("> Rewards reduced by %.0f%% for being over-leveled.\n", (1 - penalty) * 100);
        }

        double rewardMultiplier = 1.0 + (0.05 * (stageNumber - 1)) + (0.1 * (worldNumber - 1));
        double gainedCurr = enemy.getCurrencyDrop() * rewardMultiplier * penalty;
        double gainedExp = enemy.getExperienceDrop() * rewardMultiplier * penalty;
        
        player.addCurrency(gainedCurr);
        player.addExperience(gainedExp);
        System.out.printf("\nYou gained %.1f currency and %.1f experience!\n", gainedCurr, gainedExp);
    }
    
    private static void handleLevelUp(Player player, Scanner scanner) {
        while (player.canLevelUp()) {
            player.performLevelUp();
            
            List<Ability> newAbilities = player.getNewLevelUpAbilities();
            
            if (player.getLevelsGained() > 0 && player.getLevelsGained() % 3 == 0 && !newAbilities.isEmpty()) {
                System.out.println("\n--- Choose Your Level-Up Bonus ---");
                System.out.println("1) Learn a new ability");
                System.out.println("2) Upgrade an existing ability");
                int choice = getSafeIntInput(scanner, "Enter your choice [1-2]: ", 1, 2);

                if (choice == 1) {
                    learnNewAbility(player, newAbilities, scanner);
                    continue;
                }
            }
            upgradeExistingAbility(player, scanner);
        }
    }

    private static void learnNewAbility(Player player, List<Ability> newAbilities, Scanner scanner) {
        System.out.println("\nChoose one new ability to learn:");
        for (int i = 0; i < newAbilities.size(); i++) {
            Ability a = newAbilities.get(i);
            System.out.printf("%d) %s (Dmg %d–%d, CD:%d)\n", i + 1, a.getAbilityName(), a.getMinDamage(), a.getMaxDamage(), a.getCooldown());
        }
        int abilityChoice = getSafeIntInput(scanner, "Enter choice [1-" + newAbilities.size() + "]: ", 1, newAbilities.size());
        Ability learned = newAbilities.get(abilityChoice - 1);
        player.getAbilities().add(learned);
        System.out.println("Learned new ability: " + learned.getAbilityName() + "!");
    }

    private static void upgradeExistingAbility(Player player, Scanner scanner) {
        System.out.println("\nChoose one ability to improve:");
        ArrayList<Ability> currentAbilities = player.getAbilities();
        for (int i = 0; i < currentAbilities.size(); i++) {
            Ability a = currentAbilities.get(i);
            System.out.printf("%d) %-15s (Dmg %d–%d, CD %d, Effect %s)\n", i + 1, a.getAbilityName(), a.getMinDamage(), a.getMaxDamage(), a.getCooldown(), a.getStatusInflicted());
        }
        int upgradeChoice = getSafeIntInput(scanner, "Enter choice [1-" + currentAbilities.size() + "]: ", 1, currentAbilities.size());
        Ability toBuff = currentAbilities.get(upgradeChoice - 1);

        // --- NEW: Offer different upgrade paths ---
        System.out.println("\nWhat to upgrade for " + toBuff.getAbilityName() + "?");
        System.out.println("1) Increase Damage (+2 Min, +3 Max)");
        boolean hasStatus = !toBuff.getStatusInflicted().equalsIgnoreCase("None");
        if (hasStatus) {
            System.out.println("2) Increase Status Chance (+15%)");
        }
        
        int maxChoice = hasStatus ? 2 : 1;
        int buffChoice = getSafeIntInput(scanner, "Enter choice: ", 1, maxChoice);
        
        if (buffChoice == 1) {
            toBuff.buffDamage(3);
            System.out.println("Upgraded " + toBuff.getAbilityName() + "'s damage!");
        } else if (buffChoice == 2 && hasStatus) {
            toBuff.increaseStatusChance(0.15);
             System.out.println("Upgraded " + toBuff.getAbilityName() + "'s status chance!");
        }
    }
    
    private static void useItemFromInventory(Player player, Scanner scanner) {
        List<Shop.ShopItem> inv = player.getInventory();
        if (inv.isEmpty()) {
            System.out.println("> Inventory is empty.");
            return;
        }
        System.out.println("\n--- Your Inventory ---");
        Map<String,Integer> counts = new java.util.LinkedHashMap<>();
        for (Shop.ShopItem it : inv) counts.merge(it.name, 1, Integer::sum);
        
        int idx = 1;
        List<String> uniqueItemNames = new ArrayList<>();
        for (var entry : counts.entrySet()) {
            System.out.printf("%d) %s x%d%n", idx++, entry.getKey(), entry.getValue());
            uniqueItemNames.add(entry.getKey());
        }
        
        int itemNum = getSafeIntInput(scanner, "Enter item number to use (0 to cancel): ", 0, uniqueItemNames.size());

        if (itemNum > 0) {
            String chosenName = uniqueItemNames.get(itemNum - 1);
            int inventoryIndex = -1;
            for (int k = 0; k < inv.size(); k++) {
                if (inv.get(k).name.equals(chosenName)) {
                    inventoryIndex = k;
                    break;
                }
            }
            if (inventoryIndex != -1) {
                player.useInventoryItem(inventoryIndex);
            }
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
                if (choice >= min && choice <= max) break;
                System.out.println("> Invalid choice. Please enter a number between " + min + " and " + max + ".");
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
