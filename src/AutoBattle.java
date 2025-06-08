import java.util.ArrayList;
import java.util.List;

public class AutoBattle {

    /**
     * A simple helper to pause execution, making the auto-battle log easier to read.
     * @param ms Milliseconds to pause for.
     */
    private static void delay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            // This is acceptable in a simple game like this.
        }
    }

    /**
     * Runs an entire combat encounter automatically.
     * @param player The player character.
     * @param enemies The list of enemies in this encounter.
     */
    public static void runStage(Player player, ArrayList<Enemy> enemies) {
        System.out.println("\n--- AUTO-BATTLE INITIATED ---");
        player.setAutoMode(true);
        
        for (Enemy currentEnemy : enemies) {
            if (player.getHealthPoints() <= 0) {
                break; // Player was defeated in a previous fight.
            }

            System.out.println("\nNext Opponent: " + currentEnemy.getName());
            System.out.printf("Player Stats – HP: %d/%d | MP: %d/%d\n", player.getHealthPoints(), player.getMaxHealth(), player.getMp(), player.getMaxMp());

            // Reset cooldowns for the new fight.
            for (Ability a : player.getAbilities()) a.resetCooldown();
            for (Ability a : currentEnemy.getAbilities()) a.resetCooldown();

            // The main combat loop for this enemy.
            while (player.getHealthPoints() > 0 && currentEnemy.getHealthPoints() > 0) {
                
                // --- Player's Turn ---
                System.out.println("\n--- Player's Turn (Auto) ---");
                performPlayerTurn(player, currentEnemy);
                if (currentEnemy.getHealthPoints() <= 0) continue; // Enemy defeated.
    
                // --- Enemy's Turn ---
                System.out.println("\n--- Enemy's Turn (Auto) ---");
                performEnemyTurn(player, currentEnemy);
            }

            if (player.getHealthPoints() <= 0) {
                break; // Stop the stage if the player is defeated.
            } else {
                System.out.println("\n> " + currentEnemy.getName() + " defeated!");
            }
        }
        
        if (player.getHealthPoints() <= 0) {
            System.out.println("\nAuto-Battle Result: " + player.getName() + " was defeated...");
        }
        player.setAutoMode(false);
    }
    
    /**
     * Contains the logic for the player's automated action.
     */
    private static void performPlayerTurn(Player player, Enemy currentEnemy) {
        // AI selects the best ability to use.
        Ability bestAbility = selectBestAbility(player, currentEnemy);
        
        if (bestAbility != null) {
            delay(500);
            System.out.println("> Auto: " + player.getName() + " uses " + bestAbility.getAbilityName());
            delay(500);
            
            int baseDmg = bestAbility.getRandomDamage();
            int extraDmg = player.getPermanentDamageBonus() + player.getTemporaryDamageBuff();
            int totalDmg = baseDmg + extraDmg;
            
            currentEnemy.takeDamage(totalDmg);
            System.out.println("> Auto dealt " + totalDmg + " damage to " + currentEnemy.getName() + " (HP left: " + currentEnemy.getHealthPoints() + ")");
            
            bestAbility.use();
            
            // Handle MP cost for Wizards
            if ("wizard".equalsIgnoreCase(player.getPlayerClass())) {
                int mpCost = bestAbility.getAbilityName().equals("Mana Dart") ? 0 : bestAbility.getMinDamage();
                player.reduceMp(mpCost);
            }

        } else {
            System.out.println("> Auto: " + player.getName() + " has no abilities ready.");
        }

        // Tick cooldowns for all abilities at the end of the turn.
        for (Ability a : player.getAbilities()) a.tickCooldown();
    }

    /**
     * AI logic to select the most efficient ability based on damage, cooldown, and enemy health.
     * @return The best Ability to use, or null if none are usable.
     */
    private static Ability selectBestAbility(Player player, Enemy enemy) {
        Ability bestAbility = null;
        int bestScore = -1;

        for (Ability a : player.getAbilities()) {
            int mpCost = "wizard".equalsIgnoreCase(player.getPlayerClass()) && !a.getAbilityName().equals("Mana Dart") ? a.getMinDamage() : 0;
            
            if (a.isReady() && player.getMp() >= mpCost) {
                // Score considers damage efficiency (damage per turn, factoring in cooldown).
                int score = a.getMaxDamage() / (a.getCooldown() + 1);

                // If the ability is massive overkill, reduce its score significantly to save it for tougher enemies.
                if (a.getMinDamage() > enemy.getHealthPoints()) {
                   score /= 2;
                }

                if (score > bestScore) {
                    bestScore = score;
                    bestAbility = a;
                }
            }
        }
        
        // Fallback to the first available ability if no "best" was found (e.g., all scores were 0).
        if (bestAbility == null) {
            for (Ability a : player.getAbilities()) {
                 int mpCost = "wizard".equalsIgnoreCase(player.getPlayerClass()) && !a.getAbilityName().equals("Mana Dart") ? a.getMinDamage() : 0;
                 if (a.isReady() && player.getMp() >= mpCost) {
                    bestAbility = a;
                    break;
                 }
            }
        }
        return bestAbility;
    }

    /**
     * Contains the logic for the enemy's automated action.
     */
    private static void performEnemyTurn(Player player, Enemy enemy) {
        if (enemy.isStunned()) {
            System.out.println("> " + enemy.getName() + " is stunned and cannot act!");
            enemy.setStunned(false); // Stun wears off.
            return;
        }

        ArrayList<Ability> enemyAbilities = enemy.getAbilities();
        if (enemyAbilities.isEmpty()) {
            System.out.println("> " + enemy.getName() + " has no abilities!");
            return;
        }
        
        // Simple AI: Choose a random usable ability.
        Ability chosenEnemyAbility = null;
        java.util.Collections.shuffle(enemyAbilities); // Randomize ability order
        for(Ability a : enemyAbilities) {
            if (a.isReady()) {
                chosenEnemyAbility = a;
                break;
            }
        }

        if (chosenEnemyAbility != null) {
            delay(500);
            int baseDamage = chosenEnemyAbility.getRandomDamage();
            // Apply difficulty multiplier.
            double mult = DifficultyManager.getDifficulty().getEnemyDamageMultiplier();
            int finalDamage = (int) Math.round(baseDamage * mult);
            
            System.out.println("> " + enemy.getName() + " uses " + chosenEnemyAbility.getAbilityName() + "!");
            player.takeDamage(finalDamage);
            chosenEnemyAbility.use();
            System.out.println("> Auto-deals " + finalDamage + " damage to " + player.getName() + " (HP left: " + player.getHealthPoints() + ")");
        } else {
            System.out.println("> " + enemy.getName() + " tried to act, but all abilities are on cooldown.");
        }

        // Tick cooldowns for all abilities at the end of the turn.
        for (Ability a : enemy.getAbilities()) a.tickCooldown();
    }
}