// EnemyAbilityLoader.java

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnemyAbilityLoader {
    private static final Map<String, List<String>> enemyAbilityMap = new HashMap<>();

    public static void loadEnemyAbilities(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            br.readLine(); 
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 2); 
                if (parts.length < 2) continue;
                String enemyName = parts[0].trim();
                List<String> abilities = new ArrayList<>(Arrays.asList(parts[1].split(",")));
                enemyAbilityMap.put(enemyName, abilities);
            }
            System.out.println("Successfully loaded ability sets for " + enemyAbilityMap.size() + " enemies.");
        } catch (IOException | NumberFormatException e) {
            System.err.println("FATAL: Failed to load enemy abilities from " + filename + ": " + e.getMessage());
        }
    }

    public static ArrayList<Ability> getAbilitiesForEnemy(String enemyName) {
        ArrayList<Ability> abilities = new ArrayList<>();
        List<String> abilityNames = enemyAbilityMap.getOrDefault(enemyName, List.of("Basic Strike"));

        for (String name : abilityNames) {
            Ability ability = AbilityFactory.createAbility(name.trim());
            if (ability != null) {
                abilities.add(ability);
            }
        }
        return abilities;
    }
}