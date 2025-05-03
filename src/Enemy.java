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

}
