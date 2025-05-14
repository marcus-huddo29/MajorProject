import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList; //Owen
import java.util.Scanner;


public class Client {
    // Create a Scanner object that will read what the user types in 
    private static Scanner scanner = new Scanner(System.in); 

    public static void playerCombatSequence(ArrayList<Ability> abilities){
        //int enemyToBeAttacked = 0;

        while(true){
            //print out the attack options for the player based on their current ability selection
            System.out.println("Choose your ability:");
            System.out.println("1 - " + abilities.get(0).getAbilityName() + " (Level " + abilities.get(0).getLevel() + ")");
            System.out.println("2 - " + abilities.get(1).getAbilityName() + " (Level " + abilities.get(0).getLevel() + ")");
            System.out.println("3 - " + abilities.get(2).getAbilityName() + " (Level " + abilities.get(0).getLevel() + ")");

            System.out.print("Enter the number corresponding to your chosen attack: ");
            
            //choice tree for player's abilities 
            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();  // Read user input as integer
                scanner.nextLine();

                if (choice >= 1 && choice <= 3) {
                    System.out.println("You cast " + abilities.get(choice - 1).getAbilityName() + "!");
                    
                    // TODO: make enemy take damage when attack happens - structure will be something along these lines:
                    // stageEnemies.get(enemyToBeAttacked).takeDamage(abilities.get(choice - 1).damage());
                    break;
                } else {
                    System.out.println("Invalid choice. Please select 1, 2 or 3.");
                } 
            } else {
                System.out.println("Invalid choice.");
                scanner.nextLine();
            } 
        }

        // scanner.close();  // Close the scanner
    }

    public static void enemyCombatSequence(ArrayList<Ability> abilities){
        // STILL IN PROGRESS. NOT WORKING YET
        
        System.out.println("Choose your ability:");
        System.out.println("1 - " + abilities.get(0).getAbilityName());
        System.out.println("2 - " + abilities.get(1).getAbilityName());
        System.out.println("3 - " + abilities.get(2).getAbilityName());

        System.out.print("Enter the number corresponding to your chosen attack: ");
        int choice = scanner.nextInt();  // Read user input as integer

        if (choice == 1) {
            System.out.println("You cast " + abilities.get(0).getAbilityName() + "!");
            // Code for option 1
        } else if (choice == 2) {
            System.out.println("You cast " + abilities.get(1).getAbilityName() + "!");
            // Code for option 2
        } else if (choice == 3) {
            System.out.println("You cast " + abilities.get(2).getAbilityName() + "!");
            // Code for option 3
        } else {
            System.out.println("Invalid choice.");
        }
    }

    public static Enemy generateEnemies(){
        int eHp = 0; 
        int eArmour = 0; 
        int eInitiative= 0;
        double currencyDrop = 0;
        double experienceDrop = 0;
        String eName = ""; 

        try {
            BufferedReader readerEnemy = new BufferedReader(new FileReader("enemyStats.csv"));

            String lineEnemy = readerEnemy.readLine();
            lineEnemy = readerEnemy.readLine();

            // parse through playerStats csv collecting the initial stats from the columns
            while(lineEnemy != null){
                String[] stats = lineEnemy.split(",");
                eName = stats[0];
                eHp = Integer.parseInt(stats[1]);
                eArmour = Integer.parseInt(stats[2]);
                eInitiative =Integer.parseInt(stats[3]);
                currencyDrop = Double.parseDouble(stats[4]);
                experienceDrop = Double.parseDouble(stats[5]);

                // test prints to confirm csv read
                // System.out.println(eName);
                // System.out.println(eHp);
                // System.out.println(eArmour);
                // System.out.println(eInitiative);
                // System.out.println(currencyDrop);
                // System.out.println(experienceDrop);

            lineEnemy = readerEnemy.readLine();
            }
            readerEnemy.close();

        } catch (FileNotFoundException err) {
            System.out.println("The file 'enemyStats.csv' was not found.");
            err.printStackTrace();
        } catch (IOException err) {
            System.out.println("An error occurred while reading the file.");
            err.printStackTrace();
        }

        Enemy generatedEnemy = new Enemy (eName,eHp,eArmour,eInitiative,currencyDrop,experienceDrop);

        return generatedEnemy;
    }


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

        Enemy enemy1 = generateEnemies();
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

        
        // roll for initiative and begin combat
            int playerInit = player1.rollInitiative();
            int enemy1Init = enemy1.rollInitiative();

            System.out.println(player1.name + " rolled a " + playerInit + ".");
            System.out.println(enemy1.name + " rolled a " + enemy1Init + ".");

        while(enemy1.healthPoints > 0 || player1.health > 0){
            if(playerInit >= enemy1Init){
                System.out.println(player1.name + " was prepared, they attack first!");
                playerCombatSequence(abilities);
            } else {
                System.out.println(player1.name + " was caught by surprise, " + enemy1.name + " attacks first!");
                System.out.println("**ENEMY ATTACK GOES HERE**");
                //enemyCombatSequence()
                
            }
        }
        
        
    }
}
