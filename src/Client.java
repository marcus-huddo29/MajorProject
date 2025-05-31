import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList; //Owen
import java.util.Scanner;


public class Client {
     
    public static void delay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupted status
            System.err.println("Delay was interrupted!");
        }
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
           
            System.out.println("Name:"+name);
             delay(100);
            System.out.println("class:"+playerClass);
             delay(100);
            System.out.println("health:"+maxHP);
             delay(100);
            System.out.println("starting:"+startingArmour);
             delay(100);
            System.out.println("initiativeRange:"+initiativeRange);
             delay(100);
            System.out.println("startCurr:"+startCurr);
             delay(100);
            System.out.println("experience:"+experience);
             delay(100);

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
        int stageNumber=1;
       while(true){
            Player player1 = new Player(maxHP, startingArmour, initiativeRange, name, playerClass, startCurr, experience);
            
            Enemy enemy1 = Enemy.generateEnemies();
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
       
        Stage stage1= new Stage(stageNumber, player1, enemy1);
        stage1.startStage();
        // delay for easier legibility
        delay(1000);
        //commence combat sequence with the player and the enemy
        combat.combatSequenceInit(player1, enemy1, abilities);
       if (stage1.isStageOver()) {
          //  isStageCleared = true;
          if(enemy1.healthPoints<=0){
            System.out.println("Stage " + stageNumber + " cleared!");
            player1.resetHealth(); // Ensure resetHealth() is implemented

        System.out.print("Press Enter to continue to the next stage...");
        while (!scanner.nextLine().isEmpty()) {
            System.out.print("Invalid input. Just press Enter to continue: ");
        }

        stageNumber++;

    } else if (player1.health <= 0) {
            System.out.println(player1.name + " has been defeated...");
            String input="";
            while(true){
                 System.out.print("Do you want to restart the game? (yes/no): ");
            input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("yes")) {
                break; // Exit inner loop to restart game
            } else if (input.equals("no")) {
                System.out.println("Thanks for playing!");
                System.exit(0);
            } else {
                System.out.println("Invalid input. Please type 'yes' or 'no'.");
            }
            }
            break;
        }
    }
       }
   }


}       
