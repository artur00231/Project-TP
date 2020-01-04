package tp_project.GoGame;

import tp_project.GoGame.GoMove.TYPE;

public class GoAIPlayer implements GoPlayer {
    private GoGame game;
    private boolean is_game_runnig = true;
    private String player_ID;
    private int game_size;
    private boolean is_my_move = false;

    public GoAIPlayer(int game_size) {
        this.player_ID = "BOT";
        this.game_size = game_size;
    }

    @Override
    public void setListener(GoPlayerListener listener) {
        return;
    }

    @Override
    public void setGame(GoGame game) {
        this.game = game;
    }

    public boolean isGameRunnig() {
        return is_game_runnig;
    }

    @Override
    public void gameEnded() {
        return;
    }

    @Override
    public boolean update() {
        if (is_my_move) {
            doMove();
            is_my_move = false;
        }

        return true;
    }

    @Override
    public void yourMove() {
        is_my_move = true;
    }

    @Override
    public String getID() {
        return player_ID;
    }

    @Override
    public void boardUpdated() {
    }

    private void doMove() {
        GoMove move = new GoMove(TYPE.MOVE);

        for (int i = 0; i < game_size; i++) {
            for (int j = 0; j < game_size; j++) {
                move.x = i;
                move.y = j;
                if (game.makeMove(move, this)) {
                    return;
                }
            }
        }
    }

    @Override
    public String getName() {
        return player_ID;
    }
}