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
    private GoGame game;
    private Thread game_thread;
    private String host_id;

    public GoGameService(String ID, String sKey, String host, SocketIO host_socketIO, String host_id,
            GameServiceManager manager) {
        super(ID, sKey, host, host_socketIO, host_id, manager);

        players_info = new HashMap<>();
        PlayerInfo player_info = new PlayerInfo();
        player_info.colour = 0;
        players_info.put(host_id, player_info);
        this.host_id = host_id;
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
        if (players_info.size() != 2)
            return false;

        boolean ready = true;

        for (PlayerInfo player_info : players_info.values()) {
            ready = player_info.ready && ready;
        }

        return ready;
    }

    @Override
    protected void startGame() {
        GoPlayer[] players = new GoPlayer[2];
        String[] players_id = new String[2];

        players[0] = new GoRemotePlayer(getClientSocketIO(host_id));
        players_id[0] = host_id;

        for (String id : players_info.keySet()) {
            if (id.equals(host_id)) continue;
            if (getClientSocketIO(id) != null) {
                players[1] = new GoRemotePlayer(getClientSocketIO(id));
            } else {
                // TODO add bot
            }

            players_id[1] = id;
        }

        game = new GoGame(players[0], players_id[0], players[1], players_id[1], enemy_colour == 1 ? 0 : 1, this);
        players[0].setGame(game);
        players[1].setGame(game);

        game_thread = new Thread(game);
        game_thread.start();
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

    @Override
    public void gameEnded() {
        super.gameEnded();

        try {
            game_thread.join();
        } catch (InterruptedException e) {
        }

        game = null;
    }
    
}