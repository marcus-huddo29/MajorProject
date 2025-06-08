import java.util.ArrayList;

public class AutoBattle {

    private static void delay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }

    /**
     * Runs an entire combat encounter automatically.
     */
    public static void runStage(Player player, ArrayList<Enemy> enemies) {
        System.out.println("\n--- AUTO-BATTLE INITIATED ---");
        
        for (Enemy currentEnemy : enemies) {
            if (player.getHealthPoints() <= 0) break;

            System.out.println("\nNext Opponent: " + currentEnemy.getName());
            
            player.clearAllStatusEffects();
            currentEnemy.clearAllStatusEffects();
            for (Ability a : player.getAbilities()) a.resetCooldown();
            for (Ability a : currentEnemy.getAbilities()) a.resetCooldown();

            // The main combat loop for this enemy.
            while (player.getHealthPoints() > 0 && currentEnemy.getHealthPoints() > 0) {
                
                // --- Player's Turn ---
                System.out.println("\n--- Player's Turn (Auto) ---");
                player.tickStatusEffects();
                if(player.getHealthPoints() <= 0) continue;
                if (!player.hasStatus("stun")) {
                     performPlayerTurn(player, currentEnemy);
                } else {
                     System.out.println("> " + player.getName() + " is stunned and cannot act!");
                }
                if (currentEnemy.getHealthPoints() <= 0) continue;
    
                // --- Enemy's Turn ---
                System.out.println("\n--- Enemy's Turn (Auto) ---");
                currentEnemy.tickStatusEffects();
                if(currentEnemy.getHealthPoints() <= 0) continue;
                if (!currentEnemy.hasStatus("stun")) {
                    performEnemyTurn(player, currentEnemy);
                } else {
                    System.out.println("> " + currentEnemy.getName() + " is stunned and cannot act!");
                }
            }

            if (player.getHealthPoints() > 0) {
                System.out.println("\n> " + currentEnemy.getName() + " defeated!");
            }
        }
        
        if (player.getHealthPoints() <= 0) {
            System.out.println("\nAuto-Battle Result: " + player.getName() + " was defeated...");
        }
    }
    
    private static void performPlayerTurn(Player player, Enemy currentEnemy) {
        Ability bestAbility = selectBestPlayerAbility(player, currentEnemy);
        
        if (bestAbility != null) {
            delay(500);
            System.out.println("> Auto: " + player.getName() + " uses " + bestAbility.getAbilityName());
            delay(500);
            
            int baseDmg = bestAbility.getRandomDamage();
            int extraDmg = player.getPermanentDamageBonus() + player.getTemporaryDamageBuff();
            double difficultyMultiplier = DifficultyManager.getDifficulty().getPlayerDamageMultiplier();
            int totalDmg = (int) ((baseDmg + extraDmg) * difficultyMultiplier);
            
            if (totalDmg > 0) {
                currentEnemy.takeDamage(totalDmg);
                System.out.println("> Auto dealt " + totalDmg + " damage to " + currentEnemy.getName() + " (HP left: " + currentEnemy.getHealthPoints() + ")");
            }
            
            if(Math.random() < bestAbility.getStatusChance()){
                currentEnemy.applyStatus(bestAbility.getStatusInflicted(), 3);
            }
            
            bestAbility.use();
            
            if ("wizard".equalsIgnoreCase(player.getPlayerClass())) {
                int mpCost = (int)(bestAbility.getMinDamage() * 0.8);
                if(bestAbility.getAbilityName().equals("Mana Dart")) mpCost = 0;
                player.reduceMp(mpCost);
            }

        } else {
            System.out.println("> Auto: " + player.getName() + " has no abilities ready.");
        }

        for (Ability a : player.getAbilities()) a.tickCooldown();
    }

    /**
     * UPDATED: Improved AI logic to select the most efficient ability.
     * @return The best Ability to use, or null if none are usable.
     */
    private static Ability selectBestPlayerAbility(Player player, Enemy enemy) {
        Ability bestAbility = null;
        double bestScore = -1;

        for (Ability a : player.getAbilities()) {
            int mpCost = "wizard".equalsIgnoreCase(player.getPlayerClass()) ? (int)(a.getMinDamage() * 0.8) : 0;
            if (a.getAbilityName().equals("Mana Dart")) mpCost = 0;
            
            if (a.isReady() && player.getMp() >= mpCost) {
                // Score is based on average damage.
                double score = (double)(a.getMinDamage() + a.getMaxDamage()) / 2.0;

                // If the ability is massive overkill, reduce its score to save it.
                if (a.getMinDamage() > enemy.getHealthPoints()) {
                   score *= 0.5;
                }

                if (score > bestScore) {
                    bestScore = score;
                    bestAbility = a;
                }
            }
        }
        
        // Fallback to the first available ability if no "best" was found.
        if (bestAbility == null) {
            for (Ability a : player.getAbilities()) {
                 int mpCost = "wizard".equalsIgnoreCase(player.getPlayerClass()) && !a.getAbilityName().equals("Mana Dart") ? a.getMinDamage() : 0;
                 if (a.isReady() && player.getMp() >= mpCost) {
                    return a;
                 }
            }
        }
        return bestAbility;
    }

    private static void performEnemyTurn(Player player, Enemy enemy) {
        Ability chosenEnemyAbility = enemy.chooseBestAbility(player);

        if (chosenEnemyAbility != null) {
            delay(500);
            int baseDamage = chosenEnemyAbility.getRandomDamage();
            double mult = DifficultyManager.getDifficulty().getEnemyDamageMultiplier();
            int finalDamage = (int) Math.round(baseDamage * mult);
            
            System.out.println("> " + enemy.getName() + " uses " + chosenEnemyAbility.getAbilityName() + "!");
            if(finalDamage > 0) {
                 player.takeDamage(finalDamage);
                 System.out.println("> Auto-deals " + finalDamage + " damage to " + player.getName() + " (HP left: " + player.getHealthPoints() + ")");
            }

            if(Math.random() < chosenEnemyAbility.getStatusChance()){
                player.applyStatus(chosenEnemyAbility.getStatusInflicted(), 3);
            }
           
            chosenEnemyAbility.use();
        } else {
            System.out.println("> " + enemy.getName() + " has no abilities ready.");
        }

        for (Ability a : enemy.getAbilities()) a.tickCooldown();
    }
}
