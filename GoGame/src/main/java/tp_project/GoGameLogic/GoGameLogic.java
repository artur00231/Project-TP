package tp_project.GoGameLogic;

import java.util.ArrayList;
import java.util.List;

import tp_project.GoGame.GoBoard;
import tp_project.GoGame.GoMove;

public class GoGameLogic {
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

    public static class Score {
        public int white = 0;
        public int black = 0;
        public int stones_capured_by_black;
        public int stones_capured_by_white;
    }

    public static class Board {
        int size;
        Cell[][] board;
        Cell[][] prev;
        private Player current_player;
        private int stones_capured_by_black;
        private int stones_capured_by_white;


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

        public void resetScore(int stones_capured_by_black, int stones_capured_by_white) {
            this.stones_capured_by_black = stones_capured_by_black;
            this.stones_capured_by_white = stones_capured_by_white;
        }

        private Board(Board b) {
            this.current_player = b.current_player.getOpponent();
            this.size = b.size;
            this.stones_capured_by_black = b.stones_capured_by_black;
            this.stones_capured_by_white = b.stones_capured_by_white;
            board = new Cell[size][size];
            prev = b.getBoard();
            for (int i = 0; i < size; ++i) {
                for (int j = 0; j < size; ++j) {
                    board[i][j] = prev[i][j];
                }
            }
        }

        private static class _Score {
            int count = 0;
            boolean black = false;
            boolean white = false;
        }

        public Player getCurrent_player() {
            return current_player;
        }

        public Score getScore(boolean finished) {
            Score score = new Score();
            score.stones_capured_by_black = stones_capured_by_black;
            score.stones_capured_by_white = stones_capured_by_white;

            if (!finished) return score;

            boolean[][] visited = new boolean[size][size];
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
            score.black += stones_capured_by_black;
            score.white += stones_capured_by_white;

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
            this.current_player = this.current_player.getOpponent();
            return this;
        }

        public Board makeMove(GoMove move, Player player) {
            if (!move.move_type.equals(GoMove.TYPE.MOVE)) {
                if (move.move_type.equals(GoMove.TYPE.PASS)) {
                    return pass();
                } else {
                    return null;
                }
            }

            if (move.x < 0 || move.x >= size || move.y < 0 || move.y >= size) return null;
            if (!player.equals(current_player)) return null;
            if (!board[move.y][move.x].equals(Cell.EMPTY)) return null;

            ArrayList<int[]> to_delete = new ArrayList<>();
            boolean suicide = true;

            for (int[] i : getNeighbours(move.x, move.y)) {
                int x = i[0], y = i[1];
                if (board[y][x].equals(Cell.EMPTY)) suicide = false;
                int b = getBreaths(x, y);
                if (board[y][x].equals(player.getColor()) && b > 1) suicide = false;
                if (board[y][x].equals(player.getOpponent().getColor()) && b == 1) {
                    suicide = false;
                    to_delete.add(new int[]{x, y});
                }
            }
            if (suicide) return null;

            Board next = new Board(this);
            next.getBoard()[move.y][move.x] = player.getColor();

            
            for (int[] i : to_delete) {
                int x = i[0], y = i[1];
                
                if (current_player.equals(Player.BLACK)) {
                    next.stones_capured_by_black += next.remove(x, y);
                } else {
                    next.stones_capured_by_white += next.remove(x, y);
                }
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

        private int remove(int x, int y) {
            if (board[y][x].equals(Cell.EMPTY)) return 0;
            Cell color = board[y][x];
            board[y][x] = Cell.EMPTY;
            int sum = 1;

            for (int[] i : getNeighbours(x, y)) {
                if (board[i[1]][i[0]].equals(color)) sum += remove(i[0], i[1]);
            }

            return sum;
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

        public Board getPreview(GoMove move, Player player) {
            if (!move.move_type.equals(GoMove.TYPE.MOVE)) return null;

            if (!computed[move.y][move.x]) {
                previews[move.y][move.x] = GoGameLogic.this.board.makeMove(move, player);
                computed[move.y][move.x] = true;
            }

            return previews[move.y][move.x];
        }
    }

    private Board board;
    private Previews previews;
    private int size;

    public GoGameLogic(int size) {
        board = new Board(size);
        this.size = size;
        previews = new Previews();
    }

    public void restartGame(int stones_capured_by_black, int stones_capured_by_white) {
        board = new Board(size);
        board.resetScore(stones_capured_by_black, stones_capured_by_white);
        previews = new Previews();
    }

    public Player getCurrentPlayer() {
        return board.getCurrent_player();
    }

    public boolean isLegal(GoMove move, Player player) {
        return (previews.getPreview(move, player) != null);
    }

    public boolean makeMove(GoMove move, Player player) {
        if (move.move_type.equals(GoMove.TYPE.PASS)) {
            board.pass();
            previews.reset();
            return true;
        }

        Board next = previews.getPreview(move, player);
        if (next != null) {
            board = next;
            previews.reset();
            return true;
        }
        else return false;
    }

    public Cell[][] getBoard() {
        return board.getBoard();
    }

    public GoBoard getGoBoard() {
        GoBoard go_board = new GoBoard(size);
        Cell[][] arr_board = board.getBoard();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                go_board.setValue(i, j, arr_board[i][j] == Cell.BLACK ? go_board.BLACK : (arr_board[i][j] == Cell.WHITE ? go_board.WHITE : go_board.EMPTY));
            }
        }

        return go_board;
    }

    public void setBoard(Board board) {
        Cell[][] prev = this.board.prev;
        this.board = board;
        this.board.prev = prev;
        previews.reset();
    }

    public void setBoard(Cell[][] board, boolean update_prev) {
        if (update_prev) {
            this.board.prev = this.board.board;
        }
        this.board.board = board;
        previews.reset();
    }

    public Score getGameScore(boolean finished) {
        return board.getScore(finished);
    }

    public void setCurrentPlayer(Player player) {
        this.board.current_player = player;
        previews.reset();
    }
}
