package tp_project.GoReplay;

import java.util.HashMap;
import java.util.Map;

import tp_project.Network.SocketIO;
import tp_project.Server.GameService;
import tp_project.Server.GameServiceManager;
import tp_project.Server.ServerCommand;

public class GoReplayService extends GameService {
    private class PlayerInfo {
        public boolean ready = false;
    }

    private HashMap<String, PlayerInfo> players_info;
    private GoReplayGame game;
    private Thread game_thread;
    private boolean is_game_runnig = false;

    public GoReplayService(String ID, String sKey, String host, SocketIO host_socketIO, String host_id,
            GameServiceManager manager) {
        super(ID, sKey, host, host_socketIO, host_id, manager);

        players_info = new HashMap<>();
        PlayerInfo player_info = new PlayerInfo();
        players_info.put(host_id, player_info);
    }

    @Override
    public int getMaxPlayersCount() {
        return 1;
    }

    @Override
    public String getGameName() {
        return "GoReplay";
    }

    @Override
    protected boolean isPlayerReady(String player_id) {
        PlayerInfo player_info = players_info.get(player_id);
        if (player_info != null)
            return player_info.ready;
        return false;
    }

    @Override
    protected void setPlayerReady(String player_id, boolean ready) {
        PlayerInfo player_info = players_info.get(player_id);
        if (player_info != null)
            player_info.ready = ready;
    }

    @Override
    protected boolean isGameReady() {
        if (players_info.size() != 1)
            return false;

        boolean ready = true;

        for (PlayerInfo player_info : players_info.values()) {
            ready = player_info.ready && ready;
        }

        return ready;
    }

    @Override
    protected boolean isGameRunnig() {
        return is_game_runnig;
    }

    @Override
    protected void startGame() {
        String id = players_info.keySet().stream().findAny().get();

        GoReplayPlayer player = new GoReplayPlayer(getClientInforamtion(id).socketIO, id, getClientInforamtion(id).name);

        game = new GoReplayGame(player, this);
        player.setGame(game);

        is_game_runnig = true;
        game_thread = new Thread(game);
        game_thread.start();
    }

    @Override
    public boolean addPlayer(String player_name, String player_id, SocketIO socketIO) {
        boolean success = super.addPlayer(player_name, player_id, socketIO);

        if (success) {
            PlayerInfo player_info = new PlayerInfo();
            players_info.put(player_id, player_info);
        }

        return success;
    }

    @Override
    public boolean removePlayer(String player_id) {
        boolean success = super.removePlayer(player_id);

        if (success) {
            players_info.remove(player_id);
        }

        return success;
    }

    protected int handleExtendetCommands(String client, ServerCommand command, SocketIO socketIO) {
        if (command.getValue("GoReplay") != null) {
            if (command.getValue("GoReplay").equals("getGoReplayServiceInfo")) {
                GoReplayServiceInfo info = new GoReplayServiceInfo();
                for (Map.Entry<String, PlayerInfo> pair : players_info.entrySet()) {
                    info.addPlayer(pair.getKey(), pair.getValue().ready);
                }

                socketIO.send(info);
                return 1;

            } else {
                sendCode(400, socketIO);
            }
        }

        return 0;
    }

    @Override
    public void gameEnded() {
        super.gameEnded();

        is_game_runnig = false;
    }
    
}