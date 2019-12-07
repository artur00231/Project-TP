package tp_project.Server;

import java.util.HashMap;
import java.util.Map;

import tp_project.Network.Command;
import tp_project.Network.ICommand;
import tp_project.Network.SocketIO;

public abstract class GameService {
    private class Client {
        public String name;
        public SocketIO socketIO;
        public Client(String name, SocketIO socketIO) { this.name = name; this.socketIO = socketIO; };
    }
    private String host;
    private String host_id;
    private String sKey;
    private Map<String, Client> players;
    private String ID;
    private GameServiceMenager menager;

    public abstract int getMaxPlayersCount();
    public abstract String getGameName();
    protected abstract boolean isPlayerReady(String player_id);
    protected abstract void setPlayerReady(String player_id, boolean ready);
    protected abstract boolean isGameRedy();
    protected abstract void startGame();

    public GameService(String ID, String host, SocketIO host_socketIO, String host_id, GameServiceMenager meanager) {
        this.ID = ID;
        this.host = host;
        this.host_id = host_id;
        menager = meanager;

        players = new HashMap<String, Client>();
        players.put(host_id, new Client(host, host_socketIO));

        if (isGameRedy()) {
            startGame();
        }
    }

    public String getID() {
        return ID;
    }

    public boolean checkSKey(String sKey) {
        return this.sKey.equals(sKey);
    }

    public GameServiceInfo getInfo() {
        GameServiceInfo info = new GameServiceInfo();
        info.host = host;
        info.players = new HashMap<>();

        for (Map.Entry<String, Client> player: players.entrySet()) {
            info.players.put(player.getKey(), player.getValue().name);
        }
 
        return info;
    }

    public boolean addPlayer(String player_name, String player_id, SocketIO socketIO) {
        if (players.get(player_id) != null) {
            return false;
        }

        players.put(player_id, new Client(player_name, socketIO));

        return true;
    }

    public boolean checkPlayer(String player_id) {
        return players.get(player_id) != null;
    }

    public boolean removePlayer(String player_id, String sKey) {
        if (!checkSKey(sKey)) return false;
        if (!checkPlayer(player_id)) return false;

        players.remove(player_id);
        menager.playerRemoved(player_id);
        return true;
    }

    public void setReady(String player_id, boolean ready, String sKey) {
        if (player_id == host_id && checkSKey(sKey)) {
            setPlayerReady(player_id, ready);
        } else if (player_id != host_id) {
            setPlayerReady(player_id, ready);
        }

        if (isGameRedy()) {
            startGame();
        }
    }

    public void update(SocketIO socketIO) {
        Command command = socketIO.getCommand();

        if (command == null) return;
        if (!(command.getCommand().getCommandType().equals("ServerCommand"))) return;

        ServerCommand cmd = (ServerCommand)command.getCommand();
        System.out.println("Game_Service" + cmd.getCode());
    }
}