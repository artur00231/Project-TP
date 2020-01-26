package Server;

public interface Player {
    public boolean update();

    public void yourMove();

    public void boardUpdated();

    public boolean isReady();
}