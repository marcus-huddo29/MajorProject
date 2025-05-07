public class Ability {

    private String abilityName;
    private int minDamage;
    private int maxDamage;
    private double experience;
    private double experienceRequired;
    private int level;
    private String statusInflicted;
    private int cooldown;
    private int currentCooldown;

    //Constructor
    public Ability(String abilityName, int minDamage, int maxDamage, double experienceRequired,
                   String statusInflicted, int cooldown) {
        this.abilityName = abilityName;
        this.minDamage = minDamage;
        this.maxDamage = maxDamage;
        this.experience = 0;
        this.experienceRequired = experienceRequired;
        this.level = 1;
        this.statusInflicted = statusInflicted;
        this.cooldown = cooldown;
        this.currentCooldown = 0;
    }

    public int getRandomDamage() {
        return (int) (Math.random() * (maxDamage - minDamage + 1)) + minDamage;
    }

    public void gainExperience(double amount) {
        experience += amount;
        if (experience >= experienceRequired) {
            levelUp();
        }
    }

    private void levelUp() {
        level++;
        minDamage += 1;
        maxDamage += 1;
        experience = 0;
        experienceRequired *= 1.5;
        System.out.println(abilityName + " leveled up to level " + level + "!");
    }

    public boolean isReady() {
        return currentCooldown == 0;
    }

    public void use() {
        if (isReady()) {
            currentCooldown = cooldown;
        } else {
            System.out.println(abilityName + " is still on cooldown for " + currentCooldown + " more turns.");
        }
    }

    public void tickCooldown() {
        if (currentCooldown > 0) {
            currentCooldown--;
        }
    }

    public String getAbilityName() {
        return abilityName;
    }

    public int getMinDamage() {
        return minDamage;
    }

    public int getMaxDamage() {
        return maxDamage;
    }

    public double getExperience() {
        return experience;
    }

    public double getExperienceRequired() {
        return experienceRequired;
    }

    public int getLevel() {
        return level;
    }

    public String getStatusInflicted() {
        return statusInflicted;
    }

    public int getCooldown() {
        return cooldown;
    }

    public int getCurrentCooldown() {
        return currentCooldown;
    }
}