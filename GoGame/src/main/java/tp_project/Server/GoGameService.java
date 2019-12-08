package tp_project.Server;

import java.util.HashMap;

import tp_project.Network.SocketIO;

public class GoGameService extends GameService {
    private class PlayerInfo {
        public boolean ready = false;
        public int colour;
    }
    private HashMap<String, PlayerInfo> players_info;

    public GoGameService(String ID, String sKey, String host, SocketIO host_socketIO, String host_id, GameServiceMenager meanager) {
        super(ID, sKey, host, host_socketIO, host_id, meanager);

        players_info = new HashMap<String, PlayerInfo>();
        PlayerInfo player_info = new PlayerInfo();
        player_info.colour = 0;
        players_info.put(host_id, player_info);
    }

    @Override
    public int getMaxPlayersCount() {
        return 2;
    }

    @Override
    public String getGameName() {
        return "GoGame";
    }

    @Override
    protected boolean isPlayerReady(String player_id) {
        PlayerInfo player_info = players_info.get(player_id);
        if (player_info != null) return player_info.ready;
        return false;
    }

    @Override
    protected void setPlayerReady(String player_id, boolean ready) {
        PlayerInfo player_info = players_info.get(player_id);
        if (player_info != null) player_info.ready = true;
    }

    @Override
    protected boolean isGameRedy() {
        if (players_info.size() != 2) return false;

        boolean ready = true;

        for (PlayerInfo player_info : players_info.values()) {
            ready = player_info.ready && ready;
        }

        return ready;
    }

    @Override
    protected void startGame() {
        // TODO implament
    }

    @Override
    public boolean addPlayer(String player_name, String player_id, SocketIO socketIO) {
        boolean success = super.addPlayer(player_name, player_id, socketIO);

        if (success) {
            PlayerInfo player_info = new PlayerInfo();
            player_info.colour = 1;
            players_info.put(player_id, player_info);
        }

        return success;
    }
    
}