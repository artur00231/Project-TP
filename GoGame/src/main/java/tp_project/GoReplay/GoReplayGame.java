package tp_project.GoReplay;

import java.sql.Date;
import java.time.Instant;

import tp_project.GoGame.GoBoard;
import tp_project.GoGameDBObject.DBGoGames;
import tp_project.GoGameDBObject.DBGoManager;
import tp_project.GoGameDBObject.DBGoStatus;
import tp_project.Server.Game;
import tp_project.Server.GameManager;

public class GoReplayGame implements Game {
    private GoReplayPlayer player;
    private boolean is_running;
    private GameManager manager;

    public GoReplayGame(GoReplayPlayer p1, GameManager manager) {
        player = p1;
        this.manager = manager;
    }

    @Override
    public void run() {
        is_running = true;
        manager.gameStated();

        for (int i = 0; (!player.isReady()) && i < 10 * 3; i++) { //Wait for players max 3 secound
            player.update();

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
        }

        player.yourMove();

        while (is_running) {
            if (!player.update()) {
                end();
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }

        player.gameEnded();

        manager.gameEnded();
    }

    public DBGoGames getGames(Date date) {
        return DBGoManager.getInstance().getGames(Instant.ofEpochMilli(date.getTime()));
    }


    public void end() {
        is_running = false;
    }

	public DBGoStatus getStatus(Integer game_id) {
		return DBGoManager.getInstance().getStatus(game_id);
	}

	public GoBoard getBoard(Integer game_id, Integer round) {
		return DBGoManager.getInstance().getBoard(game_id, round);
	}
}