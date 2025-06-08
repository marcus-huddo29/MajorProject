import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class Player {

    private final String name;
    private final String playerClass;
    private int healthPoints;
    private final int maxHealth;
    private final int armour;
    private final int initiativeRange;
    private int mp;
    private final int maxMp;
    private final int attackDistance;
    public double currency;
    public double experience;
    // Inventory of purchased shop items
    private final java.util.List<Shop.ShopItem> inventory = new java.util.ArrayList<>();

    // Auto-battle mode flag
    private boolean autoMode = false;
    // Track if attack buff was used this stage (for shop stacking restriction)
    private boolean usedAttackBuffThisStage = false;

    // Called by Shop to check if attack buff purchased this stage
    public boolean hasUsedAttackBuffThisStage() {
        return usedAttackBuffThisStage;
    }
    // Called by Shop to set flag when attack buff is bought
    public void setUsedAttackBuffThisStage(boolean used) {
        this.usedAttackBuffThisStage = used;
    }
    // Should be called at the start of each stage (reset flag)
    public void resetAttackBuffThisStage() {
        this.usedAttackBuffThisStage = false;
    }
    private final Ability ability1;
    private final Ability ability2;
    private final Ability ability3;
    private final ArrayList<Ability> abilities;

    private int extraDamage = 0;

    private int guardRounds = 0;

    private int levelsGained = 0;

    // Buff: number of rounds of cooldown immunity (when >0, can use abilities even if on cooldown)
    private int cooldownBuffRounds = 0;
    // Cooldown buff: returns true if player currently has cooldown immunity
    public boolean hasCooldownBuff() {
        return cooldownBuffRounds > 0;
    }
    // Set the number of rounds for cooldown immunity
    public void setCooldownBuffRounds(int rounds) {
        this.cooldownBuffRounds = rounds;
    }
    // Decrement cooldown buff rounds (if active)
    public void decrementCooldownBuff() {
        if (cooldownBuffRounds > 0) {
            cooldownBuffRounds--;
        }
    }

    // Abilities available when leveling for each class
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
        this.healthPoints = hp;
        this.maxHealth = hp;
        this.armour = ar;
        this.initiativeRange = ini;
        this.maxMp = maxMp;
        this.mp = maxMp;
        this.attackDistance = attackDistance;
        this.currency = cu;
        this.experience = xp;
        this.ability1 = a1;
        this.ability2 = a2;
        this.ability3 = a3;
        this.abilities = new ArrayList<>();
        this.abilities.add(a1);
        this.abilities.add(a2);
        this.abilities.add(a3);
    }
    public int getExtraDamage() {
        return extraDamage;
    }

    public int rollInitiative() {
        return 1 + new Random().nextInt(this.initiativeRange);
    }

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
        // Reset attack buff purchase flag at new stage or restart
        resetAttackBuffThisStage();
    }

    public int getHealthPoints() {
        return this.healthPoints;
    }

    public int getMaxHealth() {
        return this.maxHealth;
    }

    public String getName() {
        return this.name;
    }

    public ArrayList<Ability> getAbilities() {
        return this.abilities;
    }

    public double getCurrency() {
        return this.currency;
    }

    public double getExperience() {
        return this.experience;
    }

    public int getMp() {
        return this.mp;
    }

    public int getMaxMp() {
        return this.maxMp;
    }

    public void reduceMp(int amount) {
        this.mp = Math.max(0, this.mp - amount);
    }

    public void restoreMp(int amount) {
        this.mp = Math.min(this.maxMp, this.mp + amount);
    }

    public int getAttackDistance() {
        return this.attackDistance;
    }

    // Expose class for shop restrictions
    public String getPlayerClass() {
        return this.playerClass;
    }

    // Heal the player, capped at maxHealth
    public void heal(int amount) {
        this.healthPoints = Math.min(this.healthPoints + amount, this.maxHealth);
    }

    // Add purchased ShopItem to inventory
    public void addItemToInventory(Shop.ShopItem item) {
        inventory.add(item);
    }

    // Return inventory list
    public java.util.List<Shop.ShopItem> getInventory() {
        return inventory;
    }

    // Use an item from inventory by index, applying its effect
    public boolean useInventoryItem(int idx) {
        if (idx < 0 || idx >= inventory.size()) return false;
        Shop.ShopItem item = inventory.remove(idx);
        switch(item.type) {
            case "hp":
                int healAmount = (int) Math.round(this.maxHealth * item.value / 100.0);
                heal(healAmount);
                System.out.println("Used " + item.name +
                                   " and restored " + healAmount +
                                   " HP (" + item.value + "% of max).");
                break;
            case "mp":
                int mpAmount = (int) Math.round(this.maxMp * item.value / 100.0);
                restoreMp(mpAmount);
                System.out.println("Used " + item.name +
                                   " and restored " + mpAmount +
                                   " MP (" + item.value + "% of max).");
                break;
            case "clear_cd":
                // set buff to ignore cooldowns for the next 5 rounds (fixed)
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

    // Level up when experience crosses threshold
    public void levelUp() {
        final double THRESHOLD = 20.0;
        Scanner sc = new Scanner(System.in);
        if (this.experience >= THRESHOLD) {
            this.experience -= THRESHOLD;
            levelsGained++;
            System.out.println(this.name + " leveled up! Max HP +5, Armour +1, Initiative +1.");
            heal(5);
            try {
                // increase armour and initiative via reflection (as before)
                java.lang.reflect.Field ar = Player.class.getDeclaredField("armour");
                ar.setAccessible(true);
                ar.setInt(this, this.armour + 1);
                java.lang.reflect.Field ir = Player.class.getDeclaredField("initiativeRange");
                ir.setAccessible(true);
                ir.setInt(this, this.initiativeRange + 1);
            } catch (Exception e) {
                // ignore
            }
            // Offer new ability or upgrade existing one according to new rule
            List<Ability> potential = new ArrayList<>(LEVEL_UP_POOL.getOrDefault(this.playerClass, List.of()));
            // Filter out ones already known
            List<Ability> newOptions = new ArrayList<>();
            for (Ability a : potential) {
                boolean known = false;
                for (Ability owned : this.abilities) {
                    if (owned.getAbilityName().equals(a.getAbilityName())) {
                        known = true;
                        break;
                    }
                }
                if (!known) newOptions.add(a);
            }
            if (levelsGained % 3 == 0 && !newOptions.isEmpty()) {
                // Every 3rd level: only allow learning a new ability
                System.out.println("Choose one new ability to learn:");
                for (int i = 0; i < newOptions.size(); i++) {
                    Ability a = newOptions.get(i);
                    System.out.printf("%d) %s (Damage %d–%d, CD:%d)%n",
                                      i+1, a.getAbilityName(),
                                      a.getMinDamage(), a.getMaxDamage(), a.getCooldown());
                }
                int choice = -1;
                while (choice < 1 || choice > newOptions.size()) {
                    System.out.print("Enter choice [1-" + newOptions.size() + "]: ");
                    try {
                        choice = Integer.parseInt(sc.nextLine().trim());
                    } catch (NumberFormatException ex) {
                        System.out.println("Invalid input.");
                    }
                }
                Ability learned = newOptions.get(choice - 1);
                this.abilities.add(learned);
                System.out.println("Learned new ability: " + learned.getAbilityName() + "!");
            } else {
                // All other levels: only upgrade existing ability
                System.out.println("Choose one ability to improve:");
                for (int i = 0; i < this.abilities.size(); i++) {
                    Ability a = this.abilities.get(i);
                    System.out.printf("%d) %s (Damage %d–%d, CD:%d)%n",
                                      i+1, a.getAbilityName(),
                                      a.getMinDamage(), a.getMaxDamage(), a.getCooldown());
                }
                int choice = -1;
                while (choice < 1 || choice > this.abilities.size()) {
                    System.out.print("Enter choice [1-" + this.abilities.size() + "]: ");
                    try {
                        choice = Integer.parseInt(sc.nextLine().trim());
                    } catch (NumberFormatException ex) {
                        System.out.println("Invalid input.");
                    }
                }
                Ability toBuff = this.abilities.get(choice - 1);
                toBuff.buffDamage(4);
                System.out.println("Upgraded " + toBuff.getAbilityName() + " damage by 4!");
            }
        }
    }
    /** Enable or disable auto-battle mode */
    public void setAutoMode(boolean autoMode) {
        this.autoMode = autoMode;
    }
    /** Check if auto-battle mode is active */
    public boolean isAutoMode() {
        return this.autoMode;
    }
}