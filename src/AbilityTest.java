public class AbilityTest {
    public static void main(String[] args) {
        Ability fireball = new Ability("Fireball", 5, 10, 30, "Burning", 2);

        System.out.println("Using: " + fireball.getAbilityName());

        for (int i = 0; i < 5; i++) {
            if (fireball.isReady()) {
                int damage = fireball.getRandomDamage();
                System.out.println("Turn " + (i+1) + ": Dealt " + damage + " damage!");
                fireball.use();
            } else {
                System.out.println("Turn " + (i+1) + ": Ability on cooldown (" + fireball.getCurrentCooldown() + ")");
            }

            fireball.tickCooldown();

            fireball.gainExperience(10);
        }
    }
}