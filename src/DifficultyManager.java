public class DifficultyManager {
    private static Difficulty current = Difficulty.EASY;

    public static void setDifficulty(Difficulty d) {
        current = d;
    }

    public static Difficulty getDifficulty() {
        return current;
    }
}
