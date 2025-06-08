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
    
    // Refactored damage bonuses for clarity.
    private int permanentDamageBonus; // From weapons
    private int temporaryDamageBuff;  // From consumables

    private final ArrayList<Ability> abilities;
    private final List<Shop.ShopItem> inventory = new ArrayList<>();
    private final List<String> ownedWeapons = new ArrayList<>();
    
    private int levelsGained = 0;
    private int guardRounds = 0; // For "Guard Stance" like abilities
    private boolean autoMode = false;

    // A map to hold abilities that can be learned on level-up, specific to each class.
    private static final Map<String, List<Ability>> LEVEL_UP_POOL = new HashMap<>();
    static {
        LEVEL_UP_POOL.put("knight", Arrays.asList(
            new Ability("Whirlwind", 10, 15, "None", 3),
            new Ability("Guard Stance", 0, 0, "Block", 4),
            new Ability("Last Stand", 0, 0, "Invulnerable", 6)
        ));
        LEVEL_UP_POOL.put("archer", Arrays.asList(
            new Ability("Piercing Shot", 8, 12, "Pierce", 2),
            new Ability("Rapid Fire", 4, 7, "None", 1),
            new Ability("Called Shot", 25, 30, "Vulnerable", 5)
        ));
        LEVEL_UP_POOL.put("wizard", Arrays.asList(
            new Ability("Meteor Strike", 20, 25, "Burn", 5),
            new Ability("Mana Shield", 0, 0, "Shield", 4),
            new Ability("Polymorph", 0, 0, "Stun", 6)
        ));
    }

    /**
     * Constructor for a new Player. Initializes stats and abilities based on class.
     */
    public Player(String name, String playerClass) {
        this.name = name;
        this.playerClass = playerClass.toLowerCase();
        this.abilities = new ArrayList<>();
        this.permanentDamageBonus = 0;
        this.temporaryDamageBuff = 0;
        
        // Initialize stats and starting abilities based on the chosen class.
        switch (this.playerClass) {
            case "wizard":
                this.maxHealth = 30; this.armour = 1; this.initiativeRange = 8; this.maxMp = 60;
                abilities.add(new Ability("Fireball", 8, 12, "Burn", 2));
                abilities.add(new Ability("Ice Lance", 4, 8, "Slow", 1));
                abilities.add(new Ability("Arcane Blast", 12, 15, "None", 3));
                abilities.add(new Ability("Mana Dart", 2, 4, "None", 0)); // Basic, no-cost attack.
                break;
            case "archer":
                this.maxHealth = 35; this.armour = 2; this.initiativeRange = 12; this.maxMp = 0;
                abilities.add(new Ability("Arrow Shot", 5, 10, "None", 0));
                abilities.add(new Ability("Poison Arrow", 3, 7, "Poison", 2));
                abilities.add(new Ability("Volley", 15, 25, "None", 4));
                break;
            case "knight":
                this.maxHealth = 40; this.armour = 3; this.initiativeRange = 10; this.maxMp = 0;
                abilities.add(new Ability("Slash", 6, 10, "None", 0));
                abilities.add(new Ability("Shield Bash", 4, 8, "Stun", 2));
                abilities.add(new Ability("Power Strike", 15, 20, "None", 3));
                break;
        }
        this.healthPoints = this.maxHealth;
        this.mp = this.maxMp;
    }
    
    /**
     * Handles the logic for leveling up the player.
     */
    public void performLevelUp() {
        final double THRESHOLD = 20.0 + (levelsGained * 5); // Experience requirement increases with each level.
        if (this.experience < THRESHOLD) return;
        
        this.experience -= THRESHOLD;
        this.levelsGained++;
        
        // Apply stat increases.
        this.maxHealth += 5;
        this.armour += 1;
        this.initiativeRange += 1;
        
        // Fully restore health and mana on level up.
        this.healthPoints = this.maxHealth;
        this.mp = this.maxMp;
        
        System.out.println("\n**************** LEVEL UP! ****************");
        System.out.println(this.name + " reached level " + (this.levelsGained + 1) + "!");
        System.out.println("Max HP +5, Armour +1, Initiative +1. You are fully healed and refreshed!");
        System.out.println("*******************************************");
    }

    /**
     * Checks if the player has enough experience to level up.
     * @return true if experience is sufficient, false otherwise.
     */
    public boolean canLevelUp() {
        final double THRESHOLD = 20.0 + (levelsGained * 5);
        return this.experience >= THRESHOLD;
    }

    /**
     * Gets a list of new abilities the player can learn, filtering out ones they already know.
     * @return A list of new, learnable abilities.
     */
    public List<Ability> getNewLevelUpAbilities() {
        List<Ability> potential = LEVEL_UP_POOL.getOrDefault(this.playerClass, List.of());
        List<Ability> newOptions = new ArrayList<>();
        for (Ability a : potential) {
            // Check if the player already knows an ability with the same name.
            boolean known = this.abilities.stream().anyMatch(owned -> owned.getAbilityName().equals(a.getAbilityName()));
            if (!known) {
                newOptions.add(a);
            }
        }
        return newOptions;
    }
    
    /**
     * Handles the logic for using an item from the inventory.
     * @param inventoryIndex The index of the item in the inventory list.
     * @return true if the item was successfully used.
     */
    public boolean useInventoryItem(int inventoryIndex) {
        if (inventoryIndex < 0 || inventoryIndex >= inventory.size()) return false;
        
        Shop.ShopItem item = inventory.remove(inventoryIndex);
        System.out.println(); // Add spacing for clarity.
        
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
                // This now correctly adds to a temporary buff.
                this.temporaryDamageBuff += item.value;
                System.out.println("> Used " + item.name + ". Your temporary damage has been increased by " + item.value + "!");
                break;
            default:
                // Fallback for any other item types.
                System.out.println("> Used " + item.name + ".");
        }
        return true;
    }
    
    /**
     * Applies a permanent damage bonus from equipping a weapon.
     * @param weapon The weapon item from the shop.
     */
    public void equipWeapon(Shop.ShopItem weapon) {
        this.permanentDamageBonus += weapon.value;
        this.ownedWeapons.add(weapon.name);
        System.out.println("> You equipped the " + weapon.name + ", permanently increasing your damage by " + weapon.value + "!");
    }

    public int rollInitiative() { return 1 + new Random().nextInt(this.initiativeRange); }
    
    public void takeDamage(int amount) {
        if (hasGuard()) {
            System.out.println("> " + name + " blocks all damage with Guard Stance!");
            guardRounds--;
            return;
        }
        int actual = Math.max(1, amount - this.armour);
        this.healthPoints = Math.max(0, this.healthPoints - actual);
    }
    
    public boolean hasGuard() { return guardRounds > 0; }
    public void heal(int amount) { this.healthPoints = Math.min(this.healthPoints + amount, this.maxHealth); }
    public void restoreMp(int amount) { this.mp = Math.min(this.maxMp, this.mp + amount); }
    public void reduceMp(int amount) { this.mp = Math.max(0, this.mp - amount); }
    public void addCurrency(double amount) { this.currency += amount; }
    public void addExperience(double amount) { this.experience += amount; }
    public void addItemToInventory(Shop.ShopItem item) { inventory.add(item); }
    public void resetTemporaryBuffs() { this.temporaryDamageBuff = 0; }

    // --- Getters and Setters ---
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
    public void setGuardRounds(int r) { guardRounds = r; }
}