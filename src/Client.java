// Client.java

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Client {

    private static final int FINAL_WORLD = 3; 
    // --- UPGRADE --- Added a Random instance for randomized encounters
    private static final Random random = new Random();

    public static void main(String[] args) {
        AbilityFactory.loadAbilities("abilities.csv");
        EnemyAbilityLoader.loadEnemyAbilities("enemy_abilities.csv");

        System.out.println("==============================================");
        System.out.println("         A LEGEND IN THE MAKING");
        System.out.println("==============================================");
        delay(1500);
        System.out.println("\nThe land is shrouded in darkness, overrun by monsters.");
        System.out.println("A lone adventurer rises to the challenge, seeking to end the chaos.");
        delay(2500);
        System.out.println("\nYour journey begins now...");
        delay(1000);


        try (Scanner scanner = new Scanner(System.in)) {
            boolean playAgain = true;
            while(playAgain) {
                Player player1 = setupPlayer(scanner);
                if (player1 == null) {
                    System.err.println("Failed to create player. Exiting.");
                    return;
                }
                
                boolean playerWonGame = gameLoop(player1, scanner);

                if (playerWonGame) {
                     System.out.println("\n*******************************************");
                     System.out.println("The final boss has been vanquished!");
                     System.out.println("Light returns to the realm, and your name is sung by bards for generations.");
                     System.out.println("Congratulations, " + player1.getName() + ". You are a true hero!");
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
                System.out.println("\n\n!! WARNING: A powerful presence bars your way !!\n");
                delay(2000);
                
                // --- REFACTOR --- Pass the pre-loaded enemy list to the boss creation method
                Enemy boss = createBossEnemy(worldNumber, allEnemies);
                if (boss == null) {
                    System.err.println("FATAL: Could not create boss for world " + worldNumber);
                    return false;
                }
                System.out.println("The air grows heavy... " + boss.getName() + ", scourge of World " + worldNumber + ", blocks your path!");
                ArrayList<Enemy> bossEncounter = new ArrayList<>();
                bossEncounter.add(boss);

                // --- UPGRADE --- Reset temporary buffs before boss fight
                player1.resetTemporaryBuffs();
                Combat.combatSequenceInit(player1, bossEncounter, scanner);

                if (player1.getHealthPoints() <= 0) {
                    System.out.println("\n> You have been vanquished by " + boss.getName() + "... Game Over.");
                    return false;
                }

                System.out.println("\n> VICTORY! You have defeated " + boss.getName() + "!");
                double currencyReward = boss.getCurrencyDrop() * worldNumber;
                double expReward = boss.getExperienceDrop() * worldNumber;
                player1.addCurrency(currencyReward);
                player1.addExperience(expReward);
                System.out.printf("You gained a massive bonus of %.1f currency and %.1f experience!\n", currencyReward, expReward);
                
                if (worldNumber >= FINAL_WORLD) return true;

                handleTownHub(player1, scanner, worldNumber);
                
                worldNumber++;
                stageNumber = 1;
                
                System.out.println("\n=================================");
                System.out.println("      ENTERING WORLD " + worldNumber);
                System.out.println("=================================");
                delay(1500);
                if (worldNumber == 2) System.out.println("You venture forth, deeper into the corrupted heartlands...");
                else if (worldNumber == 3) System.out.println("The darkness is thickest here. The final challenge must be close.");
                else System.out.println("The journey continues into even more dangerous territory.");
                delay(2000);
                System.out.println("\nThe enemies have grown stronger!");
            }
            
            // --- UPGRADE --- Use new randomized enemy generation method
            ArrayList<Enemy> stageEnemies = generateStageEnemies(allEnemies, stageNumber, worldNumber);
            if (stageEnemies.isEmpty()) {
                System.err.println("Could not generate enemies for this stage. Skipping.");
                stageNumber++;
                continue;
            }
            
            System.out.println("\n----------------- Stage " + worldNumber + "-" + stageNumber + " -----------------");
            // --- NARRATIVE UPGRADE --- Add flavour text for the stage
            printStagePreamble(worldNumber, stageNumber);
            delay(1500);

            System.out.println("Enemies this stage:");
            for(Enemy e : stageEnemies) {
                System.out.println("- " + e.getName() + " (HP: " + e.getHealthPoints() + ", Armour: " + e.getArmour() + ")");
            }

            // --- UPGRADE --- Reset temporary buffs before each stage
            player1.resetTemporaryBuffs();
            handlePreCombatActions(player1, scanner);

            if (player1.isAutoMode()) {
                 AutoBattle.runStage(player1, stageEnemies);
            } else {
                Combat.combatSequenceInit(player1, stageEnemies, scanner);
            }

            if (player1.getHealthPoints() <= 0) {
                System.out.println("\n> " + player1.getName() + " has been defeated... Game Over.");
                return false; 
            }

            for (Enemy defeatedEnemy : stageEnemies) {
                handlePostCombatRewards(player1, defeatedEnemy, stageNumber, worldNumber);
            }

            if (player1.canLevelUp()) {
                handleLevelUp(player1, scanner);
            } else {
                int recovery = (int)(player1.getMaxHealth() * 0.15);
                player1.heal(recovery);
                System.out.printf("You recovered %d HP.\n", recovery);
            }
            System.out.printf("Current HP: %d/%d\n", player1.getHealthPoints(), player1.getMaxHealth());
            
            System.out.printf("\n> Stage %d cleared!\n", stageNumber);
            
            stageNumber++;
        }
    }
    
    // --- NARRATIVE UPGRADE --- New method to add atmospheric text.
    private static void printStagePreamble(int worldNumber, int stageNumber) {
        String[] world1Ambiance = {"You step into a gnarled forest, the air thick with the scent of moss and goblin mischief.", "A crude wooden watchtower, swarming with bandits, looms ahead.", "The path leads to a dripping cave, echoing with the skittering of unseen things."};
        String[] world2Ambiance = {"The ground is scorched and cracked. You feel the heat of a Fire Elemental nearby.", "You enter an ancient crypt. The rattling of bones announces the Skeleton Knights.", "A chilling wind blows through the abandoned keep, a known haunt of Shadow Mages."};
        String[] world3Ambiance = {"A massive, moss-covered Stone Guardian blocks the ancient mountain pass.", "The air crackles with lightning as a Storm Eagle circles overhead.", "You push through the gloom into the throne room of the forgotten king..."};
        
        System.out.print("> ");
        switch (worldNumber) {
            case 1:
                System.out.println(world1Ambiance[random.nextInt(world1Ambiance.length)]);
                break;
            case 2:
                System.out.println(world2Ambiance[random.nextInt(world2Ambiance.length)]);
                break;
            case 3:
                 if (stageNumber < 7) {
                    System.out.println(world3Ambiance[random.nextInt(world3Ambiance.length - 1)]); // Don't use last one for normal stages
                 } else {
                    System.out.println(world3Ambiance[2]); // Final boss preamble
                 }
                break;
            default:
                System.out.println("The path ahead is uncertain, yet you press on.");
                break;
        }
    }

    private static void handleTownHub(Player player, Scanner scanner, int worldNumber) {
        System.out.println("\n--- You arrive at a small, fortified outpost. ---");
        // --- UPGRADE --- Reduced healer cost scaling to be more affordable
        int healerCost = 10 * worldNumber;

        while(true){
            System.out.println("\nWhat would you like to do?");
            System.out.println("1) Visit the Shop");
            System.out.printf("2) Visit the Healer (Fully restore HP/MP for %d currency)\n", healerCost);
            System.out.println("3) Speak to the Sage");
            System.out.println("4) Continue your journey");

            int choice = getSafeIntInput(scanner, "Enter your choice: ", 1, 4);
            if(choice == 1) {
                Shop.openShop(player, scanner);
            } else if (choice == 2) {
                if (player.getCurrency() >= healerCost) {
                    player.addCurrency(-healerCost);
                    player.heal(player.getMaxHealth());
                    player.restoreMp(player.getMaxMp());
                    System.out.println("> The Healer's magic soothes your wounds. You are fully restored!");
                } else {
                    System.out.println("> You don't have enough currency.");
                }
            } else if (choice == 3) {
                 System.out.println("\nThe Sage looks at you with ancient eyes and whispers...");
                 if (worldNumber == 1) {
                    System.out.println("\"...the path ahead is guarded by a crude king, but true evil lurks in the shadows beyond...\"");
                 } else if (worldNumber == 2) {
                    System.out.println("\"...beware the master of the undead... their fall will herald the true dawn... or your own...\"");
                 }
                 
            } else {
                System.out.println("\nYou restock your supplies and head back into the wilds.");
                break;
            }
        }
    }

    // --- REFACTOR --- This method is now more efficient, using the pre-loaded enemy list.
    private static Enemy createBossEnemy(int worldNumber, ArrayList<Enemy> allEnemies) {
        String bossName;
        switch(worldNumber) {
            case 1: bossName = "The Goblin King"; break;
            case 2: bossName = "High Necromancer"; break;
            case 3: bossName = "The Gloom King"; break;
            default: return null;
        }

        for (Enemy enemyTemplate : allEnemies) {
            if (enemyTemplate.getName().equalsIgnoreCase(bossName)) {
                // Use the copy constructor from Enemy.java to create a fresh instance
                return new Enemy(enemyTemplate);
            }
        }

        System.err.println("Error creating boss: Could not find '" + bossName + "' in the loaded enemy list.");
        return null;
    }


    private static Player setupPlayer(Scanner scanner) {
        System.out.print("\nChoose starting difficulty (easy, normal, hard, impossible): ");
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
        
        Player player = loadPlayerFromCSV("playerStats.csv", name, playerClass);
        if (player != null) {
            player.addCurrency(50);
            System.out.println("\nWelcome, " + name + "! Starting as a " + playerClass + ".");
            System.out.println("You start with 50 currency.");
            delay(500);
            System.out.println("\n--- Your Abilities ---");
            for (Ability a : player.getAbilities()) System.out.println("- " + a.getAbilityName());
        }
        return player;
    }

    private static Player loadPlayerFromCSV(String filename, String playerName, String playerClass) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p[1].trim().equalsIgnoreCase(playerClass)) {
                    int maxHP = Integer.parseInt(p[2].trim());
                    int armour = Integer.parseInt(p[3].trim());
                    int init = Integer.parseInt(p[4].trim());
                    int maxMp = Integer.parseInt(p[5].trim());
                    int maxRage = Integer.parseInt(p[6].trim());
                    int maxFocus = Integer.parseInt(p[7].trim());
                    return new Player(playerName, playerClass, maxHP, armour, init, maxMp, maxRage, maxFocus);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Failed to load player data from " + filename + ": " + e.getMessage());
        }
        return null;
    }
    
    private static void handlePreCombatActions(Player player, Scanner scanner) {
        while (true) {
            System.out.print("\nChoose action: [start] combat, [auto] combat, [shop], or [use] item: ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("shop")) { Shop.openShop(player, scanner); continue; } 
            if (input.equals("use")) { useItemFromInventory(player, scanner); continue; }
            if (input.equals("auto")) { player.setAutoMode(true); break; }
            if (input.equals("start")) { player.setAutoMode(false); break; }
            System.out.println("> Invalid entry.");
        }
    }

    // --- UPGRADE --- New randomized enemy generation logic
    private static ArrayList<Enemy> generateStageEnemies(ArrayList<Enemy> allEnemies, int stageNumber, int worldNumber) {
        ArrayList<Enemy> stageEnemies = new ArrayList<>();
        List<Enemy> enemyPool = getEnemyPoolForWorld(allEnemies, worldNumber);

        if (enemyPool.isEmpty()) return stageEnemies; // Return empty list if no enemies for this world

        // Choose a random enemy from the world's pool
        Enemy template = enemyPool.get(random.nextInt(enemyPool.size()));
        
        // Use the same scaling logic as before
        double hpMultiplier = 1.0 + (0.1 * (stageNumber - 1)) + (0.25 * (worldNumber - 1));
        double armourMultiplier = 1.0 + (0.04 * (stageNumber - 1)) + (0.10 * (worldNumber - 1));
        double damageMultiplier = 1.0 + (0.06 * (stageNumber - 1)) + (0.15 * (worldNumber - 1));
        
        int finalHp = (int) Math.round(template.getMaxHealth() * hpMultiplier);
        int finalArmour = (int) Math.round(template.getArmour() * armourMultiplier);
        
        // Clone and scale abilities
        ArrayList<Ability> scaledAbilities = new ArrayList<>();
        ArrayList<Ability> baseAbilities = EnemyAbilityLoader.getAbilitiesForEnemy(template.getName());
        for (Ability baseAbility : baseAbilities) {
            Ability newAbility = AbilityFactory.createAbility(baseAbility.getAbilityName());
            newAbility.applyDamageMultiplier(damageMultiplier);
            scaledAbilities.add(newAbility);
        }
        
        stageEnemies.add(new Enemy(template.getName(), finalHp, finalArmour, template.getInitiative(), template.getCurrencyDrop(), template.getExperienceDrop(), scaledAbilities, template.getAiType()));
        
        // Add a second enemy on harder difficulties or later stages
        Difficulty diff = DifficultyManager.getDifficulty();
        boolean addSecondEnemy = (diff == Difficulty.HARD || diff == Difficulty.IMPOSSIBLE) && stageNumber > 2;
        if (!addSecondEnemy && stageNumber > 4) { // Also add a second enemy on later stages in normal difficulty
            addSecondEnemy = random.nextBoolean();
        }

        if (addSecondEnemy) {
            // Create a distinct second enemy
            Enemy secondTemplate = enemyPool.get(random.nextInt(enemyPool.size()));
            int secondHp = (int) Math.round(secondTemplate.getMaxHealth() * hpMultiplier);
            int secondArmour = (int) Math.round(secondTemplate.getArmour() * armourMultiplier);
            ArrayList<Ability> secondEnemyAbilities = new ArrayList<>();
            ArrayList<Ability> secondBaseAbilities = EnemyAbilityLoader.getAbilitiesForEnemy(secondTemplate.getName());
            for (Ability a : secondBaseAbilities) {
                Ability newAbility = AbilityFactory.createAbility(a.getAbilityName());
                newAbility.applyDamageMultiplier(damageMultiplier);
                secondEnemyAbilities.add(newAbility);
            }
            stageEnemies.add(new Enemy(secondTemplate.getName(), secondHp, secondArmour, secondTemplate.getInitiative(), secondTemplate.getCurrencyDrop(), secondTemplate.getExperienceDrop(), secondEnemyAbilities, secondTemplate.getAiType()));
        }

        return stageEnemies;
    }

    // --- UPGRADE --- Helper method to define enemy pools for each world
    private static List<Enemy> getEnemyPoolForWorld(List<Enemy> allEnemies, int worldNumber) {
        List<String> world1Enemies = List.of("Goblin", "Thief", "Cave Bat", "Orc Brute", "Skeleton Archer", "Poisonous Frog");
        List<String> world2Enemies = List.of("Hobgoblin", "Bandit Leader", "Skeleton Knight", "Shadow Mage", "Fire Elemental", "Vampire Bat");
        List<String> world3Enemies = List.of("Orc Warchief", "Ice Golem", "Stone Guardian", "Shadow Assassin", "Lava Beast", "Storm Eagle");

        List<String> targetNames;
        switch (worldNumber) {
            case 1: targetNames = world1Enemies; break;
            case 2: targetNames = world2Enemies; break;
            case 3: targetNames = world3Enemies; break;
            default: targetNames = world1Enemies; // Failsafe
        }
        
        return allEnemies.stream()
                         .filter(e -> targetNames.contains(e.getName()))
                         .collect(Collectors.toList());
    }


    private static void handlePostCombatRewards(Player player, Enemy enemy, int stageNumber, int worldNumber) {
        double levelDifference = player.getLevelsGained() - (stageNumber + (worldNumber - 1) * 7);
        double penalty = 1.0;
        if (levelDifference > 3) {
            penalty = Math.max(0.1, 1.0 - (levelDifference - 3) * 0.2);
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
                    continue; // Check if another level-up is possible
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