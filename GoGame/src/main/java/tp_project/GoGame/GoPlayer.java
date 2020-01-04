package tp_project.GoGame;

import tp_project.Server.Player;

public interface GoPlayer extends Player {

    public void setListener(GoPlayerListener listener);

    public void setGame(GoGame game);

    public void gameEnded();

    public String getID();

    public String getName();
}