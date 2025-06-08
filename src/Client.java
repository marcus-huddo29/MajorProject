import java.util.ArrayList;
import java.util.List;
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
                    break; // Valid input, exit loop
                } else {
                    System.out.println("> Invalid choice. Please enter a number between " + min + " and " + max + ".");
                }
            } catch (NumberFormatException e) {
                System.out.println("> Invalid input. Please enter a number.");
            }
        }
        return choice;
    }

    private static void handleLevelUp(Player player, Scanner scanner) {
        while (player.canLevelUp()) {
            player.performLevelUp(); // This now handles the stat increases and full heal
            
            // --- CHANGE --- Reworked level up choices to be more engaging.
            // Every level, player can choose to upgrade an existing ability.
            // Every 3 levels, they can also choose to learn a new one if available.
            List<Ability> newAbilities = player.getNewLevelUpAbilities();
            
            System.out.println("You can upgrade an ability!");
            
            // Option 1: Learn a new ability (if eligible)
            if (player.getLevelsGained() > 0 && player.getLevelsGained() % 3 == 0 && !newAbilities.isEmpty()) {
                System.out.println("You can also choose to learn a powerful new ability.");
                System.out.println("\n--- Choose Your Level-Up Bonus ---");
                System.out.println("1) Learn a new ability");
                System.out.println("2) Upgrade an existing ability");
                int choice = getSafeIntInput(scanner, "Enter your choice [1-2]: ", 1, 2);

                if (choice == 1) {
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
                    continue; // Skip the upgrade part
                }
            }

            // Option 2: Upgrade an existing ability (default choice)
            System.out.println("\nChoose one ability to improve:");
            ArrayList<Ability> currentAbilities = player.getAbilities();
            for (int i = 0; i < currentAbilities.size(); i++) {
                Ability a = currentAbilities.get(i);
                System.out.printf("%d) %s (Current Damage %d–%d)\n",
                                  i + 1, a.getAbilityName(), a.getMinDamage(), a.getMaxDamage());
            }
            int upgradeChoice = getSafeIntInput(scanner, "Enter choice [1-" + currentAbilities.size() + "]: ", 1, currentAbilities.size());
            Ability toBuff = currentAbilities.get(upgradeChoice - 1);
            toBuff.buffDamage(4); // Buff amount can be adjusted for balance
            System.out.println("Upgraded " + toBuff.getAbilityName() + " damage by 4!");
        }
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean playAgain = true;
            while(playAgain) {
                Player player1 = setupPlayer(scanner);
                
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

    private static Player setupPlayer(Scanner scanner) {
        // --- CHANGE --- Difficulty is now set at the start and can be changed later.
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

        int maxHP = 0, startingArmour = 0, initiativeRange = 0, maxMp = 0, attackDistance = 0;
        ArrayList<Ability> classAbilities = new ArrayList<>();
        // --- CHANGE --- Rebalanced starting abilities for all classes.
        switch (playerClass) {
            case "wizard":
                maxHP = 30; startingArmour = 1; initiativeRange = 8; maxMp = 60; attackDistance = 5;
                classAbilities.add(new Ability("Fireball", 8, 12, 10.0, "Burn", 2));
                classAbilities.add(new Ability("Ice Lance", 4, 8, 8.0, "Slow", 1));
                classAbilities.add(new Ability("Arcane Blast", 12, 15, 15.0, "None", 3));
                classAbilities.add(new Ability("Mana Dart", 2, 4, 0.0, "None", 0)); // Replaced Wand Bonk
                break;
            case "archer":
                maxHP = 35; startingArmour = 2; initiativeRange = 12; maxMp = 0; attackDistance = 6;
                classAbilities.add(new Ability("Arrow Shot", 5, 10, 0.0, "None", 0));
                classAbilities.add(new Ability("Poison Arrow", 3, 7, 0.0, "Poison", 2));
                classAbilities.add(new Ability("Volley", 15, 25, 0.0, "None", 4)); // Slightly increased CD for balance
                break;
            case "knight":
                maxHP = 40; startingArmour = 3; initiativeRange = 10; maxMp = 0; attackDistance = 1;
                classAbilities.add(new Ability("Slash", 6, 10, 0.0, "None", 0));
                classAbilities.add(new Ability("Shield Bash", 4, 8, 0.0, "Stun", 2));
                classAbilities.add(new Ability("Power Strike", 15, 20, 0.0, "None", 3));
                break;
        }

        Player player = new Player(maxHP, startingArmour, initiativeRange, maxMp, attackDistance, name, playerClass, 0.0, 0.0, new ArrayList<>(classAbilities));
        
        System.out.println("\nWelcome, " + name + "! Starting as a " + playerClass + " with HP=" + maxHP + ", Armour=" + startingArmour + ", MP=" + maxMp);
        delay(500);
        System.out.println("\n--- Your Abilities ---");
        for (Ability a : player.getAbilities()) {
            System.out.println("- " + a.getAbilityName());
        }
        return player;
    }

    private static boolean gameLoop(Player player1, Scanner scanner) {
        int worldNumber = 1;
        int stageNumber = 1;
        ArrayList<Enemy> allEnemies = Enemy.generateEnemies();

        while (true) {
            // --- CHANGE --- Difficulty is no longer forced, player can choose.
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
            if (stageNumber > allEnemies.size()) {
                return true; 
            }
            
            Enemy template1 = allEnemies.get(stageNumber - 1);
            // --- CHANGE --- Switched to additive scaling to prevent HP bloat in late game.
            double hpMultiplier = 1.0 + (0.1 * (stageNumber - 1)) + (0.2 * (worldNumber - 1));
            int finalHp = (int) Math.round(template1.getMaxHealth() * hpMultiplier);
            
            ArrayList<Enemy> stageEnemies = new ArrayList<>();
            stageEnemies.add(new Enemy(template1.getName(), finalHp, template1.getArmour(), template1.getInitiative(), template1.getAttackDistance(), template1.getCurrencyDrop(), template1.getExperienceDrop(), template1.getAbilities()));
            
            Difficulty diff = DifficultyManager.getDifficulty();
            if (diff == Difficulty.HARD || diff == Difficulty.IMPOSSIBLE) {
                // Add a second enemy on harder difficulties
                stageEnemies.add(new Enemy(template1.getName(), finalHp, template1.getArmour(), template1.getInitiative(), template1.getAttackDistance(), template1.getCurrencyDrop(), template1.getExperienceDrop(), template1.getAbilities()));
            }

            System.out.println("\n----------------- Stage " + stageNumber + " -----------------");
            System.out.println("Enemies this stage:");
            for(Enemy e : stageEnemies) {
                System.out.println("- " + e.getName() + " (HP: " + e.getHealthPoints() + ")");
            }

            for (int i = 0; i < stageEnemies.size(); i++) {
                Enemy currentEnemy = stageEnemies.get(i);
                System.out.println("\n--- Encounter " + (i + 1) + "/" + stageEnemies.size() + ": " + currentEnemy.getName() + " ---");
                
                String combatMode = "";
                while (true) {
                    System.out.print("\nChoose action: [start] manual combat, [auto] combat, [shop], or [use] item: ");
                    String input = scanner.nextLine().trim().toLowerCase();
                    if (input.equals("shop")) {
                        Shop.openShop(player1, scanner);
                    } else if (input.equals("use")) {
                        java.util.List<Shop.ShopItem> inv = player1.getInventory();
                        if (inv.isEmpty()) {
                            System.out.println("> Inventory is empty.");
                            continue;
                        }
                        System.out.println("\n--- Your Inventory ---");
                        java.util.Map<String,Integer> counts = new java.util.LinkedHashMap<>();
                        for (Shop.ShopItem it : inv) counts.merge(it.name, 1, Integer::sum);
                        
                        int idx = 1;
                        java.util.List<String> names = new java.util.ArrayList<>();
                        for (var entry : counts.entrySet()) {
                            System.out.printf("%d) %s x%d%n", idx++, entry.getKey(), entry.getValue());
                            names.add(entry.getKey());
                        }
                        
                        int itemNum = getSafeIntInput(scanner, "Enter item number to use (0 to cancel): ", 0, names.size());

                        if (itemNum == 0) continue;
                        
                        String chosenName = names.get(itemNum-1);
                        int invIdx = -1;
                        for (int k = 0; k < inv.size(); k++) { if (inv.get(k).name.equals(chosenName)) { invIdx = k; break; } }
                        if (invIdx != -1) player1.useInventoryItem(invIdx);

                    } else if (input.equals("auto") || input.equals("start")) {
                        combatMode = input;
                        break;
                    } else {
                        System.out.println("> Invalid entry. Please choose 'start', 'auto', 'shop', or 'use'.");
                    }
                }
                
                if (combatMode.equals("auto")) {
                    AutoBattle.runStage(player1, new ArrayList<>(List.of(currentEnemy)), stageNumber);
                } else {
                    Combat.combatSequenceInit(player1, new ArrayList<>(List.of(currentEnemy)), player1.getAbilities(), scanner);
                }

                if (player1.getHealthPoints() <= 0) {
                    System.out.println("\n> " + player1.getName() + " has been defeated...");
                    System.out.println("> Game Over.");
                    return false; 
                }

                System.out.printf("\n> %s defeated!%n", currentEnemy.getName());
                double rewardMultiplier = 1.0 + (0.05 * (stageNumber - 1)) + (0.1 * (worldNumber - 1));
                double gainedCurr = currentEnemy.getCurrencyDrop() * rewardMultiplier;
                double gainedExp = currentEnemy.getExperienceDrop() * rewardMultiplier;
                
                player1.addCurrency(gainedCurr);
                player1.addExperience(gainedExp);
                System.out.printf("You gained %.1f currency and %.1f experience!\n", gainedCurr, gainedExp);

                if (player1.canLevelUp()) {
                    handleLevelUp(player1, scanner);
                } else {
                    // Heal for a small amount if no level up
                    int recovery = (int)(player1.getMaxHealth() * 0.15);
                    player1.heal(recovery);
                    System.out.printf("You recovered %d HP.\n", recovery);
                }
                 System.out.printf("Current HP: %d/%d\n", player1.getHealthPoints(), player1.getMaxHealth());
            }

            System.out.printf("\n> Stage %d cleared! Your abilities have been refreshed.\n", stageNumber);
            player1.resetAttackBuffThisStage(); // Reset shop buffs
            for (Ability ab : player1.getAbilities()) ab.resetCooldown();
            stageNumber++;
        }
    }
}
