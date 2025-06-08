import static java.lang.Thread.sleep;
import java.util.ArrayList;

public class AutoBattle {

    private static void delay(int ms) {
        try {
            sleep(ms);
        } catch (InterruptedException ignored) {}
    }

    public static void runStage(Player player, ArrayList<Enemy> enemies, int stageNumber) {
        // --- MODIFIED: Removed the main header to prevent confusing duplicate output ---
        player.setAutoMode(true);
        
        ArrayList<Enemy> enemiesInStage = new ArrayList<>(enemies);
        
        for(int i = 0; i < enemiesInStage.size(); i++) {
            Enemy currentEnemy = enemiesInStage.get(i);
            
            if (player.getHealthPoints() <= 0) {
                break;
            }

            // --- MODIFIED: The Client now handles this announcement ---
            // System.out.printf("--- Beginning Encounter vs %s ---\n", currentEnemy.getName());
            System.out.printf("Player Stats – HP: %d/%d | MP: %d/%d\n", player.getHealthPoints(), player.getMaxHealth(), player.getMp(), player.getMaxMp());

            for (Ability a : player.getAbilities()) a.resetCooldown();
            for (Ability a : currentEnemy.getAbilities()) a.resetCooldown();

            while (player.getHealthPoints() > 0 && currentEnemy.getHealthPoints() > 0) {
                
                if (player.getHealthPoints() <= 0) break;
                
                System.out.println("\n--- Player's Turn ---");
    
                Ability bestAbility = null;
                int bestScore = -1;

                // --- NEW: Smarter AI Logic ---
                for (Ability a : player.getAbilities()) {
                    int cost = a.getAbilityName().equals("Wand Bonk") ? 0 : a.getMinDamage();
                    if (a.isReady() && (player.getMaxMp() == 0 || player.getMp() >= cost)) {
                        
                        // Overkill-prevention: don't use a powerful ability on a near-dead enemy
                        if (a.getMaxDamage() > currentEnemy.getHealthPoints() * 2 && a.getCooldown() > 0) {
                            continue; // Save this strong cooldown for a tougher fight
                        }

                        int score = a.getMaxDamage() + player.getExtraDamage();
                        if (score > bestScore) {
                            bestScore = score;
                            bestAbility = a;
                        }
                    }
                }
                // If no 'smart' choice was found, fall back to any available move
                if (bestAbility == null) {
                    for (Ability a : player.getAbilities()) {
                         int cost = a.getAbilityName().equals("Wand Bonk") ? 0 : a.getMinDamage();
                         if (a.isReady() && (player.getMaxMp() == 0 || player.getMp() >= cost)) {
                            bestAbility = a;
                            break;
                         }
                    }
                }
    
                if (bestAbility != null) {
                    delay(300);
                    System.out.println("Auto: " + player.getName() + " uses " + bestAbility.getAbilityName());
                    delay(300);
                    int dmg = bestAbility.getRandomDamage() + player.getExtraDamage();
                    currentEnemy.takeDamage(dmg);
                    System.out.println("Auto dealt " + dmg + " to " + currentEnemy.getName() + " (HP left: " + currentEnemy.getHealthPoints() + ")");
                    bestAbility.use();
                    if (player.getMaxMp() > 0 && !bestAbility.getAbilityName().equals("Wand Bonk")) {
                        player.reduceMp(bestAbility.getMinDamage());
                    }
                } else {
                    System.out.println(player.getName() + " has no abilities ready.");
                }
                for (Ability a : player.getAbilities()) a.tickCooldown();
    
                if (currentEnemy.getHealthPoints() <= 0) {
                    continue; 
                }
    
                System.out.println("--- Enemies Strike ---");
                if (player.getHealthPoints() > 0) { 
                    ArrayList<Ability> enemyAbilities = currentEnemy.getAbilities();
                    if (!enemyAbilities.isEmpty()) {
                        Ability chosenEnemyAbility = enemyAbilities.get((int) (Math.random() * enemyAbilities.size()));
                        if (chosenEnemyAbility.isReady()) {
                            int baseDamage = chosenEnemyAbility.getRandomDamage();
                            double mult = DifficultyManager.getDifficulty().getEnemyDamageMultiplier();
                            int finalDamage = (int) Math.round(baseDamage * mult);
                            player.takeDamage(finalDamage);
                            chosenEnemyAbility.use();
                            System.out.println(currentEnemy.getName() + " uses " + chosenEnemyAbility.getAbilityName() + " and auto-deals " + finalDamage + " to " + player.getName() + " (HP left: " + player.getHealthPoints() + ")");
                        } else {
                            System.out.println(currentEnemy.getName() + " tried to use " + chosenEnemyAbility.getAbilityName() + " but it's on cooldown.");
                        }
                        for (Ability a : currentEnemy.getAbilities()) a.tickCooldown();
                    }
                }
            }
        }
        
        // --- MODIFIED: Removed confusing end-of-battle message ---
        if (player.getHealthPoints() <= 0) {
            System.out.println("Auto: " + player.getName() + " was defeated...");
        }
        player.setAutoMode(false);
    }
}
