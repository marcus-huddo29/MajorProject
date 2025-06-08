// Player.java

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
    private int rage;
    private int maxRage;
    private int focus;
    private int maxFocus;

    private double currency;
    private double experience;
    
    private int permanentDamageBonus;
    private int temporaryDamageBuff;

    private final ArrayList<Ability> abilities;
    private final List<Shop.ShopItem> inventory = new ArrayList<>();
    private final List<String> ownedWeapons = new ArrayList<>();
    
    private final Map<String, Integer> statusEffects = new HashMap<>();
    private final Map<String, Integer> statusResistance = new HashMap<>();

    private int levelsGained = 0;
    private boolean autoMode = false;

    public Player(String name, String playerClass, int maxHealth, int armour, int initiative, int maxMp, int maxRage, int maxFocus) {
        this.name = name;
        this.playerClass = playerClass.toLowerCase();
        this.abilities = new ArrayList<>();
        this.permanentDamageBonus = 0;
        this.temporaryDamageBuff = 0;
        
        this.maxHealth = maxHealth;
        this.armour = armour;
        this.initiativeRange = initiative;
        this.maxMp = maxMp;
        this.maxRage = maxRage;
        this.maxFocus = maxFocus;

        switch (this.playerClass) {
            case "wizard":
                abilities.add(AbilityFactory.createAbility("Fireball"));
                abilities.add(AbilityFactory.createAbility("Ice Lance"));
                abilities.add(AbilityFactory.createAbility("Mana Dart"));
                break;
            case "archer":
                abilities.add(AbilityFactory.createAbility("Arrow Shot"));
                abilities.add(AbilityFactory.createAbility("Poison Arrow"));
                break;
            case "knight":
                abilities.add(AbilityFactory.createAbility("Slash"));
                abilities.add(AbilityFactory.createAbility("Power Strike"));
                abilities.add(AbilityFactory.createAbility("Shield Bash"));
                break;
        }
        abilities.removeIf(java.util.Objects::isNull);

        this.healthPoints = this.maxHealth;
        this.mp = this.maxMp;
        this.rage = 0;
        this.focus = 50;
    }

    public void dealDamage(int damage, Enemy target){
        if (this.playerClass.equals("knight")){
            gainRage(damage / 2);
        }
        target.takeDamage(damage);
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

    public List<Ability> getNewLevelUpAbilities() {
        List<String> abilityNames;
        switch (this.playerClass) {
            case "knight":
                abilityNames = Arrays.asList("Whirlwind", "Guard Stance", "Last Stand");
                break;
            case "archer":
                abilityNames = Arrays.asList("Piercing Shot", "Rapid Fire", "Called Shot", "Volley");
                break;
            case "wizard":
                abilityNames = Arrays.asList("Meteor Strike", "Mana Shield", "Polymorph", "Arcane Blast");
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
        if(this.playerClass.equals("knight")){
            gainRage(actual);
        }
        this.healthPoints = Math.max(0, this.healthPoints - actual);
    }
    
    public void applyStatus(String status, int duration) {
        if (status == null || status.equalsIgnoreCase("None")) return;
        
        if(status.equalsIgnoreCase("stun") && hasStatus("stun_immunity")){
            System.out.println("> " + this.name + " is immune to stun!");
            return;
        }

        int timesApplied = statusResistance.getOrDefault(status.toLowerCase(), 0);
        int finalDuration = (int) (duration / Math.pow(2, timesApplied));

        if(finalDuration <= 0) {
            System.out.println("> " + this.name + " resisted the " + status + " effect!");
            return;
        }

        statusEffects.put(status.toLowerCase(), finalDuration);
        statusResistance.put(status.toLowerCase(), timesApplied + 1);
        System.out.println("> " + this.name + " is now affected by " + status + " for " + finalDuration + " turns!");
    }

    public boolean hasStatus(String status) {
        return statusEffects.containsKey(status.toLowerCase());
    }
    
    public void tickStatusEffects() {
        if(playerClass.equals("archer")){
            gainFocus(15);
        }

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
            if(status.equalsIgnoreCase("stun")){
                applyStatus("stun_immunity", 2);
            }
        }
    }
    
    public void clearAllStatusEffects() {
        statusEffects.clear();
        statusResistance.clear();
    }

    public void heal(int amount) { this.healthPoints = Math.min(this.healthPoints + amount, this.maxHealth); }
    public void restoreMp(int amount) { this.mp = Math.min(this.maxMp, this.mp + amount); }
    public void reduceMp(int amount) { this.mp = Math.max(0, this.mp - amount); }
    public void gainRage(int amount) { this.rage = Math.min(this.maxRage, this.rage + amount); }
    public void spendRage(int amount) { this.rage = Math.max(0, this.rage - amount); }
    public void gainFocus(int amount) { this.focus = Math.min(this.maxFocus, this.focus + amount); }
    public void spendFocus(int amount) { this.focus = Math.max(0, this.focus - amount); }

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
    public int getRage() { return rage; }
    public int getMaxRage() { return maxRage; }
    public int getFocus() { return focus; }
    public int getMaxFocus() { return maxFocus; }
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