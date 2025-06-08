public class Ability {

    private final String abilityName;
    private int minDamage;
    private int maxDamage;
    private final String statusInflicted;
    private final int cooldown; // The base cooldown, does not change.
    private int currentCooldown;

    /**
     * Constructor for a new Ability.
     */
    public Ability(String abilityName, int minDamage, int maxDamage, String statusInflicted, int cooldown) {
        this.abilityName = abilityName;
        this.minDamage = minDamage;
        this.maxDamage = maxDamage;
        this.statusInflicted = statusInflicted;
        this.cooldown = cooldown;
        this.currentCooldown = 0; // Abilities are ready to use initially.
    }

    /**
     * Calculates a random damage value between the ability's min and max damage.
     * @return A random integer representing damage.
     */
    public int getRandomDamage() {
        if (maxDamage <= minDamage) {
            return minDamage;
        }
        // The +1 makes the maximum damage inclusive.
        return (int) (Math.random() * (maxDamage - minDamage + 1)) + minDamage;
    }

    /**
     * Checks if the ability is ready to be used (i.e., not on cooldown).
     * @return true if currentCooldown is 0, false otherwise.
     */
    public boolean isReady() {
        return currentCooldown == 0;
    }

    /**
     * Puts the ability on cooldown after it has been used.
     * The actual cooldown duration is affected by the game's difficulty setting.
     */
    public void use() {
        if (!isReady()) {
            System.out.println(abilityName + " is still on cooldown for " + currentCooldown + " more turns.");
            return;
        }
        
        // Impossible difficulty doubles the cooldown duration.
        if (DifficultyManager.getDifficulty().useLongerCooldown()) {
            this.currentCooldown = this.cooldown * 2;
        } else {
            this.currentCooldown = this.cooldown;
        }
    }

    /**
     * Reduces the current cooldown by one turn. This should be called at the end of a character's turn.
     */
    public void tickCooldown() {
        if (currentCooldown > 0) {
            currentCooldown--;
        }
    }

    /**
     * Resets the cooldown to 0. Typically used at the start of a new stage.
     */
    public void resetCooldown() {
        this.currentCooldown = 0;
    }

    // --- Getters ---
    public String getAbilityName() { return abilityName; }
    public int getMinDamage() { return minDamage; }
    public int getMaxDamage() { return maxDamage; }
    public String getStatusInflicted() { return statusInflicted; }
    public int getCooldown() { return cooldown; }
    public int getCurrentCooldown() { return currentCooldown; }

    /**
     * Increases the ability's damage. Used for level-up upgrades.
     * @param amount The value to add to both min and max damage.
     */
    public void buffDamage(int amount) {
        this.minDamage += amount;
        this.maxDamage += amount;
    }
}