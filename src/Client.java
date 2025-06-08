
import java.util.ArrayList;
import java.util.Scanner;



public class Client {

    public static void delay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Delay was interrupted!");
        }
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
        // Prompt for difficulty
        System.out.print("Choose difficulty (easy, normal, hard, impossible): ");
        String chosenDiff = scanner.nextLine().trim().toLowerCase();
        switch (chosenDiff) {
            case "easy":
                DifficultyManager.setDifficulty(Difficulty.EASY);
                break;
            case "normal":
                DifficultyManager.setDifficulty(Difficulty.NORMAL);
                break;
            case "hard":
                DifficultyManager.setDifficulty(Difficulty.HARD);
                break;
            case "impossible":
                DifficultyManager.setDifficulty(Difficulty.IMPOSSIBLE);
                break;
            default:
                System.out.println("Unrecognized input. Defaulting to Easy.");
                DifficultyManager.setDifficulty(Difficulty.EASY);
        }
        // Prompt for player name and class
        System.out.print("Enter your player name: ");
        String name = scanner.nextLine().trim();
        // Prompt for valid class
        String playerClass;
        while (true) {
            System.out.print("Choose your class (knight, wizard, archer): ");
            playerClass = scanner.nextLine().trim().toLowerCase();
            if (playerClass.equals("knight") ||
                playerClass.equals("wizard") ||
                playerClass.equals("archer")) {
                break;
            } else {
                System.out.println("> Invalid class. Please enter 'knight', 'wizard', or 'archer'.");
            }
        }

        // Initialize class-specific stats and abilities
        int maxHP = 0, startingArmour = 0, initiativeRange = 0, maxMp = 0, attackDistance = 0;
        ArrayList<Ability> classAbilities = new ArrayList<>();
        switch (playerClass) {
            case "wizard":
                maxHP = 30; startingArmour = 1; initiativeRange = 8;
                maxMp = 60; attackDistance = 5;
                classAbilities.add(new Ability("Fireball", 8, 12, 10.0, "Burn", 2));
                classAbilities.add(new Ability("Ice Lance", 4, 8,  8.0, "Slow", 0));    // MP cost 4
                classAbilities.add(new Ability("Arcane Blast", 12, 15, 15.0, "", 3)); // MP cost 12
                classAbilities.add(new Ability("Wand Bonk", 1, 3,  0.0, "", 0));
                break;
            case "archer":
                maxHP = 35; startingArmour = 2; initiativeRange = 12;
                maxMp = 0; attackDistance = 6;
                classAbilities.add(new Ability("Arrow Shot", 5, 10, 0.0, "", 0));
                classAbilities.add(new Ability("Poison Arrow", 3, 7, 0.0, "Poison", 2));
                classAbilities.add(new Ability("Volley", 15, 25, 0.0, "", 3));
                break;
            case "knight":
                maxHP = 40; startingArmour = 3; initiativeRange = 10;
                maxMp = 0; attackDistance = 1;
                classAbilities.add(new Ability("Slash", 6, 10, 0.0, "", 0));
                classAbilities.add(new Ability("Shield Bash", 4, 8, 0.0, "Stun", 2));
                classAbilities.add(new Ability("Power Strike", 15, 20, 0.0, "", 3));
                break;
        }
        // Construct player with manual config
        Player player1 = new Player(
            maxHP, startingArmour, initiativeRange, maxMp, attackDistance,
            name, playerClass, 0.0, 0.0,
            classAbilities.get(0), classAbilities.get(1), classAbilities.get(2)
        );
        // Add any extra abilities beyond the first three
        for (int i = 3; i < classAbilities.size(); i++) {
            player1.getAbilities().add(classAbilities.get(i));
        }
        System.out.println("Welcome, " + name + "! Starting as a " + playerClass +
                           " with HP=" + maxHP + ", Armour=" + startingArmour +
                           ", MP=" + maxMp + ", InitiativeRange=" + initiativeRange +
                           ", AttackDistance=" + attackDistance);
        delay(500);
        System.out.println("\nLoaded Abilities:");
        for (Ability a : player1.getAbilities()) {
            System.out.println("- " + a.getAbilityName());
        }
        // Initialize world and stage counters and load all enemies from CSV
        int worldNumber = 1;
        int stageNumber = 1;
        ArrayList<Enemy> allEnemies = Enemy.generateEnemies();

        mainLoop:
        while (true) {

            // World and stage rollover logic
            if (stageNumber > 7) {
                worldNumber++;
                stageNumber = 1;
                System.out.println("\n=== World " + worldNumber + " Begins! ===");
                // Bump difficulty for world 2+ if desired
                Difficulty newDiff = (worldNumber == 2) ? Difficulty.NORMAL : Difficulty.HARD;
                DifficultyManager.setDifficulty(newDiff);
            } else if (stageNumber > allEnemies.size()) {
                System.out.println("No more stages available. You win!");
                System.out.println("Final Currency: " + player1.currency +
                                   ", Final Experience: " + player1.experience);
                System.exit(0);
            }
            Enemy template1 = allEnemies.get(stageNumber - 1);
            // Get base stats from template
            int baseHp = template1.getHealthPoints();
            // Stage HP multiplier: 1.0 + 0.1*(stageNumber-1)
            double stageHpMult = 1.0 + 0.1 * (stageNumber - 1);
            // World bonus on HP
            double worldHpMult = (worldNumber == 2) ? 1.2 : (worldNumber >= 3 ? 1.3 : 1.0);
            int finalHp = (int) Math.round(baseHp * stageHpMult * worldHpMult);
            // Reward multipliers (scale currency and XP like HP)
            double stageRewardMult = 1.0 + 0.05 * (stageNumber - 1);
            double worldRewardMult = worldHpMult;  // reuse world HP bonus for rewards

            Enemy enemy1 = new Enemy(
                template1.getName(),
                finalHp,
                template1.getArmour(),
                template1.getInitiative(),
                template1.getAttackDistance(),
                template1.currencyDrop,
                template1.experienceDrop
            );
            // If HARD or IMPOSSIBLE and a second template exists, add two enemies
            ArrayList<Enemy> stageEnemies = new ArrayList<>();
            stageEnemies.add(enemy1);
            Difficulty diff = DifficultyManager.getDifficulty();
            if ((diff == Difficulty.HARD || diff == Difficulty.IMPOSSIBLE) &&
                stageNumber < allEnemies.size()) {
                Enemy template2 = allEnemies.get(stageNumber); // next in list
                int baseHp2 = template2.getHealthPoints();
                double stageHpMult2 = 1.0 + 0.1 * (stageNumber - 1);
                double worldHpMult2 = worldHpMult;
                int finalHp2 = (int) Math.round(baseHp2 * stageHpMult2 * worldHpMult2);

                Enemy enemy2 = new Enemy(
                    template2.getName(),
                    finalHp2,
                    template2.getArmour(),
                    template2.getInitiative(),
                    template2.getAttackDistance(),
                    template2.currencyDrop,
                    template2.experienceDrop
                );
                stageEnemies.add(enemy2);
            }

            // Track if auto was used for this stage
            boolean usedAuto = false;
            // Prompt to start, open shop, or use inventory
            while (true) {
                System.out.print("\nType 'start', 'auto', 'shop', or 'use': ");
                String input = scanner.nextLine().trim().toLowerCase();
                if (input.equals("shop")) {
                    Shop.openShop(player1);
                } else if (input.equals("use")) {
                    // Display and use inventory
                    java.util.List<Shop.ShopItem> inv = player1.getInventory();
                    if (inv.isEmpty()) {
                        System.out.println("Inventory is empty.");
                        continue;
                    }
                    System.out.println("Inventory:");
                    // Collapse duplicates for display
                    java.util.Map<String,Integer> counts = new java.util.LinkedHashMap<>();
                    for (Shop.ShopItem it : inv) counts.merge(it.name, 1, Integer::sum);
                    int idx = 1;
                    java.util.List<String> names = new java.util.ArrayList<>();
                    for (var entry : counts.entrySet()) {
                        System.out.printf("%d) %s x%d%n", idx++, entry.getKey(), entry.getValue());
                        names.add(entry.getKey());
                    }
                    int num;
                    while (true) {
                        System.out.print("Enter item number to use (0 to cancel): ");
                        String line = scanner.nextLine().trim();
                        try {
                            num = Integer.parseInt(line);
                        } catch (NumberFormatException ex) {
                            System.out.println("Invalid input.");
                            continue;
                        }
                        if (num < 0 || num > names.size()) {
                            System.out.println("Invalid input.");
                            continue;
                        }
                        break;
                    }
                    if (num == 0) {
                        // cancel, return to main prompt
                        continue;
                    }
                    // Find the first instance of this item name in inventory
                    String chosenName = names.get(num-1);
                    int invIdx = -1;
                    for (int i = 0; i < inv.size(); i++) {
                        if (inv.get(i).name.equals(chosenName)) {
                            invIdx = i;
                            break;
                        }
                    }
                    if (invIdx != -1 && player1.useInventoryItem(invIdx)) {
                        System.out.println("Item used.");
                    } else {
                        System.out.println("Invalid item.");
                    }
                } else if (input.equals("auto")) {
                    // backup enemy list for rewards after auto-battle
                    ArrayList<Enemy> autoTargets = new ArrayList<>(stageEnemies);
                    // Run a fully automated battle for this stage
                    AutoBattle.runStage(player1, stageEnemies, stageNumber);
                    usedAuto = true;
                    // After auto, break out to reward/progression
                    // store backup for rewards
                    stageEnemies = autoTargets;
                    break;
                } else if (input.equals("start")) {
                    System.out.println("Let's begin.");
                    // Show player full stats before the stage
                    System.out.printf("Player Stats – HP: %d/%d", player1.getHealthPoints(), player1.getMaxHealth());
                    if (player1.getMaxMp() > 0) {
                        System.out.printf(" | MP: %d/%d", player1.getMp(), player1.getMaxMp());
                    }
                    System.out.printf(" | Currency: %.1f | Experience: %.1f%n",
                                      player1.getCurrency(), player1.getExperience());
                    break;
                } else {
                    System.out.println("> Invalid entry. Please type 'start', 'auto', 'shop', or 'use'.");
                }
            }

            // If auto was used, skip manual combat and apply rewards directly
            if (usedAuto) {
                if (player1.getHealthPoints() <= 0) {
                    System.out.println("Auto: " + player1.getName() + " was defeated...");
                    while (true) {
                        System.out.print("Do you want to restart the game? (yes/no): ");
                        String reply = scanner.nextLine().trim().toLowerCase();
                        if (reply.equals("yes")) {
                            player1.resetHealth();
                            for (Ability ab : player1.getAbilities()) { ab.resetCooldown(); }
                            player1.currency = 0.0;
                            player1.experience = 0.0;
                            stageNumber = 1;
                            continue mainLoop;
                        } else if (reply.equals("no")) {
                            System.out.println("Thanks for playing!");
                            System.exit(0);
                        } else {
                            System.out.println("> Invalid input. Please type 'yes' or 'no'.");
                        }
                    }
                } else {
                    // reward each enemy as if auto-battle defeated them
                    for (Enemy currentEnemy : stageEnemies) {
                        // only reward if player still alive
                        if (player1.getHealthPoints() <= 0) break;
                        double gainedCurr = currentEnemy.currencyDrop * stageRewardMult * worldRewardMult;
                        double gainedExp  = currentEnemy.experienceDrop * stageRewardMult * worldRewardMult;
                        player1.currency += gainedCurr;
                        player1.experience += gainedExp;
                        System.out.println("> " + currentEnemy.getName() + " auto-defeated!");
                        System.out.printf("You gained %.1f currency and %.1f experience!%n", gainedCurr, gainedExp);
                        player1.levelUp();
                    }
                    // stage cleared
                    System.out.printf("> Stage %d cleared!%n", stageNumber);
                    System.out.printf("Current Currency: %.1f | Current Experience: %.1f%n",
                                      player1.getCurrency(), player1.getExperience());
                    for (Ability ab : player1.getAbilities()) ab.resetCooldown();
                    // Prompt to continue or quit, with special prompt for world transition
                    String cont;
                    while (true) {
                        if (stageNumber == 7) {
                            System.out.print("All stages in World " + worldNumber +
                                " cleared! Do you want to go to World " + (worldNumber + 1) +
                                "? (yes/no): ");
                        } else {
                            System.out.print("Do you want to continue to stage " +
                                (stageNumber + 1) + "? (yes/no): ");
                        }
                        cont = scanner.nextLine().trim().toLowerCase();
                        if (cont.equals("yes")) {
                            if (stageNumber == 7) {
                                worldNumber++;
                                stageNumber = 1;
                                System.out.println("\n=== World " + worldNumber + " Begins! ===");
                                Difficulty newDiff = (worldNumber == 2)
                                    ? Difficulty.NORMAL
                                    : Difficulty.HARD;
                                DifficultyManager.setDifficulty(newDiff);
                            } else {
                                stageNumber++;
                            }
                            break;
                        } else if (cont.equals("no")) {
                            System.out.printf("Final Currency: %.1f | Final Experience: %.1f%n",
                                              player1.getCurrency(), player1.getExperience());
                            System.out.println("Thanks for playing!");
                            System.exit(0);
                        } else {
                            System.out.println("> Invalid input. Please type 'yes' or 'no'.");
                        }
                    }
                    continue mainLoop;
                }
            } else {
                // Initialize and run stage against one or two enemies
                Stage stage1 = new Stage(stageNumber, player1, stageEnemies);
                stage1.startStage();
                // Show player full stats before the stage (again for manual)
                System.out.printf("Player Stats – HP: %d/%d", player1.getHealthPoints(), player1.getMaxHealth());
                if (player1.getMaxMp() > 0) {
                    System.out.printf(" | MP: %d/%d", player1.getMp(), player1.getMaxMp());
                }
                System.out.printf(" | Currency: %.1f | Experience: %.1f%n",
                                  player1.getCurrency(), player1.getExperience());
                stage1.printStageStatus();
                delay(500);

                // Fight each enemy in order
                for (int idx = 0; idx < stageEnemies.size(); idx++) {
                    Enemy currentEnemy = stageEnemies.get(idx);
                    System.out.println("\n--- Combat vs " + currentEnemy.getName() + " ---");
                    Combat.combatSequenceInit(player1, stageEnemies, player1.getAbilities());
                    stage1.printStageStatus();
                    if (player1.getHealthPoints() > 0) {
                        System.out.println("> " + currentEnemy.getName() + " defeated!");
                        double gainedCurr = currentEnemy.currencyDrop * stageRewardMult * worldRewardMult;
                        double gainedExp  = currentEnemy.experienceDrop * stageRewardMult * worldRewardMult;
                        player1.currency += gainedCurr;
                        player1.experience += gainedExp;
                        System.out.printf("You gained %.1f currency and %.1f experience!%n", gainedCurr, gainedExp);
                        player1.levelUp();
                    } else {
                        break; // player died, exit loop
                    }
                }
            }

            // Check outcome: either all enemies or player is dead
            if (player1.getHealthPoints() > 0) {
                // Stage cleared
                System.out.printf("> Stage %d cleared!%n", stageNumber);
                System.out.printf("Current Currency: %.1f | Current Experience: %.1f%n",
                                  player1.getCurrency(), player1.getExperience());
                // Keep current HP (do not reset); only reset cooldowns
                for (Ability ab : player1.getAbilities()) {
                    ab.resetCooldown();
                }
                // Prompt to continue or quit, with special prompt for world transition
                String cont;
                while (true) {
                    if (stageNumber == 7) {
                        System.out.print("All stages in World " + worldNumber +
                            " cleared! Do you want to go to World " + (worldNumber + 1) +
                            "? (yes/no): ");
                    } else {
                        System.out.print("Do you want to continue to stage " +
                            (stageNumber + 1) + "? (yes/no): ");
                    }
                    cont = scanner.nextLine().trim().toLowerCase();
                    if (cont.equals("yes")) {
                        if (stageNumber == 7) {
                            worldNumber++;
                            stageNumber = 1;
                            System.out.println("\n=== World " + worldNumber + " Begins! ===");
                            Difficulty newDiff = (worldNumber == 2)
                                ? Difficulty.NORMAL
                                : Difficulty.HARD;
                            DifficultyManager.setDifficulty(newDiff);
                        } else {
                            stageNumber++;
                        }
                        break;
                    } else if (cont.equals("no")) {
                        System.out.printf("Final Currency: %.1f | Final Experience: %.1f%n",
                                          player1.getCurrency(), player1.getExperience());
                        System.out.println("Thanks for playing!");
                        System.exit(0);
                    } else {
                        System.out.println("> Invalid input. Please type 'yes' or 'no'.");
                    }
                }
                continue mainLoop;
            } else {
                // Player died mid-stage
                System.out.println("> " + player1.getName() + " has been defeated...");
                while (true) {
                    System.out.print("Do you want to restart the game? (yes/no): ");
                    String reply = scanner.nextLine().trim().toLowerCase();
                    if (reply.equals("yes")) {
                        // Reset player stats before restarting
                        player1.resetHealth();
                        for (Ability ab : player1.getAbilities()) {
                            ab.resetCooldown();
                        }
                        player1.currency = 0.0;
                        player1.experience = 0.0;
                        stageNumber = 1;
                        continue mainLoop;
                    } else if (reply.equals("no")) {
                        System.out.println("Thanks for playing!");
                        System.exit(0);
                    } else {
                        System.out.println("> Invalid input. Please type 'yes' or 'no'.");
                    }
                }
            }
        }
    }
}
}
