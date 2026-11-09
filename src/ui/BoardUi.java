package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Displays the rectangular Minesweeper board using Swing buttons
 * Handles mouse interactions and visual updates for cells, flags, and mines
 */
public class BoardUi extends JPanel {
    private final int cellSize = 55;
    private JButton[][] cells;

    public interface CellClickListener {
        void onCellClicked(int row, int col);
        void onCellRightClicked(int row, int col);
    }

    private CellClickListener cellClickListener;

    protected void fireCellClicked(int row, int col) {
        if (cellClickListener != null) {
            cellClickListener.onCellClicked(row, col);
        }
    }

    protected void fireCellRightClicked(int row, int col) {
        if (cellClickListener != null) {
            cellClickListener.onCellRightClicked(row, col);
        }
    }

    public void setCellClickListener(CellClickListener listener) {
        this.cellClickListener = listener;
    }

    public BoardUi(int rows, int cols) {
        buildBoard(rows, cols);
    }

    // Creates a rectangular grid of buttons and registers mouse listeners.
    public void buildBoard(int rows, int cols) {
        removeAll();
        cells = new JButton[rows][cols];
        Dimension borderSize = new Dimension(cellSize * cols, cellSize * rows);

        setLayout(new GridLayout(rows, cols, 1, 1));
        setPreferredSize(borderSize);
        setMaximumSize(borderSize);
        setMinimumSize(borderSize);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                JButton cell = new JButton();
                final int finalI = i;
                final int finalJ = j;
                cells[i][j] = cell;

                cell.addActionListener(e -> {
                    if (cellClickListener != null) {
                        cellClickListener.onCellClicked(finalI, finalJ);
                    }
                });
                cell.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            if (cellClickListener != null) {
                                cellClickListener.onCellRightClicked(finalI, finalJ);
                            }
                        }
                    }
                });
                add(cell);
            }
        }
        revalidate();
        repaint();
    }

    // Prevent a revealed cell from being pressed again.
    private void makeCellUnclickable(JButton cell) {
        cell.setModel(new DefaultButtonModel() {
            @Override
            public void setPressed(boolean pressed) {
                // Ignore button presses.
            }

            @Override
            public void setArmed(boolean armed) {
                // Prevent the pressed appearance.
            }
        });
    }

    public void makeAllCellsUnclickable() {
        for (JButton[] row : cells) {
            for (JButton cell : row) {
                makeCellUnclickable(cell);
            }
        }
    }

    public void revealCell(int row, int col, int adjacentMines) {
        JButton cell = cells[row][col];

        // Update the appearance of a revealed cell.
        cell.setBackground(new Color(230, 230, 230));
        cell.setContentAreaFilled(true);
        cell.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        cell.setEnabled(false);

        cell.setFont(new Font("SansSerif", Font.BOLD, 20));

        if (adjacentMines > 0) {
            cell.setText(String.valueOf(adjacentMines));

            switch (adjacentMines) {
                case 1 -> cell.setForeground(Color.BLUE);
                case 2 -> cell.setForeground(new Color(0, 128, 0));
                case 3 -> cell.setForeground(Color.RED);
                case 4 -> cell.setForeground(new Color(0, 0, 128));
                default -> cell.setForeground(Color.DARK_GRAY);
            }
        }
        makeCellUnclickable(cell);
    }

    private void displayMine(int row, int col, Color backgroundColor) {
        JButton cell = cells[row][col];

        cell.setText("✹");
        cell.setFont(new Font("SansSerif", Font.BOLD, 20));
        cell.setForeground(Color.BLACK);
        cell.setBackground(backgroundColor);

        makeCellUnclickable(cell);
    }

    public void showMine(int row, int col) {
        displayMine(row, col, new Color(230, 230, 230));
    }

    public void showHitMine(int row, int col) {
        displayMine(row, col, new Color(255, 160, 160));
    }

    public void hideMine(int row, int col) {
        JButton cell = cells[row][col];

        cell.setText("");
        cell.setBackground(null);
        cell.setForeground(null);
        cell.setFont(null);

        // Restore normal click behavior after undo.
        cell.setModel(new DefaultButtonModel());
    }

    public void showFlag(int row, int col) {
        JButton cell = cells[row][col];

        cell.setText("⚑");
        cell.setForeground(new Color(200, 70, 60));
        cell.setFont(new Font("SansSerif", Font.BOLD, 20));
    }

    public void hideFlag(int row, int col) {
        JButton cell = cells[row][col];
        cell.setText("");
        cell.setForeground(null);
    }
}
