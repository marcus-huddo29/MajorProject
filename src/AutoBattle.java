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
        
        player.clearAllStatusEffects();
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
                for(Enemy target : enemies){
                    if(target.getHealthPoints() > 0) applyAutoAbility(player, bestAbility, target);
                }
            } else {
                Enemy target = enemies.stream()
                                      .filter(e -> e.getHealthPoints() > 0)
                                      .min(Comparator.comparingInt(Enemy::getHealthPoints))
                                      .orElse(null);
                if(target != null) applyAutoAbility(player, bestAbility, target);
            }
            
            bestAbility.use();
            int cost = bestAbility.getMpCost();
            switch (player.getPlayerClass()) {
                case "wizard": player.reduceMp(cost); break;
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
        }
    }

    private static Ability selectBestPlayerAbility(Player player, ArrayList<Enemy> enemies) {
        Ability bestAbility = null;
        double bestScore = -1;
        List<Enemy> livingEnemies = enemies.stream().filter(e -> e.getHealthPoints() > 0).collect(Collectors.toList());

        for (Ability a : player.getAbilities()) {
            if (!a.isReady()) continue;

            int cost = a.getMpCost();
            boolean canAfford = false;
            switch(player.getPlayerClass()){
                case "wizard": if(player.getMp() >= cost) canAfford = true; break;
                case "knight": if(player.getRage() >= cost) canAfford = true; break;
                case "archer": if(player.getFocus() >= cost) canAfford = true; break;
            }
            if(!canAfford) continue;

            double score = (double)(a.getMinDamage() + a.getMaxDamage()) / 2.0;
            if(a.getTargetType().equalsIgnoreCase("All")) {
                score *= Math.min(livingEnemies.size(), 3);
            }
            
            if (score > bestScore) {
                bestScore = score;
                bestAbility = a;
            }
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
                 System.out.println("> " + enemy.getName() + " uses a support ability.");
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