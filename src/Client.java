import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList; //Owen

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
        
            System.out.println(name);
            System.out.println(playerClass);
            System.out.println(maxHP);
            System.out.println(startingArmour);
            System.out.println(initiativeRange);
            System.out.println(startCurr);
            System.out.println(experience);

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

        System.out.println(player1.name + " rolled a " + player1.rollInitiative() + "!");

        Stage stage1 = new Stage(1, player1); //Stage by Owen
        stage1.startStage();

        while (!stage1.isStageOver()) {
            stage1.runTurn();
        }
    }
}
