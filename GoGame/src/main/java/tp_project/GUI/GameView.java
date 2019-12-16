package tp_project.GUI;

import tp_project.GoGame.GoBoard;
import tp_project.GoGame.GoMove;
import tp_project.GoGame.GoPlayerListener;
import tp_project.GoGame.GoRemotePlayer;
import tp_project.GoGame.GoStatus;
import tp_project.GoGame.GoMove.TYPE;
import tp_project.GoGameLogic.GoGameLogic;
import tp_project.GoGameLogic.GoGameLogic.Cell;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

class GoPlayerThread implements Runnable {
    GoRemotePlayer player;
    boolean running = true;

    public GoPlayerThread (GoRemotePlayer player) {
        this.player = player;
    }

    @Override
    public void run() {
        while (true) {
            if (!running)
                return;
                player.update();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public void stop() {
        running = false;
    }
}

public class GameView extends JPanel {
    private GoGameLogic.Player player_color;
    public enum ACTION { END };

    private GoGameLogic go_game;
    private Board board;
    private int size;
    private JButton pass_button = new JButton("Pass");
    private JButton give_up_button = new JButton("Give up");
    private ControlPanel control_panel = new ControlPanel();
    private GoRemotePlayer player;
    private ActionListener action_listener;
    private GoPlayerThread thread;

    public GameView(GoRemotePlayer player, int size, GoGameLogic.Player player_color, ActionListener a) {
        go_game = new GoGameLogic(size);
        this.size = size;
        this.player_color = player_color;
        this.player = player;

        board = new Board();
        board.set(go_game.getBoard());

        this.setLayout(new BorderLayout());
        this.add(board, BorderLayout.CENTER);
        this.add(control_panel, BorderLayout.EAST);

        player.setListener(new GoPlayerListener(){
        
            @Override
            public void updated() {
                if (!player.isGameRunnig()) {
                    GoStatus status = player.getLastStatus();
                    thread.stop();
                    
                    if (status.winner.equals(player.getID())) {
                        JOptionPane.showMessageDialog(null, "You won", "Game ended", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "You lost", "Game ended", JOptionPane.INFORMATION_MESSAGE);
                    }

                    action_listener.actionPerformed(new ActionEvent(ACTION.END, 0, ""));
                }

                player.getGameBoard();
            }
        
            @Override
            public void yourMove() {
            }

            @Override
            public void setStatus(GoStatus go_status) {
            }
        
            @Override
            public void setBoard(GoBoard go_board) {
                Cell[][] cells = new Cell[size][size];
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        cells[i][j] = (go_board.getValue(i, j) == go_board.EMPTY ? Cell.EMPTY : (go_board.getValue(i, j) == go_board.BLACK ? Cell.BLACK : Cell.WHITE));
                    }
                }
                board.set(cells);
            }
        
            @Override
            public void error() {
                player.getGameBoard();
            }
        });

        pass_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.makeMove(new GoMove(TYPE.PASS));
            }
        });
        give_up_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.makeMove(new GoMove(TYPE.GIVEUP));
            }
        });

        action_listener = a;

        thread = new GoPlayerThread(player);
        Thread t = new Thread(thread);
        t.start();
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

        public void set(GoGameLogic.Cell[][] board) {
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
            GoGameLogic.Cell cell_state;

            public Cell(int x, int y) {
                this.x = x;
                this.y = y;
                this.setOpaque(false);
                paint_preview = false;

                this.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        paint_preview = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        paint_preview = false;
                        repaint();
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        GoMove move = new GoMove(TYPE.MOVE);
                        move.x = x;
                        move.y = y;
                        player.makeMove(move);
                        set(go_game.getBoard());
                        paint_preview = false;
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
                if (!this.cell_state.equals(GoGameLogic.Cell.EMPTY)) {
                    if (this.cell_state.equals(GoGameLogic.Cell.WHITE))
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
                    if (player_color.equals(GoGameLogic.Player.BLACK))
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
