
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Combat {

    private static final Scanner scanner = new Scanner(System.in);

    public static void delay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Delay was interrupted!");
        }
    }

    public static void printHealthBarStatus(int currentHealth, int maxHealth) {
        double fraction = (double) currentHealth / maxHealth;
        int barLength = 20;
        int filledBars = (int) (fraction * barLength);
        if (filledBars < 0) filledBars = 0;
        if (filledBars > barLength) filledBars = barLength;
        int emptyBars = barLength - filledBars;

        StringBuilder bar = new StringBuilder();
        bar.append("HP: ");
        for (int i = 0; i < filledBars; i++) bar.append("█");
        for (int i = 0; i < emptyBars; i++) bar.append("-");
        System.out.println(bar.toString());
    }

    public static void printManaBarStatus(int currentMp, int maxMp) {
        double fraction = (double) currentMp / maxMp;
        int barLength = 20;
        int filledBars = (int) (fraction * barLength);
        if (filledBars < 0) filledBars = 0;
        if (filledBars > barLength) filledBars = barLength;
        int emptyBars = barLength - filledBars;

        StringBuilder bar = new StringBuilder();
        bar.append("MP: ");
        for (int i = 0; i < filledBars; i++) bar.append("█");
        for (int i = 0; i < emptyBars; i++) bar.append("-");
        System.out.println(bar.toString());
    }

    public static int playerCombatSequence(ArrayList<Ability> abilities, Enemy enemy, Player player1) {
        while (true) {
            System.out.println("======================================");
            printHealthBarStatus(player1.getHealthPoints(), player1.getMaxHealth());
            System.out.println("HP: " + player1.getHealthPoints() + "/" + player1.getMaxHealth());
            if (player1.getMaxMp() > 0) {
                printManaBarStatus(player1.getMp(), player1.getMaxMp());
                System.out.println("MP: " + player1.getMp() + "/" + player1.getMaxMp());
            }
            System.out.println();
            System.out.println("Choose your action:");
            for (int i = 0; i < abilities.size(); i++) {
                Ability a = abilities.get(i);
                String line;
                if (a.getMinDamage() == 0 && a.getMaxDamage() == 0) {
                    line = String.format("%d - %s (Level %d) – %s", i + 1, a.getAbilityName(), a.getLevel(), "Special Ability");
                } else {
                    line = String.format("%d - %s (Level %d)", i + 1, a.getAbilityName(), a.getLevel());
                }
                if (a.getAbilityName().equals("Volley")) {
                    line += " [AoE: hits all enemies]";
                }
                if (!a.isReady()) {
                    line += " [CD:" + a.getCurrentCooldown() + "]";
                }
                if (player1.getMaxMp() > 0) {
                    int displayCost = a.getAbilityName().equals("Wand Bonk") ? 0 : a.getMinDamage();
                    line += " [MP:" + displayCost + "]";
                }
                System.out.println(line);
            }
            int useItemOption = abilities.size() + 1;
            System.out.println(useItemOption + " - Use Item");
            System.out.println("======================================");
            System.out.print("Enter a number between 1 and " + useItemOption + ": ");

            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                scanner.nextLine();

                if (choice == useItemOption) {
                    java.util.List<Shop.ShopItem> inv = player1.getInventory();
                    if (inv.isEmpty()) {
                        System.out.println("Inventory is empty. You did not lose your turn.");
                        continue; 
                    }
                    System.out.println("Choose item to use:");
                    java.util.Map<String,Integer> counts = new java.util.LinkedHashMap<>();
                    for (Shop.ShopItem it : inv) counts.merge(it.name, 1, Integer::sum);
                    int idx = 1;
                    java.util.List<String> names = new java.util.ArrayList<>();
                    for (var entry : counts.entrySet()) {
                        System.out.printf("%d) %s x%d%n", idx++, entry.getKey(), entry.getValue());
                        names.add(entry.getKey());
                    }
                     System.out.println("0) Cancel");
                    int itemChoice = scanner.nextInt();
                    scanner.nextLine();
                    if(itemChoice > 0 && itemChoice <= names.size()) {
                        String chosenName = names.get(itemChoice-1);
                        int invIdx = -1;
                        for (int i = 0; i < inv.size(); i++) { if (inv.get(i).name.equals(chosenName)) { invIdx = i; break; } }
                        if (invIdx != -1) {
                            player1.useInventoryItem(invIdx);
                            return enemy.getHealthPoints(); // End turn after using item
                        }
                    } else {
                         System.out.println("Cancelled using item. You did not lose your turn.");
                         continue; 
                    }
                }

                if (choice >= 1 && choice <= abilities.size()) {
                    Ability chosen = abilities.get(choice - 1);
                    if (chosen.isReady() || player1.hasCooldownBuff()) {
                        if (player1.getMaxMp() > 0) {
                            int cost = chosen.getAbilityName().equals("Wand Bonk") ? 0 : chosen.getMinDamage();
                            if (player1.getMp() < cost) {
                                System.out.println("> Not enough MP to cast " + chosen.getAbilityName() + " (requires " + cost + ").");
                                continue;
                            }
                        }
                        System.out.println("You cast " + chosen.getAbilityName() + "!");
                        delay(500);
                        int base = chosen.getRandomDamage();
                        int extra = player1.getExtraDamage();
                        int damage = base + extra;
                        enemy.takeDamage(damage);
                        System.out.println("You dealt " + base + " + " + extra + " = " + damage + " damage to " + enemy.getName() + "!");
                        System.out.println(enemy.getName() + " HP: " + enemy.getHealthPoints());
                        chosen.use();
                        if (chosen.getAbilityName().equals("Shield Bash")) {
                            if (Math.random() < 0.5) {
                                System.out.println("> " + enemy.getName() + " is stunned!");
                                enemy.setStunned(true);
                            }
                        } else if (chosen.getAbilityName().equals("Guard Stance")) {
                            System.out.println("> " + player1.getName() + " braces for incoming attacks!");
                            player1.setGuardRounds(1);
                        }
                        if (player1.getMaxMp() > 0) {
                            int cost = chosen.getAbilityName().equals("Wand Bonk") ? 0 : chosen.getMinDamage();
                            player1.reduceMp(cost);
                        }
                        if (player1.hasCooldownBuff()) {
                            player1.decrementCooldownBuff();
                        }
                        delay(500);
                        return enemy.getHealthPoints();
                    } else {
                        System.out.println("> That ability is on cooldown (" + chosen.getCurrentCooldown() + ").");
                        continue;
                    }
                } else {
                    System.out.println("> Invalid choice. Try again.");
                }
            } else {
                System.out.println("> Please enter a number.");
                scanner.nextLine();
            }
        }
    }

    public static int enemyCombatSequence(Player player1, Enemy enemy) {
        if (enemy.isStunned()) {
            System.out.println("> " + enemy.getName() + " is stunned and cannot act!");
            enemy.setStunned(false); // Stun wears off
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
            player1.takeDamage(damage);
            System.out.println(enemy.getName() + " used " + chosen.getAbilityName() + " and dealt " + damage + " damage to " + player1.getName() + "!");
            chosen.use();
        } else {
            System.out.println("> " + enemy.getName() + " tried to use " + chosen.getAbilityName() + " but it was on cooldown.");
        }
        delay(500);
        return player1.getHealthPoints();
    }

    public static void combatSequenceInit(Player plyr, ArrayList<Enemy> enemies, ArrayList<Ability> plyrAbilities) {
        Enemy currentEnemy = enemies.get(0);
        System.out.println("\n=== Combat vs " + currentEnemy.getName() + " ===");
        
        for (Ability a : plyr.getAbilities()) a.resetCooldown();
        for (Ability a : currentEnemy.getAbilities()) a.resetCooldown();

        int roundCounter = 0;

        while (plyr.getHealthPoints() > 0 && currentEnemy.getHealthPoints() > 0) {
            roundCounter++;
            System.out.println("\n--- Round " + roundCounter + " vs " + currentEnemy.getName() + " ---");

            java.util.Map<String, Integer> initMap = new java.util.LinkedHashMap<>();
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
                    System.out.println("\n--- Player's Turn ---");
                    playerCombatSequence(plyrAbilities, currentEnemy, plyr);
                    for (Ability a : plyrAbilities) a.tickCooldown();
                } else if (actor.equals(currentEnemy.getName())) {
                    if (currentEnemy.getHealthPoints() <= 0) continue;
                    
                    System.out.println("\n--- " + currentEnemy.getName() + "'s Turn ---");
                    enemyCombatSequence(plyr, currentEnemy);
                    for (Ability a : currentEnemy.getAbilities()) a.tickCooldown();
                }
            }
            
            System.out.println();
            printHealthBarStatus(plyr.getHealthPoints(), plyr.getMaxHealth());
            System.out.printf("HP: %d/%d%n", plyr.getHealthPoints(), plyr.getMaxHealth());
        }
    }
}
