import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class AbilityLoader {

    public static ArrayList<Ability> loadAbilitiesFromCSV(String filename) {
        ArrayList<Ability> abilities = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            br.readLine(); // Skip header

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");

                String name = data[0];
                int minDamage = Integer.parseInt(data[1]);
                int maxDamage = Integer.parseInt(data[2]);
                double experienceRequired = Double.parseDouble(data[3]);
                String status = data[4];
                int cooldown = Integer.parseInt(data[5]);

                Ability ability = new Ability(name, minDamage, maxDamage, experienceRequired, status, cooldown);
                abilities.add(ability);
            }

        } catch (IOException e) {
            System.out.println("Error reading CSV: " + e.getMessage());
        }

        return abilities;
    }
}