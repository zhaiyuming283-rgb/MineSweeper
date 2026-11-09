package model;

/**
 * Defines the configuration of each game difficulty.
 */
public enum Difficulty {
    SIMPLE("Simple", 9, 9, 15, BoardType.RECTANGLE),
    ADVANCED("Advanced", 12, 12, 30, BoardType.RECTANGLE),
    INNOVATIVE("Innovative", 10, 10, 20, BoardType.HEXAGON);

    public final String modeName;
    public final int rows;
    public final int cols;
    public final int mineCount;
    public final BoardType boardType;

    Difficulty(String modeName, int rows, int cols,
               int mineCount, BoardType boardType) {
        this.modeName = modeName;
        this.rows = rows;
        this.cols = cols;
        this.mineCount = mineCount;
        this.boardType = boardType;
    }

    @Override
    public String toString() {
        return modeName;
    }
}
