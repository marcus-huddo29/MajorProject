import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Comparator;

public class Enemy {

    private final String name;
    private int healthPoints;
    private final int maxHealth;
    private int armour;
    private final int initiative;
    private final double currencyDrop;
    private final double experienceDrop;
    private final ArrayList<Ability> abilities;
    
    private final Map<String, Integer> statusEffects = new HashMap<>();
    private int temporaryDamageBuff = 0; // For rally buff

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
            modifiedArmour /= 2;
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

    // A much smarter AI that can decide to heal or buff allies
    public Ability chooseBestAbility(Player player, List<Enemy> allies) {
        List<Ability> availableAbilities = new ArrayList<>();
        for (Ability a : this.abilities) {
            if (a.isReady()) {
                availableAbilities.add(a);
            }
        }
        if (availableAbilities.isEmpty()) return null;

        // --- AI Decision Making ---

        // 1. Healing Logic: If an ally (or self) is below 50% health, consider healing.
        for (Ability ability : availableAbilities) {
            if (ability.getStatusInflicted().equalsIgnoreCase("Heal")) {
                // Find the most wounded ally (including self)
                Enemy targetToHeal = allies.stream()
                    .filter(e -> e.getHealthPoints() > 0 && (double)e.getHealthPoints() / e.getMaxHealth() < 0.5)
                    .min(Comparator.comparingInt(Enemy::getHealthPoints))
                    .orElse(null);
                
                if (targetToHeal != null) {
                    return ability; // Decision: Heal the wounded ally!
                }
            }
        }

        // 2. Buffing Logic: If an ally doesn't have a damage buff, consider buffing.
        for (Ability ability : availableAbilities) {
            if (ability.getStatusInflicted().equalsIgnoreCase("Buff")) {
                // Find an ally without a damage buff
                Enemy targetToBuff = allies.stream()
                    .filter(e -> e.getHealthPoints() > 0 && e.temporaryDamageBuff == 0)
                    .findFirst()
                    .orElse(null);

                if (targetToBuff != null) {
                    return ability; // Decision: Buff an ally!
                }
            }
        }

        // 3. Attack Logic: If no support action is taken, attack the player.
        // Prioritize status effects on the player
        for (Ability a : availableAbilities) {
            String status = a.getStatusInflicted();
            if (status != null && !status.equalsIgnoreCase("None") && !status.equalsIgnoreCase("Heal") && !status.equalsIgnoreCase("Buff") && !player.hasStatus(status)) {
                if (Math.random() < 0.4) {
                    return a; // Decision: Debuff the player!
                }
            }
        }

        // Default to highest damage attack if no other action is chosen
        return availableAbilities.stream()
            .filter(a -> !a.getStatusInflicted().equalsIgnoreCase("Heal") && !a.getStatusInflicted().equalsIgnoreCase("Buff"))
            .max(Comparator.comparingInt(a -> (a.getMinDamage() + a.getMaxDamage()) / 2))
            .orElse(null); // Return best attack, or null if only support abilities are left
    }
    
    public static ArrayList<Enemy> generateEnemiesFromCSV(String filename) {
        ArrayList<Enemy> enemyList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if(parts.length < 7) continue;
                String eName = parts[0].trim();
                int eHp = Integer.parseInt(parts[1].trim());
                int eArmour = Integer.parseInt(parts[2].trim());
                int eInitiative = Integer.parseInt(parts[3].trim());
                double eCurr = Double.parseDouble(parts[5].trim());
                double eExp = Double.parseDouble(parts[6].trim());
                ArrayList<Ability> enemyAbilities = EnemyAbilityLoader.getAbilitiesForEnemy(eName);
                enemyList.add(new Enemy(eName, eHp, eArmour, eInitiative, eCurr, eExp, enemyAbilities));
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
            int duration = entry.getValue() - 1;

            if (status.equalsIgnoreCase("buff")) { // Buffs also tick down
                if (duration <= 0) {
                    this.temporaryDamageBuff = 0;
                    System.out.println("> " + name + "'s rally buff has worn off.");
                }
            }
            
            switch (status) {
                case "poison":
                case "burn":
                    int dotDamage = (int)(this.maxHealth * 0.05);
                    this.healthPoints = Math.max(0, this.healthPoints - dotDamage);
                    System.out.println("> " + name + " takes " + dotDamage + " damage from " + status + ".");
                    break;
            }

            if (duration <= 0) {
                expired.add(status);
            } else {
                statusEffects.put(status, duration);
            }
        }
        for (String status : expired) {
            statusEffects.remove(status);
            System.out.println("> " + status + " has worn off for " + name + ".");
        }
    }
    
    public void clearAllStatusEffects() {
        statusEffects.clear();
        temporaryDamageBuff = 0;
    }

    public void heal(int amount) {
        this.healthPoints = Math.min(this.maxHealth, this.healthPoints + amount);
    }
    
    public void applyBuff(int damageBonus) {
        this.temporaryDamageBuff += damageBonus;
        applyStatus("Buff", 3); // Buff lasts 3 turns
    }

    // --- Getters ---
    public int getTemporaryDamageBuff() { return temporaryDamageBuff; }
    public ArrayList<Ability> getAbilities() { return this.abilities; }
    public String getName() { return this.name; }
    public int getArmour() { return this.armour; }
    public int getMaxHealth() { return this.maxHealth; }
    public int getHealthPoints() { return this.healthPoints; }
    public double getCurrencyDrop() { return this.currencyDrop; }
    public double getExperienceDrop() { return this.experienceDrop; }
    public int getInitiative() { return this.initiative; }
}