// src/Enemy.java

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Comparator;
import java.util.stream.Collectors;

public class Enemy {

    private final String name;
    private int healthPoints;
    private final int maxHealth;
    private int armour;
    private final int initiative;
    private final double currencyDrop;
    private final double experienceDrop;
    private final ArrayList<Ability> abilities;
    private final String aiType;
    
    private StatusEffectNode statusEffectsHead = null; // Head of the status effects linked list
    private final Map<String, Integer> statusResistance = new HashMap<>();
    private int temporaryDamageBuff = 0;

    public Enemy(String name, int healthPoints, int armour, int initiative,
                 double currencyDrop, double experienceDrop, ArrayList<Ability> abilities, String aiType) {
        this.name = name;
        this.maxHealth = healthPoints;
        this.healthPoints = healthPoints;
        this.armour = armour;
        this.initiative = initiative;
        this.currencyDrop = currencyDrop;
        this.experienceDrop = experienceDrop;
        this.abilities = abilities;
        this.aiType = aiType;
    }
    
    public Enemy(Enemy template) {
        this.name = template.name;
        this.maxHealth = template.maxHealth;
        this.healthPoints = template.maxHealth;
        this.armour = template.armour;
        this.initiative = template.initiative;
        this.currencyDrop = template.currencyDrop;
        this.experienceDrop = template.experienceDrop;
        this.aiType = template.aiType;
        this.abilities = new ArrayList<>();
        for (Ability a : EnemyAbilityLoader.getAbilitiesForEnemy(template.name)) {
            this.abilities.add(AbilityFactory.createAbility(a.getAbilityName()));
        }
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

    public Ability chooseBestAbility(Player player, List<Enemy> allies) {
        List<Ability> availableAbilities = this.abilities.stream()
            .filter(Ability::isReady)
            .collect(Collectors.toList());

        if (availableAbilities.isEmpty()) return null;

        switch(this.aiType.toLowerCase()) {
            case "defensive":
                return chooseDefensiveAbility(availableAbilities, allies);
            case "saboteur":
                return chooseSaboteurAbility(availableAbilities, player);
            case "aggressive":
            default:
                return chooseAggressiveAbility(availableAbilities);
        }
    }

    private Ability chooseAggressiveAbility(List<Ability> available) {
        return available.stream()
            .filter(a -> !a.getStatusInflicted().equalsIgnoreCase("Heal") && !a.getStatusInflicted().equalsIgnoreCase("Buff"))
            .max(Comparator.comparingInt(a -> (a.getMinDamage() + a.getMaxDamage()) / 2))
            .orElse(available.get(0));
    }

    private Ability chooseDefensiveAbility(List<Ability> available, List<Enemy> allies) {
        for (Ability ability : available) {
            if (ability.getStatusInflicted().equalsIgnoreCase("Heal")) {
                boolean someoneNeedsHealing = allies.stream()
                    .anyMatch(e -> e.getHealthPoints() > 0 && (double)e.getHealthPoints() / e.getMaxHealth() < 0.6);
                if (someoneNeedsHealing) return ability;
            }
        }
        for (Ability ability : available) {
             if (ability.getStatusInflicted().equalsIgnoreCase("Buff")) {
                boolean someoneNeedsBuff = allies.stream()
                    .anyMatch(e -> e.getHealthPoints() > 0 && e.temporaryDamageBuff == 0);
                if(someoneNeedsBuff) return ability;
            }
        }
        return chooseAggressiveAbility(available);
    }

    private Ability chooseSaboteurAbility(List<Ability> available, Player player) {
        for (Ability a : available) {
            String status = a.getStatusInflicted();
            if (status != null && !status.equalsIgnoreCase("None") && !player.hasStatus(status)) {
                return a; 
            }
        }
        return chooseAggressiveAbility(available);
    }
    
    public static ArrayList<Enemy> generateEnemiesFromCSV(String filename) {
        ArrayList<Enemy> enemyList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if(parts.length < 8) continue;
                String eName = parts[0].trim();
                int eHp = Integer.parseInt(parts[1].trim());
                int eArmour = Integer.parseInt(parts[2].trim());
                int eInitiative = Integer.parseInt(parts[3].trim());
                double eCurr = Double.parseDouble(parts[5].trim());
                double eExp = Double.parseDouble(parts[6].trim());
                String eAiType = parts[7].trim();
                ArrayList<Ability> enemyAbilities = EnemyAbilityLoader.getAbilitiesForEnemy(eName);
                enemyList.add(new Enemy(eName, eHp, eArmour, eInitiative, eCurr, eExp, enemyAbilities, eAiType));
            }
        } catch (IOException | NumberFormatException ex) {
            System.err.println("Error loading or parsing " + filename + ": " + ex.getMessage());
        }
        return enemyList;
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

        StatusEffectNode newEffect = new StatusEffectNode(status.toLowerCase(), finalDuration);
        if (statusEffectsHead != null) {
            newEffect.next = statusEffectsHead;
        }
        statusEffectsHead = newEffect;

        statusResistance.put(status.toLowerCase(), timesApplied + 1);
        System.out.println("> " + this.name + " is now affected by " + status + " for " + finalDuration + " turns!");
    }

    public boolean hasStatus(String status) {
        StatusEffectNode current = statusEffectsHead;
        while (current != null) {
            if (current.effectName.equalsIgnoreCase(status)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    public void tickStatusEffects() {
        StatusEffectNode current = statusEffectsHead;
        StatusEffectNode previous = null;
    
        while (current != null) {
            if (current.effectName.equalsIgnoreCase("buff")) {
                if (current.duration - 1 <= 0) {
                    this.temporaryDamageBuff = 0;
                    System.out.println("> " + name + "'s rally buff has worn off.");
                }
            }
            
            switch (current.effectName) {
                case "poison":
                case "burn":
                    int dotDamage = (int)(this.maxHealth * 0.05);
                    this.healthPoints = Math.max(0, this.healthPoints - dotDamage);
                    System.out.println("> " + name + " takes " + dotDamage + " damage from " + current.effectName + ".");
                    break;
            }
    
            current.duration--;
    
            StatusEffectNode nextNode = current.next;
            if (current.duration <= 0) {
                System.out.println("> " + current.effectName + " has worn off for " + name + ".");
                if(current.effectName.equalsIgnoreCase("stun")){
                    applyStatus("stun_immunity", 2);
                }
    
                if (previous == null) {
                    statusEffectsHead = nextNode;
                } else {
                    previous.next = nextNode;
                }
            } else {
                previous = current;
            }
            current = nextNode;
        }
    }
    
    public void clearAllStatusEffects() {
        statusEffectsHead = null;
        statusResistance.clear();
        temporaryDamageBuff = 0;
    }

    public void heal(int amount) {
        this.healthPoints = Math.min(this.maxHealth, this.healthPoints + amount);
    }
    
    public void applyBuff(int damageBonus) {
        this.temporaryDamageBuff += damageBonus;
        applyStatus("Buff", 3);
    }

    // --- Getters ---
    public String getAiType() { return aiType; }
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