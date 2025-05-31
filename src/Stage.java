import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Stage {

    private int stageNumber;
    private Enemy enemies;
    private Player player;
    private boolean isStageCleared;
    private int turnCounter;
    private int currentEnemyIndex;

    public Stage(int stageNumber, Player player, Enemy enemy) {
        this.stageNumber = stageNumber;
        this.player = player;
        this.enemies = enemy;
        this.isStageCleared = false;
        this.turnCounter = 0;
        this.currentEnemyIndex = 0;
    }

    public void startStage() {
        System.out.println("=== Stage " + stageNumber + " Begins ===");
        System.out.println("Enemies generated: ");
       // for (Enemy e : enemies) {
            System.out.println("- " + enemies.name + " (HP: " +  enemies.healthPoints + ")");
       // }
      //  printStageStatus();
    }

    public void runTurn() {
        System.out.println("\n-- Turn " + (++turnCounter) + " --");

        // Player turn (simplified: attacks first enemy with first ability)
        if (!enemies.isEmpty()) {
            Ability firstAbility = player.getAbilities().get(0);
            if (firstAbility.isReady()) {
                int damage = firstAbility.getRandomDamage();
                Enemy target = enemies.get(0);
                target.takeDamage(damage);
                firstAbility.use();
                System.out.println(player.name + " used " + firstAbility.getAbilityName() +
                        " and dealt " + damage + " damage to " + target.getName() + "!");
                if (target.getHealthPoints() <= 0) {
                    System.out.println(target.getName() + " is defeated!");
                    enemies.remove(0);
                }
            } else {
                System.out.println(firstAbility.getAbilityName() + " is on cooldown.");
            }
        }

        // Tick cooldowns
        for (Ability a : player.getAbilities()) {
            a.tickCooldown();
        }

        // Enemy turn (first enemy attacks)
        if (!enemies.isEmpty()) {
            Enemy attacker = enemies.get(currentEnemyIndex);
            int damage = attacker.getRandomAttackDamage();
            player.takeDamage(damage);
            System.out.println(attacker.getName() + " attacked and dealt " + damage + " to " + player.name + "!");
        }

        // Check if stage is over
        if (isStageOver()) {
            isStageCleared = true;
            System.out.println("Stage " + stageNumber + " cleared!");
        } else if (player.health <= 0) {
            System.out.println(player.name + " has been defeated...");
        }
    }

public void removeDefeatedEnemies() {
    enemies.removeIf(enemy -> enemy.healthPoints <= 0);
    currentEnemyIndex = 0; // Reset index after removal
}
    public boolean isStageOver() {
        return enemies.healthPoints <= 0 || player.health <= 0;
    }

    public void printStageStatus() {
        System.out.println("\nPlayer: " + player.name + " | HP: " + player.health);
       System.out.println("Enemies remaining: " + combat.enemyList.size());
        for (Enemy e : combat.enemyList.size()) {
            System.out.println(" - " + e.name + " | HP: " + e.healthPoints);
     }
    }

   /*  private ArrayList<Enemy> generateEnemies(int stageNumber) {
        ArrayList<Enemy> generated = new ArrayList<>();
        try {
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader("enemyStats.csv"));
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                String name = data[0];
                int baseHP = Integer.parseInt(data[1]);
                int baseAttack = Integer.parseInt(data[2]);
                int maxAttack = Integer.parseInt(data[3]);
                int armour = Integer.parseInt(data[4]);
                int currencyDrop = Integer.parseInt(data[5]);
                int experienceDrop = Integer.parseInt(data[6]);

                int scaledHP = baseHP + stageNumber * 5;
                int scaledBaseAttack = baseAttack + stageNumber * 2;
                int scaledMaxAttack = maxAttack + stageNumber * 2;

                Enemy enemy = new Enemy(name, scaledHP, scaledBaseAttack, scaledMaxAttack, armour, currencyDrop, experienceDrop);
                generated.add(enemy);
            }
            reader.close();
        } catch (Exception e) {
            System.out.println("Error loading enemies: " + e.getMessage());
        }
        return generated;
     }

   private ArrayList<Enemy> generateEnemies(int stageNumber) {
    ArrayList<Enemy> generated = new ArrayList<>();
    int enemiesPerStage = 2 + (stageNumber / 2); // Scale enemy count with stage
    
    try (BufferedReader reader = new BufferedReader(new FileReader("enemyStats.csv"))) {
        reader.readLine(); // Skip header
        
        // Read all possible enemy templates
        List<String[]> enemyTemplates = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            enemyTemplates.add(line.split(","));
        }
        
        // Create scaled enemies
        Random rand = new Random();
        for (int i = 0; i < enemiesPerStage; i++) {
            String[] template = enemyTemplates.get(rand.nextInt(enemyTemplates.size()));
            
            String name = template[0];
            int baseHP = Integer.parseInt(template[1]);
            int armour = Integer.parseInt(template[2]);
            int initiative = Integer.parseInt(template[3]);
            double currencyDrop = Double.parseDouble(template[4]);
            double experienceDrop = Double.parseDouble(template[5]);
            
            // Scale stats based on stage
            int scaledHP = baseHP + (stageNumber * 3);
            int scaledArmour = armour + (stageNumber / 2);
            
            generated.add(new Enemy(name, 
                scaledHP, 
                scaledArmour, 
                initiative, 
                currencyDrop * (1 + stageNumber * 0.2), 
                experienceDrop * (1 + stageNumber * 0.3)
            ));
        }
    } catch (Exception e) {
        System.out.println("Error loading enemies: " + e.getMessage());
    }
    return generated;
}
    */
}  