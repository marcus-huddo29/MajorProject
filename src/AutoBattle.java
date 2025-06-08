import static java.lang.Thread.sleep;
import java.util.ArrayList;

public class AutoBattle {

    private static void delay(int ms) {
        try {
            sleep(ms);
        } catch (InterruptedException ignored) {}
    }

    /**
     * Runs a fully automated battle for one stage.
     * @param player the Player object
     * @param enemies the list of enemies for this stage
     */
    public static void runStage(Player player, ArrayList<Enemy> enemies, int stageNumber) {
        System.out.println("\n========================================");
        System.out.println("   🚩 Auto-Battle Stage " + stageNumber + " Begins");
        System.out.println("========================================\n");
        delay(800);
        // enable auto-mode on player to suppress interactive prompts
        player.setAutoMode(true);
        // Show player full stats before the stage
        System.out.printf("Player Stats – HP: %d/%d", player.getHealthPoints(), player.getMaxHealth());
        if (player.getMaxMp() > 0) {
            System.out.printf(" | MP: %d/%d", player.getMp(), player.getMaxMp());
        }
        System.out.printf(" | Currency: %.1f | Experience: %.1f%n%n",
                          player.currency, player.experience);
        // Reset cooldowns at stage start
        for (Ability a : player.getAbilities()) {
            a.resetCooldown();
        }

        // Loop until all enemies are dead or player is dead
        while (player.getHealthPoints() > 0 
               && enemies.stream().anyMatch(e -> e.getHealthPoints() > 0)) {

            // For each alive enemy, handle one player+enemy turn
            for (Enemy e : new ArrayList<>(enemies)) {
                if (player.getHealthPoints() <= 0) break;
                if (e.getHealthPoints() <= 0) {
                    // Remove defeated enemy
                    continue;
                }

                // — Player Turn —
                Ability best = null;
                int bestScore = -1;
                for (Ability a : player.getAbilities()) {
                    int cost = a.getAbilityName().equals("Wand Bonk") ? 0 : a.getMinDamage();
                    // Allow abilities for classes with no MP (maxMp == 0), or if player has enough MP
                    if (a.isReady() && (player.getMaxMp() == 0 || player.getMp() >= cost)) {
                        int score = a.getMaxDamage() + player.getExtraDamage();
                        if (score > bestScore) {
                            bestScore = score;
                            best = a;
                        }
                    }
                }
                if (best != null) {
                    delay(500);
                    System.out.println("\n--- Player Action ---");
                    delay(500);
                    System.out.println("Auto: " + player.getName()
                        + " uses " + best.getAbilityName());
                    delay(300);
                    int dmg = best.getRandomDamage() + player.getExtraDamage();
                    e.takeDamage(dmg);
                    System.out.println("Auto dealt " + dmg
                        + " to " + e.getName()
                        + " (HP left: " + e.getHealthPoints() + ")");
                    best.use();
                    // Subtract MP if applicable
                    if (player.getMaxMp() > 0 && !best.getAbilityName().equals("Wand Bonk")) {
                        player.reduceMp(best.getMinDamage());
                    }
                    delay(300);
                    System.out.println();
                    delay(300);
                }

                // Tick all cooldowns
                for (Ability a : player.getAbilities()) {
                    a.tickCooldown();
                }

                // Remove defeated enemy
                if (e.getHealthPoints() <= 0) {
                    delay(300);
                    // System.out.println("Auto: " + e.getName() + " defeated!"); // redundant, summary is printed later
                    enemies.remove(e);
                    delay(300);
                }

                if (player.getHealthPoints() <= 0) break;

                // — Enemy Turn —
                int attacks = DifficultyManager.getDifficulty().getEnemiesAttackingCount();
                delay(500);
                System.out.println("\n--- Enemies Strike ---");
                for (int i = 0; i < attacks; i++) {
                    delay(500);
                    if (e.getHealthPoints() <= 0) break;
                    int base = 1 + (int)(Math.random() * e.getInitiative());
                    int dmg = (int)Math.round(
                        base * DifficultyManager.getDifficulty().getEnemyDamageMultiplier());
                    player.takeDamage(dmg);
                    delay(300);
                    System.out.println(e.getName()
                        + " auto-deals " + dmg
                        + " to " + player.getName()
                        + " (HP left: " + player.getHealthPoints() + ")");
                    if (player.getHealthPoints() <= 0) break;
                }
                System.out.println();
                delay(300);

                if (player.getHealthPoints() <= 0) break;
            }
        }

        // Outcome
        if (player.getHealthPoints() <= 0) {
            System.out.println("Auto: " + player.getName() + " was defeated...");
            System.out.printf("> Final Currency: %.1f | Final Experience: %.1f%n",
                              player.currency, player.experience);
        } else {
            System.out.println("Auto-Battle cleared stage!");
            System.out.printf("> Stage %d cleared!%nCurrent Currency: %.1f | Current Experience: %.1f%n",
                              stageNumber, player.currency, player.experience);
        }
        // disable auto-mode so subsequent manual play prompts function normally
        player.setAutoMode(false);
    }
}