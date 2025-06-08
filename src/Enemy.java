import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Enemy {

    private final String name;
    private int healthPoints;
    private final int maxHealth;
    private final int armour;
    private final int initiative;
    private final int attackDistance;
    private final double currencyDrop;
    private final double experienceDrop;
    private boolean stunned = false;
    private final ArrayList<Ability> abilities;

    public Enemy(String name, int healthPoints, int armour, int initiative,
                 int attackDistance, double currencyDrop, double experienceDrop, ArrayList<Ability> abilities) {
        this.name = name;
        this.maxHealth = healthPoints;
        this.healthPoints = healthPoints;
        this.armour = armour;
        this.initiative = initiative;
        this.attackDistance = attackDistance;
        this.currencyDrop = currencyDrop;
        this.experienceDrop = experienceDrop;
        this.abilities = abilities;
    }

    /**
     * Reduces the enemy's health by a given amount, factoring in armour.
     * @param amount The incoming damage before armour reduction.
     */
    public void takeDamage(int amount) {
        // Armour always blocks some damage, but the attack always deals at least 1 damage.
        int actualDamage = Math.max(1, amount - this.armour);
        
        this.healthPoints -= actualDamage;
        if (this.healthPoints < 0) {
            this.healthPoints = 0;
        }
    }

    /**
     * Rolls a random number for initiative to determine turn order.
     * @return An integer representing the initiative roll.
     */
    public int rollInitiative() {
        return 1 + new Random().nextInt(initiative);
    }
    
    /**
     * Loads a list of all possible enemies from a CSV file.
     * @param filename The path to the enemy stats CSV file.
     * @return An ArrayList of Enemy objects.
     */
    public static ArrayList<Enemy> generateEnemiesFromCSV(String filename) {
        ArrayList<Enemy> enemyList = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if(parts.length < 7) continue; // Skip malformed lines.

                String eName = parts[0];
                int eHp = Integer.parseInt(parts[1]);
                int eArmour = Integer.parseInt(parts[2]);
                int eInitiative = Integer.parseInt(parts[3]);
                int eDistance = Integer.parseInt(parts[4]);
                double eCurr = Double.parseDouble(parts[5]);
                double eExp = Double.parseDouble(parts[6]);

                // A simple system to assign abilities based on enemy type.
                ArrayList<Ability> chosenAbilities = new ArrayList<>();
                if (eName.contains("Mage") || eName.contains("Sorcerer")) {
                    chosenAbilities.add(new Ability("Shadow Bolt", 4, 8, "None", 1));
                } else {
                    chosenAbilities.add(new Ability("Basic Strike", 3, 7, "None", 1));
                    chosenAbilities.add(new Ability("Power Hit", 5, 10, "None", 2));
                }
                enemyList.add(new Enemy(eName, eHp, eArmour, eInitiative, eDistance, eCurr, eExp, chosenAbilities));
            }
        } catch (IOException | NumberFormatException ex) {
            System.err.println("Error loading or parsing " + filename + ": " + ex.getMessage());
            // Create a default enemy if loading fails so the game doesn't crash.
            ArrayList<Ability> fallback = new ArrayList<>();
            fallback.add(new Ability("Desperate Lunge", 1, 1, "None", 0));
            enemyList.add(new Enemy("Error Blob", 10, 0, 1, 1, 1, 1, fallback));
        }
        return enemyList;
    }

    // --- Getters and Setters ---
    public ArrayList<Ability> getAbilities() { return this.abilities; }
    public String getName() { return this.name; }
    public int getArmour() { return this.armour; }
    public int getInitiative() { return this.initiative; }
    public int getMaxHealth() { return this.maxHealth; }
    public int getHealthPoints() { return this.healthPoints; }
    public double getCurrencyDrop() { return this.currencyDrop; }
    public double getExperienceDrop() { return this.experienceDrop; }
    public int getAttackDistance() { return this.attackDistance; }
    public boolean isStunned() { return stunned; }
    public void setStunned(boolean s) { this.stunned = s; }
}