package tp_project.GoGameLogic;

import java.util.ArrayList;
import java.util.List;

public class GoGame {
    public enum Cell {
        EMPTY, WHITE, BLACK
    }
    public enum Player {
        WHITE {
            @Override

            public Cell getColor() {
                return Cell.WHITE;
            }

            @Override
            public Player getOpponent() {
                return BLACK;
            }
        },
        BLACK {
            @Override
            public Cell getColor() {
                return Cell.BLACK;
            }

            @Override
            public Player getOpponent() {
                return WHITE;
            }
        };
        abstract public Player getOpponent();
        abstract public Cell getColor();
    }

    public static class Move {
        int row, col;
        Player player;

        public Move(int x, int y, Player p) {
            row = y;
            col = x;
            player = p;
        }
    }

    public static class Board {
        int size;
        Cell[][] board;
        Cell[][] prev;
        private Player current_player;

        Cell getCell(int x, int y) {
            return board[y][x];
        }

        Cell[][] getBoard() {
            return board;
        }

        void setCell(int x, int y, Cell c) {
            board[y][x] = c;
        }

        public Board(int size) {
            current_player = Player.BLACK;
            this.size = size;
            board = new Cell[size][size];
            prev = board;
            for (int i = 0; i < size; ++i) {
                for (int j = 0; j < size; ++j) {
                    board[i][j] = Cell.EMPTY;
                }
            }
        }

        private Board(Board b) {
            this.current_player = b.current_player.getOpponent();
            this.size = b.size;
            board = new Cell[size][size];
            prev = b.getBoard();
            for (int i = 0; i < size; ++i) {
                for (int j = 0; j < size; ++j) {
                    board[i][j] = prev[i][j];
                }
            }
        }

        public static class Score {
            int white = 0;
            int black = 0;
        }

        private static class _Score {
            int count = 0;
            boolean black = false;
            boolean white = false;
        }

        public Player getCurrent_player() {
            return current_player;
        }

        public Score getScore() {
            boolean[][] visited = new boolean[size][size];
            Score score = new Score();
            for (int i = 0; i < size; ++i) {
                for (int j = 0; j < size; ++j) {
                    visited[i][j] = false;
                }
            }

            for (int i = 0; i < size; ++i) {
                for (int j = 0; j < size; ++j) {
                    if (!visited[i][j] && board[i][j].equals(Cell.EMPTY)) {
                        _Score _score = new _Score();
                        _getScore(j, i, visited, _score);
                        if (_score.white && !_score.black)
                            score.white += _score.count;
                        if (!_score.white && _score.black)
                            score.black += _score.count;
                    }
                    if (board[i][j].equals(Cell.BLACK))
                        ++score.black;
                    else if (board[i][j].equals(Cell.WHITE))
                        ++score.white;
                    visited[i][j] = true;
                }
            }
            return score;
        }

        private void  _getScore(int x, int y, boolean[][] visited, _Score _score) {
            visited[y][x] = true;
            ++_score.count;
            for (int[] i : getNeighbours(x, y)) {
                int _x = i[0], _y = i[1];
                switch (board[_y][_x]) {
                    case BLACK:
                        _score.black = true;
                        break;
                    case WHITE:
                        _score.white = true;
                        break;
                    case EMPTY:
                        if (!visited[_y][_x])
                            _getScore(_x, _y, visited, _score);
                }
            }
        }

        public Board pass() {
            return new Board(this);
        }

        public Board makeMove(Move m) {
            if (m.col < 0 || m.col >= size || m.row < 0 || m.row >= size) return null;
            if (!m.player.equals(current_player)) return null;
            if (!board[m.row][m.col].equals(Cell.EMPTY)) return null;

            ArrayList<int[]> to_delete = new ArrayList<>();
            boolean suicide = true;

            for (int[] i : getNeighbours(m.col, m.row)) {
                int x = i[0], y = i[1];
                if (board[y][x].equals(Cell.EMPTY)) suicide = false;
                int b = getBreaths(x, y);
                if (board[y][x].equals(m.player.getColor()) && b > 1) suicide = false;
                if (board[y][x].equals(m.player.getOpponent().getColor()) && b == 1) {
                    suicide = false;
                    to_delete.add(new int[]{x, y});
                }
            }
            if (suicide) return null;

            Board next = new Board(this);
            next.getBoard()[m.row][m.col] = m.player.getColor();
            for (int[] i : to_delete) {
                int x = i[0], y = i[1];
                next.remove(x, y);
            }

            for (int i = 0; i < size; ++i) {
                for (int j = 0; j < size; ++j) {
                    if (!prev[i][j].equals(next.getBoard()[i][j])) return next;
                }
            }

            return null;
        }

        private List<int[]> getNeighbours(int x, int y) {
            ArrayList<int[]> neighbours = new ArrayList<>();
            if (x - 1 >= 0) neighbours.add(new int[]{x - 1, y});
            if (x + 1 < size) neighbours.add(new int[]{x + 1, y});
            if (y - 1 >= 0) neighbours.add(new int[]{x, y - 1});
            if (y + 1 < size) neighbours.add(new int[]{x, y + 1});

            return neighbours;
        }

        private int getBreaths(int x, int y) {
            boolean[][] visited = new boolean[size][size];
            for (int i = 0; i < size; ++i) {
                for (int j = 0; j < size; ++j) {
                    visited[i][j] = false;
                }
            }

            return _getBreaths(x, y, visited);
        }

        private int _getBreaths(int x, int y, boolean[][] visited) {
            if (visited[y][x]) return 0;
            visited[y][x] = true;

            int breaths = 0;
            for (int[] i : getNeighbours(x, y)) {
                int _x = i[0], _y = i[1];
                if (board[_y][_x].equals(Cell.EMPTY)) {
                    if (!visited[_y][_x]) ++breaths;
                    visited[_y][_x] = true;
                }
                if (board[_y][_x].equals(board[y][x]))
                    breaths += _getBreaths(_x, _y, visited);
            }

            return breaths;
        }

        private void remove(int x, int y) {
            if (board[y][x].equals(Cell.EMPTY)) return;
            Cell color = board[y][x];
            board[y][x] = Cell.EMPTY;

            for (int[] i : getNeighbours(x, y)) {
                if (board[i[1]][i[0]].equals(color)) remove(i[0], i[1]);
            }
        }
    }

    class Previews {
        boolean[][] computed;
        Board[][] previews;

        public Previews() {
            computed = new boolean[size][size];
            previews = new Board[size][size];

            for (int i = 0; i < size; ++i) {
                for (int j = 0; j < size; ++j) {
                    computed[i][j] = false;
                }
            }
        }

        public void reset() {
            for (int i = 0; i < size; ++i) {
                for (int j = 0; j < size; ++j) {
                    computed[i][j] = false;
                }
            }
        }

        public Board getPreview(Move m) {
            if (!computed[m.row][m.col]) {
                previews[m.row][m.col] = GoGame.this.board.makeMove(m);
                computed[m.row][m.col] = true;
            }
            return previews[m.row][m.col];
        }
    }

    private Board board;
    private Previews previews;
    private int size;

    public GoGame(int size) {
        board = new Board(size);
        this.size = size;
        previews = new Previews();
    }

    public Player getCurrentPlayer() {
        return board.getCurrent_player();
    }

    public boolean isLegal(Move m) {
        return (previews.getPreview(m) != null);
    }

    public boolean makeMove(Move m) {
        Board next = previews.getPreview(m);
        if (next != null) {
            board = next;
            previews.reset();
            System.out.println(board.getScore().black + " " + board.getScore().white);
            return true;
        }
        else return false;
    }

    public Cell[][] getBoard() {
        return board.getBoard();
    }
}
