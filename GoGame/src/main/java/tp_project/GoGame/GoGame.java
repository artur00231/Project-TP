package tp_project.GoGame;

import tp_project.GoGame.GoMove.TYPE;
import tp_project.GoGameLogic.GoGameLogic;
import tp_project.GoGameLogic.GoGameLogic.Cell;
import tp_project.GoGameLogic.GoGameLogic.Player;
import tp_project.Server.Game;
import tp_project.Server.GameManager;

public class GoGame implements Game {
    private GoPlayer player1;
    private GoPlayer player2;
    private GoGameLogic.Player player1_colour;
    private boolean is_running;
    private GameManager manager;
    private GoGameLogic game_logic;
    private GoMove last_move = new GoMove(TYPE.MOVE);
    private GoStatus game_status = new GoStatus();
    private int size;

    public GoGame(int size, GoPlayer p1, String p1_id, GoPlayer p2, String p2_id, int player1_colour, GameManager manager) {
        player1 = p1;
        game_status.player1 = p1_id;
        player2 = p2;
        game_status.player2 = p2_id;
        this.player1_colour = (player1_colour == 0 ? Player.BLACK : Player.WHITE);
        this.manager = manager;
        game_logic = new GoGameLogic(size);
        this.size = size;
    }

    @Override
    public void run() {
        is_running = true;
        manager.gameStated();

        player1.yourMove();

        while (is_running) {
            if (!player1.update()) {
                makeMove(new GoMove(TYPE.GIVEUP), player1);
            }
            if (!player2.update()) {
                makeMove(new GoMove(TYPE.GIVEUP), player2);
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }

        player1.gameEnded();
        player2.gameEnded();

        manager.gameEnded();
    }

    public boolean makeMove(GoMove move, GoPlayer player) {
        if (!is_running) return false;
        Player player_colour = player1_colour;
        if (player != player1) {
            player_colour = player_colour.getOpponent();
        }

        if (move.move_type == TYPE.GIVEUP) {
            is_running = false;
            game_status.game_ended = true;
            if (player == player1) {
                game_status.player_1_giveup = true;
                game_status.winner = player2.getID();
            } else {
                game_status.player_2_giveup = true;
                game_status.winner = player1.getID();
            }
            
            return true;
        }

        if (!game_logic.getCurrentPlayer().equals(player_colour)) return false;

        if (move.move_type == TYPE.PASS && last_move.move_type == TYPE.PASS) {
            is_running = false;
            game_status.game_ended = true;
            player1.boardUpdated();
            player2.boardUpdated();
            return true;
        }
        last_move.fromText(move.toText());

        boolean success = game_logic.makeMove(move, player_colour);

        if (success) {
            if (game_logic.getCurrentPlayer() == player1_colour) {
                player1.yourMove();
            } else {
                player2.yourMove();
            }
            player1.boardUpdated();
            player2.boardUpdated();
        } else {
            if (game_logic.getCurrentPlayer() == player1_colour) {
                player1.yourMove();
            } else {
                player2.yourMove();
            }
        }

        return success;
    }

    public GoStatus getGameStatus() {
        GoGameLogic.Score score = game_logic.getGameScore(!is_running);
        if (game_logic.getCurrentPlayer().equals(player1_colour)) {
            game_status.curr_move = game_status.player1;
        } else {
            game_status.curr_move = game_status.player2;
        }

        if (player1_colour.equals(GoGameLogic.Player.BLACK)) {
            game_status.player1_total_score = score.black;
            game_status.stones_capured_by_player1 = score.stones_capured_by_black;
            game_status.player2_total_score = score.white;
            game_status.stones_capured_by_player2 = score.stones_capured_by_white;

            if (!is_running) {
                if (score.black > score.white) {
                    game_status.winner = player1.getID();
                } else if (score.black < score.white) {
                    game_status.winner = player2.getID();
                }
            }
        } else {
            game_status.player2_total_score = score.black;
            game_status.stones_capured_by_player2 = score.stones_capured_by_black;
            game_status.player1_total_score = score.white;
            game_status.stones_capured_by_player1 = score.stones_capured_by_white;

            if (!is_running) {
                if (score.white > score.black) {
                    game_status.winner = player1.getID();
                } else if (score.white < score.black) {
                    game_status.winner = player2.getID();
                }
            }
        }

        game_status.game_ended = !is_running;

        return game_status;
    }

    public GoBoard getBoard() {
        game_logic.getBoard();

        GoBoard board = new GoBoard(size);
        Cell[][] raw_board = game_logic.getBoard();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board.setValue(i, j, raw_board[i][j] == Cell.BLACK ? board.BLACK : (raw_board[i][j] == Cell.WHITE ? board.WHITE : board.EMPTY ));
            }
        }

        return board;
    }
}