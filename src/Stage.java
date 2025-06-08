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

    // --- The runTurn() method was removed as it was unused ---

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
        long enemiesAlive = enemies.stream().filter(e -> e.getHealthPoints() > 0).count();
        System.out.println("Enemies remaining: " + enemiesAlive);
        for (Enemy e : enemies) {
            if (e.getHealthPoints() > 0) {
                System.out.println(" - " + e.getName() + " | HP: " + e.getHealthPoints());
            }
        }
        System.out.println("---------------");
    }
}