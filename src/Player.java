import java.util.*;

public class Player {
    String name, playerClass;
    Integer health, armour, initiativeRange; 
    Double currency, experience;  
    Ability ability1, ability2, ability3;
    
    public Player(Integer hp, Integer ar, Integer ini, String n, String pCl, Double cu, Double xp){
        health = hp;
        armour = ar;
        initiativeRange = ini;
        name = n;
        playerClass = pCl;
        currency = cu;
        experience = xp; 
    }

    public Integer rollInitiative(){
        Integer initiative = java.util.random(this.initiativeRange);

        return initiative;
    }

    public void takeDamage(Enemy.Ability.damage){
        //psuedo code:
        // player's health is reduced by the ability damage minus the player's armour value
        // this.health - (Enemy.Ability.damage - this.armour) 


        // this can also be activated if the player uses a healing ability
        // i.e. damage would be a negative value
        // if(damage < 0){
        // this.health - this.damage;
        // }
        // health will hence go up.
    }


}
