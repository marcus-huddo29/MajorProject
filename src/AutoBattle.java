import static java.lang.Thread.sleep;
import java.util.ArrayList;

public class AutoBattle {

    private static void delay(int ms) {
        try {
            sleep(ms);
        } catch (InterruptedException ignored) {}
    }

    public static void runStage(Player player, ArrayList<Enemy> enemies, int stageNumber) {
        player.setAutoMode(true);
        
        ArrayList<Enemy> enemiesInStage = new ArrayList<>(enemies);
        
        for(int i = 0; i < enemiesInStage.size(); i++) {
            Enemy currentEnemy = enemiesInStage.get(i);
            
            if (player.getHealthPoints() <= 0) {
                break;
            }

            System.out.printf("Player Stats – HP: %d/%d | MP: %d/%d\n", player.getHealthPoints(), player.getMaxHealth(), player.getMp(), player.getMaxMp());

            for (Ability a : player.getAbilities()) a.resetCooldown();
            for (Ability a : currentEnemy.getAbilities()) a.resetCooldown();

            while (player.getHealthPoints() > 0 && currentEnemy.getHealthPoints() > 0) {
                
                if (player.getHealthPoints() <= 0) break;
                
                System.out.println("\n--- Player's Turn (Auto) ---");
    
                Ability bestAbility = null;
                int bestScore = -1;

                // --- CHANGE --- Smarter AI logic that considers cooldowns and remaining enemy HP to avoid overkill.
                for (Ability a : player.getAbilities()) {
                    int mpCost = player.getPlayerClass().equals("wizard") && !a.getAbilityName().equals("Mana Dart") ? a.getMinDamage() : 0;
                    if (a.isReady() && player.getMp() >= mpCost) {
                        
                        // Score considers damage efficiency (damage per turn if cooldown is factored in).
                        int score = a.getMaxDamage() / (a.getCooldown() + 1);

                        // If the ability is massive overkill, reduce its score significantly.
                        if (a.getMinDamage() > currentEnemy.getHealthPoints()) {
                           score /= 2;
                        }

                        if (score > bestScore) {
                            bestScore = score;
                            bestAbility = a;
                        }
                    }
                }
                
                // Fallback to the first available ability if no "best" was found (e.g. all scores were 0)
                if (bestAbility == null) {
                    for (Ability a : player.getAbilities()) {
                         int mpCost = player.getPlayerClass().equals("wizard") && !a.getAbilityName().equals("Mana Dart") ? a.getMinDamage() : 0;
                         if (a.isReady() && player.getMp() >= mpCost) {
                            bestAbility = a;
                            break;
                         }
                    }
                }
    
                if (bestAbility != null) {
                    delay(500);
                    System.out.println("Auto: " + player.getName() + " uses " + bestAbility.getAbilityName());
                    delay(500);
                    int dmg = bestAbility.getRandomDamage() + player.getExtraDamage();
                    currentEnemy.takeDamage(dmg);
                    System.out.println("Auto dealt " + dmg + " to " + currentEnemy.getName() + " (HP left: " + currentEnemy.getHealthPoints() + ")");
                    bestAbility.use();
                    
                    if (player.getPlayerClass().equals("wizard") && !bestAbility.getAbilityName().equals("Mana Dart")) {
                        player.reduceMp(bestAbility.getMinDamage());
                    }

                } else {
                    System.out.println(player.getName() + " has no abilities ready.");
                }

                for (Ability a : player.getAbilities()) a.tickCooldown();
    
                if (currentEnemy.getHealthPoints() <= 0) {
                    continue; 
                }
    
                System.out.println("\n--- Enemies Strike (Auto) ---");
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
        
        if (player.getHealthPoints() <= 0) {
            System.out.println("\nAuto-Battle Result: " + player.getName() + " was defeated...");
        }
        player.setAutoMode(false);
    }
}
