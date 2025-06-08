// DifficultyManager.java

public class DifficultyManager {
    private static Difficulty current = Difficulty.EASY;

    public static void setDifficulty(Difficulty d) {
        current = d;
        System.out.println("> Difficulty set to " + d.name());
    }

    public static Difficulty getDifficulty() {
        return current;
    }
}