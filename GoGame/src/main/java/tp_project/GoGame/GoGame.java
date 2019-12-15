package tp_project.GoGame;

import tp_project.GoGame.GoMove.TYPE;
import tp_project.GoGameLogic.GoGameLogic;
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

    public GoGame(GoPlayer p1, String p1_id, GoPlayer p2, String p2_id, int player1_colour, GameManager manager) {
        player1 = p1;
        game_status.player1 = p1_id;
        player2 = p2;
        game_status.player2 = p2_id;
        this.player1_colour = (player1_colour == 0 ? Player.BLACK : Player.WHITE);
        this.manager = manager;
        game_logic = new GoGameLogic(13);
    }

    @Override
    public void run() {
        is_running = true;
        manager.gameStated();

        while (is_running) {
            player1.update();
            player2.update();

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }

        player1.gameEnded();
        player2.gameEnded();

        manager.gameEnded();
    }

    boolean makeMove(GoMove move, GoPlayer player) {
        if (!is_running) return false;
        Player player_colour = player1_colour;
        if (player != player1) {
            player_colour = player_colour.getOpponent();
        }

        if (!game_logic.isMyMove(player_colour)) return false;

        if (move.move_type == TYPE.PASS && last_move.move_type == TYPE.PASS) {
            is_running = false;
            game_status.game_ended = true;
            return true;
        }
        last_move.fromText(move.toText());

        if (move.move_type == TYPE.GIVEUP) {
            is_running = false;
            game_status.game_ended = true;
            if (player == player1) {
                game_status.player_1_giveup = true;
            } else {
                game_status.player_1_giveup = false;
            }

            return true;
        }

        if (move.move_type == TYPE.PASS) return true;

        return game_logic.makeMove(new GoGameLogic.Move(move.x, move.y, player_colour));
    }

    public GoStatus getGameStatus() {
        if (game_logic.isMyMove(player1_colour)) {
            game_status.curr_move = game_status.player1;
        } else {
            game_status.curr_move = game_status.player2;
        }

        game_status.game_ended = !is_running;

        return game_status;
    }

    public GoBoard getBoard() {
        //TODO
        return new GoBoard(2);
    }
}