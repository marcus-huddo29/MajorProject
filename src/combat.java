import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class combat {

    // Create a Scanner object that will read what the user types in 
    private static Scanner scanner = new Scanner(System.in);

    public static void delay(int milliseconds) {
        // function to implement a delay on print statements to improve readability
        
        //implemented through a try catch as to be considerate of interrupts
        try {
            // sleep module uses value in miliseconds to delay program.
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("delay interrupt");
        }
}

public static void printHealthBarStatus(int currentHealth, int maxHealth) {

    // establish maximum health and how much health the player has currently
    int filledBars = (int) ((double) currentHealth / maxHealth * maxHealth);
    // how much health has the player lost
    int emptyBars = maxHealth - filledBars;

    StringBuilder bar = new StringBuilder();
    // build a string statement by appending the number of health points 
    // with the number of empty health points
    bar.append("HP: ");
    for (int i = 0; i < filledBars; i++){
        bar.append("█");
    }
    for (int i = 0; i < emptyBars; i++) {
        bar.append("-");
    }
    // print out the new health bar 
    System.out.println(bar.toString());
}

    public static int playerCombatSequence(ArrayList<Ability> abilities, Enemy enemy, Player player1){

        while(true){
            //print out the attack options for the player based on their current ability selection
            System.out.println("======================================");
            printHealthBarStatus(player1.health, 30);;// function to print out a progressive health bar
            System.out.println("Choose your ability:");
            System.out.println("1 - " + abilities.get(0).getAbilityName() + " (Level " + abilities.get(0).getLevel() + ")");
            System.out.println("2 - " + abilities.get(1).getAbilityName() + " (Level " + abilities.get(0).getLevel() + ")");
            System.out.println("3 - " + abilities.get(2).getAbilityName() + " (Level " + abilities.get(0).getLevel() + ")");
            System.out.println("======================================" );
            System.out.print("Enter the number corresponding to your chosen attack: ");
            
            //choice tree for player's abilities 
            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();  // Read user input as integer
                scanner.nextLine();

                // checks that choice is within the valid options
                if (choice >= 1 && choice <= 3) {

                    System.out.println("You cast " + abilities.get(choice - 1).getAbilityName() + "!");
                    delay(1000);
                    // calculate the damage of the chosen ability. index is from 0 so -1 to the choice.
                    int damage = abilities.get(choice - 1).getRandomDamage();
                    // subtract enemy health from the damage dealt.
                    enemy.healthPoints -= damage;
                    System.out.println("You dealt " + damage + " damage to " + enemy.name + "!");
                    System.out.println(enemy.name +"'s health: "+ enemy.healthPoints );
                    delay(1500);
                    break;

                } else {
                    System.out.println("Invalid choice. Please select 1, 2 or 3.");
                } 
            } else {
                System.out.println("Invalid choice.");
                scanner.nextLine();
            } 
        }
        //return enemy health after combat for the next turn
        return enemy.healthPoints;
    }

    public static int enemyCombatSequence(ArrayList<Ability> abilities, Player player1){


        System.out.println("The enemy makes their move: ");
        delay(1000);
        int randomEnemyChoice = (int)(Math.random() * 2);;

        int damage = abilities.get(randomEnemyChoice).getRandomDamage();

        player1.health -= damage;
        System.out.println("The enemy used " + abilities.get(randomEnemyChoice).getAbilityName() + " to deal " + damage + " damage to " + player1.name + "!");
        delay(1500);
        return player1.health;
        
    }
   

   


   public static void combatSequenceInit(Player plyr, Enemy enmy, ArrayList<Ability> ablty) {
    int playerInit = plyr.rollInitiative();
    int enemyInit = enmy.rollInitiative();

    System.out.println(plyr.name + " rolled a " + playerInit + ".");
    delay(500);
    System.out.println(enmy.name + " rolled a " + enemyInit + ".");
    delay(500);

    while(enmy.healthPoints > 0 && plyr.health > 0) {
        if(playerInit >= enemyInit) {
            if(playerInit < 10) {
                System.out.println(plyr.name + " was prepared, they attack first!");
            }
            enmy.healthPoints = combat.playerCombatSequence(ablty, enmy, plyr);
            enemyInit += 100;
            
            // Check if enemy was defeated
            if(enmy.healthPoints <= 0) break;
        } else {
            if(enemyInit < 10) {
                System.out.println(plyr.name + " was caught by surprise, " + enmy.name + " attacks first!");
            }
            
            plyr.health = combat.enemyCombatSequence(ablty, plyr);
            playerInit += 100;
            
            // Check if player was defeated
            if(plyr.health <= 0) break;
        }
    }
}
}
