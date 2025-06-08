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

    private static void handleLevelUp(Player player, Scanner scanner) {
        while (player.canLevelUp()) {
            player.performLevelUp(); // This now handles the stat increases and full heal
            
            List<Ability> newAbilities = player.getNewLevelUpAbilities();
            if (player.getLevelsGained() % 3 == 0 && !newAbilities.isEmpty()) {
                System.out.println("Choose one new ability to learn:");
                for (int i = 0; i < newAbilities.size(); i++) {
                    Ability a = newAbilities.get(i);
                    System.out.printf("%d) %s (Damage %d–%d, CD:%d)%n",
                                      i + 1, a.getAbilityName(), a.getMinDamage(), a.getMaxDamage(), a.getCooldown());
                }
                int choice = -1;
                while (choice < 1 || choice > newAbilities.size()) {
                    System.out.print("Enter choice [1-" + newAbilities.size() + "]: ");
                    try {
                        choice = Integer.parseInt(scanner.nextLine().trim());
                         if (choice < 1 || choice > newAbilities.size()) {
                            System.out.println("Invalid input.");
                        }
                    } catch (NumberFormatException ex) {
                        System.out.println("Invalid input.");
                    }
                }
                Ability learned = newAbilities.get(choice - 1);
                player.getAbilities().add(learned);
                System.out.println("Learned new ability: " + learned.getAbilityName() + "!");
            } else {
                System.out.println("Choose one ability to improve:");
                ArrayList<Ability> currentAbilities = player.getAbilities();
                for (int i = 0; i < currentAbilities.size(); i++) {
                    Ability a = currentAbilities.get(i);
                    System.out.printf("%d) %s (Current Damage %d–%d)%n",
                                      i + 1, a.getAbilityName(), a.getMinDamage(), a.getMaxDamage());
                }
                int choice = -1;
                 while (choice < 1 || choice > currentAbilities.size()) {
                    System.out.print("Enter choice [1-" + currentAbilities.size() + "]: ");
                    try {
                        choice = Integer.parseInt(scanner.nextLine().trim());
                        if (choice < 1 || choice > currentAbilities.size()) {
                            System.out.println("Invalid input.");
                        }
                    } catch (NumberFormatException ex) {
                         System.out.println("Invalid input.");
                    }
                }
                Ability toBuff = currentAbilities.get(choice - 1);
                toBuff.buffDamage(4);
                System.out.println("Upgraded " + toBuff.getAbilityName() + " damage by 4!");
            }
        }
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean playAgain = true;
            while(playAgain) {
                Player player1 = setupPlayer(scanner);
                
                boolean playerWonGame = gameLoop(player1, scanner);

                if (playerWonGame) {
                     System.out.println("Congratulations on completing the game!");
                }
                
                System.out.print("Do you want to restart the game? (yes/no): ");
                String reply = scanner.nextLine().trim().toLowerCase();
                // --- MODIFIED: More forgiving restart input ---
                if (!reply.startsWith("y")) {
                    playAgain = false;
                }
            }
            System.out.println("Thanks for playing!");

        } catch (Exception e) {
            System.err.println("An unexpected error occurred. The game will now exit.");
            e.printStackTrace();
        }
    }

    private static Player setupPlayer(Scanner scanner) {
        System.out.print("Choose difficulty (easy, normal, hard, impossible): ");
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
        switch (playerClass) {
            case "wizard":
                maxHP = 30; startingArmour = 1; initiativeRange = 8; maxMp = 60; attackDistance = 5;
                classAbilities.add(new Ability("Fireball", 8, 12, 10.0, "Burn", 2));
                classAbilities.add(new Ability("Ice Lance", 4, 8, 8.0, "Slow", 0));
                classAbilities.add(new Ability("Arcane Blast", 12, 15, 15.0, "", 3));
                classAbilities.add(new Ability("Wand Bonk", 1, 3, 0.0, "", 0));
                break;
            case "archer":
                maxHP = 35; startingArmour = 2; initiativeRange = 12; maxMp = 0; attackDistance = 6;
                classAbilities.add(new Ability("Arrow Shot", 5, 10, 0.0, "", 0));
                classAbilities.add(new Ability("Poison Arrow", 3, 7, 0.0, "Poison", 2));
                classAbilities.add(new Ability("Volley", 15, 25, 0.0, "", 3));
                break;
            case "knight":
                maxHP = 40; startingArmour = 3; initiativeRange = 10; maxMp = 0; attackDistance = 1;
                classAbilities.add(new Ability("Slash", 6, 10, 0.0, "", 0));
                classAbilities.add(new Ability("Shield Bash", 4, 8, 0.0, "Stun", 2));
                classAbilities.add(new Ability("Power Strike", 15, 20, 0.0, "", 3));
                break;
        }

        Player player = new Player(maxHP, startingArmour, initiativeRange, maxMp, attackDistance, name, playerClass, 0.0, 0.0, classAbilities.get(0), classAbilities.get(1), classAbilities.get(2));
        for (int i = 3; i < classAbilities.size(); i++) {
            player.getAbilities().add(classAbilities.get(i));
        }
        System.out.println("Welcome, " + name + "! Starting as a " + playerClass + " with HP=" + maxHP + ", Armour=" + startingArmour + ", MP=" + maxMp + ", InitiativeRange=" + initiativeRange + ", AttackDistance=" + attackDistance);
        delay(500);
        System.out.println("\nLoaded Abilities:");
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
            if (stageNumber > 7) {
                worldNumber++;
                stageNumber = 1;
                System.out.println("\n=== World " + worldNumber + " Begins! ===");
                if (worldNumber == 2) DifficultyManager.setDifficulty(Difficulty.NORMAL);
                else if (worldNumber >= 3) DifficultyManager.setDifficulty(Difficulty.HARD);
            }
            if (stageNumber > allEnemies.size()) {
                return true; 
            }
            
            Enemy template1 = allEnemies.get(stageNumber - 1);
            double stageHpMult = 1.0 + 0.1 * (stageNumber - 1);
            double worldHpMult = 1.0 + (0.2 * (worldNumber - 1));
            int finalHp = (int) Math.round(template1.getHealthPoints() * stageHpMult * worldHpMult);
            
            ArrayList<Enemy> stageEnemies = new ArrayList<>();
            stageEnemies.add(new Enemy(template1.getName(), finalHp, template1.getArmour(), template1.getInitiative(), template1.getAttackDistance(), template1.currencyDrop, template1.experienceDrop, template1.getAbilities()));
            
            Difficulty diff = DifficultyManager.getDifficulty();
            if (diff == Difficulty.HARD || diff == Difficulty.IMPOSSIBLE) {
                stageEnemies.add(new Enemy(template1.getName(), finalHp, template1.getArmour(), template1.getInitiative(), template1.getAttackDistance(), template1.currencyDrop, template1.experienceDrop, template1.getAbilities()));
            }

            System.out.println("\n--- Stage " + stageNumber + " ---");
            System.out.println("Enemies this stage:");
            for(Enemy e : stageEnemies) {
                System.out.println("- " + e.getName() + " (HP: " + e.getHealthPoints() + ")");
            }

            for (int i = 0; i < stageEnemies.size(); i++) {
                Enemy currentEnemy = stageEnemies.get(i);
                System.out.println("\n--- Encounter " + (i + 1) + "/" + stageEnemies.size() + " ---");
                
                String combatMode = "";
                while (true) {
                    System.out.print("\nType 'start', 'auto', 'shop', or 'use': ");
                    String input = scanner.nextLine().trim().toLowerCase();
                    if (input.equals("shop")) {
                        Shop.openShop(player1);
                    } else if (input.equals("use")) {
                        java.util.List<Shop.ShopItem> inv = player1.getInventory();
                        if (inv.isEmpty()) {
                            System.out.println("Inventory is empty.");
                            continue;
                        }
                        System.out.println("Inventory:");
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
                                if (num >= 0 && num <= names.size()) break;
                                else System.out.println("Invalid number.");
                            } catch (NumberFormatException ex) { 
                                 System.out.println("Invalid input.");
                            }
                        }
                        if (num == 0) continue;
                        String chosenName = names.get(num-1);
                        int invIdx = -1;
                        for (int k = 0; k < inv.size(); k++) { if (inv.get(k).name.equals(chosenName)) { invIdx = k; break; } }
                        if (invIdx != -1) player1.useInventoryItem(invIdx);

                    } else if (input.equals("auto") || input.equals("start")) {
                        combatMode = input;
                        break;
                    } else {
                        System.out.println("> Invalid entry. Please type 'start', 'auto', 'shop', or 'use'.");
                    }
                }
                
                if (combatMode.equals("auto")) {
                    AutoBattle.runStage(player1, new ArrayList<>(List.of(currentEnemy)), stageNumber);
                } else {
                    Combat.combatSequenceInit(player1, new ArrayList<>(List.of(currentEnemy)), player1.getAbilities());
                }

                if (player1.getHealthPoints() <= 0) {
                    System.out.println("> " + player1.getName() + " has been defeated...");
                    return false; 
                }

                System.out.printf("> %s defeated!%n", currentEnemy.getName());
                double stageRewardMult = 1.0 + 0.05 * (stageNumber - 1);
                double worldRewardMult = 1.0 + (0.1 * (worldNumber - 1));
                double gainedCurr = currentEnemy.currencyDrop * stageRewardMult * worldRewardMult;
                double gainedExp = currentEnemy.experienceDrop * stageRewardMult * worldRewardMult;
                
                player1.currency += gainedCurr;
                player1.experience += gainedExp;
                System.out.printf("You gained %.1f currency and %.1f experience!%n", gainedCurr, gainedExp);

                boolean didLevelUp = player1.canLevelUp();
                handleLevelUp(player1, scanner);
                
                if (!didLevelUp) {
                    int recovery = (int)(player1.getMaxHealth() * 0.15);
                    player1.heal(recovery);
                    System.out.printf("You recovered %d HP.%n", recovery);
                }
                 System.out.printf("Current HP: %d/%d%n", player1.getHealthPoints(), player1.getMaxHealth());
            }

            System.out.printf("> Stage %d cleared!%n", stageNumber);
            for (Ability ab : player1.getAbilities()) ab.resetCooldown();
            stageNumber++;
        }
    }
}
