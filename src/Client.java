import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList; //Owen
import java.util.Scanner;


public class Client {
     
    public static void main(String[] args) throws Exception {
        
        int maxHP = 0; 
        int startingArmour = 0; 
        int initiativeRange = 0;
        double startCurr = 0;
        double experience = 0;
        String name = ""; 
        String playerClass = "";

        // activate a reader object to interface with the csv
        BufferedReader readerStats = new BufferedReader(new FileReader("playerStats.csv"));

        String lineStats = readerStats.readLine();
        lineStats = readerStats.readLine();

        // parse through playerStats csv collecting the initial stats from the columns
        while(lineStats != null){
            String[] stats = lineStats.split(",");
            name = stats[0];
            playerClass = stats[1];
            maxHP = Integer.parseInt(stats[2]);
            startingArmour = Integer.parseInt(stats[3]);
            initiativeRange = Integer.parseInt(stats[4]);
            startCurr = Double.parseDouble(stats[5]);
            experience = Double.parseDouble(stats[6]);
        
            // test prints to confirm csv read
            // System.out.println(name);
            // System.out.println(playerClass);
            // System.out.println(maxHP);
            // System.out.println(startingArmour);
            // System.out.println(initiativeRange);
            // System.out.println(startCurr);
            // System.out.println(experience);

            lineStats = readerStats.readLine();
        }
        readerStats.close();
        
        // Load abilities from abilities.csv by Owen
        ArrayList<Ability> abilities = new AbilityLoader().loadAbilitiesFromCSV("abilities.csv");

        // Print all loaded abilities by Owen
        System.out.println("Loaded Abilities:");
        for (Ability ability : abilities) {
            System.out.println("- " + ability.getAbilityName() + " (Level " + ability.getLevel() + ")");
        }

        Player player1 = new Player(maxHP, startingArmour, initiativeRange, name, playerClass, startCurr, experience);

        Enemy enemy1 = combat.generateEnemies();
        Scanner scanner = new Scanner(System.in);  


        // Text input to confirm player is ready to begin.
        while(true){
            System.out.println("Type 'start' to begin combat:");
            String input = scanner.nextLine();  // Read user input as string

            if (input.equalsIgnoreCase("start")) {
                System.out.println("Let's begin.");
                break;
            } else { // if start is not received, loop back and let the player try again
                System.out.println("Invalid entry. Try again.");
            }
        }

        combat.combatSequenceInit(player1, enemy1, abilities);
        
        
    }
}
