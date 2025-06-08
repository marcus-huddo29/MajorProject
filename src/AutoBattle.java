// AutoBattle.java

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AutoBattle {

    private static void delay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }

    public static void runStage(Player player, ArrayList<Enemy> enemies) {
        System.out.println("\n--- AUTO-BATTLE INITIATED ---");
        
        // --- UPGRADE --- Reset temp buffs and cooldowns at start of auto-battle
        player.clearAllStatusEffects();
        player.resetTemporaryBuffs();
        for (Ability a : player.getAbilities()) a.resetCooldown();
        for (Enemy e : enemies) {
            e.clearAllStatusEffects();
            for (Ability a : e.getAbilities()) a.resetCooldown();
        }

        while (player.getHealthPoints() > 0 && areEnemiesAlive(enemies)) {
            // Player's Turn
            System.out.println("\n--- Player's Turn (Auto) ---");
            player.tickStatusEffects();
            if (player.getHealthPoints() <= 0) continue;
            if (!player.hasStatus("stun")) {
                performPlayerTurn(player, enemies);
            } else {
                System.out.println("> " + player.getName() + " is stunned and cannot act!");
            }
            tickPlayerCooldowns(player);
            if (!areEnemiesAlive(enemies)) break;

            // Enemies' Turn
            for (Enemy enemy : enemies) {
                if (enemy.getHealthPoints() > 0 && player.getHealthPoints() > 0) {
                     System.out.println("\n--- " + enemy.getName() + "'s Turn (Auto) ---");
                     enemy.tickStatusEffects();
                     if (enemy.getHealthPoints() <= 0) continue;
                     if (!enemy.hasStatus("stun")) {
                         performEnemyTurn(player, enemy, enemies);
                     } else {
                         System.out.println("> " + enemy.getName() + " is stunned and cannot act!");
                     }
                     tickEnemyCooldowns(enemy);
                }
            }
        }
        
        if (player.getHealthPoints() <= 0) {
            System.out.println("\nAuto-Battle Result: " + player.getName() + " was defeated...");
        } else {
            System.out.println("\nAuto-Battle Result: VICTORY!");
        }
    }

    private static void performPlayerTurn(Player player, ArrayList<Enemy> enemies) {
        Ability bestAbility = selectBestPlayerAbility(player, enemies);
        
        if (bestAbility != null) {
            delay(500);
            System.out.println("> Auto: " + player.getName() + " uses " + bestAbility.getAbilityName());
            delay(500);
            
            if(bestAbility.getTargetType().equalsIgnoreCase("All")){
                List<Enemy> livingEnemies = enemies.stream().filter(e -> e.getHealthPoints() > 0).collect(Collectors.toList());
                for(Enemy target : livingEnemies){
                    applyAutoAbility(player, bestAbility, target);
                }
            } else {
                // --- UPGRADE --- AI now targets the lowest HP enemy
                Enemy target = enemies.stream()
                                      .filter(e -> e.getHealthPoints() > 0)
                                      .min(Comparator.comparingInt(Enemy::getHealthPoints))
                                      .orElse(null);
                if(target != null) applyAutoAbility(player, bestAbility, target);
            }
            
            bestAbility.use();
            int cost = bestAbility.getMpCost();
            // --- UPGRADE --- Uses correct resource based on class
            switch (player.getPlayerClass()) {
                case "wizard": player.reduceMp(cost); break;
                // In auto-battle, knight and archer costs are resource costs, not generators
                case "knight": player.spendRage(cost); break;
                case "archer": player.spendFocus(cost); break;
            }

        } else {
            System.out.println("> Auto: " + player.getName() + " has no abilities ready.");
        }
    }

    private static void applyAutoAbility(Player player, Ability ability, Enemy target) {
        int baseDmg = ability.getRandomDamage();
        int extraDmg = player.getPermanentDamageBonus() + player.getTemporaryDamageBuff();
        double difficultyMultiplier = DifficultyManager.getDifficulty().getPlayerDamageMultiplier();
        int totalDmg = (int) ((baseDmg + extraDmg) * difficultyMultiplier);
        
        if (totalDmg > 0) {
            player.dealDamage(totalDmg, target);
            System.out.println("> Auto dealt " + totalDmg + " damage to " + target.getName() + " (HP left: " + target.getHealthPoints() + ")");
        }
        
        if(Math.random() < ability.getStatusChance()){
            target.applyStatus(ability.getStatusInflicted(), 3);
            if (target.getHealthPoints() <= 0) {
                System.out.println("> " + target.getName() + " was defeated!");
            }
        }
    }

    // --- UPGRADE --- Completely new, smarter AI logic
    private static Ability selectBestPlayerAbility(Player player, ArrayList<Enemy> enemies) {
        List<Enemy> livingEnemies = enemies.stream().filter(e -> e.getHealthPoints() > 0).collect(Collectors.toList());
        if (livingEnemies.isEmpty()) return null;

        Ability bestAbility = null;
        double bestScore = -1.0;

        for (Ability a : player.getAbilities()) {
            if (!a.isReady()) continue;

            // Check if player can afford the ability
            int cost = a.getMpCost();
            boolean canAfford = false;
            switch(player.getPlayerClass()){
                case "wizard": canAfford = player.getMp() >= cost; break;
                case "knight": canAfford = player.getRage() >= cost; break;
                case "archer": canAfford = player.getFocus() >= cost; break;
            }
            if(!canAfford) continue;

            double score = 0.0;
            double avgDamage = (a.getMinDamage() + a.getMaxDamage()) / 2.0;
            
            // --- AI UPGRADE --- Removed reference to "Heal" as players don't have it.
            // Rule 1: Use defensive abilities if health is critical (below 40%)
            if (player.getHealthPoints() < player.getMaxHealth() * 0.4) {
                if (a.getStatusInflicted().equalsIgnoreCase("Guard")) {
                    score = 1000; // High priority for survival
                }
            }

            // Rule 2: Evaluate offensive abilities
            if (a.getTargetType().equalsIgnoreCase("All")) {
                // AoE is better with more targets
                if (livingEnemies.size() >= 2) {
                    score = avgDamage * livingEnemies.size();
                } else {
                    score = avgDamage * 0.5; // Penalize using AoE on a single target
                }
            } else {
                // Single target ability
                score = avgDamage;
                // Prioritize finishing off a low-health enemy
                for (Enemy e : livingEnemies) {
                    if (e.getHealthPoints() < score) {
                        score += 50; // Add a bonus for securing a kill
                    }
                }
            }
            
            // Rule 3: Add value for applying useful status effects
            if (!a.getStatusInflicted().equalsIgnoreCase("None") && !a.getStatusInflicted().equalsIgnoreCase("Heal")) {
                score *= 1.2; // 20% score bonus for abilities with status effects
            }

            // Rule 4: Penalize overkill with high-cooldown abilities
            if (a.getCooldown() > 3 && livingEnemies.size() == 1 && livingEnemies.get(0).getHealthPoints() < avgDamage) {
                score *= 0.1; // Greatly reduce score if it's an unnecessary finisher
            }

            if (score > bestScore) {
                bestScore = score;
                bestAbility = a;
            }
        }
        
        // Failsafe: if no ability scored well (e.g., only high-cost ones left), find the cheapest usable one.
        if (bestAbility == null) {
            bestAbility = player.getAbilities().stream()
                .filter(a -> a.isReady() && a.getMpCost() == 0)
                .findFirst()
                .orElse(null);
        }

        return bestAbility;
    }


    private static void performEnemyTurn(Player player, Enemy enemy, ArrayList<Enemy> allEnemies) {
        Ability chosenEnemyAbility = enemy.chooseBestAbility(player, allEnemies);

        if (chosenEnemyAbility != null) {
            delay(500);
            System.out.println("> " + enemy.getName() + " uses " + chosenEnemyAbility.getAbilityName() + "!");
            String effect = chosenEnemyAbility.getStatusInflicted();
            
            if (!effect.equalsIgnoreCase("Heal") && !effect.equalsIgnoreCase("Buff")) {
                int baseDamage = chosenEnemyAbility.getRandomDamage() + enemy.getTemporaryDamageBuff();
                double mult = DifficultyManager.getDifficulty().getEnemyDamageMultiplier();
                int finalDamage = (int) Math.round(baseDamage * mult);
                
                if(finalDamage > 0) {
                     player.takeDamage(finalDamage);
                     System.out.println("> Auto-deals " + finalDamage + " damage to " + player.getName() + " (HP left: " + player.getHealthPoints() + ")");
                }

                if(Math.random() < chosenEnemyAbility.getStatusChance()){
                    player.applyStatus(chosenEnemyAbility.getStatusInflicted(), 3);
                }
            } else {
                 // --- UPGRADE --- Add logic for AI healing/buffing its allies
                 if (effect.equalsIgnoreCase("Heal")) {
                    Enemy targetToHeal = allEnemies.stream()
                        .filter(e -> e.getHealthPoints() > 0 && (double)e.getHealthPoints() / e.getMaxHealth() < 0.6)
                        .min(Comparator.comparingInt(Enemy::getHealthPoints)).orElse(enemy); // Heal self if no other target
                    
                    int healAmount = chosenEnemyAbility.getMaxDamage();
                    targetToHeal.heal(healAmount);
                    System.out.println("> It healed " + targetToHeal.getName() + " for " + healAmount + " HP!");

                 } else if (effect.equalsIgnoreCase("Buff")) {
                     Enemy targetToBuff = allEnemies.stream()
                        .filter(e -> e.getHealthPoints() > 0 && e.getTemporaryDamageBuff() == 0)
                        .findFirst().orElse(enemy); // Buff self if no other target

                    int buffAmount = chosenEnemyAbility.getMaxDamage();
                    targetToBuff.applyBuff(buffAmount);
                    System.out.println("> It granted " + targetToBuff.getName() + " a +" + buffAmount + " damage buff!");
                 }
            }
           
            chosenEnemyAbility.use();
        } else {
            System.out.println("> " + enemy.getName() + " has no abilities ready.");
        }
    }
    
    private static void tickPlayerCooldowns(Player player) { for (Ability a : player.getAbilities()) a.tickCooldown(); }
    private static void tickEnemyCooldowns(Enemy enemy) { for (Ability a : enemy.getAbilities()) a.tickCooldown(); }
    private static boolean areEnemiesAlive(ArrayList<Enemy> enemies) {
        return enemies.stream().anyMatch(e -> e.getHealthPoints() > 0);
    }
}