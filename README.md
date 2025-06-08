A Legend in the Making
Table of Contents
Introduction

Background Story

Features

How to Play

Prerequisites

Compilation

Running the Game

Gameplay Basics

Strategy Guide

General Strategy by Difficulty

Strategy by Class

Project Structure

Source Files

Data Files

Introduction
Welcome to "A Legend in the Making," a classic text-based, turn-based RPG built in Java. Step into the shoes of a lone adventurer and embark on a quest to drive back the darkness threatening the realm. Choose your class, battle hordes of monsters, level up your abilities, and face powerful world bosses in a journey to become a true hero.

This project is a demonstration of core object-oriented programming principles and data-driven game design, with game logic and stats loaded dynamically from CSV files.

Background Story
For generations, the kingdom of Eldoria has known peace. But a creeping shadow has begun to fall across the land, a blight that withers crops and emboldens the monstrous creatures of the wild. It started with whispers—tales of goblins growing bolder, of dark beasts stirring in the deep woods, and of a malevolent intelligence guiding their chaos.

Now, the whispers have become a roar. The roads are no longer safe. Villages on the frontier have fallen silent. The Royal Knights are stretched thin, and a sense of dread hangs heavy in the air. Sages and scholars point to a prophecy: when the three ancient seals weaken, a champion must rise to face the encroaching Gloom and its forgotten king.

You are an adventurer, drawn to the heart of the kingdom by tales of danger and the promise of glory. Whether you are a stalwart Knight, a wise Wizard, or a keen-eyed Archer, your path leads to the front lines of this desperate war. The fate of Eldoria rests on your shoulders. Your legend begins now.

Features
Class-Based System: Choose from three distinct classes, each with a unique resource and playstyle:

Knight: A durable warrior who generates Rage from taking and dealing damage to fuel powerful attacks.

Wizard: A master of the arcane who wields powerful spells by managing a pool of Mana Points (MP).

Archer: A nimble marksman who uses Focus to execute precise and deadly shots.

Turn-Based Tactical Combat: Plan your moves carefully in a strategic, initiative-based combat system.

Dynamic Enemy AI: Enemies make intelligent decisions, choosing to heal allies, buff their team, or exploit player weaknesses.

Deep Progression:

Gain experience to level up, increasing your core stats.

Choose between learning new abilities or upgrading existing ones.

Equip powerful weapons and use consumable items purchased from the shop.

Data-Driven Design: All abilities, character stats, enemy stats, and shop items are loaded from easy-to-edit .csv files, allowing for rapid iteration and balancing.

Robust Status Effects: Inflict or be afflicted by effects like Burn, Poison, Stun, and Guard. A diminishing returns system prevents overpowered stunlocking.

Scaling Difficulty: The game features multiple difficulty settings that adjust enemy stats and mechanics. Enemies also grow stronger as you progress through different worlds.

Town Hub: Between worlds, visit a town hub to heal, shop, and gather narrative clues.

Auto-Battle Mode: A smart auto-battle AI that can handle complex multi-enemy encounters for faster gameplay.

How to Play
1. Prerequisites
You need to have the Java Development Kit (JDK) installed on your system to compile and run the game.

2. Compilation
Navigate to the project's root directory in your terminal.

Make sure all .java files are located in a src folder.

Run the Java compiler:

javac src/*.java -d bin

This command compiles all .java files from the src directory and places the resulting .class files into a bin (binary) directory.

3. Running the Game
After compiling, ensure you are still in the project's root directory.

The required .csv files (abilities.csv, playerStats.csv, etc.) must be in the same root directory.

Run the game using the Client class, making sure to include the bin directory in the classpath:

java -cp bin Client

The game will start in your terminal. Follow the on-screen prompts to begin your adventure!

4. Gameplay Basics
The Goal: Progress through stages and worlds by defeating enemies. After 7 stages in a world, you will face a powerful boss. Defeat the final boss of World 3 to win the game.

Combat: At the start of a stage, you can choose to fight manually (start), let the AI take over (auto), visit the shop, or use an item. In combat, you'll choose from a list of abilities or items to use on your turn.

Progression: After each fight, you gain currency and experience. After clearing a stage, you will recover some health. When you gain enough experience, you will level up, fully restoring your stats and allowing you to upgrade your abilities.

The Town Hub: After defeating a world boss, you'll arrive at an outpost where you can heal, shop, and prepare for the next world.

Strategy Guide
General Strategy by Difficulty
Your approach will need to change as you ramp up the difficulty.

Easy: This mode is for learning the ropes and enjoying the story.

Focus: Experiment with different abilities. You can be aggressive without much punishment.

Economy: Spend currency freely on HP potions and weapon upgrades as they become available.

Combat: Auto-battle is highly effective here. Manually, you can focus on using your highest-damage abilities without much concern for resource management.

Normal: A balanced experience that requires some tactical thought.

Focus: Pay attention to enemy types. Prioritise taking out enemies that can heal or apply dangerous status effects (like Stun or Vulnerable).

Economy: Be more deliberate with your spending. Keep a stock of HP potions, but save up for the major weapon upgrades offered in the shop. The Reset Cooldowns item can be a lifesaver in boss fights.

Combat: Don't use your powerful cooldown abilities on weak enemies. Save them for tougher encounters or when you need to burst down a priority target.

Hard: This mode is a significant challenge that punishes mistakes.

Focus: Resource management is key. Wasting MP, Rage, or Focus will lead to defeat. You must understand when to use your big abilities and when to use your basic, resource-building attacks.

Economy: Every coin matters. The Healer in the Town Hub is expensive, so rely on HP potions. Avoid buying temporary attack buffs and save everything for permanent weapon upgrades and a large stock of potions.

Combat: You will often face multiple enemies. You must focus-fire on one enemy at a time to reduce incoming damage as quickly as possible. Spreading damage around is a recipe for disaster. Use status effects like Slow and Stun to control the battlefield.

Impossible: This mode is for true masters of the game and requires near-perfect play.

Focus: Survival is everything. Defensive abilities like the Knight's Guard Stance or the Wizard's Mana Shield are no longer luxuries—they are essential.

Economy: You will likely not be able to afford everything. Prioritise the first weapon upgrade and then hoard potions. You may need to grind earlier stages for extra currency before tackling a boss.

Combat: You must exploit the diminishing returns system. Plan your stun usage carefully. A single wasted stun on a boss could mean the end of your run. Know every enemy's abilities and anticipate their moves. You cannot afford to be surprised.

Strategy by Class
Knight
The Knight is a frontline brawler who thrives in the heat of battle. Your core mechanic is Rage, which you gain by dealing and taking damage.

Core Strategy: Be aggressive. You need to be in the fight to build Rage, so don't be afraid to take a few hits. Use your basic Slash to build Rage against weaker enemies, then unleash Power Strike on high-health targets or Shield Bash to stun a dangerous foe.

Leveling Up:

Early Game: Focus on upgrading the damage of Slash and Power Strike.

Mid Game: Whirlwind is an excellent AoE for Hard/Impossible. Guard Stance is a must-have for surviving big boss attacks.

Late Game: Last Stand can give you the turn you need to win an otherwise impossible fight. Choose it if you're struggling with survivability.

Stat Priority: Damage > Cooldown Reduction. The more damage you do, the more Rage you build.

Wizard
The Wizard is a glass cannon who controls the battlefield from a distance with powerful spells, all dependent on Mana Points (MP).

Core Strategy: Your MP is your lifeblood. Use the free Mana Dart to finish off low-health enemies or when you're low on MP. Your goal is to manage your MP pool so you always have enough for a critical Fireball or a defensive Mana Shield.

Leveling Up:

Early Game: Upgrade Fireball's damage immediately. Learning Arcane Blast early gives you a strong single-target nuke.

Mid Game: Meteor Strike is one of the best AoE abilities in the game and is a priority. Mana Shield is crucial for higher difficulties.

Late Game: Polymorph is the ultimate control spell. Use it on bosses (before they gain too much resistance) or on a dangerous secondary enemy in a multi-enemy fight.

Stat Priority: Status Chance > Damage. A Wizard's power comes from control. Increasing the chance to Burn, Slow, or Stun is often more valuable than raw damage.

Archer
The Archer is a tactical damage dealer who relies on a regenerating resource, Focus, to deliver high-impact shots.

Core Strategy: Your gameplay is about rhythm. You passively regenerate Focus each turn. Open with a powerful shot like Poison Arrow, then use your basic Arrow Shot for a turn or two to let your Focus recover before using another special ability.

Leveling Up:

Early Game: Poison Arrow is fantastic; upgrading its status chance makes early fights much easier.

Mid Game: Piercing Shot and Rapid Fire offer different tactical options (high damage vs. low cooldown). Volley is a solid AoE choice if you find yourself struggling with groups.

Late Game: Called Shot is your boss-killer. The Vulnerable status it applies will dramatically increase your party's damage output. It's a top-tier ability.

Stat Priority: Damage > Status Chance. As an Archer, your primary role is to eliminate threats quickly. While poison is useful, your main contribution is high, consistent damage.

Project Structure
The project is organized into source files and data files.

Source Files (src/)
Client.java: The main entry point of the application. Contains the main game loop, player setup, and high-level game flow logic.

Player.java: Defines the player character, including their stats, abilities, inventory, and class-specific resources.

Enemy.java: Defines enemy characters, including their stats, abilities, and combat AI.

Ability.java: Defines the structure of a single ability, including its damage, cost, cooldown, and effects.

Combat.java: Manages the turn-by-turn logic of a combat encounter, including turn order, action handling, and status updates.

AutoBattle.java: Implements the AI for automatically resolving combat encounters.

Shop.java: Handles the logic for the item shop, including loading items and processing purchases.

AbilityFactory.java: A factory class responsible for creating Ability objects from the abilities.csv data file.

EnemyAbilityLoader.java: Loads and assigns ability sets to enemies from enemy_abilities.csv.

Difficulty.java & DifficultyManager.java: An enum and manager class to handle different game difficulty levels.

Stage.java: A container for the state of a single stage, including the player and the list of enemies.

Data Files (.csv)
abilities.csv: A database of all abilities in the game.

playerStats.csv: Contains the base stats for each player class.

enemyStats.csv: A database of all base enemy types.

enemy_abilities.csv: Maps which abilities each enemy type can use.

shop.csv: A list of all items available for purchase in the shop.