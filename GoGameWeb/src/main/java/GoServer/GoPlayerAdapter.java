package GoServer;

import GoGame.GoBoard;
import GoGame.GoPlayerListener;
import GoGame.GoStatus;

public class GoPlayerAdapter implements GoPlayerListener {
    public GoBoard last_board = null;
    public GoStatus last_status = null;

    public boolean is_my_move = false;
    public boolean is_updated = false;
    public boolean is_status = false;
    public boolean is_board = false;
    public boolean is_error = false;
    public boolean is_game_ended = false;

    @Override
    public void yourMove() {
        is_my_move = true;
    }

    @Override
    public void boardUpdated() {
        is_updated = true;
    }

    @Override
    public void setBoard(GoBoard go_board) {
        last_board = go_board;
        is_board = true;
    }

    @Override
    public void setStatus(GoStatus go_status) {
        last_status = go_status;
        is_status = true;
    }

    @Override
    public void error() {
        is_error = true;
    }

    @Override
    public void gameEnded() {
        is_game_ended = true;
    }

    public void reset() {
        is_my_move = false;
        is_updated = false;
        is_status = false;
        is_board = false;
        is_error = false;
    }
    
    public boolean stateChanged() {
        return is_my_move || is_updated || is_status|| is_board || is_error;
    }
}