import java.util.ArrayList;

public class Stage {

    private int stageNumber;
    private ArrayList<Enemy> enemies;
    private Player player;
    private boolean isStageCleared;
    private int turnCounter;
    private int currentEnemyIndex;

    public Stage(int stageNumber, Player player) {
        this.stageNumber = stageNumber;
        this.player = player;
        this.enemies = generateEnemies(stageNumber);
        this.isStageCleared = false;
        this.turnCounter = 0;
        this.currentEnemyIndex = 0;
    }

    public void startStage() {
        System.out.println("=== Stage " + stageNumber + " Begins ===");
        System.out.println("Enemies generated: ");
        for (Enemy e : enemies) {
            System.out.println("- " + e.getName() + " (HP: " + e.getHealthPoints() + ")");
        }
        printStageStatus();
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
        } else if (player.getHealthPoints() <= 0) {
            System.out.println(player.name + " has been defeated...");
        }
    }

    public boolean isStageOver() {
        return enemies.isEmpty() || player.getHealthPoints() <= 0;
    }

    public void printStageStatus() {
        System.out.println("\nPlayer: " + player.name + " | HP: " + player.getHealthPoints());
        System.out.println("Enemies remaining: " + enemies.size());
        for (Enemy e : enemies) {
            System.out.println(" - " + e.getName() + " | HP: " + e.getHealthPoints());
        }
    }

    private ArrayList<Enemy> generateEnemies(int stageNumber) {
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
}