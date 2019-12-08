package tp_project.Server;

public interface GameServiceMenager {
    public void playerRemoved(String ID);
    public void unregisterPlayer(String ID);
    public void registerPlayer(String ID);
    public void deleteLater(String game_service_id);
}