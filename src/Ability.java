import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents a single ability in the game.
 * UPDATED: Now includes mpCost for abilities.
 */
public class Ability {

    private final String abilityName;
    private int minDamage;
    private int maxDamage;
    private final String statusInflicted;
    private int baseCooldown;
    private final String targetType;
    private int currentCooldown;
    private double statusChance;
    private final int mpCost; // NEW: Cost for the ability

    public Ability(String abilityName, int minDamage, int maxDamage, String statusInflicted, int cooldown, String targetType, int mpCost) {
        this.abilityName = abilityName;
        this.minDamage = minDamage;
        this.maxDamage = maxDamage;
        this.statusInflicted = statusInflicted;
        this.baseCooldown = cooldown;
        this.targetType = targetType;
        this.mpCost = mpCost; // NEW
        this.currentCooldown = 0;
        this.statusChance = (statusInflicted != null && !statusInflicted.equalsIgnoreCase("None")) ? 1.0 : 0.0;
    }

    public int getRandomDamage() {
        if (maxDamage <= minDamage) return minDamage;
        return ThreadLocalRandom.current().nextInt(minDamage, maxDamage + 1);
    }

    public boolean isReady() {
        return currentCooldown <= 0;
    }

    public void use() {
        if (!isReady()) {
            System.out.println(abilityName + " is still on cooldown for " + currentCooldown + " more turns.");
            return;
        }
        
        int finalCooldown = this.baseCooldown;
        if (DifficultyManager.getDifficulty().useLongerCooldown()) {
            finalCooldown = (int) Math.ceil(this.baseCooldown * 1.5);
        }
        this.currentCooldown = finalCooldown;
    }

    public void tickCooldown() {
        if (currentCooldown > 0) currentCooldown--;
    }

    public void resetCooldown() {
        this.currentCooldown = 0;
    }

    // --- Getters ---
    public String getAbilityName() { return abilityName; }
    public int getMinDamage() { return minDamage; }
    public int getMaxDamage() { return maxDamage; }
    public String getStatusInflicted() { return statusInflicted; }
    public int getCooldown() { return baseCooldown; }
    public String getTargetType() { return targetType; }
    public int getCurrentCooldown() { return currentCooldown; }
    public double getStatusChance() { return statusChance; }
    public int getMpCost() { return mpCost; } // NEW

    // --- Setters for Upgrades ---
    public void buffDamage(int amount) {
        this.minDamage += amount;
        this.maxDamage += amount;
    }
    
    public void reduceCooldown(int amount) {
        this.baseCooldown = Math.max(0, this.baseCooldown - amount);
    }

    public void increaseStatusChance(double amount) {
        this.statusChance = Math.min(1.0, this.statusChance + amount);
    }

    public void applyDamageMultiplier(double multiplier) {
        this.minDamage = (int) Math.round(this.minDamage * multiplier);
        this.maxDamage = (int) Math.round(this.maxDamage * multiplier);
    }
}