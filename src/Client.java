import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {

    /**
     * The main entry point of the application.
     */
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean playAgain = true;
            while(playAgain) {
                // Setup the player character.
                Player player1 = setupPlayer(scanner);
                
                // Start the main game loop.
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

    /**
     * The main game loop, controlling stage progression, combat, and rewards.
     * @param player The player character.
     * @param scanner The shared scanner for user input.
     * @return true if the player completes all stages, false if they are defeated.
     */
    private static boolean gameLoop(Player player1, Scanner scanner) {
        int worldNumber = 1;
        int stageNumber = 1;
        ArrayList<Enemy> allEnemies = Enemy.generateEnemiesFromCSV("enemyStats.csv");

        if (allEnemies.isEmpty()) {
            System.err.println("Could not load any enemies. Exiting game loop.");
            return false;
        }

        while (true) {
            // Check for world progression every 7 stages.
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
                    default:
                        System.out.println("Keeping current difficulty.");
                }
            }

            // Victory condition: Player has cleared all defined enemies.
            if (stageNumber > allEnemies.size()) {
                return true; 
            }
            
            // --- Stage Setup ---
            ArrayList<Enemy> stageEnemies = generateStageEnemies(allEnemies, stageNumber, worldNumber);
            
            System.out.println("\n----------------- Stage " + stageNumber + " -----------------");
            System.out.println("Enemies this stage:");
            for(Enemy e : stageEnemies) {
                System.out.println("- " + e.getName() + " (HP: " + e.getHealthPoints() + ")");
            }

            // --- Combat Encounters ---
            for (int i = 0; i < stageEnemies.size(); i++) {
                Enemy currentEnemy = stageEnemies.get(i);
                System.out.println("\n--- Encounter " + (i + 1) + "/" + stageEnemies.size() + ": " + currentEnemy.getName() + " ---");
                
                // Allow player to choose action before combat.
                handlePreCombatActions(player1, scanner);
                
                // Run combat.
                if (player1.isAutoMode()) {
                    AutoBattle.runStage(player1, new ArrayList<>(List.of(currentEnemy)));
                } else {
                    Combat.combatSequenceInit(player1, new ArrayList<>(List.of(currentEnemy)), scanner);
                }

                // --- Post-Combat Resolution ---
                if (player1.getHealthPoints() <= 0) {
                    System.out.println("\n> " + player1.getName() + " has been defeated...");
                    System.out.println("> Game Over.");
                    return false; 
                }

                // Grant rewards for defeating the enemy.
                handlePostCombatRewards(player1, currentEnemy, stageNumber, worldNumber);

                // Handle level-ups.
                if (player1.canLevelUp()) {
                    handleLevelUp(player1, scanner);
                } else {
                    // Heal for a small amount if no level up.
                    int recovery = (int)(player1.getMaxHealth() * 0.15);
                    player1.heal(recovery);
                    System.out.printf("You recovered %d HP.\n", recovery);
                }
                 System.out.printf("Current HP: %d/%d\n", player1.getHealthPoints(), player1.getMaxHealth());
            }

            System.out.printf("\n> Stage %d cleared! Your abilities have been refreshed.\n", stageNumber);
            player1.resetTemporaryBuffs(); // Reset shop buffs.
            for (Ability ab : player1.getAbilities()) ab.resetCooldown();
            
            stageNumber++;
        }
    }
    
    /**
     * Manages player setup at the beginning of the game.
     */
    private static Player setupPlayer(Scanner scanner) {
        System.out.print("Choose starting difficulty (easy, normal, hard, impossible): ");
        String chosenDiff = scanner.nextLine().trim().toLowerCase();
        switch (chosenDiff) {
            case "easy": DifficultyManager.setDifficulty(Difficulty.EASY); break;
            case "normal": DifficultyManager.setDifficulty(Difficulty.NORMAL); break;
            case "hard": DifficultyManager.setDifficulty(Difficulty.HARD); break;
            case "impossible": DifficultyManager.setDifficulty(Difficulty.IMPOSSIBLE); break;
            default:
                System.out.println("Unrecognized input. Defaulting to Easy.");
                DifficultyManager.setDifficulty(Difficulty.EASY);
        }

        System.out.print("Enter your player name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            name = "Adventurer";
        }

        String playerClass;
        while (true) {
            System.out.print("Choose your class (knight, wizard, archer): ");
            playerClass = scanner.nextLine().trim().toLowerCase();
            if (playerClass.equals("knight") || playerClass.equals("wizard") || playerClass.equals("archer")) {
                break;
            } else {
                System.out.println("> Invalid class. Please enter 'knight', 'wizard', or 'archer'.");
            }
        }
        
        // Create player from data. A more robust system would load this from a CSV.
        Player player = new Player(name, playerClass);
        
        System.out.println("\nWelcome, " + name + "! Starting as a " + playerClass + " with HP=" + player.getMaxHealth() + ", Armour=" + player.getArmour() + ", MP=" + player.getMaxMp());
        delay(500);
        System.out.println("\n--- Your Abilities ---");
        for (Ability a : player.getAbilities()) {
            System.out.println("- " + a.getAbilityName());
        }
        return player;
    }

    /**
     * Handles the pre-combat menu where the player can shop or use items.
     */
    private static void handlePreCombatActions(Player player, Scanner scanner) {
        while (true) {
            System.out.print("\nChoose action: [start] manual combat, [auto] combat, [shop], or [use] item: ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            if (input.equals("shop")) {
                Shop.openShop(player, scanner);
                continue; // Show menu again after shopping.
            } 
            
            if (input.equals("use")) {
                useItemFromInventory(player, scanner);
                continue; // Show menu again after using item.
            }
            
            if (input.equals("auto")) {
                player.setAutoMode(true);
                break; // Start combat.
            }
            
            if (input.equals("start")) {
                player.setAutoMode(false);
                break; // Start combat.
            }
            
            System.out.println("> Invalid entry. Please choose 'start', 'auto', 'shop', or 'use'.");
        }
    }

    /**
     * Creates the list of enemies for the current stage, scaling their stats.
     */
    private static ArrayList<Enemy> generateStageEnemies(ArrayList<Enemy> allEnemies, int stageNumber, int worldNumber) {
        ArrayList<Enemy> stageEnemies = new ArrayList<>();
        Enemy template = allEnemies.get(stageNumber - 1);
        
        // Scale enemy stats based on progress.
        double hpMultiplier = 1.0 + (0.1 * (stageNumber - 1)) + (0.2 * (worldNumber - 1));
        int finalHp = (int) Math.round(template.getMaxHealth() * hpMultiplier);
        
        // Create a new enemy instance from the template with scaled HP.
        stageEnemies.add(new Enemy(template.getName(), finalHp, template.getArmour(), template.getInitiative(), template.getAttackDistance(), template.getCurrencyDrop(), template.getExperienceDrop(), template.getAbilities()));
        
        // Add a second identical enemy on harder difficulties.
        Difficulty diff = DifficultyManager.getDifficulty();
        if (diff == Difficulty.HARD || diff == Difficulty.IMPOSSIBLE) {
            stageEnemies.add(new Enemy(template.getName(), finalHp, template.getArmour(), template.getInitiative(), template.getAttackDistance(), template.getCurrencyDrop(), template.getExperienceDrop(), template.getAbilities()));
        }
        return stageEnemies;
    }

    /**
     * Grants currency and experience to the player after defeating an enemy.
     */
    private static void handlePostCombatRewards(Player player, Enemy enemy, int stageNumber, int worldNumber) {
        // Rewards scale with progress.
        double rewardMultiplier = 1.0 + (0.05 * (stageNumber - 1)) + (0.1 * (worldNumber - 1));
        double gainedCurr = enemy.getCurrencyDrop() * rewardMultiplier;
        double gainedExp = enemy.getExperienceDrop() * rewardMultiplier;
        
        player.addCurrency(gainedCurr);
        player.addExperience(gainedExp);
        System.out.printf("\nYou gained %.1f currency and %.1f experience!\n", gainedCurr, gainedExp);
    }
    
    /**
     * Manages the level-up process, including ability choices.
     */
    private static void handleLevelUp(Player player, Scanner scanner) {
        while (player.canLevelUp()) {
            player.performLevelUp(); // Handles stat increases and healing.
            
            List<Ability> newAbilities = player.getNewLevelUpAbilities();
            System.out.println("You can upgrade an ability!");
            
            // Every 3 levels, the player gets a choice to learn a new ability.
            if (player.getLevelsGained() > 0 && player.getLevelsGained() % 3 == 0 && !newAbilities.isEmpty()) {
                System.out.println("You can also choose to learn a powerful new ability.");
                System.out.println("\n--- Choose Your Level-Up Bonus ---");
                System.out.println("1) Learn a new ability");
                System.out.println("2) Upgrade an existing ability");
                int choice = getSafeIntInput(scanner, "Enter your choice [1-2]: ", 1, 2);

                if (choice == 1) {
                    learnNewAbility(player, newAbilities, scanner);
                    continue; // Skip the upgrade part for this level.
                }
            }

            // Default action: upgrade an existing ability.
            upgradeExistingAbility(player, scanner);
        }
    }

    /**
     * Helper for learning a new ability on level up.
     */
    private static void learnNewAbility(Player player, List<Ability> newAbilities, Scanner scanner) {
        System.out.println("\nChoose one new ability to learn:");
        for (int i = 0; i < newAbilities.size(); i++) {
            Ability a = newAbilities.get(i);
            System.out.printf("%d) %s (Damage %d–%d, CD:%d)\n",
                              i + 1, a.getAbilityName(), a.getMinDamage(), a.getMaxDamage(), a.getCooldown());
        }
        int abilityChoice = getSafeIntInput(scanner, "Enter choice [1-" + newAbilities.size() + "]: ", 1, newAbilities.size());
        Ability learned = newAbilities.get(abilityChoice - 1);
        player.getAbilities().add(learned);
        System.out.println("Learned new ability: " + learned.getAbilityName() + "!");
    }

    /**
     * Helper for upgrading an existing ability on level up.
     */
    private static void upgradeExistingAbility(Player player, Scanner scanner) {
        System.out.println("\nChoose one ability to improve:");
        ArrayList<Ability> currentAbilities = player.getAbilities();
        for (int i = 0; i < currentAbilities.size(); i++) {
            Ability a = currentAbilities.get(i);
            System.out.printf("%d) %s (Current Damage %d–%d)\n",
                              i + 1, a.getAbilityName(), a.getMinDamage(), a.getMaxDamage());
        }
        int upgradeChoice = getSafeIntInput(scanner, "Enter choice [1-" + currentAbilities.size() + "]: ", 1, currentAbilities.size());
        Ability toBuff = currentAbilities.get(upgradeChoice - 1);
        toBuff.buffDamage(4); // Buff amount can be adjusted for balance.
        System.out.println("Upgraded " + toBuff.getAbilityName() + "'s damage by 4!");
    }
    
    /**
     * Manages the "Use Item" screen logic.
     */
    private static void useItemFromInventory(Player player, Scanner scanner) {
        List<Shop.ShopItem> inv = player.getInventory();
        if (inv.isEmpty()) {
            System.out.println("> Inventory is empty.");
            return;
        }
        System.out.println("\n--- Your Inventory ---");
        // Use a map to count and display items neatly.
        java.util.Map<String,Integer> counts = new java.util.LinkedHashMap<>();
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
            // Find the first instance of this item in the actual inventory to use it.
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

    /**
     * A robust method for getting integer input from the user to prevent crashes.
     * @param scanner The shared scanner instance.
     * @param prompt The message to display to the user.
     * @param min The minimum acceptable integer value.
     * @param max The maximum acceptable integer value.
     * @return A validated integer chosen by the user.
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
                    break; // Valid input, exit loop.
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
     * A simple helper to pause execution, making the game's text easier to read.
     * @param milliseconds Milliseconds to pause for.
     */
    private static void delay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}