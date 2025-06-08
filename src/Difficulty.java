// Difficulty.java

public enum Difficulty {
    // Defines the multipliers and rules for each difficulty level.
    // UPDATED: Impossible multipliers adjusted to be challenging but not unwinnable.
    EASY(1.0, 1.0, 1, false, false),
    NORMAL(1.0, 1.5, 1, false, false),
    HARD(1.0, 2.0, 2, true, false),
    IMPOSSIBLE(0.9, 2.0, 2, true, true); // Player Damage: 0.8->0.9, Enemy Damage: 2.5->2.0

    private final double playerDamageMultiplier;
    private final double enemyDamageMultiplier;
    private final int enemiesAttackingCount; 
    private final boolean combinedInitiative;
    private final boolean longerCooldown;

    Difficulty(double playerDamageMultiplier,
               double enemyDamageMultiplier,
               int enemiesAttackingCount,
               boolean combinedInitiative,
               boolean longerCooldown) {
        this.playerDamageMultiplier = playerDamageMultiplier;
        this.enemyDamageMultiplier = enemyDamageMultiplier;
        this.enemiesAttackingCount = enemiesAttackingCount;
        this.combinedInitiative = combinedInitiative;
        this.longerCooldown = longerCooldown;
    }

    public double getPlayerDamageMultiplier() { return playerDamageMultiplier; }
    public double getEnemyDamageMultiplier() { return enemyDamageMultiplier; }
    public int getEnemiesAttackingCount() { return enemiesAttackingCount; }
    public boolean useCombinedInitiative() { return combinedInitiative; }
    public boolean useLongerCooldown() { return longerCooldown; }
}
