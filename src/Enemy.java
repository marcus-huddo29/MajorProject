import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Enemy {

    private final String name;
    private int healthPoints;
    private final int maxHealth;
    private final int armour;
    private final int initiative;
    private final double currencyDrop;
    private final double experienceDrop;
    private final ArrayList<Ability> abilities;
    
    private final Map<String, Integer> statusEffects = new HashMap<>();

    /**
     * UPDATED Constructor to remove unused attackDistance.
     */
    public Enemy(String name, int healthPoints, int armour, int initiative,
                 double currencyDrop, double experienceDrop, ArrayList<Ability> abilities) {
        this.name = name;
        this.maxHealth = healthPoints;
        this.healthPoints = healthPoints;
        this.armour = armour;
        this.initiative = initiative;
        this.currencyDrop = currencyDrop;
        this.experienceDrop = experienceDrop;
        this.abilities = abilities;
    }

    public void takeDamage(int amount) {
        int modifiedArmour = this.armour;
        if (hasStatus("vulnerable")) {
            System.out.println("> " + name + " is vulnerable, taking extra damage!");
            modifiedArmour /= 2; // Armour is less effective
        }
        int actualDamage = Math.max(1, amount - modifiedArmour);
        this.healthPoints = Math.max(0, this.healthPoints - actualDamage);
    }
    
    public int rollInitiative() {
        int roll = 1 + new Random().nextInt(initiative);
        if (hasStatus("slow")) {
            System.out.println("> " + name + " is slowed, rolling with disadvantage.");
            roll = Math.min(roll, 1 + new Random().nextInt(initiative));
        }
        return roll;
    }

    public Ability chooseBestAbility(Player player) {
        List<Ability> availableAbilities = new ArrayList<>();
        for (Ability a : this.abilities) {
            if (a.isReady()) {
                availableAbilities.add(a);
            }
        }
        if (availableAbilities.isEmpty()) return null;

        availableAbilities.sort((a1, a2) -> Integer.compare(a2.getMaxDamage(), a1.getMaxDamage()));
        
        return availableAbilities.get(0);
    }
    
    public static ArrayList<Enemy> generateEnemiesFromCSV(String filename) {
        ArrayList<Enemy> enemyList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if(parts.length < 7) continue;

                String eName = parts[0];
                int eHp = Integer.parseInt(parts[1]);
                int eArmour = Integer.parseInt(parts[2]);
                int eInitiative = Integer.parseInt(parts[3]);
                // int eDistance = Integer.parseInt(parts[4]); // REMOVED
                double eCurr = Double.parseDouble(parts[5]);
                double eExp = Double.parseDouble(parts[6]);

                ArrayList<Ability> chosenAbilities = new ArrayList<>();
                if (eName.contains("Mage") || eName.contains("Sorcerer")) {
                    chosenAbilities.add(new Ability("Shadow Bolt", 4, 8, "None", 1));
                    chosenAbilities.add(new Ability("Weakening Curse", 2, 4, "Vulnerable", 3));
                } else if (eName.contains("Thief") || eName.contains("Assassin")) {
                    chosenAbilities.add(new Ability("Quick Stab", 3, 6, "None", 0));
                    chosenAbilities.add(new Ability("Poison Shiv", 2, 4, "Poison", 2));
                } else {
                    chosenAbilities.add(new Ability("Basic Strike", 3, 7, "None", 1));
                    chosenAbilities.add(new Ability("Power Hit", 5, 10, "None", 2));
                }
                // UPDATED: Removed eDistance from constructor call
                enemyList.add(new Enemy(eName, eHp, eArmour, eInitiative, eCurr, eExp, chosenAbilities));
            }
        } catch (IOException | NumberFormatException ex) {
            System.err.println("Error loading or parsing " + filename + ": " + ex.getMessage());
        }
        return enemyList;
    }
    
    public void applyStatus(String status, int duration) {
        if (status == null || status.equalsIgnoreCase("None")) return;
        statusEffects.put(status.toLowerCase(), duration);
        System.out.println("> " + this.name + " is now affected by " + status + " for " + duration + " turns!");
    }

    public boolean hasStatus(String status) {
        return statusEffects.containsKey(status.toLowerCase());
    }

    public void tickStatusEffects() {
        List<String> expired = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : statusEffects.entrySet()) {
            String status = entry.getKey();
            int duration = entry.getValue();
            
            switch (status) {
                case "poison":
                case "burn":
                    int dotDamage = (int)(this.maxHealth * 0.05);
                    this.healthPoints = Math.max(0, this.healthPoints - dotDamage);
                    System.out.println("> " + name + " takes " + dotDamage + " damage from " + status + ".");
                    break;
            }

            if (duration - 1 <= 0) {
                expired.add(status);
            } else {
                statusEffects.put(status, duration - 1);
            }
        }
        
        for (String status : expired) {
            statusEffects.remove(status);
            System.out.println("> " + status + " has worn off for " + name + ".");
        }
    }
    
    public void clearAllStatusEffects() {
        statusEffects.clear();
    }

    // --- Getters ---
    public ArrayList<Ability> getAbilities() { return this.abilities; }
    public String getName() { return this.name; }
    public int getArmour() { return this.armour; }
    public int getMaxHealth() { return this.maxHealth; }
    public int getHealthPoints() { return this.healthPoints; }
    public double getCurrencyDrop() { return this.currencyDrop; }
    public double getExperienceDrop() { return this.experienceDrop; }
    // ADDED: Getter for initiative to fix compilation error.
    public int getInitiative() { return this.initiative; }
}
