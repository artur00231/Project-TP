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

public class GameView extends JPanel {
    private static final long serialVersionUID = -353599257042183805L;
    private GoGameLogic.Player player_color;
    public enum ACTION { END, DISCONNECTED };

    private GoGameLogic go_game;
    private Board board;
    private int size;
    private JButton pass_button = new JButton("Pass") //pass_button will fill BoxLayout in Y axis
    { private static final long serialVersionUID = 2522748472460831874L; @Override public Dimension getMaximumSize() { Dimension d = super.getMaximumSize(); d.width = Integer.MAX_VALUE; return d; }};
    private JButton give_up_button = new JButton("Give up") //pass_button will fill BoxLayout in Y axis
    { private static final long serialVersionUID = 2901876623633538393L; @Override public Dimension getMaximumSize() { Dimension d = super.getMaximumSize(); d.width = Integer.MAX_VALUE; return d; }};
    private JLabel your_move_label = new JLabel("Your move", SwingConstants.CENTER) //your_move_label will fill BoxLayout in Y axis
    { private static final long serialVersionUID = 4542125361357724107L; @Override public Dimension getMaximumSize() { Dimension d = super.getMaximumSize(); d.width = Integer.MAX_VALUE; return d; }};
    private JLabel points = new JLabel("Score", SwingConstants.CENTER) //points will fill BoxLayout in Y axis
    { private static final long serialVersionUID = 4864276740641971233L; @Override public Dimension getMaximumSize() { Dimension d = super.getMaximumSize(); d.width = Integer.MAX_VALUE; return d; }};
    private ControlPanel control_panel = new ControlPanel();
    private GoRemotePlayer player;
    private ActionListener action_listener;
    private Timer update_timer;
    private boolean update_prev_board = true;

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

        switchView(go_game.getCurrentPlayer().equals(player_color));

        your_move_label.setOpaque(true);
        if (player_color.equals(GoGameLogic.Player.BLACK)) {
            your_move_label.setBackground(Color.BLACK);
            your_move_label.setForeground(Color.WHITE);
        } else {
            your_move_label.setForeground(Color.BLACK);
            your_move_label.setBackground(Color.WHITE);
        }

        player.setListener(new GoPlayerListener(){
            @Override
            public void yourMove() {
                switchView(true);
            }

            @Override
            public void setStatus(GoStatus go_status) {
                setScore(go_status);
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
                go_game.setBoard(cells, update_prev_board);
                update_prev_board = true;
            }
        
            @Override
            public void error() {
                update_prev_board = false;
                player.getGameBoard();
            }

            @Override
            public void boardUpdated() {
                player.getGameBoard();
                player.getGameStatus();
            }

            @Override
            public void gameEnded() {
                update_timer.stop();
                GoStatus status = player.getLastStatus();
                
                showEndMessage(status);

                action_listener.actionPerformed(new ActionEvent(ACTION.END, 0, ""));
            }
        });

        pass_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (go_game.getCurrentPlayer().equals(player_color)) {
                    player.makeMove(new GoMove(TYPE.PASS));
                    switchView(false);
                }
            }
        });
        give_up_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                player.makeMove(new GoMove(TYPE.GIVEUP));
            }
        });

        action_listener = a;

        update_timer = new Timer(100, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
                player.update();
				if (player.isDisconnected()) {
                    update_timer.stop();
                    action_listener.actionPerformed(new ActionEvent(ACTION.DISCONNECTED, 0, ""));
                }
			}
        });
        update_timer.start();
    }

    private class Board extends JPanel{
        private static final long serialVersionUID = -8773343169606093079L;
        JPanel inner_panel = new JPanel();
        Cell[][] _board;

        public Board() {
            inner_panel.setLayout(new GridLayout(size, size));
            inner_panel.setBackground(Color.GRAY);
            this.setBorder(new EmptyBorder(0,0,0,0));
            this.setBackground(Color.BLACK);
            this.add(inner_panel);

            _board = new Cell[size][size];
            for (int y = 0; y < size; ++y) {
                for (int x = 0; x < size; ++x) {
                    _board[y][x] = new Cell(x, y);
                    inner_panel.add(_board[y][x]);
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
                    this._board[i][j].cell_state = board[i][j];
                }
            }
            repaint();
        }

        private class Cell extends JPanel{
            private static final long serialVersionUID = 7425127337130479578L;
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
                        GoMove move = new GoMove(GoMove.TYPE.MOVE);
                        move.setXY(x, y);
                        if (go_game.isLegal(move, player_color)) paint_preview = true;
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
                        if (!go_game.isLegal(move, player_color)) return;
                        player.makeMove(move);
                        go_game.makeMove(move, player_color);
                        switchView(false);
                        paint_preview = false;
                        board.set(go_game.getBoard());
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
        private static final long serialVersionUID = 7356171699838575403L;

        public ControlPanel() {
            this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            this.add(your_move_label);
            points.setText("<html>Score<br>00-00<br>End score<br>00-00</html>");
            this.add(points);
            this.add(Box.createGlue());
            this.add(pass_button);
            this.add(give_up_button);
        }
    }

    private void switchView(boolean your_move) {
        if (your_move) {
            go_game.setCurrentPlayer(player_color);
            pass_button.setEnabled(true);
            your_move_label.setEnabled(true);
        } else {
            go_game.setCurrentPlayer(player_color.getOpponent());
            pass_button.setEnabled(false);
            your_move_label.setEnabled(false);
        }
    }

    private void setScore(GoStatus status) {
        String my_score;
        String my_points;
        String opponent_score;
        String opponent_points;
        if (status.player1.equals(player.getID())) {
            my_score = Integer.toString(status.player1_total_score);
            opponent_score = Integer.toString(status.player2_total_score);
            my_points = Integer.toString(status.stones_capured_by_player1);
            opponent_points = Integer.toString(status.stones_capured_by_player2);
            int score_length = my_score.length() > opponent_score.length() ? my_score.length() : opponent_score.length();
            int points_length = my_points.length() > opponent_points.length() ? my_points.length() : opponent_points.length();
            score_length = score_length > 1 ? score_length : 2;
            points_length = points_length > 1 ? points_length : 2;

            my_score = String.format("%0" + score_length + "d", status.player1_total_score);
            opponent_score = String.format("%0" + score_length + "d", status.player2_total_score);
            my_points = String.format("%0" + points_length + "d", status.stones_capured_by_player1);
            opponent_points = String.format("%0" + points_length + "d", status.stones_capured_by_player2);
        } else {
            my_score = Integer.toString(status.player2_total_score);
            opponent_score = Integer.toString(status.player1_total_score);
            my_points = Integer.toString(status.stones_capured_by_player2);
            opponent_points = Integer.toString(status.stones_capured_by_player1);
            int score_length = my_score.length() > opponent_score.length() ? my_score.length() : opponent_score.length();
            int points_length = my_points.length() > opponent_points.length() ? my_points.length() : opponent_points.length();
            score_length = score_length > 1 ? score_length : 2;
            points_length = points_length > 1 ? points_length : 2;

            my_score = String.format("%0" + score_length + "d", status.player2_total_score);
            opponent_score = String.format("%0" + score_length + "d", status.player1_total_score);
            my_points = String.format("%0" + points_length + "d", status.stones_capured_by_player2);
            opponent_points = String.format("%0" + points_length + "d", status.stones_capured_by_player1);
        }

        points.setText("<html>Score<br>" + my_points + "-" + opponent_points + "<br>End score<br>" + my_score + "-" + opponent_score + "</html>");
        this.revalidate();
        this.repaint();
    }

    private void showEndMessage(GoStatus status) {
        boolean won = status.winner.equals(player.getID());
        boolean draw = status.winner.equals("XX");
        boolean other_player_giveup = false;
        boolean giveup = false;
        int my_points = 0;
        int other_player_points = 0;

        if (status.player1.equals(player.getID())) {
            other_player_giveup = status.player_2_giveup;
            giveup = status.player_1_giveup;
            my_points = status.player1_total_score;
            other_player_points = status.player2_total_score;
        } else {
            other_player_giveup = status.player_1_giveup;
            giveup = status.player_2_giveup;
            my_points = status.player2_total_score;
            other_player_points = status.player1_total_score;
        }

        if (other_player_giveup) {
            JOptionPane.showMessageDialog(null, "You won", "Game ended", JOptionPane.INFORMATION_MESSAGE);
        } else if (giveup) {
            JOptionPane.showMessageDialog(null, "You lost", "Game ended", JOptionPane.INFORMATION_MESSAGE);
        } else if (won) {
            JOptionPane.showMessageDialog(null, "You won. " + my_points + ":" + other_player_points, "Game ended", JOptionPane.INFORMATION_MESSAGE);
        } else if (draw){
            JOptionPane.showMessageDialog(null, "No one won. " + my_points + ":" + other_player_points, "Game ended", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "You lost. " + my_points + ":" + other_player_points, "Game ended", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
