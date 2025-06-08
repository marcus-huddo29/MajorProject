import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Enemy {

    private final String name;
    private int healthPoints;
    private final int armour;
    private final int initiative;
    private final int attackDistance;
    public double currencyDrop;
    public double experienceDrop;
    private boolean stunned = false;
    private final ArrayList<Ability> abilities;

    public boolean isStunned() { return stunned; }
    public void setStunned(boolean s) { stunned = s; }

    public Enemy(String name, int healthPoints, int armour, int initiative,
                 int attackDistance, double currencyDrop, double experienceDrop, ArrayList<Ability> abilities) {
        this.name = name;
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

    public int getHealthPoints() {
        return this.healthPoints;
    }

    // --- MODIFIED: Enemy now correctly uses its armour stat ---
    public void takeDamage(int amount) {
        int actualDamage = amount - this.armour;
        if (actualDamage < 0) {
            actualDamage = 0; // Prevent healing from attacks
        }
        this.healthPoints -= actualDamage;
        if (this.healthPoints < 0) {
            this.healthPoints = 0;
        }
    }

    public int getRandomAttackDamage() {
        return 1 + new Random().nextInt(initiative);
    }

    public int rollInitiative() {
        return 1 + new Random().nextInt(initiative);
    }

    public int getAttackDistance() {
        return this.attackDistance;
    }

    public static ArrayList<Enemy> generateEnemies() {
        ArrayList<Enemy> enemyList = new ArrayList<>();
        ArrayList<Ability> basicAbilities = new ArrayList<>();
        basicAbilities.add(new Ability("Basic Strike", 3, 7, 0, "None", 1));
        basicAbilities.add(new Ability("Power Hit", 5, 10, 0, "None", 2));

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
                enemyList.add(new Enemy(eName, eHp, eArmour, eInitiative, eDistance, eCurr, eExp, new ArrayList<>(basicAbilities)));
            }
        } catch (IOException ex) {
            System.err.println("Error loading enemyStats.csv: " + ex.getMessage());
            ex.printStackTrace();
        }
        return enemyList;
    }
}