package model;

import java.util.Random;

/**
 * Stores the state and core logic of the Minesweeper board
 */
public class BoardModel {
    private int rows;
    private int cols;
    private int mineCount;
    private BoardType boardType;

    private boolean isFirstClick = true;

    private boolean[][] mines;
    private boolean[][] revealed;
    private boolean[][] marked;

    public BoardModel(Difficulty difficulty) {
        this.rows = difficulty.rows;
        this.cols = difficulty.cols;
        this.mineCount = difficulty.mineCount;
        this.boardType = difficulty.boardType;

        this.mines = new boolean[rows][cols];
        this.revealed = new boolean[rows][cols];
        this.marked = new boolean[rows][cols];
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getSafeCellCount() {
        return rows * cols - mineCount;
    }

    public int getRevealedCount() {
        int count = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (revealed[row][col] && !mines[row][col]) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getLeftCount() {
        return getSafeCellCount() - getRevealedCount();
    }

    public int getMarkedCount() {
        int count = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (marked[row][col]) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Randomly places mines while excluding the first clicked cell.
     * Duplicate mine positions are skipped to ensure the required count.
     */
    private void generateMines(int safeRow, int safeCol) {
        Random rand = new Random();
        int placedMineCount = 0;
        while (placedMineCount < mineCount) {
            int row = rand.nextInt(rows);
            int col = rand.nextInt(cols);

            if (row == safeRow && col == safeCol) {
                continue;
            }

            if (!mines[row][col]) {
                mines[row][col] = true;
                placedMineCount++;
            }
        }
    }

    public boolean isMine(int row, int col) {
        return mines[row][col];
    }

    public boolean isRevealed(int row, int col) {
        return revealed[row][col];
    }

    public boolean isMarked(int row, int col) {
        return marked[row][col];
    }

    public boolean isWin() {
        return getLeftCount() == 0;
    }

    public void clickToMark(int row, int col) {
        if (revealed[row][col]) {
            return;
        }
        marked[row][col] = !marked[row][col];
    }

    public void clickToReveal(int row, int col) {
        if (isFirstClick) {
            // Generates mines after the first click so that the player's first selected cell is always safe.
            generateMines(row, col);
            isFirstClick = false;
            // just for console
            // printMines();
        }
    }

    /**
     * Returns neighboring cell coordinates for the selected board type
     * Rectangular cells have 8 neighbors; hexagonal cells have 6
     * The hexagonal board uses an odd-r offset layout, where odd rows are shifted to the right and require different neighbor offsets
     * Coordinates outside the board are filtered by the caller
     */
    private int[][] getNeighbors(int row, int col) {
        if (boardType == BoardType.HEXAGON) {
            if (row % 2 == 0) {
                return new int[][] {
                        {row - 1, col - 1},
                        {row - 1, col},
                        {row, col - 1},
                        {row, col + 1},
                        {row + 1, col - 1},
                        {row + 1, col}
                };
            } else {
                return new int[][] {
                        {row - 1, col},
                        {row - 1, col + 1},
                        {row, col - 1},
                        {row, col + 1},
                        {row + 1, col},
                        {row + 1, col + 1}
                };
            }
        }

        return new int[][] {
                {row - 1, col - 1},
                {row - 1, col},
                {row - 1, col + 1},
                {row, col - 1},
                {row, col + 1},
                {row + 1, col - 1},
                {row + 1, col},
                {row + 1, col + 1}
        };
    }

    public int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int[] neighbor : getNeighbors(row, col)) {
            int r = neighbor[0];
            int c = neighbor[1];

            if (r >= 0 && r < rows &&
                    c >= 0 && c < cols &&
                    mines[r][c]) {
                count++;
            }
        }
        return count;
    }


    /**
     * Reveals a safe cell and recursively expands empty areas
     * Expansion stops at numbered cells, flagged cells, mines, or the board boundaries.
     */
    public void revealCell(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            return;
        }

        if (revealed[row][col] || marked[row][col] || mines[row][col]) {
            return;
        }
        revealed[row][col] = true;

        if (countAdjacentMines(row, col) > 0) {
            return;
        }

        for (int[] neighbor : getNeighbors(row, col)) {
            revealCell(neighbor[0], neighbor[1]);
        }
    }

    public void printMines() {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (mines[row][col]) {
                    System.out.print("* ");
                } else {
                    System.out.print(". ");
                }
            }
            System.out.println();
        }
    }
}
