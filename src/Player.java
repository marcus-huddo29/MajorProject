import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Player {

    private final String name;
    private final String playerClass;
    private int healthPoints;
    private int maxHealth;
    private int armour;
    private int initiativeRange;
    private int mp;
    private int maxMp;
    private final int attackDistance;
    private double currency;
    private double experience;
    private final java.util.List<Shop.ShopItem> inventory = new java.util.ArrayList<>();
    private boolean autoMode = false;
    private boolean usedAttackBuffThisStage = false;
    private final ArrayList<Ability> abilities;
    private int extraDamage = 0;
    private int guardRounds = 0;
    private int levelsGained = 0;
    private int cooldownBuffRounds = 0;

    // --- CHANGE --- Expanded the level-up pool with more interesting abilities.
    private static final Map<String, List<Ability>> LEVEL_UP_POOL = new HashMap<>();
    static {
        LEVEL_UP_POOL.put("knight", Arrays.asList(
            new Ability("Whirlwind", 10, 15, 0.0, "None", 3), // AoE feel, but single target in this system
            new Ability("Guard Stance", 0, 0, 0.0, "Block", 4), // High cooldown defensive move
            new Ability("Last Stand", 0, 0, 0.0, "Invulnerable", 6) // Ultimate defensive skill
        ));
        LEVEL_UP_POOL.put("archer", Arrays.asList(
            new Ability("Piercing Shot", 8, 12, 0.0, "Pierce", 2), // Ignores some armour (conceptual)
            new Ability("Rapid Fire", 4, 7, 0.0, "None", 1), // Low cooldown, consistent damage
            new Ability("Called Shot", 25, 30, 0.0, "Vulnerable", 5) // High damage, long CD
        ));
        LEVEL_UP_POOL.put("wizard", Arrays.asList(
            new Ability("Meteor Strike", 20, 25, 0.0, "Burn", 5), // High damage AoE
            new Ability("Mana Shield", 0, 0, 0.0, "Shield", 4), // Converts damage to MP loss
            new Ability("Polymorph", 0, 0, 0.0, "Stun", 6) // Disables an enemy
        ));
    }

    public Player(int hp, int ar, int ini, int maxMp, int attackDistance,
                  String n, String pCl, double cu, double xp,
                  ArrayList<Ability> startingAbilities) {
        this.name = n;
        this.playerClass = pCl;
        this.maxHealth = hp;
        this.healthPoints = hp;
        this.armour = ar;
        this.initiativeRange = ini;
        this.maxMp = maxMp;
        this.mp = maxMp;
        this.attackDistance = attackDistance;
        this.currency = cu;
        this.experience = xp;
        this.abilities = startingAbilities;
    }
    
    public void increaseArmour(int amount) { this.armour += amount; }
    public void increaseInitiative(int amount) { this.initiativeRange += amount; }
    public void addCurrency(double amount) { this.currency += amount; }
    public void addExperience(double amount) { this.experience += amount; }

    public void performLevelUp() {
        final double THRESHOLD = 20.0 + (levelsGained * 5); // Experience requirement increases
        if (this.experience < THRESHOLD) return;
        
        this.experience -= THRESHOLD;
        this.levelsGained++;
        
        this.maxHealth += 5;
        this.healthPoints = this.maxHealth; // Heal to new max HP
        this.mp = this.maxMp; // Restore MP on level up
        
        System.out.println("\n**************** LEVEL UP! ****************");
        System.out.println(this.name + " reached level " + (this.levelsGained + 1) + "!");
        System.out.println("Max HP +5, Armour +1, Initiative +1. You are fully healed and refreshed!");
        System.out.println("*******************************************");
        increaseArmour(1);
        increaseInitiative(1);
    }

    public boolean canLevelUp() {
        final double THRESHOLD = 20.0 + (levelsGained * 5);
        return this.experience >= THRESHOLD;
    }

    public List<Ability> getNewLevelUpAbilities() {
        List<Ability> potential = LEVEL_UP_POOL.getOrDefault(this.playerClass, List.of());
        List<Ability> newOptions = new ArrayList<>();
        for (Ability a : potential) {
            boolean known = this.abilities.stream().anyMatch(owned -> owned.getAbilityName().equals(a.getAbilityName()));
            if (!known) {
                newOptions.add(a);
            }
        }
        return newOptions;
    }

    public int getLevelsGained() { return levelsGained; }
    public int getExtraDamage() { return extraDamage; }
    public int rollInitiative() { return 1 + new Random().nextInt(this.initiativeRange); }
    
    public void takeDamage(int amount) {
        if (hasGuard()) {
            System.out.println("> " + name + " blocks all damage with Guard Stance!");
            guardRounds--;
            return;
        }
        int actual = Math.max(1, amount - this.armour);
        if (amount <= this.armour) actual = 1;

        this.healthPoints -= actual;
        if (this.healthPoints < 0) this.healthPoints = 0;
    }

    public void setGuardRounds(int r) { guardRounds = r; }
    public boolean hasGuard() { return guardRounds > 0; }
    public int getHealthPoints() { return this.healthPoints; }
    public int getMaxHealth() { return this.maxHealth; }
    public String getName() { return this.name; }
    public ArrayList<Ability> getAbilities() { return this.abilities; }
    public double getCurrency() { return this.currency; }
    public int getMp() { return this.mp; }
    public int getMaxMp() { return this.maxMp; }
    public void reduceMp(int amount) { this.mp = Math.max(0, this.mp - amount); }
    public void restoreMp(int amount) { this.mp = Math.min(this.maxMp, this.mp + amount); }
    public String getPlayerClass() { return this.playerClass; }
    public void heal(int amount) { this.healthPoints = Math.min(this.healthPoints + amount, this.maxHealth); }
    public void addItemToInventory(Shop.ShopItem item) { inventory.add(item); }
    public java.util.List<Shop.ShopItem> getInventory() { return inventory; }
    
    public boolean useInventoryItem(int idx) {
        if (idx < 0 || idx >= inventory.size()) return false;
        Shop.ShopItem item = inventory.remove(idx);
        System.out.println(); // Add spacing for clarity
        switch(item.type) {
            case "hp":
                int healAmount = (int) Math.round(this.maxHealth * item.value / 100.0);
                heal(healAmount);
                System.out.println("> Used " + item.name + " and restored " + healAmount + " HP (" + item.value + "% of max).");
                break;
            case "mp":
                int mpAmount = (int) Math.round(this.maxMp * item.value / 100.0);
                restoreMp(mpAmount);
                System.out.println("> Used " + item.name + " and restored " + mpAmount + " MP (" + item.value + "% of max).");
                break;
            case "clear_cd":
                for (Ability a : abilities) {
                    a.resetCooldown();
                }
                System.out.println("> Used " + item.name + ". All ability cooldowns have been reset!");
                break;
            case "attack_buff":
            case "weapon":
                this.extraDamage += item.value;
                System.out.println("> Used " + item.name + ". Your extra damage has been increased by " + item.value + "!");
                break;
            default:
                System.out.println("> Used " + item.name + ".");
        }
        return true;
    }
    
    public void setUsedAttackBuffThisStage(boolean used) { this.usedAttackBuffThisStage = used; }
    public void resetAttackBuffThisStage() { this.usedAttackBuffThisStage = false; }
    public boolean hasCooldownBuff() { return cooldownBuffRounds > 0; }
    public void decrementCooldownBuff() { if (cooldownBuffRounds > 0) { cooldownBuffRounds--; } }
    public void setAutoMode(boolean autoMode) { this.autoMode = autoMode; }
}
