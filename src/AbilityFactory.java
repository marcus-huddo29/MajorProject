// AbilityFactory.java

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AbilityFactory {

    private static final Map<String, Ability> ABILITY_TEMPLATES = new HashMap<>();

    public static void loadAbilities(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length < 7) continue;

                String name = p[0].trim();
                int minDmg = Integer.parseInt(p[1].trim());
                int maxDmg = Integer.parseInt(p[2].trim());
                String status = p[3].trim();
                int cooldown = Integer.parseInt(p[4].trim());
                String targetType = p[5].trim();
                int mpCost = Integer.parseInt(p[6].trim());

                ABILITY_TEMPLATES.put(name, new Ability(name, minDmg, maxDmg, status, cooldown, targetType, mpCost));
            }
            System.out.println("Successfully loaded " + ABILITY_TEMPLATES.size() + " ability templates.");
        } catch (IOException | NumberFormatException e) {
            System.err.println("FATAL: Failed to load abilities from " + filename + ": " + e.getMessage());
        }
    }

    public static Ability createAbility(String name) {
        Ability template = ABILITY_TEMPLATES.get(name);
        if (template == null) {
            System.err.println("Warning: Could not find ability template for '" + name + "'");
            return null;
        }
        return new Ability(template.getAbilityName(), template.getMinDamage(), template.getMaxDamage(), 
                           template.getStatusInflicted(), template.getCooldown(), template.getTargetType(),
                           template.getMpCost());
    }
}