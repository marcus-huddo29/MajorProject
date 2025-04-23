import java.io.BufferedReader;
import java.io.FileReader;

public class Client {
    public static void main(String[] args) throws Exception {
        
        Integer maxHP, startingArmour, initiativeRange;
        Double startCurr, experience;
        String name, playerClass;

        // activate a reader object to interface with the csv
        BufferedReader readerStats = new BufferedReader(new FileReader("playerStats.csv"));

        String lineStats = readerStats.readLine();
        lineStats = readerStats.readLine();

        // parse through playerStats csv collecting the initial stats from the columns
        while(lineStats != null){
            String[] stats = lineStats.split(",");
            maxHP = Integer.parseInt(stats[0]);
            startingArmour = Integer.parseInt(stats[1]);
            initiativeRange = Integer.parseInt(stats[2]);
            startCurr = Double.parseDouble(stats[3]);
            experience = Double.parseDouble(stats[4]);
        }
        //close reader object once we're done
        readerStats.close();

        // activate a reader object to interface with the csv
        BufferedReader readerName = new BufferedReader(new FileReader("playerNameClass.csv"));

        String lineName = readerStats.readLine();
        lineName = readerStats.readLine();

        // parse through playerNameClass csv collecting the entries from the columns
        while(lineName != null){
            String[] names = lineName.split(",");
            name = names[0];
            playerClass = names[1];
        }
        //close reader object once we're done
        readerName.close();


        Player player1 = new Player(maxHP, startingArmour, initiativeRange, name, playerClass, startCurr, experience);
        
    }
}
