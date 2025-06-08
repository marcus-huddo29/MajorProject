import java.util.ArrayList;

public class Stage {

    private final int stageNumber;
    private final ArrayList<Enemy> enemies;
    private final Player player;
    private int turnCounter;

    public Stage(int stageNumber, Player player, ArrayList<Enemy> enemies) {
        this.stageNumber = stageNumber;
        this.player = player;
        this.enemies = enemies;
        this.turnCounter = 0;
    }

    public void startStage() {
        System.out.println("\n=== Stage " + stageNumber + " Begins ===");
        System.out.println("Enemies generated (" + enemies.size() + "):");
        for (Enemy e : enemies) {
            System.out.println("- " + e.getName() + " (HP: " + e.getHealthPoints() + ")");
        }
    }

    public void runTurn() {
        System.out.println("\n-- Turn " + (++turnCounter) + " --");
        if (!enemies.isEmpty()) {
            Ability firstAbility = player.getAbilities().get(0);
            if (firstAbility.isReady()) {
                int dmg = firstAbility.getRandomDamage();
                Enemy target = enemies.get(0);
                target.takeDamage(dmg);
                firstAbility.use();
                System.out.println(player.getName() + " used " + firstAbility.getAbilityName() +
                                   " and dealt " + dmg + " damage to " + target.getName() + "!");
                if (target.getHealthPoints() <= 0) {
                    System.out.println("> " + target.getName() + " was defeated!");
                    enemies.remove(0);
                }
            } else {
                System.out.println("> " + firstAbility.getAbilityName() + " is on cooldown.");
            }
        }

        for (Ability ab : player.getAbilities()) {
            ab.tickCooldown();
        }

        if (!enemies.isEmpty()) {
            Enemy attacker = enemies.get(0);
            int dmg = attacker.getRandomAttackDamage();
            player.takeDamage(dmg);
            System.out.println("> " + attacker.getName() + " attacked and dealt " + dmg +
                               " damage to " + player.getName() + "!");
        }

        if (isStageOver()) {
            System.out.println(">>> Stage " + stageNumber + " cleared! <<<");
        } else if (player.getHealthPoints() <= 0) {
            System.out.println(player.getName() + " has been defeated...");
        }
    }

    public boolean isStageOver() {
        if (player.getHealthPoints() <= 0) {
            return true;
        }
        for (Enemy e : enemies) {
            if (e.getHealthPoints() > 0) {
                return false;
            }
        }
        return true;
    }

    public void printStageStatus() {
        System.out.println("\n--- STATUS ---");
        System.out.println("Player: " + player.getName() + " | HP: " + player.getHealthPoints());
        System.out.println("Enemies remaining: " + enemies.size());
        for (Enemy e : enemies) {
            System.out.println(" - " + e.getName() + " | HP: " + e.getHealthPoints());
        }
        System.out.println("---------------");
    }
}