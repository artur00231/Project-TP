package tp_project.GoGame;

import java.util.HashMap;
import java.util.Map;

import tp_project.Network.SocketIO;
import tp_project.Server.GameService;
import tp_project.Server.GameServiceManager;
import tp_project.Server.ServerCommand;

public class GoGameService extends GameService {
    private class PlayerInfo {
        public boolean ready = false;
        public int colour;
    }
    private HashMap<String, PlayerInfo> players_info;
    private int enemy_colour = 1;

    public GoGameService(String ID, String sKey, String host, SocketIO host_socketIO, String host_id, GameServiceManager manager) {
        super(ID, sKey, host, host_socketIO, host_id, manager);

        players_info = new HashMap<>();
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
    protected boolean isGameReady() {
        if (players_info.size() != 2) return false;

        boolean ready = true;

        for (PlayerInfo player_info : players_info.values()) {
            ready = player_info.ready && ready;
        }

        return ready;
    }

    @Override
    protected void startGame() {
        // TODO implement
    }

    @Override
    public boolean addPlayer(String player_name, String player_id, SocketIO socketIO) {
        boolean success = super.addPlayer(player_name, player_id, socketIO);

        if (success) {
            PlayerInfo player_info = new PlayerInfo();
            player_info.colour = enemy_colour;
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

    protected boolean handleExtendetCommands(String client, ServerCommand command, SocketIO socketIO) {
        if (command.getValue("GoGame") != null) {
            if (command.getValue("GoGame").equals("flip")) {
                if (checkSKey(command.getValue("sKey"))) {
                    enemy_colour = 0;

                    for (PlayerInfo info : players_info.values()) {
                        info.colour = (info.colour + 1) % 2;
                    }

                    sendCode(200, socketIO);
                    updatePlayers();
                    return true;
                } else {
                    sendCode(400, socketIO);
                }
            } else if (command.getValue("GoGame").equals("getGoGameServiceInfo")) {
                GoGameServiceInfo info = new GoGameServiceInfo();
                for (Map.Entry<String, PlayerInfo> pair : players_info.entrySet()) {
                    info.addPlayer(pair.getKey(), pair.getValue().ready, pair.getValue().colour);
                }

                socketIO.send(info);
            }
        }

        return false;
    }
    
}