package tp_project.GoGameLogic;

import java.util.ArrayList;
import java.util.List;

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

    public static class Move {
        int row, col;
        Player player;

        public Move(int x, int y, Player p) {
            row = y;
            col = x;
            player = p;
        }
    }

    private Cell[][] board;
    private Player current_player;
    private int size;

    public GoGameLogic(int size) {
        board = new Cell[size][size];
        current_player = Player.BLACK;
        this.size = size;

        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < size; ++j) {
                board[i][j] = Cell.EMPTY;
            }
        }
    }

    public boolean isMyMove(Player player) {
        return player.equals(current_player);
    }

    public boolean isLegal(Move m) {
        if (m.col < 0 || m.col >= size || m.row < 0 || m.row >= size) return false;
        if (!m.player.equals(current_player)) return false;
        if (!board[m.row][m.col].equals(Cell.EMPTY)) return false;
        if (isSuicide(m)) return false;
        return true;
    }

    public boolean makeMove(Move m) {
        if (isLegal(m)) {
            board[m.row][m.col] = m.player.getColor();
            for (int[] i : getNeighbours(m.col, m.row)) {
                int x = i[0], y = i[1];
                if (board[y][x].equals(m.player.getOpponent().getColor()) && getBreaths(x, y) == 0) remove(x, y);
            }
            current_player = current_player.getOpponent();
            return true;
        }
        else return false;
    }

    public Cell[][] getBoard() {
        return board;
    }

    private void remove(int x, int y) {
        if (board[y][x].equals(Cell.EMPTY)) return;
        Cell color = board[y][x];
        board[y][x] = Cell.EMPTY;

        for (int[] i : getNeighbours(x, y)) {
            if (board[i[1]][i[0]].equals(color)) remove(i[0], i[1]);
        }
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

        return _getBreaths(x, y, visited, board[y][x]);
    }

    private int _getBreaths(int x, int y, boolean[][] visited, Cell color) {
        if (visited[y][x]) return 0;
        visited[y][x] = true;

        int breaths = 0;
        for (int[] i : getNeighbours(x, y)) {
            int _x = i[0], _y = i[1];
            if (board[_y][_x].equals(Cell.EMPTY)) {
                if (!visited[_y][_x]) ++breaths;
                visited[_y][_x] = true;
            }
            if (board[_y][_x].equals(color))
                breaths += _getBreaths(_x, _y, visited, color);
        }

        return breaths;
    }

    private boolean isSuicide(Move m) {
        for (int[] i : getNeighbours(m.col, m.row)) {
            int x = i[0], y = i[1];
            if (board[y][x].equals(Cell.EMPTY)) return false;
            int b = getBreaths(x, y);
            if (board[y][x].equals(m.player.getColor()) && b > 1) return false;
            if (board[y][x].equals(m.player.getOpponent().getColor()) && b == 1) return false;
        }
        return true;
    }
}
