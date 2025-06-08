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
    private double currency;
    private double experience;
    
    private int permanentDamageBonus; // From weapons
    private int temporaryDamageBuff;  // From consumables

    private final ArrayList<Ability> abilities;
    private final List<Shop.ShopItem> inventory = new ArrayList<>();
    private final List<String> ownedWeapons = new ArrayList<>();
    
    // --- NEW: Status Effect Handling ---
    private final Map<String, Integer> statusEffects = new HashMap<>(); // Key: status name, Value: duration

    private int levelsGained = 0;
    private boolean autoMode = false;

    /**
     * UPDATED Constructor for a new Player. Initializes stats from parameters (loaded from CSV).
     */
    public Player(String name, String playerClass, int maxHealth, int armour, int initiative, int maxMp) {
        this.name = name;
        this.playerClass = playerClass.toLowerCase();
        this.abilities = new ArrayList<>();
        this.permanentDamageBonus = 0;
        this.temporaryDamageBuff = 0;
        
        // Stats are now passed in directly from Client.java after being read from CSV.
        this.maxHealth = maxHealth;
        this.armour = armour;
        this.initiativeRange = initiative;
        this.maxMp = maxMp;

        // Initialize starting abilities based on the chosen class using the AbilityFactory.
        switch (this.playerClass) {
            case "wizard":
                abilities.add(AbilityFactory.createAbility("Fireball"));
                abilities.add(AbilityFactory.createAbility("Ice Lance"));
                abilities.add(AbilityFactory.createAbility("Arcane Blast"));
                abilities.add(AbilityFactory.createAbility("Mana Dart"));
                break;
            case "archer":
                abilities.add(AbilityFactory.createAbility("Arrow Shot"));
                abilities.add(AbilityFactory.createAbility("Poison Arrow"));
                abilities.add(AbilityFactory.createAbility("Volley"));
                break;
            case "knight":
                abilities.add(AbilityFactory.createAbility("Slash"));
                abilities.add(AbilityFactory.createAbility("Shield Bash"));
                abilities.add(AbilityFactory.createAbility("Power Strike"));
                break;
        }
        // Remove nulls in case an ability wasn't found in the factory
        abilities.removeIf(java.util.Objects::isNull);

        this.healthPoints = this.maxHealth;
        this.mp = this.maxMp;
    }
    
    public void performLevelUp() {
        final double THRESHOLD = 20.0 + (levelsGained * 5);
        if (this.experience < THRESHOLD) return;
        
        this.experience -= THRESHOLD;
        this.levelsGained++;
        
        this.maxHealth += 5;
        this.armour += 1;
        this.initiativeRange += 1;
        
        this.healthPoints = this.maxHealth;
        this.mp = this.maxMp;
        
        System.out.println("\n**************** LEVEL UP! ****************");
        System.out.println(this.name + " reached level " + (this.levelsGained + 1) + "!");
        System.out.println("Max HP +5, Armour +1, Initiative +1. You are fully healed and refreshed!");
        System.out.println("*******************************************");
    }

    public boolean canLevelUp() {
        final double THRESHOLD = 20.0 + (levelsGained * 5);
        return this.experience >= THRESHOLD;
    }

    /**
     * CORRECTED: This single method now correctly returns a list of new abilities from the factory.
     * The old LEVEL_UP_POOL has been completely removed.
     */
    public List<Ability> getNewLevelUpAbilities() {
        List<String> abilityNames;
        switch (this.playerClass) {
            case "knight":
                abilityNames = Arrays.asList("Whirlwind", "Guard Stance", "Last Stand");
                break;
            case "archer":
                abilityNames = Arrays.asList("Piercing Shot", "Rapid Fire", "Called Shot");
                break;
            case "wizard":
                abilityNames = Arrays.asList("Meteor Strike", "Mana Shield", "Polymorph");
                break;
            default:
                return List.of();
        }

        List<Ability> newOptions = new ArrayList<>();
        for (String name : abilityNames) {
            boolean alreadyKnown = this.abilities.stream().anyMatch(owned -> owned.getAbilityName().equals(name));
            if (!alreadyKnown) {
                Ability newAbility = AbilityFactory.createAbility(name);
                if (newAbility != null) {
                    newOptions.add(newAbility);
                }
            }
        }
        return newOptions;
    }
    
    public boolean useInventoryItem(int inventoryIndex) {
        if (inventoryIndex < 0 || inventoryIndex >= inventory.size()) return false;
        
        Shop.ShopItem item = inventory.remove(inventoryIndex);
        System.out.println();
        
        switch(item.type) {
            case "hp":
                int healAmount = (int) Math.round(this.maxHealth * item.value / 100.0);
                heal(healAmount);
                System.out.println("> Used " + item.name + " and restored " + healAmount + " HP.");
                break;
            case "mp":
                int mpAmount = (int) Math.round(this.maxMp * item.value / 100.0);
                restoreMp(mpAmount);
                System.out.println("> Used " + item.name + " and restored " + mpAmount + " MP.");
                break;
            case "clear_cd":
                for (Ability a : abilities) a.resetCooldown();
                System.out.println("> Used " + item.name + ". All ability cooldowns reset!");
                break;
            case "attack_buff":
                this.temporaryDamageBuff += item.value;
                System.out.println("> Used " + item.name + ". Temporary damage increased by " + item.value + "!");
                break;
        }
        return true;
    }
    
    public void equipWeapon(Shop.ShopItem weapon) {
        this.permanentDamageBonus += weapon.value;
        this.ownedWeapons.add(weapon.name);
        System.out.println("> Equipped " + weapon.name + ", permanently increasing damage by " + weapon.value + "!");
    }

    public int rollInitiative() { 
        int roll = 1 + new Random().nextInt(this.initiativeRange);
        if (hasStatus("Slow")) {
            System.out.println("> " + name + " is slowed, rolling with disadvantage.");
            roll = Math.min(roll, 1 + new Random().nextInt(this.initiativeRange));
        }
        return roll;
    }
    
    public void takeDamage(int amount) {
        if (hasStatus("Guard") || hasStatus("Invulnerable")) {
            System.out.println("> " + name + " blocks all incoming damage!");
            return;
        }
        int modifiedArmour = this.armour;
        if (hasStatus("Vulnerable")) {
            System.out.println("> " + name + " is vulnerable, taking extra damage!");
            modifiedArmour /= 2;
        }
        int actual = Math.max(1, amount - modifiedArmour);
        this.healthPoints = Math.max(0, this.healthPoints - actual);
    }
    
    // --- NEW: Status Effect Methods ---
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
            
            // Handle per-turn effects
            switch (status) {
                case "poison":
                case "burn":
                    int dotDamage = (int)(this.maxHealth * 0.05); // 5% max HP per turn
                    this.healthPoints = Math.max(0, this.healthPoints - dotDamage);
                    System.out.println("> " + name + " takes " + dotDamage + " damage from " + status + ".");
                    break;
            }

            // Decrement duration
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

    public void heal(int amount) { this.healthPoints = Math.min(this.healthPoints + amount, this.maxHealth); }
    public void restoreMp(int amount) { this.mp = Math.min(this.maxMp, this.mp + amount); }
    public void reduceMp(int amount) { this.mp = Math.max(0, this.mp - amount); }
    public void addCurrency(double amount) { this.currency += amount; }
    public void addExperience(double amount) { this.experience += amount; }
    public void addItemToInventory(Shop.ShopItem item) { inventory.add(item); }
    public void resetTemporaryBuffs() { this.temporaryDamageBuff = 0; }

    // --- Getters ---
    public String getName() { return this.name; }
    public String getPlayerClass() { return this.playerClass; }
    public int getHealthPoints() { return this.healthPoints; }
    public int getMaxHealth() { return this.maxHealth; }
    public int getArmour() { return this.armour; }
    public int getMp() { return this.mp; }
    public int getMaxMp() { return this.maxMp; }
    public double getCurrency() { return this.currency; }
    public ArrayList<Ability> getAbilities() { return this.abilities; }
    public List<Shop.ShopItem> getInventory() { return inventory; }
    public List<String> getOwnedWeapons() { return ownedWeapons; }
    public int getPermanentDamageBonus() { return permanentDamageBonus; }
    public int getTemporaryDamageBuff() { return temporaryDamageBuff; }
    public int getLevelsGained() { return levelsGained; }
    public boolean isAutoMode() { return autoMode; }
    public void setAutoMode(boolean autoMode) { this.autoMode = autoMode; }
}