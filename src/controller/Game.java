package controller;

import model.BoardModel;
import model.Difficulty;
import ui.BoardUi;
import ui.MiniTimer;
import model.BoardType;
import ui.HexBoardUi;

import javax.swing.*;
import java.awt.*;

/**
 * Controls the main Minesweeper game
 */
public class Game {
    private JFrame mainFrame;
    private MiniTimer miniTimer;
    private JButton pauseBtn;

    private boolean isPaused = false;
    private boolean undoUsed = false;
    private boolean isGameOver = false;

    private Difficulty difficulty = Difficulty.SIMPLE;

    private JLabel revealedLabel;
    private JLabel leftLabel;
    private JLabel markedLabel;

    private BoardUi boardUi;
    private BoardModel boardModel;

    private JPanel bottomPanel;
    private JLayeredPane boardLayer;
    private JPanel pauseOverlay;

    public Game() {
        boardModel = new BoardModel(difficulty);
        buildGame();
    }

    // ---------- UI Construction ----------
    // Builds the main window and organizes the game panels.
    private void buildGame() {
        mainFrame = new JFrame("MineSweeper");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 10, 20));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buildTopPanel(topPanel);

        JPanel mediumPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buildMediumPanel(mediumPanel);

        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        buildBottomPanel(bottomPanel);

        topPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        mediumPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        leftPanel.add(topPanel);
        leftPanel.add(mediumPanel);
        leftPanel.add(Box.createVerticalStrut(25));
        leftPanel.add(bottomPanel);

        JPanel rightPanel = new JPanel(new BorderLayout());
        JPanel rulesPanel = new JPanel();
        rulesPanel.setLayout(new BoxLayout(rulesPanel, BoxLayout.Y_AXIS));

        buildRulesPanel(rulesPanel);

        rightPanel.add(Box.createVerticalStrut(20), BorderLayout.NORTH);
        rightPanel.add(rulesPanel, BorderLayout.CENTER);

        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(Box.createHorizontalStrut(30), BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        mainFrame.add(mainPanel);
        mainFrame.pack();
        mainFrame.setMinimumSize(mainFrame.getSize());
    }

    private void buildTopPanel(JPanel topPanel) {
        JPanel difficultyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<Difficulty> difficultyComboBox = new JComboBox<>(Difficulty.values());

        difficultyComboBox.setSelectedItem(difficulty);
        difficultyComboBox.addActionListener(e -> {
            difficulty = (Difficulty) difficultyComboBox.getSelectedItem();
            // Start a new game whenever the difficulty changes.
            restartGame();
        });

        difficultyPanel.add(new JLabel("Difficulty: "));
        difficultyPanel.add(difficultyComboBox);

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timePanel.add(new JLabel("Time: "));
        miniTimer = new MiniTimer();
        timePanel.add(miniTimer);

        topPanel.add(difficultyPanel);
        topPanel.add(timePanel);
    }

    private void buildMediumPanel(JPanel mediumPanel) {
        JPanel revealedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        revealedPanel.add(new JLabel("Revealed: "));
        revealedLabel = new JLabel(String.valueOf(boardModel.getRevealedCount()));
        revealedPanel.add(revealedLabel);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.add(new JLabel("Left: "));

        leftLabel = new JLabel(String.valueOf(boardModel.getLeftCount()));
        leftPanel.add(leftLabel);

        JPanel markedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        markedPanel.add(new JLabel("Marked: "));
        markedLabel = new JLabel(String.valueOf(boardModel.getMarkedCount()));
        markedPanel.add(markedLabel);

        mediumPanel.add(revealedPanel);
        mediumPanel.add(leftPanel);
        mediumPanel.add(markedPanel);
    }

    // Selects a rectangular or hexagonal board based on the difficulty.
    private BoardUi createBoardUi() {
        if (difficulty.boardType == BoardType.HEXAGON) {
            return new HexBoardUi(
                    boardModel.getRows(),
                    boardModel.getCols()
            );
        }

        return new BoardUi(
                boardModel.getRows(),
                boardModel.getCols()
        );
    }

    private void buildBottomPanel(JPanel bottomPanel) {
        boardUi = createBoardUi();
        boardUi.setCellClickListener(new BoardUi.CellClickListener() {
            @Override
            public void onCellClicked(int row, int col) {
                handleCellClick(row, col);
            }

            @Override
            public void onCellRightClicked(int row, int col) {
                handleCellRightClick(row, col);
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 15));
        buildButtonPanel(buttonPanel);
        boardLayer = createBoardLayer();

        bottomPanel.add(boardLayer);
        bottomPanel.add(buttonPanel);
    }

    private void buildRulesPanel(JPanel rulesPanel) {
        rulesPanel.add(new JLabel("Rules:"));
        rulesPanel.add(new JLabel("• Left-click to reveal a cell; right-click to flag or unflag it."));
        rulesPanel.add(new JLabel("• Numbers show how many adjacent cells contain mines."));
        rulesPanel.add(new JLabel("• Your first revealed cell is always safe."));
        rulesPanel.add(new JLabel("• Simple mode allows one undo after hitting a mine."));
        rulesPanel.add(new JLabel("• Innovative mode uses hexagonal cells with up to 6 neighbors."));
        rulesPanel.add(new JLabel("• Use Pause to pause the game, or Reset to start over."));
        rulesPanel.add(new JLabel("• Reveal all safe cells to win!"));
    }

    private void buildButtonPanel(JPanel buttonPanel) {
        pauseBtn = new JButton("PAUSE");
        JButton resetBtn = new JButton("RESET");

        Dimension buttonSize = new Dimension(90, 25);

        pauseBtn.setPreferredSize(buttonSize);
        resetBtn.setPreferredSize(buttonSize);

        buttonPanel.add(pauseBtn);
        buttonPanel.add(resetBtn);

        // Toggles the timer and overlay; pause is disabled after game over.
        pauseBtn.addActionListener(e -> {
            if (isGameOver) {
                return;
            }
            if (isPaused) {
                miniTimer.startTimer();
                isPaused = false;
                pauseBtn.setText("PAUSE");
                pauseOverlay.setVisible(false);
            } else {
                miniTimer.pauseTimer();
                isPaused = true;
                pauseBtn.setText("RESUME");
                pauseOverlay.setVisible(true);
            }
        });

        resetBtn.addActionListener(e -> {
            restartGame();
        });
    }

    // Places the board and pause overlay in separate layers.
    // The overlay blocks board interaction without changing the layout.
    private JLayeredPane createBoardLayer() {
        JLayeredPane layer = new JLayeredPane();

        Dimension size = boardUi.getPreferredSize();
        layer.setPreferredSize(size);
        layer.setMinimumSize(size);
        layer.setMaximumSize(size);

        boardUi.setBounds(0, 0, size.width, size.height);
        layer.add(boardUi, JLayeredPane.DEFAULT_LAYER);

        pauseOverlay = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(235, 240, 248, 175));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };

        pauseOverlay.setOpaque(false);

        JLabel message = new JLabel(
                "<html><div style='text-align:center;'>"
                        + "<b>GAME PAUSED</b>"
                        + "<br>Click RESUME to continue"
                        + "</div></html>",
                SwingConstants.CENTER
        );

        message.setFont(new Font("SansSerif", Font.BOLD, 17));
        message.setForeground(new Color(55, 75, 100));
        message.setOpaque(true);
        message.setBackground(new Color(250, 252, 255));
        message.setBorder(
                BorderFactory.createEmptyBorder(18, 25, 18, 25)
        );

        pauseOverlay.add(message);
        pauseOverlay.setBounds(0, 0, size.width, size.height);
        pauseOverlay.setVisible(false);

        // The overlay also intercepts mouse input while paused.
        pauseOverlay.addMouseListener(new java.awt.event.MouseAdapter() {});

        layer.add(pauseOverlay, JLayeredPane.PALETTE_LAYER);

        return layer;
    }

    // ---------- User Interactions ----------
    // Handles a left-click and updates the game according to the selected cell.
    private void handleCellClick(int row, int col) {
        // Ignore clicks when the game is paused or finished,
        // or when the selected cell is already revealed or flagged.
        if (isPaused || isGameOver ||
                boardModel.isRevealed(row, col) ||
                boardModel.isMarked(row, col)) {
            return;
        }
        boardModel.clickToReveal(row, col);

        if (boardModel.isMine(row, col)) {
            boardUi.showHitMine(row, col);
            miniTimer.pauseTimer();
            // Simple mode allows one undo after the first mine hit.
            if (difficulty == Difficulty.SIMPLE && !undoUsed) {
                int choice = JOptionPane.showConfirmDialog(
                        mainFrame,
                        "You hit a mine!\n"
                                + "Since this is your first mistake, "
                                + "you can undo this move once.\n\n"
                                + "Would you like to undo?",
                        "Mine!",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (choice == JOptionPane.YES_OPTION) {
                    boardUi.hideMine(row, col);
                    undoUsed = true;
                    miniTimer.startTimer();
                    return;
                }
            }

            // Game over if undo is unavailable or declined.
            showAllMines(row, col);
            showEndDialog(false);
        } else {
            boardModel.revealCell(row, col);
            updateRevealedCells();

            if (boardModel.isWin()) {
                showEndDialog(true);
            }
        }
    }

    // Handles a right-click to mark or unmark a cell.
    private void handleCellRightClick(int row, int col) {
        if (isPaused || isGameOver || boardModel.isRevealed(row, col)) {
            return;
        }
        boardModel.clickToMark(row, col);

        if (boardModel.isMarked(row, col)) {
            boardUi.showFlag(row, col);
        } else {
            boardUi.hideFlag(row, col);
        }

        markedLabel.setText(String.valueOf(boardModel.getMarkedCount()));
    }

    // ---------- Game State Updates ----------
    // Reveals all mines after a loss.
    // The clicked mine is skipped because it is already shown separately in red.
    private void showAllMines(int clickRow, int clickCol) {
        for (int row = 0; row < boardModel.getRows(); row++) {
            for (int col = 0; col < boardModel.getCols(); col++) {
                if (row == clickRow && col == clickCol) {
                    continue;
                }
                if (boardModel.isMine(row, col)) {
                    boardUi.showMine(row, col);
                }
            }
        }
    }

    // Synchronizes the UI with all cells revealed by the model,
    // including cells opened automatically by recursive revealing.
    private void updateRevealedCells() {
        for (int row = 0; row < boardModel.getRows(); row++) {
            for (int col = 0; col < boardModel.getCols(); col++) {
                if (boardModel.isRevealed(row, col) && !boardModel.isMine(row, col)) {
                    int adjacentMines = boardModel.countAdjacentMines(row, col);
                    boardUi.revealCell(row, col, adjacentMines);
                }
            }
        }
        revealedLabel.setText(String.valueOf(boardModel.getRevealedCount()));
        leftLabel.setText(String.valueOf(boardModel.getLeftCount()));
    }

    // Handles both win and loss states.
    // Players can restart immediately or keep the finished board for review.
    private void showEndDialog(boolean won) {
        isGameOver = true;
        pauseBtn.setEnabled(false);
        miniTimer.pauseTimer();

        if (!(boardUi instanceof HexBoardUi)) {
            boardUi.makeAllCellsUnclickable();
        }
        String title = won ? "You Win!" : "Game Over";
        String message = won
                ? "Congratulations! You cleared all safe cells!\n"
                + "Choose View Board to review your result."
                : "You hit a mine!\n"
                + "Choose View Board to review the mines.\n"
                + "Click RESET when you're ready to play again.";

        Object[] options = {"Play Again", "View Board"};

        int choice = JOptionPane.showOptionDialog(
                mainFrame,
                message,
                title,
                JOptionPane.DEFAULT_OPTION,
                won ? JOptionPane.INFORMATION_MESSAGE
                        : JOptionPane.ERROR_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            restartGame();
        }
    }

    // ---------- Reset ----------
    // Resets all game state and rebuilds the board
    // using the currently selected difficulty.
    private void resetGameData() {
        isPaused = false;
        undoUsed = false;
        isGameOver = false;
        bottomPanel.remove(boardLayer);

        pauseBtn.setEnabled(true);
        pauseBtn.setText("PAUSE");

        boardModel = new BoardModel(difficulty);

        // Create the appropriate board for this difficulty.
        boardUi = createBoardUi();
        boardUi.setCellClickListener(new BoardUi.CellClickListener() {
            @Override
            public void onCellClicked(int row, int col) {
                handleCellClick(row, col);
            }

            @Override
            public void onCellRightClicked(int row, int col) {
                handleCellRightClick(row, col);
            }
        });

        boardLayer = createBoardLayer();
        bottomPanel.add(boardLayer, 0);

        revealedLabel.setText(String.valueOf(boardModel.getRevealedCount()));
        leftLabel.setText(String.valueOf(boardModel.getLeftCount()));
        markedLabel.setText(String.valueOf(boardModel.getMarkedCount()));

        bottomPanel.revalidate();
        mainFrame.pack();
        mainFrame.repaint();
    }

    private void restartGame() {
        resetGameData();
        miniTimer.resetTimer();
    }

    public void show() {
        mainFrame.setVisible(true);
    }
}