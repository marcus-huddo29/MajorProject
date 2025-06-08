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
    private int maxHealth; // Made non-final to allow upgrades
    private int armour; // Made non-final to allow upgrades
    private int initiativeRange; // Made non-final to allow upgrades
    private int mp;
    private int maxMp; // Made non-final to allow upgrades
    private final int attackDistance;
    public double currency;
    public double experience;
    private final java.util.List<Shop.ShopItem> inventory = new java.util.ArrayList<>();
    private boolean autoMode = false;
    private boolean usedAttackBuffThisStage = false;
    private final ArrayList<Ability> abilities;
    private int extraDamage = 0;
    private int guardRounds = 0;
    private int levelsGained = 0;
    private int cooldownBuffRounds = 0;

    private static final Map<String, List<Ability>> LEVEL_UP_POOL = new HashMap<>();
    static {
        LEVEL_UP_POOL.put("knight", Arrays.asList(
            new Ability("Whirlwind", 10, 20, 0.0, "None", 3),
            new Ability("Guard Stance", 0, 0, 0.0, "Block", 2)
        ));
        LEVEL_UP_POOL.put("archer", Arrays.asList(
            new Ability("Piercing Shot", 8, 12, 0.0, "Pierce", 2),
            new Ability("Rapid Fire", 3, 6, 0.0, "None", 1)
        ));
        LEVEL_UP_POOL.put("wizard", Arrays.asList(
            new Ability("Meteor Strike", 10, 15, 0.0, "Burn", 4),
            new Ability("Mana Shield", 0, 0, 0.0, "Shield", 3)
        ));
    }

    public Player(int hp, int ar, int ini, int maxMp, int attackDistance,
                  String n, String pCl, double cu, double xp,
                  Ability a1, Ability a2, Ability a3) {
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
        this.abilities = new ArrayList<>();
        this.abilities.add(a1);
        this.abilities.add(a2);
        this.abilities.add(a3);
    }
    
    public void increaseArmour(int amount) {
        this.armour += amount;
    }

    public void increaseInitiative(int amount) {
        this.initiativeRange += amount;
    }
    
    public void performLevelUp() {
        final double THRESHOLD = 20.0;
        if (this.experience < THRESHOLD) return;
        
        this.experience -= THRESHOLD;
        this.levelsGained++;
        
        this.maxHealth += 5;
        this.healthPoints = this.maxHealth; // Heal to new max HP
        
        System.out.println(this.name + " leveled up! Max HP +5, Armour +1, Initiative +1. You are fully healed!");
        increaseArmour(1);
        increaseInitiative(1);
    }

    public boolean canLevelUp() {
        final double THRESHOLD = 20.0;
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

    public int getLevelsGained() {
        return levelsGained;
    }

    public int getExtraDamage() { return extraDamage; }
    public void setExtraDamage(int amount) { this.extraDamage = amount; }
    public int rollInitiative() { return 1 + new Random().nextInt(this.initiativeRange); }
    public void takeDamage(int amount) {
        if (hasGuard()) {
            System.out.println("> " + name + " blocks all damage with Guard Stance!");
            guardRounds--;
            return;
        }
        int actual = amount - this.armour;
        if (actual < 0) actual = 0;
        this.healthPoints -= actual;
        if (this.healthPoints < 0) this.healthPoints = 0;
    }
    public void setGuardRounds(int r) { guardRounds = r; }
    public boolean hasGuard() { return guardRounds > 0; }
    public void resetHealth() {
        this.healthPoints = this.maxHealth;
        this.mp = this.maxMp;
        resetAttackBuffThisStage();
    }
    public int getHealthPoints() { return this.healthPoints; }
    public int getMaxHealth() { return this.maxHealth; }
    public String getName() { return this.name; }
    public ArrayList<Ability> getAbilities() { return this.abilities; }
    public double getCurrency() { return this.currency; }
    public double getExperience() { return this.experience; }
    public int getMp() { return this.mp; }
    public int getMaxMp() { return this.maxMp; }
    public void reduceMp(int amount) { this.mp = Math.max(0, this.mp - amount); }
    public void restoreMp(int amount) { this.mp = Math.min(this.maxMp, this.mp + amount); }
    public int getAttackDistance() { return this.attackDistance; }
    public String getPlayerClass() { return this.playerClass; }
    public void heal(int amount) { this.healthPoints = Math.min(this.healthPoints + amount, this.maxHealth); }
    public void addItemToInventory(Shop.ShopItem item) { inventory.add(item); }
    public java.util.List<Shop.ShopItem> getInventory() { return inventory; }
    public boolean useInventoryItem(int idx) {
        if (idx < 0 || idx >= inventory.size()) return false;
        Shop.ShopItem item = inventory.remove(idx);
        switch(item.type) {
            case "hp":
                int healAmount = (int) Math.round(this.maxHealth * item.value / 100.0);
                heal(healAmount);
                System.out.println("Used " + item.name + " and restored " + healAmount + " HP (" + item.value + "% of max).");
                break;
            case "mp":
                // --- MODIFIED: MP Potions now restore a percentage of max MP ---
                int mpAmount = (int) Math.round(this.maxMp * item.value / 100.0);
                restoreMp(mpAmount);
                System.out.println("Used " + item.name + " and restored " + mpAmount + " MP (" + item.value + "% of max).");
                break;
            case "clear_cd":
                setCooldownBuffRounds(5);
                System.out.println("Used " + item.name + " and gained cooldown immunity for 5 rounds.");
                break;
            case "attack_buff":
            case "weapon":
                this.extraDamage += item.value;
                System.out.println("Used " + item.name + " and increased extra damage by " + item.value + ".");
                break;
            default:
                System.out.println("Used " + item.name + ".");
        }
        return true;
    }
    public boolean hasUsedAttackBuffThisStage() { return usedAttackBuffThisStage; }
    public void setUsedAttackBuffThisStage(boolean used) { this.usedAttackBuffThisStage = used; }
    public void resetAttackBuffThisStage() { this.usedAttackBuffThisStage = false; }
    public boolean hasCooldownBuff() { return cooldownBuffRounds > 0; }
    public void setCooldownBuffRounds(int rounds) { this.cooldownBuffRounds = rounds; }
    public void decrementCooldownBuff() { if (cooldownBuffRounds > 0) { cooldownBuffRounds--; } }
    public void setAutoMode(boolean autoMode) { this.autoMode = autoMode; }
    public boolean isAutoMode() { return this.autoMode; }
}
