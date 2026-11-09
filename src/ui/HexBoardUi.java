package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Displays the Minesweeper board using hexagonal cells
 * Handles hexagon rendering,mouse interactions and visual updates for cells, flags, and mines
 */
public class HexBoardUi extends BoardUi {
    private static final int RADIUS = 26;
    private static final int MARGIN = 35;

    private final int rows;
    private final int cols;
    private final Polygon[][] hexagons;

    private boolean[][] revealed;
    private boolean[][] flagged;
    private boolean[][] mines;
    private boolean[][] hitMines;
    private int[][] numbers;

    public HexBoardUi(int rows, int cols) {
        super(rows, cols);

        this.rows = rows;
        this.cols = cols;
        this.hexagons = new Polygon[rows][cols];

        this.revealed = new boolean[rows][cols];
        this.flagged = new boolean[rows][cols];
        this.mines = new boolean[rows][cols];
        this.hitMines = new boolean[rows][cols];
        this.numbers = new int[rows][cols];

        // Replace the rectangular button layout.
        removeAll();
        setLayout(null);

        // Calculate spacing for pointy-top hexagons.
        // Odd rows are shifted horizontally by half a cell width.
        double width = Math.sqrt(3) * RADIUS;
        double rowSpacing = 1.5 * RADIUS;

        int panelWidth = (int) Math.ceil( MARGIN * 2 + width * (cols + 0.5));
        int panelHeight = (int) Math.ceil( MARGIN * 2 + rowSpacing * (rows - 1) + 2 * RADIUS);

        setPreferredSize(new Dimension(panelWidth, panelHeight));
        setMinimumSize(getPreferredSize());
        setMaximumSize(getPreferredSize());

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double cx = MARGIN + width / 2 + col * width + (row % 2) * width / 2;
                double cy = MARGIN + RADIUS + row * rowSpacing;
                hexagons[row][col] = createHexagon(cx, cy);
            }
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int[] cell = findCell(e.getPoint());

                if (cell == null) return;

                if (SwingUtilities.isLeftMouseButton(e)) {
                    fireCellClicked(cell[0], cell[1]);
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    fireCellRightClicked(cell[0], cell[1]);
                }
            }
        });
    }

    /**
     * Creates a pointy-top hexagon centered at (cx, cy).
     * Its six vertices are calculated using 60-degree intervals.
     */
    private Polygon createHexagon(double cx, double cy) {
        Polygon polygon = new Polygon();

        for (int i = 0; i < 6; i++) {
            double angle = Math.toRadians(60 * i - 90);
            int x = (int) Math.round( cx + RADIUS * Math.cos(angle));
            int y = (int) Math.round( cy + RADIUS * Math.sin(angle));
            polygon.addPoint(x, y);
        }
        return polygon;
    }

    /**
     * Finds the hexagonal cell containing the mouse position.
     * Returns its row and column, or null if no cell was clicked.
     */
    private int[] findCell(Point point) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (hexagons[row][col].contains(point)) {
                    return new int[]{row, col};
                }
            }
        }
        return null;
    }

    // Custom rendering of hexagonal cells, including backgrounds, borders, numbers, flags, and mines.
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {

                Polygon hex = hexagons[row][col];

                // 1. Background color based on cell state
                if (hitMines[row][col]) {
                    g2.setPaint(new Color(255, 160, 160));
                } else if (revealed[row][col] || mines[row][col]) {
                    g2.setPaint(new Color(230, 230, 230));
                } else {
                    Rectangle bounds = hex.getBounds();
                    g2.setPaint(new GradientPaint(
                            0, bounds.y, Color.WHITE,
                            0, bounds.y + bounds.height,
                            new Color(190, 215, 245)
                    ));
                }
                g2.fillPolygon(hex);

                // 2. Hexagonal border
                g2.setColor(new Color(95, 135, 180));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawPolygon(hex);

                // 3. Cell content
                String text = "";
                if (mines[row][col]) {
                    text = "✹";
                } else if (flagged[row][col]) {
                    text = "⚑";
                } else if (revealed[row][col]
                        && numbers[row][col] > 0) {
                    text = String.valueOf(numbers[row][col]);
                }

                if (!text.isEmpty()) {
                    if (mines[row][col]) {
                        g2.setColor(Color.BLACK);
                    } else if (flagged[row][col]) {
                        g2.setColor(new Color(200, 70, 60));
                    } else {
                        switch (numbers[row][col]) {
                            case 1 -> g2.setColor(Color.BLUE);
                            case 2 -> g2.setColor(new Color(0, 128, 0));
                            case 3 -> g2.setColor(Color.RED);
                            case 4 -> g2.setColor(new Color(0, 0, 128));
                            default -> g2.setColor(Color.DARK_GRAY);
                        }
                    }

                    g2.setFont(new Font("SansSerif", Font.BOLD, 20));

                    FontMetrics fm = g2.getFontMetrics();
                    Rectangle bounds = hex.getBounds();

                    int textX = bounds.x + (bounds.width - fm.stringWidth(text)) / 2;
                    int textY = bounds.y + (bounds.height - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(text, textX, textY);
                }
            }
        }
        g2.dispose();
    }

    @Override
    public void revealCell(int row, int col, int adjacentMines) {
        revealed[row][col] = true;
        flagged[row][col] = false;
        numbers[row][col] = adjacentMines;
        repaint();
    }

    @Override
    public void showMine(int row, int col) {
        mines[row][col] = true;
        repaint();
    }

    @Override
    public void showHitMine(int row, int col) {
        mines[row][col] = true;
        hitMines[row][col] = true;
        repaint();
    }

    @Override
    public void hideMine(int row, int col) {
        mines[row][col] = false;
        hitMines[row][col] = false;
        repaint();
    }

    @Override
    public void showFlag(int row, int col) {
        flagged[row][col] = true;
        repaint();
    }

    @Override
    public void hideFlag(int row, int col) {
        flagged[row][col] = false;
        repaint();
    }

    // Hexagonal Board Test
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Hexagonal Board Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            HexBoardUi board = new HexBoardUi(10, 10);
            board.revealCell(2, 3, 2);
            board.revealCell(2, 4, 0);
            board.revealCell(3, 3, 1);
            board.showFlag(4, 5);
            board.showMine(5, 6);
            board.showHitMine(6, 6);

            frame.add(board);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
