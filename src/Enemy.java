import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Enemy {

    private final String name;
    private int healthPoints;
    private final int maxHealth; // Added for display purposes
    private final int armour;
    private final int initiative;
    private final int attackDistance;
    private final double currencyDrop;
    private final double experienceDrop;
    private boolean stunned = false;
    private final ArrayList<Ability> abilities;

    public boolean isStunned() { return stunned; }
    public void setStunned(boolean s) { stunned = s; }

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
    
    public ArrayList<Ability> getAbilities() {
        return this.abilities;
    }

    public String getName() {
        return this.name;
    }

    public int getArmour() {
        return this.armour;
    }

    public int getInitiative() {
        return this.initiative;
    }
    
    public int getMaxHealth() {
        return this.maxHealth;
    }

    public int getHealthPoints() {
        return this.healthPoints;
    }

    public double getCurrencyDrop() {
        return this.currencyDrop;
    }
    
    public double getExperienceDrop() {
        return this.experienceDrop;
    }

    public void takeDamage(int amount) {
        // --- CHANGE --- Armour now blocks at least 1 damage to always have some effect.
        int actualDamage = Math.max(1, amount - this.armour);
        if (amount <= this.armour) {
            actualDamage = 1; // Always take at least 1 damage
        }
        
        this.healthPoints -= actualDamage;
        if (this.healthPoints < 0) {
            this.healthPoints = 0;
        }
    }

    public int rollInitiative() {
        return 1 + new Random().nextInt(initiative);
    }

    public int getAttackDistance() {
        return this.attackDistance;
    }

    public static ArrayList<Enemy> generateEnemies() {
        ArrayList<Enemy> enemyList = new ArrayList<>();
        
        // --- CHANGE --- Each enemy type can have more distinct abilities.
        ArrayList<Ability> basicMeleeAbilities = new ArrayList<>();
        basicMeleeAbilities.add(new Ability("Basic Strike", 3, 7, 0, "None", 1));
        basicMeleeAbilities.add(new Ability("Power Hit", 5, 10, 0, "None", 2));
        
        ArrayList<Ability> basicMagicAbilities = new ArrayList<>();
        basicMagicAbilities.add(new Ability("Shadow Bolt", 4, 8, 0, "None", 1));

        try (BufferedReader reader = new BufferedReader(new FileReader("enemyStats.csv"))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String eName = parts[0];
                int eHp = Integer.parseInt(parts[1]);
                int eArmour = Integer.parseInt(parts[2]);
                int eInitiative = Integer.parseInt(parts[3]);
                int eDistance = Integer.parseInt(parts[4]);
                double eCurr = Double.parseDouble(parts[5]);
                double eExp = Double.parseDouble(parts[6]);

                // Assign abilities based on name for variety
                ArrayList<Ability> chosenAbilities;
                if (eName.contains("Mage") || eName.contains("Sorcerer")) {
                    chosenAbilities = new ArrayList<>(basicMagicAbilities);
                } else {
                    chosenAbilities = new ArrayList<>(basicMeleeAbilities);
                }
                enemyList.add(new Enemy(eName, eHp, eArmour, eInitiative, eDistance, eCurr, eExp, chosenAbilities));
            }
        } catch (IOException ex) {
            System.err.println("Error loading enemyStats.csv: " + ex.getMessage());
            // Create a default enemy if loading fails so the game doesn't crash
            ArrayList<Ability> fallback = new ArrayList<>();
            fallback.add(new Ability("Desperate Lunge", 1, 1, 0, "None", 0));
            enemyList.add(new Enemy("Error Blob", 10, 0, 1, 1, 1, 1, fallback));
        }
        return enemyList;
    }
}
