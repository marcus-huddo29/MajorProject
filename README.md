 Major Project Documentation: A Legend in the Making
1. Group Contribution Table
This table outlines the contributions of each team member, as required by the assignment specification for mark allocation. 



Name	Percentage Contribution	Modules & Files Contributed	Notes
Owen	40%	Ability.java, AbilityFactory.java, EnemyAbilityLoader.java, Stage.java, Shop.java, Client.java	Designed and implemented the complete logic for player/enemy abilities from CSV files, stage management, and the item shop functionality.
Marcus	30%	Player.java, Client.java, README.md	Designed the foundational Player class, the core combat cycle, the initial UML layout, and wrote the project's background story and initial documentation.
Ken	30%	Enemy.java, Combat.java, Client.java	Built the core Enemy class functionality, including the combat AI logic (chooseBestAbility), data integration from enemyStats.csv, and the turn-based logic in Combat.java. 


Export to Sheets
2. Project Overview
Title: A Legend in the Making
Topic: This project is an implementation of Topic 1: Masters of MQ: Turn-Based RPG Combat. 
Introduction: Welcome to "A Legend in the Making," a classic text-based, turn-based RPG built in Java. Players step into the shoes of a lone adventurer to drive back the darkness threatening the realm. Choose your class, battle hordes of monsters, level up your abilities, and face powerful world bosses in a journey to become a true hero. This project demonstrates core object-oriented programming principles and data-driven game design, with game logic and stats loaded dynamically from CSV files.
3. How to Run the Program
These instructions explain how to compile and run the project from a terminal, as required by the assignment specification. 

Prerequisites: Ensure you have the Java Development Kit (JDK) installed on your system.
Navigate to Directory: Open a terminal or command prompt and navigate to the project's root directory (the one containing the src folder and the .csv files).
Compilation: Run the Java compiler to compile all source files from the src directory into the bin directory.
Bash

javac src/*.java -d bin
Execution: Run the game using the Client class, ensuring the bin folder is in the classpath so the compiled files can be found. The required .csv files must be in the root directory.
Bash

java -cp bin Client
The game will start in your terminal. Follow the on-screen prompts to begin.

4. Class Structure & Design (UML Diagram)
The project is structured around a set of classes that manage the game state, characters, and logic. The design fulfills all scope requirements, including class composition, use of ArrayList, File I/O, and a recursive data structure. 


Visual UML Diagram Representation:

[Client] -> [Combat]
[Client] -> [Shop]
[Client] -> [Player]
[Client] -> [Stage]

[Stage] o-- "1" [Player]
[Stage] o-- "many" [Enemy]

[Combat] uses -- [Player]
[Combat] uses -- [Enemy]

[Player] o-- "many" [Ability]
[Player] o-- "1" [StatusEffectNode]  // Recursive Structure

[Enemy] o-- "many" [Ability]
[Enemy] o-- "1" [StatusEffectNode]   // Recursive Structure

[StatusEffectNode] o-- "1" [StatusEffectNode] // Recursive Link

[AbilityFactory] creates --> [Ability]
[EnemyAbilityLoader] uses --> [AbilityFactory]
Key Class Definitions:

Client.java: The main entry point and game loop orchestrator.  It handles player setup, progression between stages, and transitions to combat or the shop.
Player.java / Enemy.java: Manage the state of characters. They contain an ArrayList of Ability objects, satisfying one scope requirement. 
Stage.java: A container class that holds the state for a single level, including a Player object and an ArrayList<Enemy>. This satisfies the requirements for a class to contain both a single object and an ArrayList of objects of other user-defined classes. 
StatusEffectNode.java: This class represents a single status effect (like "Poison"). It contains a field, next, which is another StatusEffectNode. This self-referential design forms a linked list, fulfilling the recursive data structure requirement.
Factory & Loader Classes: AbilityFactory.java and EnemyAbilityLoader.java decouple game logic from data, reading .csv files to create Ability objects dynamically. This fulfills the File I/O requirement. 
5. Analysis of Methods
This section provides a detailed analysis of two methods against alternative implementations, focusing on design principles and efficiency, as required for full documentation marks. 

Method 1: Enemy.chooseBestAbility(Player player, List<Enemy> allies)
Current Implementation: This method uses a switch statement based on the enemy's aiType string ("Aggressive", "Defensive", etc.) to select different logic paths for choosing an ability. This is a straightforward, procedural approach.
Alternative (Strategy Design Pattern): A more advanced alternative would be to use the Strategy design pattern.
Define an AIStrategy interface with a method: chooseAbility(...).
Create concrete classes like AggressiveStrategy and DefensiveStrategy, each implementing the interface.
The Enemy class would hold an object of this interface (private AIStrategy strategy;) instead of a string. The chooseBestAbility method would then simply delegate the call: return this.strategy.chooseAbility(...).
Analysis: While the current implementation is simple, it violates the Open/Closed Principle. To add a new AI type, the Enemy class itself must be modified. The Strategy pattern is far more extensible, allowing new AI behaviours to be added by creating new classes without touching existing code. This makes the design more robust and maintainable.
Method 2: Client.generateStageEnemies(...)
Current Implementation: This method uses hard-coded List<String> collections within the Client.java source file to define which enemies can appear in each world. It then filters a master enemy list based on these hard-coded lists.
Alternative (Data-Driven Configuration): A more flexible approach would be to move this configuration into the data files. We could add minWorld and maxWorld columns to enemyStats.csv. The generateStageEnemies method would then read this file and dynamically create the enemy pool by filtering for enemies where the current worldNumber falls between their minWorld and maxWorld.
Analysis: The current approach mixes configuration data with application logic, making it rigid. The data-driven alternative decouples content from code. A game designer could easily balance the game or adjust enemy placements by simply editing the enemyStats.csv spreadsheet, requiring no code changes. This dramatically improves the development workflow and makes the overall system more modular.
6. Game Features & Strategy Guide
Features
Class-Based System: Choose from three distinct classes: the durable Knight (uses Rage), the arcane Wizard (uses Mana), or the tactical Archer (uses Focus).
Turn-Based Tactical Combat: Plan your moves carefully in a strategic, initiative-based combat system. 
Dynamic Enemy AI: Enemies make intelligent decisions, choosing to heal allies, buff their team, or exploit player weaknesses based on their AI type.
Deep Progression: Gain experience to level up, choose new abilities, and purchase powerful weapons from the shop.
Data-Driven Design: All abilities, character stats, and shop items are loaded from easy-to-edit .csv files.
Robust Status Effects: Inflict or be afflicted by effects like Burn, Poison, and Stun, managed by a recursive linked-list structure.
Strategy Guide
Hard Difficulty: Resource management is key. You must focus-fire on one enemy at a time to reduce incoming damage as quickly as possible. Use status effects like Stun to control the battlefield.
Impossible Difficulty: Survival is everything. Defensive abilities are essential. You must exploit the diminishing returns system on status effects and anticipate every enemy move.
Knight: Be aggressive to build Rage. Use your basic Slash to build resources, then unleash Power Strike on high-health targets or Shield Bash to stun a dangerous foe.
Wizard: Your MP is your lifeblood. Use the free Mana Dart to finish off weak enemies and conserve MP for critical spells like Fireball or Meteor Strike.
Archer: Your gameplay is about rhythm. You passively regenerate Focus. Open with a powerful shot like Poison Arrow, then use basic attacks to recover Focus before using another special ability.