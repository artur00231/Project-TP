package GoGame;

public interface GoPlayerListener {
    public void yourMove();
    public void boardUpdated();
    public void setBoard(GoBoard go_board);
    public void setStatus(GoStatus go_status);
    public void error();
    public void gameEnded();
}