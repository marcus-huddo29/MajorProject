public enum Difficulty {
    // Defines the multipliers and rules for each difficulty level.
    EASY(1.0, 1.0, 1, false, false),
    NORMAL(1.0, 1.5, 1, false, false),
    HARD(1.0, 2.0, 2, true, false),
    IMPOSSIBLE(0.8, 2.5, 2, true, true);

    private final double playerDamageMultiplier; // Not currently used, but here for future expansion.
    private final double enemyDamageMultiplier;
    private final int enemiesAttackingCount; // Not currently used.
    private final boolean combinedInitiative; // Not currently used.
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