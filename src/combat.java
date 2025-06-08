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
            System.out.println("Choose your ability:");
            for (int i = 0; i < abilities.size(); i++) {
                Ability a = abilities.get(i);
                String line;
                if (a.getMinDamage() == 0 && a.getMaxDamage() == 0) {
                    // Special non-damage abilities (e.g., Guard Stance)
                    line = String.format("%d - %s (Level %d) – %s",
                        i+1,
                        a.getAbilityName(),
                        a.getLevel(),
                        "Block incoming damage");
                } else {
                    line = String.format("%d - %s (Level %d)",
                        i+1,
                        a.getAbilityName(),
                        a.getLevel());
                }
                // append AoE note for Volley
                if (a.getAbilityName().equals("Volley")) {
                    line += " [AoE: hits all enemies]";
                }
                // append cooldown if any
                if (!a.isReady()) {
                    line += " [CD:" + a.getCurrentCooldown() + "]";
                }
                // existing MP cost append logic...
                if (player1.getMaxMp() > 0) {
                    int displayCost = a.getAbilityName().equals("Wand Bonk")
                                      ? 0
                                      : a.getMinDamage();
                    line += " [MP:" + displayCost + "]";
                }
                System.out.println(line);
            }
            System.out.println("======================================");
            System.out.print("Enter a number between 1 and " + abilities.size() + ": ");

            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                scanner.nextLine();
                if (choice >= 1 && choice <= abilities.size()) {
                    Ability chosen = abilities.get(choice - 1);
                    // Allow using ability if ready OR player has cooldown buff
                    if (chosen.isReady() || player1.hasCooldownBuff()) {
                        // Check MP availability (do not subtract yet)
                        if (player1.getMaxMp() > 0) {
                            int cost = chosen.getAbilityName().equals("Wand Bonk") 
                                       ? 0 
                                       : chosen.getMinDamage();
                            if (player1.getMp() < cost) {
                                System.out.println("> Not enough MP to cast " 
                                    + chosen.getAbilityName() + " (requires " + cost + ").");
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
                        // Shield Bash: 50% chance to stun enemy
                        if (chosen.getAbilityName().equals("Shield Bash")) {
                            if (Math.random() < 0.5) {
                                System.out.println("> " + enemy.getName() + " is stunned!");
                                enemy.setStunned(true);
                            }
                        } else if (chosen.getAbilityName().equals("Guard Stance")) {
                            System.out.println("> " + player1.getName() + " braces for incoming attacks!");
                            player1.setGuardRounds(1);
                        }
                        // Now subtract MP cost
                        if (player1.getMaxMp() > 0) {
                            int cost = chosen.getAbilityName().equals("Wand Bonk") 
                                       ? 0 
                                       : chosen.getMinDamage();
                            player1.reduceMp(cost);
                        }
                        // consume one round of cooldown immunity if active
                        if (player1.hasCooldownBuff()) {
                            player1.decrementCooldownBuff();
                        }
                        delay(500);
                        return enemy.getHealthPoints();
                    } else {
                        System.out.println("> That ability is on cooldown (" + chosen.getCurrentCooldown() + ").");
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

    public static int enemyCombatSequence(ArrayList<Ability> abilities, Player player1, Enemy enemy) {
        int randomIndex = (int) (Math.random() * abilities.size());
        Ability chosen = abilities.get(randomIndex);
        if (chosen.isReady()) {
            int baseDamage = chosen.getRandomDamage();
            double mult = DifficultyManager.getDifficulty().getEnemyDamageMultiplier();
            int damage = (int) Math.round(baseDamage * mult);
            player1.takeDamage(damage);
            System.out.println(enemy.getName() + " used " + chosen.getAbilityName() +
                               " and dealt " + damage + " damage to " + player1.getName() + "!");
            chosen.use();
        } else {
            System.out.println("> Enemy's " + chosen.getAbilityName() +
                               " is on cooldown (" + chosen.getCurrentCooldown() + ").");
        }
        delay(500);
        return player1.getHealthPoints();
    }

    // Enforces initiative-first output and delays before retaliation
    public static void combatSequenceInit(Player plyr, ArrayList<Enemy> enemies, ArrayList<Ability> plyrAbilities) {
        Difficulty diff = DifficultyManager.getDifficulty();

        // Loop through each enemy in the stage
        for (int idx = 0; idx < enemies.size(); idx++) {
            Enemy enmy = enemies.get(idx);
            int roundCounter = 0;

            // Fight until either the player or this enemy dies
            while (plyr.getHealthPoints() > 0 && enmy.getHealthPoints() > 0) {
                // ——— Print round separator ———
                roundCounter++;
                System.out.println();
                System.out.println("=== Round " + roundCounter + " ===");
                System.out.println();

                // ——— 1) ROLL INITIATIVE FOR ALL PARTICIPANTS ———
                // Player and all alive enemies roll for initiative
                java.util.Map<String, Integer> initMap = new java.util.LinkedHashMap<>();
                initMap.put(plyr.getName(), plyr.rollInitiative());
                for (Enemy en : enemies) {
                    if (en.getHealthPoints() > 0) {
                        initMap.put(en.getName(), en.rollInitiative());
                    }
                }
                // Sort participants by roll descending
                List<String> turnOrder = initMap.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue() - e1.getValue())
                    .map(Map.Entry::getKey)
                    .toList();
                System.out.print("> Turn order: ");
                System.out.println(String.join(" -> ", turnOrder));
                // Execute each in order
                for (String actor : turnOrder) {
                    if (plyr.getHealthPoints() <= 0 || enmy.getHealthPoints() <= 0) break;
                    if (actor.equals(plyr.getName())) {
                        // Player's turn
                        int remainingEnemyHP = playerCombatSequence(plyrAbilities, enmy, plyr);
                        for (Ability a : plyrAbilities) {
                            a.tickCooldown();
                        }
                        if (remainingEnemyHP <= 0) {
                            // Enemy died; exit this round
                            break;
                        }
                    } else if (actor.equals(enmy.getName()) && enmy.getHealthPoints() > 0) {
                        // Enemy's turn
                        ArrayList<Ability> dummyAbilities = new ArrayList<>();
                        dummyAbilities.add(new Ability("Enemy Strike", 2, 5, 1.0, "", 1));
                        dummyAbilities.add(new Ability("Enemy Blast", 3, 7, 2.0, "", 2));
                        dummyAbilities.add(new Ability("Enemy Bite", 1, 4, 0.5, "", 0));
                        int remainingPlayerHP = enemyCombatSequence(dummyAbilities, plyr, enmy);
                        if (remainingPlayerHP <= 0) {
                            break;
                        }
                    }
                }

                // After all moves, insert a blank line, then print the player’s health bar and HP, then a blank line
                System.out.println();
                printHealthBarStatus(plyr.getHealthPoints(), plyr.getMaxHealth());
                System.out.printf("HP: %d/%d%n%n", plyr.getHealthPoints(), plyr.getMaxHealth());

            } // end while: one enemy’s fight

            // Check if player died; if so, print here and exit. Otherwise, return to Client for defeat message.
            if (plyr.getHealthPoints() <= 0) {
                System.out.println(plyr.getName() + " has been defeated...");
                return;
            }
        }
    }
}
