import java.io.BufferedReader;
import java.io.FileReader;

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
        
        // Player player1 = new Player(maxHP, startingArmour, initiativeRange, name, playerClass, startCurr, experience);

    }
}
