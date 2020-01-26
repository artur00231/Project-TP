package GoGame;

import Server.Player;

public interface GoPlayer extends Player {

    public void setListener(GoPlayerListener listener);

    public void gameEnded();

    public String getID();

    public String getName();
}