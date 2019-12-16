package tp_project.GoGame;

public interface GoPlayerListener {
    public void updated();
    public void yourMove();
    public void setBoard(GoBoard go_board);
    public void setStatus(GoStatus go_status);
    public void error();
}