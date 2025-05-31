import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Enemy {
String name;
int healthPoints, armour, initiative;
double currencyDrop, experienceDrop;
Ability attack1;
public Enemy(String name,int healthPoints, int armour,int initiative,double currencyDrop, double experienceDrop ){
    this.name = name;
    this.healthPoints= healthPoints;
    this.armour=armour;
    this. currencyDrop=currencyDrop;
    this.experienceDrop=experienceDrop;
    this.initiative=initiative;
    
}
 public int rollInitiative() {
        Random rand = new Random();
        int roll = rand.nextInt(this.initiative);
        return roll;
    }
    
    public String toString() {
        return "Enemy(" + name + ")";
    }
     public static Enemy generateEnemies(){
    ArrayList<Enemy> enemyList = new ArrayList<>();
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
                // delay(100);
                // System.out.println("Enemy's health:"+eHp);
                // delay(100);
                // System.out.println("Enemy's armour:"+eArmour);
                // delay(100);
                // System.out.println("Enemy's initiative:"+eInitiative);
                // delay(100);
                
            lineEnemy = readerEnemy.readLine();
            enemyList.add(new Enemy(eName, eHp, eArmour, eInitiative, currencyDrop, experienceDrop));
            }
            readerEnemy.close();

        } catch (FileNotFoundException err) {
            System.out.println("The file 'enemyStats.csv' was not found.");
            err.printStackTrace();
        } catch (IOException err) {
            System.out.println("An error occurred while reading the file.");
            err.printStackTrace();
        }

       int randomIndex = (int) (Math.random() * enemyList.size());
        return enemyList.get(randomIndex);
        
    }
  
}
