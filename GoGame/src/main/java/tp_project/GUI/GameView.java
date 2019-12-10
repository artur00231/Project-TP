package tp_project.GUI;

import tp_project.GoGameLogic.GoGame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class GameView extends JPanel {
    private GoGame.Player player_color;

    private GoGame go_game;
    private Board board;
    private int size;
    private JButton pass_button = new JButton("Pass");
    private JButton give_up_button = new JButton("Give up");
    private ControlPanel control_panel = new ControlPanel();

    public GameView(int size, GoGame.Player player_color) {
        go_game = new GoGame(size);
        this.size = size;
        this.player_color = player_color;

        board = new Board();
        board.set(go_game.getBoard());

        this.setLayout(new BorderLayout());
        this.add(board, BorderLayout.CENTER);
        this.add(control_panel, BorderLayout.EAST);
    }

    private class Board extends JPanel{
        JPanel inner_panel = new JPanel();
        Cell[][] board;

        public Board() {
            inner_panel.setLayout(new GridLayout(size, size));
            inner_panel.setBackground(Color.GRAY);
            this.setBorder(new EmptyBorder(0,0,0,0));
            this.setBackground(Color.BLACK);
            this.add(inner_panel);

            board = new Cell[size][size];
            for (int y = 0; y < size; ++y) {
                for (int x = 0; x < size; ++x) {
                    board[y][x] = new Cell(x, y);
                    inner_panel.add(board[y][x]);
                }
            }
            this.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    Dimension d = e.getComponent().getSize();
                    int m = Math.min(d.width, d.height) - 10;
                    m = m - m % size;
                    inner_panel.setPreferredSize(new Dimension(m, m));
                }
            });
        }

        public void set(GoGame.Cell[][] board) {
            for (int i = 0; i < size; ++i) {
                for (int j = 0; j < size; ++j) {
                    this.board[i][j].cell_state = board[i][j];
                }
            }
            repaint();
        }

        private class Cell extends JPanel{
            int x, y;
            boolean paint_preview;
            GoGame.Cell cell_state;

            public Cell(int x, int y) {
                this.x = x;
                this.y = y;
                this.setOpaque(false);
                paint_preview = false;

                this.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (go_game.isLegal(new GoGame.Move(x, y, player_color))) paint_preview = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        paint_preview = false;
                        repaint();
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (go_game.makeMove(new GoGame.Move(x, y, player_color))) {
                            player_color = player_color.getOpponent();
                            set(go_game.getBoard());
                            paint_preview = false;
                        }
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
                if (!this.cell_state.equals(GoGame.Cell.EMPTY)) {
                    if (this.cell_state.equals(GoGame.Cell.WHITE))
                        g2d.setColor(Color.WHITE);
                    else
                        g2d.setColor(Color.BLACK);
                    g2d.fill(new Ellipse2D.Double(0, 0, this.getWidth(), this.getHeight()));
                } else {
                    g2d.setColor(Color.BLACK);
                    Point2D.Double p = new Point2D.Double(this.getWidth() / 2.0, this.getHeight() / 2.0);
                    if (this.x > 0) g2d.draw(new Line2D.Double(p.getX(), p.getY(), 0, p.getY()));
                    if (this.x < size - 1) g2d.draw(new Line2D.Double(p.getX(), p.getY(), this.getWidth(), p.getY()));
                    if (this.y > 0) g2d.draw(new Line2D.Double(p.getX(), p.getY(), p.getX(), 0));
                    if (this.y < size - 1) g2d.draw(new Line2D.Double(p.getX(), p.getY(), p.getX(), this.getHeight()));
                }

                if (this.paint_preview) {
                    if (player_color.equals(GoGame.Player.BLACK))
                        g2d.setColor(new Color(0, 0, 0, 127));
                    else
                        g2d.setColor(new Color(255, 255, 255, 127));
                    g2d.fill(new Ellipse2D.Double(0, 0, this.getWidth(), this.getHeight()));
                }
            }
        }
    }

    private class ControlPanel extends JPanel {
        public ControlPanel() {
            this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            this.add(Box.createGlue());
            this.add(pass_button);
            this.add(give_up_button);
        }
    }
}
