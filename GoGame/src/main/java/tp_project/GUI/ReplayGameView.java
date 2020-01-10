package tp_project.GUI;

import tp_project.GoGame.GoBoard;
import tp_project.GoGameDBObject.DBGoGame;
import tp_project.GoGameDBObject.DBGoStatus;
import tp_project.GoGameLogic.GoGameLogic;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class ReplayGameView extends JPanel {
    private static final long serialVersionUID = -353599257042183805L;
    public enum ACTION { END, PREV, NEXT };

    private Board board;
    private int size;
    private JButton next_button = new JButton(">>>") //pass_button will fill BoxLayout in Y axis
    { private static final long serialVersionUID = 2522748472460831874L; @Override public Dimension getMaximumSize() { Dimension d = super.getMaximumSize(); d.width = Integer.MAX_VALUE; return d; }};
    private JButton prev_button = new JButton("<<<") //pass_button will fill BoxLayout in Y axis
    { private static final long serialVersionUID = 2901876623633538393L; @Override public Dimension getMaximumSize() { Dimension d = super.getMaximumSize(); d.width = Integer.MAX_VALUE; return d; }};
    private JLabel player_move = new JLabel("move", SwingConstants.CENTER) //your_move_label will fill BoxLayout in Y axis
    { private static final long serialVersionUID = 4542125361357724107L; @Override public Dimension getMaximumSize() { Dimension d = super.getMaximumSize(); d.width = Integer.MAX_VALUE; return d; }};
    private JLabel points = new JLabel("Score", SwingConstants.CENTER) //points will fill BoxLayout in Y axis
    { private static final long serialVersionUID = 4864276740641971233L; @Override public Dimension getMaximumSize() { Dimension d = super.getMaximumSize(); d.width = Integer.MAX_VALUE; return d; }};
    private JButton exit_button = new JButton("Exit") //pass_button will fill BoxLayout in Y axis
    { private static final long serialVersionUID = 2522748472460831874L; @Override public Dimension getMaximumSize() { Dimension d = super.getMaximumSize(); d.width = Integer.MAX_VALUE; return d; }};
    private ControlPanel control_panel = new ControlPanel();
    private ActionListener action_listener;

    private DBGoGame game;
    private DBGoStatus status;

    public ReplayGameView(int size, DBGoStatus status, DBGoGame game, ActionListener a) {
        this.size = size;
        this.game = game;
        this.status = status;
        board = new Board();
        GoGameLogic tmp = new GoGameLogic(size);
        board.set(tmp.getBoard());

        this.setLayout(new BorderLayout());
        this.add(board, BorderLayout.CENTER);
        this.add(control_panel, BorderLayout.EAST);

        player_move.setOpaque(true);
        player_move.setBackground(Color.BLACK);
        player_move.setForeground(Color.WHITE);


        next_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prev_button.setEnabled(false);
                next_button.setEnabled(false);
                action_listener.actionPerformed(new ActionEvent(ACTION.NEXT, 0, null));
            }
        });
        prev_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prev_button.setEnabled(false);
                next_button.setEnabled(false);
                action_listener.actionPerformed(new ActionEvent(ACTION.PREV, 0, null));
            }
        });
        exit_button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exit_button.setEnabled(false);
                action_listener.actionPerformed(new ActionEvent(ACTION.END, 0, null));
            }
        });

        action_listener = a;

        setScore();
        showEndMessage();
    }

    public void setNextBoard(GoBoard go_board, boolean prev, boolean next) {
        GoGameLogic.Cell[][] cells = new GoGameLogic.Cell[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                cells[i][j] = (go_board.getValue(i, j) == go_board.EMPTY ? GoGameLogic.Cell.EMPTY : (go_board.getValue(i, j) == go_board.BLACK ? GoGameLogic.Cell.BLACK : GoGameLogic.Cell.WHITE));
            }
        }
        board.set(cells);

        prev_button.setEnabled(prev);
        next_button.setEnabled(next);

        if (player_move.getBackground().equals(Color.WHITE)) {
            player_move.setBackground(Color.BLACK);
            player_move.setForeground(Color.WHITE);
        } else {
            player_move.setForeground(Color.BLACK);
            player_move.setBackground(Color.WHITE);
        }
    }
    
    public void setButtons(boolean prev, boolean next) {
        prev_button.setEnabled(prev);
        next_button.setEnabled(next);
    }

    private class Board extends JPanel{
        private static final long serialVersionUID = -8773343169606093079L;
        JPanel inner_panel = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                super.getPreferredSize();
                Dimension d = Board.this.getSize();
                int m = Math.min(d.width, d.height) - 10;
                m = m - m % size;
                return new Dimension(m, m);
            }
        };
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
            GoGameLogic.Cell cell_state;

            public Cell(int x, int y) {
                this.x = x;
                this.y = y;
                this.setOpaque(false);
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
            }
        }
    }

    private class ControlPanel extends JPanel {
        private static final long serialVersionUID = 7356171699838575403L;

        public ControlPanel() {
            this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
            this.add(player_move);
            this.add(points);
            this.add(Box.createGlue());
            this.add(exit_button);
            this.add(next_button);
            this.add(prev_button);
        }
    }

    private void setScore() {
        String p1_score;
        String p1_points;
        String p2_score;
        String p2_points;

        p1_score = Integer.toString(status.player1_total_score);
        p2_score = Integer.toString(status.player2_total_score);
        p1_points = Integer.toString(status.stones_capured_by_player1);
        p2_points = Integer.toString(status.stones_capured_by_player2);
        int score_length = p1_score.length() > p2_score.length() ? p1_score.length() : p2_score.length();
        int points_length = p1_points.length() > p2_points.length() ? p1_points.length() : p2_points.length();
        score_length = score_length > 1 ? score_length : 2;
        points_length = points_length > 1 ? points_length : 2;

        p1_score = String.format("%0" + score_length + "d", status.player1_total_score);
        p2_score = String.format("%0" + score_length + "d", status.player2_total_score);
        p1_points = String.format("%0" + points_length + "d", status.stones_capured_by_player1);
        p2_points = String.format("%0" + points_length + "d", status.stones_capured_by_player2);

        points.setText("<html>Score<br>" + game.getPlayer1Name() + " vs " + game.getPlayer2Name() + "<br>" + p1_points + "-" + p2_points + "<br>End score<br>" + p1_score + "-" + p2_score + "</html>");
        this.revalidate();
        this.repaint();
    }

    private void showEndMessage() {

        if (status.player_1_giveup) {
            JOptionPane.showMessageDialog(null, game.getPlayer1Name() + " gave up", "Score", JOptionPane.INFORMATION_MESSAGE);
        } else if (status.player_2_giveup) {
            JOptionPane.showMessageDialog(null, game.getPlayer2Name() + " gave up", "Score", JOptionPane.INFORMATION_MESSAGE);
        } else if (status.winner) {
            JOptionPane.showMessageDialog(null, game.getPlayer1Name() + " won", "Score", JOptionPane.INFORMATION_MESSAGE);
        } else if (status.player1_total_score == status.player2_total_score){
            JOptionPane.showMessageDialog(null, "No one won. ", "Score", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, game.getPlayer2Name() + " won", "Score", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
