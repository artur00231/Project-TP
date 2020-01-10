package tp_project.GoReplay;

import tp_project.GoGame.GoBoard;
import tp_project.GoGameDBObject.DBGoGames;
import tp_project.GoGameDBObject.DBGoStatus;

public interface GoReplayPlayerListener {
    public void error();
    public void setGames(DBGoGames games);
    public void setBoard(GoBoard board);
    public void setStatus(DBGoStatus status);
    public void gameEnded();
}